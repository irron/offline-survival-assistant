package com.doomsday.toolbox.ui.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.doomsday.toolbox.model.ModelCatalog
import com.doomsday.toolbox.model.ModelDownloadRepository
import kotlinx.coroutines.launch

class ModelManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ModelDownloadRepository(application)
    val state = repository.state.asLiveData()
    val model = ModelCatalog.qwen35_2b

    fun download() {
        viewModelScope.launch {
            repository.downloadModel()
        }
    }
}
