package com.doomsday.toolbox.ui.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.doomsday.toolbox.R
import com.doomsday.toolbox.common.LocalImageStore
import com.doomsday.toolbox.databinding.ActivityChatBinding
import com.doomsday.toolbox.ui.model.ModelManagerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatActivity : AppCompatActivity() {
    private val viewModel by viewModels<ChatViewModel>()
    private lateinit var binding: ActivityChatBinding

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) importSelectedImage(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ChatAdapter()
        binding.recyclerChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.recyclerChat.adapter = adapter

        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonModels.setOnClickListener {
            startActivity(Intent(this, ModelManagerActivity::class.java))
        }
        binding.buttonImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.buttonClearImage.setOnClickListener { viewModel.clearPendingImage() }
        binding.buttonSend.setOnClickListener {
            val text = binding.inputQuestion.text.toString()
            binding.inputQuestion.text?.clear()
            viewModel.send(text)
        }
        binding.buttonClear.setOnClickListener { viewModel.clear() }

        viewModel.messages.observe(this) {
            adapter.submitList(it)
            binding.recyclerChat.scrollToPosition((it.size - 1).coerceAtLeast(0))
        }
        viewModel.pendingImagePath.observe(this) { path ->
            updatePendingImage(path)
        }
        viewModel.loading.observe(this) { loading ->
            binding.buttonSend.isEnabled = !loading
            binding.buttonImage.isEnabled = !loading
            binding.textStatus.text = if (loading) {
                "正在进行本地多模态推理，请稍候…"
            } else {
                getString(R.string.ai_disclaimer)
            }
        }
        viewModel.error.observe(this) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun importSelectedImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { LocalImageStore.importImage(this@ChatActivity, uri) }
                .onSuccess { path ->
                    withContext(Dispatchers.Main) {
                        viewModel.attachImage(path)
                        Toast.makeText(this@ChatActivity, "图片已添加，将结合图片内容进行推理", Toast.LENGTH_SHORT).show()
                    }
                }
                .onFailure { error ->
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ChatActivity,
                            error.message ?: "图片读取失败",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun updatePendingImage(path: String?) {
        if (path.isNullOrBlank()) {
            binding.layoutSelectedImage.visibility = View.GONE
            binding.imageSelectedPreview.setImageDrawable(null)
        } else {
            binding.layoutSelectedImage.visibility = View.VISIBLE
            binding.imageSelectedPreview.setImageURI(Uri.fromFile(File(path)))
        }
    }
}
