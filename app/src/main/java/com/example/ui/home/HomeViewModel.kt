package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.repository.BazariRepository
import com.example.domain.model.AppMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayListSummary(
    val list: ShoppingListEntity? = null,
    val totalItems: Int = 0,
    val boughtItems: Int = 0,
    val pendingItems: Int = 0,
    val estimatedCost: Double = 0.0
)

data class SmartRepeatSuggestion(
    val productNameBn: String,
    val productNameEn: String,
    val productId: String,
    val intervalDays: Int = 7
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BazariRepository
    
    private val _appMode = MutableStateFlow(AppMode.PERSONAL)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _dismissedSuggestion = MutableStateFlow(false)
    val dismissedSuggestion: StateFlow<Boolean> = _dismissedSuggestion.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BazariRepository(db, application)
        viewModelScope.launch {
            repository.ensureCatalogLoaded()
        }
    }

    val activeLists: StateFlow<List<ShoppingListEntity>> = _appMode.flatMapLatest { mode ->
        repository.getListsByMode(mode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val primaryListSummary: StateFlow<TodayListSummary> = activeLists.flatMapLatest { lists ->
        val active = lists.firstOrNull { !it.isCompleted }
        if (active != null) {
            repository.getItemsForList(active.id).flatMapLatest { items ->
                val bought = items.count { it.isBought }
                val pending = items.size - bought
                var cost = 0.0
                items.forEach { item ->
                    val qty = com.example.utils.BengaliNumberUtils.toEnglishDigits(item.quantity).toDoubleOrNull() ?: 1.0
                    cost += (qty * item.unitPrice)
                }
                flowOf(TodayListSummary(active, items.size, bought, pending, cost))
            }
        } else {
            flowOf(TodayListSummary())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayListSummary())

    val commonProducts: StateFlow<List<ProductEntity>> = repository.getCommonProducts(16)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popularTemplates: StateFlow<List<TemplateEntity>> = _appMode.flatMapLatest { mode ->
        repository.getTemplatesByCategory(mode.name)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartSuggestion: StateFlow<SmartRepeatSuggestion?> = commonProducts.flatMapLatest { products ->
        val milkOrEgg = products.find { it.id == "prod_dudh_liquid" || it.id == "prod_dim_egg" }
            ?: products.firstOrNull()
        if (milkOrEgg != null) {
            flowOf(SmartRepeatSuggestion(milkOrEgg.nameBn, milkOrEgg.nameEn, milkOrEgg.id, 7))
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun dismissSmartSuggestion() {
        _dismissedSuggestion.value = true
    }

    fun toggleAppMode() {
        _appMode.value = if (_appMode.value == AppMode.PERSONAL) AppMode.STORE else AppMode.PERSONAL
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun createList(title: String, mode: AppMode, budget: Double, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val id = repository.createNewList(title, mode, budget)
            onCreated(id)
        }
    }

    fun quickAddProductToLatestList(product: ProductEntity, onNavToList: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val currentLists = activeLists.value
            val targetList = currentLists.firstOrNull { !it.isCompleted }
            val listId = if (targetList != null) {
                targetList.id
            } else {
                repository.createNewList("আজকের বাজার", _appMode.value)
            }
            repository.quickAddProduct(listId, product)
            onNavToList?.invoke(listId)
        }
    }

    suspend fun searchSuggestions(query: String): List<ProductEntity> {
        return repository.searchProductsSync(query, limit = 6)
    }

    fun addSuggestionToActiveList(suggestion: SmartRepeatSuggestion, onNavToList: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val currentLists = activeLists.value
            val targetList = currentLists.firstOrNull { !it.isCompleted }
            val listId = if (targetList != null) {
                targetList.id
            } else {
                repository.createNewList("আজকের বাজার", _appMode.value)
            }
            repository.smartQuickEntry(listId, suggestion.productNameBn)
            _dismissedSuggestion.value = true
            onNavToList?.invoke(listId)
        }
    }

    fun smartQuickEntryToLatestList(text: String, onNavToList: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val currentLists = activeLists.value
            val targetList = currentLists.firstOrNull { !it.isCompleted }
            val listId = if (targetList != null) {
                targetList.id
            } else {
                repository.createNewList("আজকের বাজার", _appMode.value)
            }
            repository.smartQuickEntry(listId, text)
            onNavToList?.invoke(listId)
        }
    }

    fun createListFromTemplate(templateId: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val listId = repository.createListFromTemplate(templateId)
            onCreated(listId)
        }
    }

    fun duplicateList(listId: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val newId = repository.duplicateList(listId)
            onCreated(newId)
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            repository.deleteList(listId)
        }
    }

    fun exportProductsJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportProductsJson()
            onResult(json)
        }
    }

    fun importProductsJson(jsonStr: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importProductsJson(jsonStr)
            onResult(count)
        }
    }

    fun reloadFullCatalog(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.reloadCatalog()
            onResult(success)
        }
    }

    suspend fun getProductCount(): Int {
        return repository.getProductCount()
    }
}

