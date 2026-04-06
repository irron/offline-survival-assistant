package com.doomsday.toolbox.model

import android.content.Context
import com.doomsday.toolbox.common.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

data class ModelDownloadState(
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val currentFile: String = "",
    val progressPercent: Int = 0,
    val statusText: String = "尚未下载模型",
    val configPath: String? = null
)

class ModelDownloadRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val prefs = AppPrefs(context)
    private val rootDir = File(context.getExternalFilesDir(null), "models").apply { mkdirs() }

    private val _state = MutableStateFlow(readState())
    val state: StateFlow<ModelDownloadState> = _state

    fun getModelDir(definition: ModelDefinition): File = File(rootDir, definition.folderName)

    fun readState(): ModelDownloadState {
        val dir = getModelDir(ModelCatalog.qwen35_2b)
        val config = File(dir, "config.json")
        val allRequired = ModelCatalog.qwen35_2b.requiredFiles.all { File(dir, it).exists() }
        return ModelDownloadState(
            isDownloaded = allRequired && config.exists(),
            isDownloading = false,
            statusText = if (allRequired) "模型已就绪，可直接离线推理" else "尚未下载模型",
            configPath = config.takeIf { it.exists() }?.absolutePath
        )
    }

    suspend fun downloadModel(definition: ModelDefinition = ModelCatalog.qwen35_2b): Result<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val currentState = readState()
                if (currentState.isDownloaded && currentState.configPath != null) {
                    _state.value = currentState
                    return@runCatching currentState.configPath
                }

                val dir = getModelDir(definition).apply { mkdirs() }
                val total = definition.files.size

                definition.files.forEachIndexed { index, fileName ->
                    _state.value = _state.value.copy(
                        isDownloading = true,
                        currentFile = fileName,
                        progressPercent = ((index.toFloat() / total) * 100).toInt(),
                        statusText = "正在下载：$fileName"
                    )

                    val file = File(dir, fileName)
                    val url = "https://modelscope.cn/models/${definition.modelScopeId}/resolve/master/$fileName"
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            if (fileName in definition.requiredFiles) {
                                error("下载失败：$fileName (${response.code})")
                            } else {
                                return@use
                            }
                        }

                        val body = response.body ?: error("响应体为空：$fileName")
                        FileOutputStream(file).use { output ->
                            body.byteStream().copyTo(output)
                        }
                    }
                }

                val configPath = File(dir, "config.json").absolutePath
                prefs.activeModelConfigPath = configPath
                _state.value = ModelDownloadState(
                    isDownloaded = true,
                    isDownloading = false,
                    currentFile = "",
                    progressPercent = 100,
                    statusText = "下载完成，已自动设为当前模型",
                    configPath = configPath
                )
                configPath
            }.onFailure {
                _state.value = _state.value.copy(
                    isDownloading = false,
                    statusText = it.message ?: "下载失败"
                )
            }
        }
    }

    fun getActiveConfigPath(): String? {
        val saved = prefs.activeModelConfigPath
        return saved?.takeIf { File(it).exists() } ?: readState().configPath
    }
}
