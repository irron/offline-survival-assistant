package com.doomsday.toolbox.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doomsday.toolbox.ai.ChatMessage
import com.doomsday.toolbox.ai.OfflineSurvivalAssistant
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val assistant = OfflineSurvivalAssistant(application)
    private val items = mutableListOf<ChatMessage>()

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _pendingImagePath = MutableLiveData<String?>(null)
    val pendingImagePath: LiveData<String?> = _pendingImagePath

    fun attachImage(path: String) {
        _pendingImagePath.value = path
    }

    fun clearPendingImage() {
        _pendingImagePath.value = null
    }

    fun send(text: String) {
        val imagePath = _pendingImagePath.value
        if (text.isBlank() && imagePath.isNullOrBlank()) return

        items += ChatMessage(
            text = text.ifBlank { "请根据图片给我建议" },
            isUser = true,
            imagePath = imagePath
        )
        _messages.value = items.toList()
        _pendingImagePath.value = null

        viewModelScope.launch {
            _loading.value = true
            runCatching { assistant.generateAdvice(text, imagePath) }
                .onSuccess { reply ->
                    items += ChatMessage(reply, false)
                    _messages.value = items.toList()
                }
                .onFailure { error ->
                    _error.value = error.message ?: "推理失败"
                }
            _loading.value = false
        }
    }

    fun clear() {
        items.clear()
        assistant.reset()
        _messages.value = emptyList()
        _pendingImagePath.value = null
    }

    override fun onCleared() {
        assistant.release()
        super.onCleared()
    }
}
