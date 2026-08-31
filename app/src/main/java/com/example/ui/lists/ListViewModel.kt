package com.example.ui.lists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShoppingHistoryEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.data.repository.BazariRepository
import com.example.utils.BengaliNumberUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BazariRepository
    private val _listId = MutableStateFlow("")

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BazariRepository(db, application)
    }

    fun setListId(id: String) {
        _listId.value = id
    }

    val currentList: StateFlow<ShoppingListEntity?> = _listId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null) else repository.getListById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val items: StateFlow<List<ShoppingItemEntity>> = _listId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else repository.getItemsForList(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commonProducts: StateFlow<List<ProductEntity>> = repository.getCommonProducts(14)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleItemBought(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.toggleItemBought(item)
        }
    }

    fun updateItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }

    fun quickAddProduct(product: ProductEntity) {
        val listId = _listId.value
        if (listId.isNotBlank()) {
            viewModelScope.launch {
                repository.quickAddProduct(listId, product)
            }
        }
    }

    suspend fun searchSuggestions(query: String): List<ProductEntity> {
        return repository.searchProductsSync(query, limit = 6)
    }

    fun smartQuickEntry(input: String) {
        val listId = _listId.value
        if (listId.isNotBlank()) {
            viewModelScope.launch {
                repository.smartQuickEntry(listId, input)
            }
        }
    }

    fun updateBudget(budget: Double) {
        val listId = _listId.value
        if (listId.isNotBlank()) {
            viewModelScope.launch {
                repository.updateBudget(listId, budget)
            }
        }
    }

    fun clearBoughtItems() {
        val listId = _listId.value
        if (listId.isNotBlank()) {
            viewModelScope.launch {
                repository.clearBoughtItems(listId)
            }
        }
    }

    fun completeShoppingTrip(onCompleted: (ShoppingHistoryEntity) -> Unit) {
        val listId = _listId.value
        if (listId.isNotBlank()) {
            viewModelScope.launch {
                val history = repository.completeShoppingList(listId)
                onCompleted(history)
            }
        }
    }

    fun saveAsTemplate(title: String, description: String, onSaved: () -> Unit) {
        val listId = _listId.value
        if (listId.isNotBlank()) {
            viewModelScope.launch {
                repository.saveListAsCustomTemplate(listId, title, description)
                onSaved()
            }
        }
    }
}
