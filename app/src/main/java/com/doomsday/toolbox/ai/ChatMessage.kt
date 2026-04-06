package com.doomsday.toolbox.ai

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
