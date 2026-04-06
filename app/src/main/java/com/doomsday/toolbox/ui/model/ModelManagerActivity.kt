package com.doomsday.toolbox.ui.model

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.doomsday.toolbox.databinding.ActivityModelManagerBinding

class ModelManagerActivity : AppCompatActivity() {
    private val viewModel by viewModels<ModelManagerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityModelManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { finish() }
        binding.textModelName.text = viewModel.model.displayName
        binding.textModelSize.text = viewModel.model.approxSizeLabel
        binding.buttonDownload.setOnClickListener { viewModel.download() }

        viewModel.state.observe(this) { state ->
            binding.textStatus.text = state.statusText
            binding.textFile.text = if (state.currentFile.isBlank()) {
                "模型文件会保存到应用私有目录，下载完成后自动作为当前模型使用。"
            } else {
                "当前文件：${state.currentFile}"
            }
            binding.progressDownload.progress = state.progressPercent
            binding.buttonDownload.isEnabled = !state.isDownloading
            binding.buttonDownload.visibility = if (state.isDownloaded) View.GONE else View.VISIBLE
            binding.progressDownload.visibility = if (state.isDownloaded && !state.isDownloading) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}
