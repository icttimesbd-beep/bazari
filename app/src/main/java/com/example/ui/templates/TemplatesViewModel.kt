package com.example.ui.templates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.TemplateItemEntity
import com.example.data.repository.BazariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplatesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BazariRepository

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _selectedTemplateId = MutableStateFlow("")
    val selectedTemplateId: StateFlow<String> = _selectedTemplateId.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BazariRepository(db, application)
    }

    val templates: StateFlow<List<TemplateEntity>> = _selectedFilter.flatMapLatest { filter ->
        when (filter) {
            "STORE" -> repository.getTemplatesByCategory("STORE")
            "PERSONAL" -> repository.getTemplatesByCategory("PERSONAL")
            else -> repository.getAllTemplates()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSelectedTemplateId(id: String) {
        _selectedTemplateId.value = id
    }

    val currentTemplateItems: StateFlow<List<TemplateItemEntity>> = _selectedTemplateId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else repository.getItemsForTemplate(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createListFromTemplate(
        templateId: String,
        selectedItemIds: List<Long>? = null,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val listId = repository.createListFromTemplate(templateId, selectedItemIds)
            onCreated(listId)
        }
    }

    fun deleteCustomTemplate(templateId: String) {
        viewModelScope.launch {
            repository.deleteCustomTemplate(templateId)
        }
    }
}
