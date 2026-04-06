package com.doomsday.toolbox.knowledge

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class KnowledgeRepository(context: Context) {
    private val items: List<KnowledgeItem> = run {
        val json = context.assets.open("knowledge_base.json").bufferedReader().use { it.readText() }
        Gson().fromJson(json, object : TypeToken<List<KnowledgeItem>>() {}.type)
    }

    fun getAll(): List<KnowledgeItem> = items

    fun getById(id: String): KnowledgeItem? = items.firstOrNull { it.id == id }
}
