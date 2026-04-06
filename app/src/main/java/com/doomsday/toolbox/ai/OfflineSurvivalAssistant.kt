package com.doomsday.toolbox.ai

import android.content.Context
import com.doomsday.toolbox.model.ModelDownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class OfflineSurvivalAssistant(private val context: Context) {
    private val modelRepository = ModelDownloadRepository(context)
    private var session: NativeLlmSession? = null

    suspend fun ensureLoaded(): Boolean = withContext(Dispatchers.IO) {
        val configPath = modelRepository.getActiveConfigPath() ?: return@withContext false
        if (session?.isReady() == true) return@withContext true

        val mergedConfig = File(configPath).readText()
        val runtimeConfig = JSONObject()
            .put("keep_history", false)
            .put("mmap_dir", File(context.cacheDir, "mmap").apply { mkdirs() }.absolutePath)
            .toString()

        session?.release()
        session = NativeLlmSession(
            configPath = configPath,
            mergedConfigJson = mergedConfig,
            runtimeConfigJson = runtimeConfig
        )
        session?.load()
        session?.isReady() == true
    }

    suspend fun generateAdvice(userInput: String, imagePath: String? = null): String = withContext(Dispatchers.IO) {
        check(ensureLoaded()) { "请先下载并加载模型" }

        val prompt = buildPrompt(userInput = userInput, imagePath = imagePath)
        val raw = session?.generate(prompt).orEmpty()
        normalize(raw)
    }

    fun reset() {
        session?.reset()
    }

    fun release() {
        session?.release()
        session = null
    }

    private fun buildPrompt(userInput: String, imagePath: String?): String {
        val instruction = """
            你是《末日工具箱》的离线生存助手。
            回答要求：
            1. 最多 5 条步骤
            2. 每条一句话
            3. 只给可执行建议
            4. 避免危险、武器制造、毒物和不确定建议
            5. 不要输出多余解释
        """.trimIndent()

        return if (!imagePath.isNullOrBlank()) {
            val imageTask = if (userInput.isBlank()) {
                "请先识别图片中的环境、物品或风险，再给出生存建议。"
            } else {
                "请结合图片内容和下面的问题给出生存建议：\n$userInput"
            }
            "<img>$imagePath</img>\n$instruction\n$imageTask"
        } else {
            "$instruction\n用户问题：$userInput"
        }
    }

    private fun normalize(text: String): String {
        val lines = text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.contains("For reference only", ignoreCase = true) }
            .take(5)
            .mapIndexed { index, line ->
                if (line.matches(Regex("""^\d+\..*"""))) line else "${index + 1}. $line"
            }
            .toList()

        return lines.joinToString("\n").ifBlank {
            "1. 先远离眼前最直接的危险，再根据周围环境继续判断。"
        }
    }
}
