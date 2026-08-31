package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.CatalogDataLoader
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.EventExpenseEntity
import com.example.data.local.entity.EventMemberEntity
import com.example.data.local.entity.EventPlanEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductMemoryEntity
import com.example.data.local.entity.QuickTapeNoteEntity
import com.example.data.local.entity.ShoppingHistoryEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.TemplateItemEntity
import com.example.domain.model.AppMode
import com.example.domain.model.ParsedQuickEntry
import com.example.utils.BengaliNumberUtils
import com.example.utils.SmartEntryParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

data class EventInvoiceSummary(
    val event: EventPlanEntity,
    val members: List<EventMemberEntity>,
    val expenses: List<EventExpenseEntity>,
    val totalTargetCollection: Double,
    val totalPaidCollection: Double,
    val totalPendingCollection: Double,
    val totalExpenses: Double,
    val netBalance: Double
)

class BazariRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val productDao = database.productDao()
    private val listDao = database.shoppingListDao()
    private val itemDao = database.shoppingItemDao()
    private val templateDao = database.templateDao()
    private val memoryDao = database.productMemoryDao()
    private val historyDao = database.historyDao()
    private val eventDao = database.eventPlanDao()
    private val tapeNoteDao = database.quickTapeNoteDao()

    suspend fun ensureCatalogLoaded() {
        withContext(Dispatchers.IO) {
            CatalogDataLoader(context).loadCatalogIfNeeded(database)
        }
    }

    suspend fun reloadCatalog(): Boolean = withContext(Dispatchers.IO) {
        try {
            CatalogDataLoader(context).loadCatalogIfNeeded(database, forceReload = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportProductsJson(): String = withContext(Dispatchers.IO) {
        CatalogDataLoader(context).exportProductsToJson(database)
    }

    suspend fun importProductsJson(jsonStr: String): Int = withContext(Dispatchers.IO) {
        CatalogDataLoader(context).importProductsFromJson(database, jsonStr)
    }

    suspend fun getProductCount(): Int = withContext(Dispatchers.IO) {
        productDao.getProductCount()
    }

    // --- PRODUCTS ---
    fun getAllProducts(limit: Int = 200): Flow<List<ProductEntity>> = productDao.getAllProducts(limit)
    fun getCommonProducts(limit: Int = 14): Flow<List<ProductEntity>> = productDao.getCommonProducts(limit)
    fun getFavoriteProducts(): Flow<List<ProductEntity>> = productDao.getFavoriteProducts()
    fun getRecentProducts(limit: Int = 15): Flow<List<ProductEntity>> = productDao.getRecentProducts(limit)
    fun getAllCategories(): Flow<List<CategoryEntity>> = productDao.getAllCategories()

    fun searchProducts(query: String, categoryId: String = ""): Flow<List<ProductEntity>> {
        val cleanQuery = BengaliNumberUtils.toEnglishDigits(query.trim())
        val q = cleanQuery.ifBlank { query.trim() }
        return productDao.searchProducts(q, categoryId)
    }

    suspend fun searchProductsSync(query: String, limit: Int = 10): List<ProductEntity> = withContext(Dispatchers.IO) {
        val cleanQuery = BengaliNumberUtils.toEnglishDigits(query.trim())
        val q = cleanQuery.ifBlank { query.trim() }
        if (q.isBlank()) return@withContext emptyList()
        try {
            productDao.searchProducts(q, limit = limit).first()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findExactMatch(query: String): ProductEntity? = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext null
        productDao.findExactMatch(q)
    }

    suspend fun toggleProductFavorite(product: ProductEntity) = withContext(Dispatchers.IO) {
        val newFav = !product.isFavorite
        productDao.setFavorite(product.id, newFav)
        val mem = memoryDao.getMemoryForProduct(product.id)
        if (mem != null) {
            memoryDao.saveMemory(mem.copy(isFavorite = newFav))
        } else {
            memoryDao.saveMemory(
                ProductMemoryEntity(
                    productId = product.id,
                    lastQuantity = "১",
                    lastUnit = product.defaultUnit,
                    lastPrice = product.defaultPrice,
                    isFavorite = newFav
                )
            )
        }
    }

    suspend fun addCustomProduct(
        nameBn: String,
        nameEn: String,
        categoryId: String,
        defaultUnit: String,
        defaultPrice: Double,
        aliasesBn: String = "",
        aliasesEn: String = ""
    ): ProductEntity = withContext(Dispatchers.IO) {
        val id = "custom_" + UUID.randomUUID().toString().take(8)
        val product = ProductEntity(
            id = id,
            nameBn = nameBn.trim(),
            nameEn = nameEn.trim(),
            aliasesBn = aliasesBn.trim(),
            aliasesEn = aliasesEn.trim(),
            categoryId = categoryId,
            defaultUnit = defaultUnit,
            defaultPrice = defaultPrice,
            isCommon = false,
            isFavorite = true,
            usageCount = 1,
            lastUsedTimestamp = System.currentTimeMillis(),
            isCustom = true
        )
        productDao.insertProduct(product)
        product
    }

    // --- SHOPPING LISTS ---
    fun getListsByMode(mode: AppMode): Flow<List<ShoppingListEntity>> = listDao.getListsByMode(mode.name)
    fun getAllActiveLists(): Flow<List<ShoppingListEntity>> = listDao.getAllActiveLists()
    fun getListById(id: String): Flow<ShoppingListEntity?> = listDao.getListById(id)

    suspend fun createNewList(
        title: String,
        mode: AppMode = AppMode.PERSONAL,
        budget: Double = 0.0,
        notes: String = ""
    ): String = withContext(Dispatchers.IO) {
        val listId = UUID.randomUUID().toString()
        val newList = ShoppingListEntity(
            id = listId,
            title = title.ifBlank { if (mode == AppMode.PERSONAL) "আজকের বাজার" else "দোকানের নতুন ফর্দ" },
            mode = mode.name,
            budget = budget,
            notes = notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        listDao.insertList(newList)
        listId
    }

    suspend fun updateList(list: ShoppingListEntity) = withContext(Dispatchers.IO) {
        listDao.updateList(list.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateBudget(listId: String, budget: Double) = withContext(Dispatchers.IO) {
        listDao.updateBudget(listId, budget)
    }

    suspend fun deleteList(listId: String) = withContext(Dispatchers.IO) {
        listDao.deleteList(listId)
    }

    suspend fun duplicateList(listId: String): String = withContext(Dispatchers.IO) {
        val originalList = listDao.getListByIdSync(listId) ?: return@withContext ""
        val originalItems = itemDao.getItemsForListSync(listId)

        val newListId = UUID.randomUUID().toString()
        val duplicatedList = originalList.copy(
            id = newListId,
            title = "${originalList.title} (কপি)",
            isCompleted = false,
            completedAt = 0L,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        listDao.insertList(duplicatedList)

        val duplicatedItems = originalItems.map {
            it.copy(
                id = UUID.randomUUID().toString(),
                listId = newListId,
                isBought = false,
                boughtAt = 0L,
                createdAt = System.currentTimeMillis()
            )
        }
        itemDao.insertItems(duplicatedItems)
        newListId
    }

    // --- SHOPPING ITEMS ---
    fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>> = itemDao.getItemsForList(listId)

    suspend fun addItemToList(
        listId: String,
        nameBn: String,
        quantity: String = "১",
        unit: String = "কেজি",
        unitPrice: Double = 0.0,
        categoryId: String = "rice_grains",
        categoryBn: String = "মুদি",
        productId: String? = null,
        brand: String = "",
        notes: String = ""
    ): String = withContext(Dispatchers.IO) {
        val itemId = UUID.randomUUID().toString()
        val item = ShoppingItemEntity(
            id = itemId,
            listId = listId,
            productId = productId,
            nameBn = nameBn,
            quantity = quantity,
            unit = unit,
            unitPrice = unitPrice,
            categoryId = categoryId,
            categoryBn = categoryBn,
            brand = brand,
            notes = notes,
            isBought = false,
            createdAt = System.currentTimeMillis()
        )
        itemDao.insertItem(item)

        // Save memory and increment product usage
        if (!productId.isNullOrBlank()) {
            productDao.incrementUsage(productId)
            val existingMem = memoryDao.getMemoryForProduct(productId)
            memoryDao.saveMemory(
                ProductMemoryEntity(
                    productId = productId,
                    lastQuantity = quantity,
                    lastUnit = unit,
                    lastPrice = unitPrice,
                    lastBrand = brand,
                    useCount = (existingMem?.useCount ?: 0) + 1,
                    isFavorite = existingMem?.isFavorite ?: false,
                    lastUsedTimestamp = System.currentTimeMillis()
                )
            )
        }

        // Touch list updatedAt
        val list = listDao.getListByIdSync(listId)
        if (list != null) {
            listDao.updateList(list.copy(updatedAt = System.currentTimeMillis()))
        }
        itemId
    }

    suspend fun quickAddProduct(listId: String, product: ProductEntity) = withContext(Dispatchers.IO) {
        // Check if item already exists in this list
        val currentItems = itemDao.getItemsForListSync(listId)
        val existing = currentItems.find { it.productId == product.id || it.nameBn.equals(product.nameBn, ignoreCase = true) }

        if (existing != null) {
            // Increment existing quantity by 1
            val currentEngQty = BengaliNumberUtils.toEnglishDigits(existing.quantity).toDoubleOrNull() ?: 1.0
            val newQtyBn = BengaliNumberUtils.toBengaliDigits(currentEngQty + 1.0)
            itemDao.updateQuantity(existing.id, newQtyBn)
        } else {
            val memory = memoryDao.getMemoryForProduct(product.id)
            val qty = memory?.lastQuantity ?: "১"
            val unit = memory?.lastUnit ?: product.defaultUnit
            val price = if (memory != null && memory.lastPrice > 0) memory.lastPrice else product.defaultPrice

            addItemToList(
                listId = listId,
                nameBn = product.nameBn,
                quantity = qty,
                unit = unit,
                unitPrice = price,
                categoryId = product.categoryId,
                productId = product.id
            )
        }
    }

    suspend fun smartQuickEntry(listId: String, input: String) = withContext(Dispatchers.IO) {
        val parsedList = SmartEntryParser.parseMultiple(input)
        for (parsed in parsedList) {
            val rawName = parsed.productName.trim()
            if (rawName.isBlank()) continue

            // 1. Look for EXACT product match (by exact name or alias)
            val exactMatch = findExactMatch(rawName)

            if (exactMatch != null) {
                val unit = if (parsed.unit != "পিস") parsed.unit else exactMatch.defaultUnit
                val price = exactMatch.defaultPrice
                addItemToList(
                    listId = listId,
                    nameBn = rawName, // Preserve user's input name (e.g. "আম", "চাল", "ডিম")
                    quantity = parsed.quantity,
                    unit = unit,
                    unitPrice = price,
                    categoryId = exactMatch.categoryId,
                    productId = exactMatch.id
                )
            } else {
                // 2. Custom or partial entry: PRESERVE rawName strictly, infer category smartly
                val (catId, catBn) = guessCategory(rawName)
                addItemToList(
                    listId = listId,
                    nameBn = rawName, // NEVER overwrite user's input with another product like 'আমদানি' or 'চাল কুমড়া'
                    quantity = parsed.quantity,
                    unit = parsed.unit,
                    unitPrice = 0.0,
                    categoryId = catId,
                    categoryBn = catBn
                )
            }
        }
    }

    fun guessCategory(productName: String): Pair<String, String> {
        val lower = productName.lowercase()
        return when {
            lower.contains("মাছ") || lower.contains("ইলিশ") || lower.contains("রুই") || lower.contains("কাতলা") ||
            lower.contains("চিংড়ি") || lower.contains("চিংড়ি") || lower.contains("পাঙ্গাশ") || lower.contains("পাঙ্গাস") ||
            lower.contains("তেলাপিয়া") || lower.contains("তেলাপিয়া") || lower.contains("কৈ") || lower.contains("শিং") ||
            lower.contains("মাগুর") || lower.contains("পাবদা") || lower.contains("টেংরা") || lower.contains("বোয়াল") ||
            lower.contains("শুঁটকি") || lower.contains("শুটকি") || lower.contains("fish") -> Pair("fish_seafood", "মাছ ও সামুদ্রিক")

            lower.contains("মাংস") || lower.contains("গরু") || lower.contains("গোশত") || lower.contains("খাসি") ||
            lower.contains("মুরগি") || lower.contains("মুরগী") || lower.contains("চিকেন") || lower.contains("বিফ") ||
            lower.contains("মটন") || lower.contains("হাঁস") || lower.contains("কলিজা") || lower.contains("কিমা") ||
            lower.contains("meat") || lower.contains("beef") || lower.contains("chicken") || lower.contains("mutton") -> Pair("meat_poultry", "মাংস ও পোল্ট্রি")

            lower.contains("ডিম") || lower.contains("egg") -> Pair("dairy_eggs", "দুধ ও ডিম")

            lower.contains("দুধ") || lower.contains("দই") || lower.contains("ছানা") || lower.contains("পনির") ||
            lower.contains("মাখন") || lower.contains("বাটার") || lower.contains("ঘি") || lower.contains("milk") ||
            lower.contains("curd") || lower.contains("cheese") || lower.contains("butter") || lower.contains("ghee") -> Pair("dairy_eggs", "দুধ ও ডিম")

            lower.contains("আম") || lower.contains("কলা") || lower.contains("আপেল") || lower.contains("কমলা") ||
            lower.contains("মাল্টা") || lower.contains("আঙুর") || lower.contains("আঙ্গুর") || lower.contains("পেঁপে") ||
            lower.contains("পেপে") || lower.contains("তরমুজ") || lower.contains("পেয়ারা") || lower.contains("পেয়ারা") ||
            lower.contains("লিচু") || lower.contains("আনারস") || lower.contains("কাঁঠাল") || lower.contains("ডালিম") ||
            lower.contains("বেদানা") || lower.contains("খেজুর") || lower.contains("বাদাম") || lower.contains("ফল") ||
            lower.contains("fruit") || lower.contains("mango") || lower.contains("banana") || lower.contains("apple") -> Pair("fruits", "ফলমূল ও বাদাম")

            lower.contains("আলু") || lower.contains("পেঁয়াজ") || lower.contains("পেয়াজ") || lower.contains("রসুন") ||
            lower.contains("আদা") || lower.contains("মরিচ") || lower.contains("টমেটো") || lower.contains("বেগুন") ||
            lower.contains("পটল") || lower.contains("লাউ") || lower.contains("কুমড়া") || lower.contains("কুমড়া") ||
            lower.contains("ঝিঙে") || lower.contains("করলা") || lower.contains("উচ্ছে") || lower.contains("গাজর") ||
            lower.contains("শসা") || lower.contains("লেবু") || lower.contains("শাক") || lower.contains("ঢ্যাঁড়শ") ||
            lower.contains("ভেন্ডি") || lower.contains("ঝিঙ্গা") || lower.contains("কচু") || lower.contains("ক্যাপসিকাম") ||
            lower.contains("ফুলকপি") || lower.contains("বাঁধাকপি") || lower.contains("সবজি") || lower.contains("vegetable") -> Pair("vegetables", "শাকসবজি ও কাঁচাবাজার")

            lower.contains("চাল") || lower.contains("ডাল") || lower.contains("মুড়ি") || lower.contains("মুড়ি") ||
            lower.contains("চিড়া") || lower.contains("চিড়ে") || lower.contains("আটা") || lower.contains("ময়দা") ||
            lower.contains("সুজি") || lower.contains("তেল") || lower.contains("চিনি") || lower.contains("লবণ") ||
            lower.contains("নুডলস") || lower.contains("সেমাই") || lower.contains("চা") || lower.contains("কফি") ||
            lower.contains("rice") || lower.contains("oil") || lower.contains("sugar") || lower.contains("salt") -> Pair("rice_grains", "মুদি ও চাল-ডাল")

            else -> Pair("rice_grains", "মুদি ও কাঁচাবাজার")
        }
    }

    suspend fun toggleItemBought(item: ShoppingItemEntity) = withContext(Dispatchers.IO) {
        val newBought = !item.isBought
        val boughtAt = if (newBought) System.currentTimeMillis() else 0L
        itemDao.setItemBought(item.id, newBought, boughtAt)
        
        // Touch list updatedAt so Room DB observers and list ordering stay perfectly in sync
        val list = listDao.getListByIdSync(item.listId)
        if (list != null) {
            listDao.updateList(list.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun updateItem(item: ShoppingItemEntity) = withContext(Dispatchers.IO) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItem(itemId: String) = withContext(Dispatchers.IO) {
        itemDao.deleteItem(itemId)
    }

    suspend fun clearBoughtItems(listId: String) = withContext(Dispatchers.IO) {
        itemDao.clearBoughtItems(listId)
    }

    // --- TEMPLATES ---
    fun getAllTemplates(): Flow<List<TemplateEntity>> = templateDao.getAllTemplates()
    fun getTemplatesByCategory(category: String): Flow<List<TemplateEntity>> = templateDao.getTemplatesByCategory(category)
    fun getItemsForTemplate(templateId: String): Flow<List<TemplateItemEntity>> = templateDao.getItemsForTemplate(templateId)

    suspend fun createListFromTemplate(
        templateId: String,
        selectedItemIds: List<Long>? = null, // null means all items
        customTitle: String? = null
    ): String = withContext(Dispatchers.IO) {
        val template = templateDao.getTemplateById(templateId) ?: return@withContext ""
        val items = templateDao.getItemsForTemplateSync(templateId)

        val targetItems = if (selectedItemIds == null) {
            items
        } else {
            items.filter { selectedItemIds.contains(it.id) }
        }

        val mode = if (template.category == "STORE") AppMode.STORE else AppMode.PERSONAL
        val listId = createNewList(
            title = customTitle ?: template.titleBn,
            mode = mode
        )

        val shoppingItems = targetItems.map { tItem ->
            ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = listId,
                productId = tItem.productId,
                nameBn = tItem.nameBn,
                nameEn = tItem.nameEn,
                quantity = tItem.quantity,
                unit = tItem.unit,
                unitPrice = tItem.defaultPrice,
                categoryId = tItem.categoryId,
                categoryBn = tItem.categoryBn,
                isBought = false,
                createdAt = System.currentTimeMillis()
            )
        }
        itemDao.insertItems(shoppingItems)
        listId
    }

    suspend fun saveListAsCustomTemplate(
        listId: String,
        title: String,
        description: String = ""
    ): String = withContext(Dispatchers.IO) {
        val list = listDao.getListByIdSync(listId) ?: return@withContext ""
        val items = itemDao.getItemsForListSync(listId)

        val templateId = "custom_tpl_" + UUID.randomUUID().toString().take(8)
        val template = TemplateEntity(
            id = templateId,
            titleBn = title.ifBlank { list.title },
            icon = list.iconName,
            category = list.mode,
            descriptionBn = description,
            isCustom = true,
            createdAt = System.currentTimeMillis()
        )
        templateDao.insertTemplate(template)

        val templateItems = items.mapIndexed { idx, item ->
            TemplateItemEntity(
                templateId = templateId,
                productId = item.productId,
                nameBn = item.nameBn,
                nameEn = item.nameEn,
                quantity = item.quantity,
                unit = item.unit,
                defaultPrice = item.unitPrice,
                categoryId = item.categoryId,
                categoryBn = item.categoryBn,
                sortOrder = idx
            )
        }
        templateDao.insertTemplateItems(templateItems)
        templateId
    }

    suspend fun deleteCustomTemplate(templateId: String) = withContext(Dispatchers.IO) {
        templateDao.deleteItemsForTemplate(templateId)
        templateDao.deleteTemplate(templateId)
    }

    // --- COMPLETION & HISTORY ---
    suspend fun completeShoppingList(listId: String): ShoppingHistoryEntity = withContext(Dispatchers.IO) {
        val list = listDao.getListByIdSync(listId) ?: throw IllegalStateException("List not found")
        val items = itemDao.getItemsForListSync(listId)

        val boughtItems = items.filter { it.isBought }
        var totalCost = 0.0
        for (item in items) {
            val qty = BengaliNumberUtils.toEnglishDigits(item.quantity).toDoubleOrNull() ?: 1.0
            totalCost += (qty * item.unitPrice)
        }

        val summaryText = boughtItems.take(5).joinToString(", ") { "${it.nameBn} (${it.quantity} ${it.unit})" }

        val history = ShoppingHistoryEntity(
            id = UUID.randomUUID().toString(),
            listId = list.id,
            title = list.title,
            mode = list.mode,
            totalSpent = totalCost,
            budget = list.budget,
            totalItemCount = items.size,
            boughtItemCount = boughtItems.size,
            itemsSummary = summaryText,
            completedDate = System.currentTimeMillis()
        )

        historyDao.insertHistory(history)
        listDao.setListCompleted(listId, isCompleted = true)

        history
    }

    fun getAllHistory(): Flow<List<ShoppingHistoryEntity>> = historyDao.getAllHistory()
    suspend fun deleteHistory(historyId: String) = withContext(Dispatchers.IO) {
        historyDao.deleteHistory(historyId)
    }

    // ==========================================
    // --- EVENT & PICNIC / WEDDING PLANNER ---
    // ==========================================
    fun getAllActiveEvents(): Flow<List<EventPlanEntity>> = eventDao.getAllActiveEvents()
    fun getEventById(id: String): Flow<EventPlanEntity?> = eventDao.getEventById(id)

    suspend fun createEvent(
        title: String,
        eventType: String = "PICNIC",
        targetBudget: Double = 0.0,
        eventDate: Long = System.currentTimeMillis(),
        organizerName: String = "",
        location: String = "",
        notes: String = ""
    ): String = withContext(Dispatchers.IO) {
        val eventId = "evt_" + UUID.randomUUID().toString().take(8)
        val event = EventPlanEntity(
            id = eventId,
            title = title.trim().ifBlank { "পিকনিক ও ইভেন্ট হিসাব" },
            eventType = eventType,
            targetBudget = targetBudget,
            eventDate = eventDate,
            organizerName = organizerName.trim(),
            location = location.trim(),
            notes = notes.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        eventDao.insertEvent(event)
        eventId
    }

    suspend fun updateEvent(event: EventPlanEntity) = withContext(Dispatchers.IO) {
        eventDao.updateEvent(event.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteEvent(eventId: String) = withContext(Dispatchers.IO) {
        eventDao.deleteEvent(eventId)
    }

    // --- EVENT MEMBERS (CONTRIBUTIONS) ---
    fun getMembersForEvent(eventId: String): Flow<List<EventMemberEntity>> = eventDao.getMembersForEvent(eventId)

    suspend fun addMemberToEvent(
        eventId: String,
        name: String,
        phone: String = "",
        targetAmount: Double = 0.0,
        paidAmount: Double = 0.0,
        isPaid: Boolean = false,
        paymentMethod: String = "নগদ",
        notes: String = ""
    ): String = withContext(Dispatchers.IO) {
        val memberId = UUID.randomUUID().toString()
        val member = EventMemberEntity(
            id = memberId,
            eventId = eventId,
            name = name.trim().ifBlank { "সদস্য" },
            phone = phone.trim(),
            targetAmount = targetAmount,
            paidAmount = if (isPaid && paidAmount == 0.0) targetAmount else paidAmount,
            isPaid = isPaid || (paidAmount > 0 && paidAmount >= targetAmount && targetAmount > 0),
            paymentMethod = paymentMethod,
            notes = notes.trim(),
            createdAt = System.currentTimeMillis()
        )
        eventDao.insertMember(member)
        memberId
    }

    suspend fun addMembersBatch(eventId: String, rawNames: String, defaultTarget: Double = 0.0) = withContext(Dispatchers.IO) {
        val lines = rawNames.split("\n", ",", "।", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val members = lines.map { line ->
            // Support formats like "রাকিব ৫০০" or "সাকিব"
            val parts = line.split("\\s+".toRegex())
            val possibleAmount = parts.lastOrNull()?.let { BengaliNumberUtils.toEnglishDigits(it).toDoubleOrNull() }
            val name = if (possibleAmount != null && parts.size > 1) parts.dropLast(1).joinToString(" ") else line
            val target = possibleAmount ?: defaultTarget

            EventMemberEntity(
                id = UUID.randomUUID().toString(),
                eventId = eventId,
                name = name,
                targetAmount = target,
                paidAmount = 0.0,
                isPaid = false,
                paymentMethod = "নগদ",
                createdAt = System.currentTimeMillis()
            )
        }
        if (members.isNotEmpty()) {
            eventDao.insertMembers(members)
        }
    }

    suspend fun setMemberPayment(memberId: String, isPaid: Boolean, paidAmount: Double) = withContext(Dispatchers.IO) {
        eventDao.setMemberPayment(memberId, isPaid, paidAmount)
    }

    suspend fun deleteMember(memberId: String) = withContext(Dispatchers.IO) {
        eventDao.deleteMember(memberId)
    }

    // --- EVENT EXPENSES & BAZAAR ITEMS ---
    fun getExpensesForEvent(eventId: String): Flow<List<EventExpenseEntity>> = eventDao.getExpensesForEvent(eventId)

    suspend fun addExpenseToEvent(
        eventId: String,
        title: String,
        category: String = "BAZAAR",
        quantity: String = "১",
        unit: String = "কেজি",
        unitPrice: Double = 0.0,
        amount: Double = 0.0,
        paidBy: String = "কমন ফান্ড",
        isBought: Boolean = false,
        notes: String = ""
    ): String = withContext(Dispatchers.IO) {
        val expenseId = UUID.randomUUID().toString()
        val calculatedAmount = if (amount > 0.0) amount else {
            val q = BengaliNumberUtils.toEnglishDigits(quantity).toDoubleOrNull() ?: 1.0
            q * unitPrice
        }
        val expense = EventExpenseEntity(
            id = expenseId,
            eventId = eventId,
            title = title.trim(),
            category = category,
            quantity = quantity,
            unit = unit,
            unitPrice = unitPrice,
            amount = calculatedAmount,
            paidBy = paidBy.trim().ifBlank { "কমন ফান্ড" },
            isBought = isBought,
            notes = notes.trim(),
            createdAt = System.currentTimeMillis()
        )
        eventDao.insertExpense(expense)
        expenseId
    }

    suspend fun setExpenseBought(expenseId: String, isBought: Boolean) = withContext(Dispatchers.IO) {
        eventDao.setExpenseBought(expenseId, isBought)
    }

    suspend fun deleteExpense(expenseId: String) = withContext(Dispatchers.IO) {
        eventDao.deleteExpense(expenseId)
    }

    suspend fun getEventInvoiceSummary(eventId: String): EventInvoiceSummary? = withContext(Dispatchers.IO) {
        val event = eventDao.getEventByIdSync(eventId) ?: return@withContext null
        val members = eventDao.getMembersForEventSync(eventId)
        val expenses = eventDao.getExpensesForEventSync(eventId)

        val totalTarget = members.sumOf { it.targetAmount }
        val totalPaid = members.sumOf { it.paidAmount }
        val totalPending = (totalTarget - totalPaid).coerceAtLeast(0.0)
        val totalExp = expenses.sumOf { it.amount }
        val net = totalPaid - totalExp

        EventInvoiceSummary(
            event = event,
            members = members,
            expenses = expenses,
            totalTargetCollection = totalTarget,
            totalPaidCollection = totalPaid,
            totalPendingCollection = totalPending,
            totalExpenses = totalExp,
            netBalance = net
        )
    }

    suspend fun convertEventBazaarToShoppingList(eventId: String): String = withContext(Dispatchers.IO) {
        val event = eventDao.getEventByIdSync(eventId) ?: return@withContext ""
        val expenses = eventDao.getExpensesForEventSync(eventId)

        val listId = createNewList(
            title = "${event.title} — বাজার ফর্দ",
            mode = AppMode.PERSONAL,
            budget = event.targetBudget
        )

        val items = expenses.map { exp ->
            val (catId, catBn) = guessCategory(exp.title)
            ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = listId,
                productId = null,
                nameBn = exp.title,
                quantity = exp.quantity,
                unit = exp.unit,
                unitPrice = exp.unitPrice,
                isBought = exp.isBought,
                categoryId = catId,
                categoryBn = catBn,
                notes = if (exp.paidBy.isNotBlank() && exp.paidBy != "কমন ফান্ড") "পরিশোধক: ${exp.paidBy}" else exp.notes,
                createdAt = System.currentTimeMillis()
            )
        }
        itemDao.insertItems(items)
        listId
    }

    // ==========================================
    // --- SMART TAPE & QUICK DUES CALCULATOR ---
    // ==========================================
    fun getAllTapeNotes(): Flow<List<QuickTapeNoteEntity>> = tapeNoteDao.getAllNotes()

    suspend fun saveTapeNote(
        title: String,
        rawEntriesText: String,
        totalSum: Double,
        noteType: String = "CALC_TAPE",
        personName: String = "",
        isSettled: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val noteId = "tape_" + UUID.randomUUID().toString().take(8)
        val note = QuickTapeNoteEntity(
            id = noteId,
            title = title.trim().ifBlank { if (noteType == "DUES_NOTE") "বাকি হিসাব" else "হিসাব-নোট" },
            rawEntriesText = rawEntriesText.trim(),
            totalSum = totalSum,
            noteType = noteType,
            personName = personName.trim(),
            isSettled = isSettled,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        tapeNoteDao.insertNote(note)
        noteId
    }

    suspend fun setNoteSettled(noteId: String, isSettled: Boolean) = withContext(Dispatchers.IO) {
        tapeNoteDao.setNoteSettled(noteId, isSettled)
    }

    suspend fun deleteTapeNote(noteId: String) = withContext(Dispatchers.IO) {
        tapeNoteDao.deleteNote(noteId)
    }

    suspend fun convertTapeToShoppingList(title: String, rawEntriesText: String): String = withContext(Dispatchers.IO) {
        val listId = createNewList(
            title = title.ifBlank { "ক্যালকুলেটরের বাজার ফর্দ" },
            mode = AppMode.PERSONAL
        )

        // Parse line by line
        val lines = rawEntriesText.split("\n").filter { it.isNotBlank() }
        for (line in lines) {
            smartQuickEntry(listId, line)
        }
        listId
    }
}
