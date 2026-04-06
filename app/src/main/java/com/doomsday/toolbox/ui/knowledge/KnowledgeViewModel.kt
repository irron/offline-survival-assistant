package com.doomsday.toolbox.ui.knowledge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doomsday.toolbox.knowledge.KnowledgeItem
import com.doomsday.toolbox.knowledge.KnowledgeRepository

class KnowledgeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = KnowledgeRepository(application)
    private val _items = MutableLiveData(repository.getAll())
    val items: LiveData<List<KnowledgeItem>> = _items

    fun getItem(id: String): KnowledgeItem? = repository.getById(id)
}
