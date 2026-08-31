package com.example.ui.product_picker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.repository.BazariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductPickerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BazariRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("ALL")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BazariRepository(db, application)
    }

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = combine(_searchQuery, _selectedCategoryId) { query, catId ->
        Pair(query, catId)
    }.flatMapLatest { (query, catId) ->
        when {
            query.isNotBlank() -> repository.searchProducts(query, if (catId == "ALL" || catId == "FAV" || catId == "COMMON") "" else catId)
            catId == "FAV" -> repository.getFavoriteProducts()
            catId == "COMMON" -> repository.getCommonProducts(30)
            catId != "ALL" -> repository.searchProducts("", catId)
            else -> repository.getAllProducts()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSelectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleFavorite(product: ProductEntity) {
        viewModelScope.launch {
            repository.toggleProductFavorite(product)
        }
    }

    fun addProductToList(listId: String, product: ProductEntity) {
        viewModelScope.launch {
            repository.quickAddProduct(listId, product)
        }
    }

    fun createCustomProduct(
        nameBn: String,
        nameEn: String,
        categoryId: String,
        unit: String,
        price: Double,
        listId: String? = null
    ) {
        viewModelScope.launch {
            val product = repository.addCustomProduct(
                nameBn = nameBn,
                nameEn = nameEn,
                categoryId = categoryId,
                defaultUnit = unit,
                defaultPrice = price
            )
            if (listId != null && listId.isNotBlank()) {
                repository.quickAddProduct(listId, product)
            }
        }
    }
}
