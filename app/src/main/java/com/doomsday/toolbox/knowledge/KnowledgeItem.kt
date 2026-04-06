package com.doomsday.toolbox.knowledge

data class KnowledgeItem(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val content: List<String>
)
