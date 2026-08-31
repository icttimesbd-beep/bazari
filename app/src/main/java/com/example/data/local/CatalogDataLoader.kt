package com.example.data.local

import android.content.Context
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.TemplateItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CatalogDataLoader(private val context: Context) {

    suspend fun loadCatalogIfNeeded(database: AppDatabase, forceReload: Boolean = false) = withContext(Dispatchers.IO) {
        val productCount = database.productDao().getProductCount()
        if (productCount >= 5000 && !forceReload) {
            return@withContext
        }

        try {
            // 1. Load Categories
            val categoriesJsonStr = context.assets.open("catalog/categories.json").bufferedReader().use { it.readText() }
            val categoriesJsonObj = JSONObject(categoriesJsonStr)
            val categoriesArray = categoriesJsonObj.getJSONArray("categories")
            val categoryEntities = mutableListOf<CategoryEntity>()

            for (i in 0 until categoriesArray.length()) {
                val obj = categoriesArray.getJSONObject(i)
                categoryEntities.add(
                    CategoryEntity(
                        id = obj.getString("id"),
                        nameBn = obj.getString("nameBn"),
                        nameEn = obj.optString("nameEn", ""),
                        icon = obj.optString("icon", "category"),
                        color = obj.optString("color", "#4CAF50")
                    )
                )
            }
            database.productDao().insertCategories(categoryEntities)

            // 2. Load Products (Batch insertion for large catalog)
            val productsJsonStr = context.assets.open("catalog/products.json").bufferedReader().use { it.readText() }
            val productsArray = JSONArray(productsJsonStr)
            val batchSize = 500
            var currentBatch = mutableListOf<ProductEntity>()

            for (i in 0 until productsArray.length()) {
                val obj = productsArray.getJSONObject(i)
                val aliasesBnArray = obj.optJSONArray("aliasesBn")
                val aliasesBn = if (aliasesBnArray != null) {
                    (0 until aliasesBnArray.length()).joinToString(",") { aliasesBnArray.getString(it) }
                } else obj.optString("aliasesBn", "")

                val aliasesEnArray = obj.optJSONArray("aliasesEn")
                val aliasesEn = if (aliasesEnArray != null) {
                    (0 until aliasesEnArray.length()).joinToString(",") { aliasesEnArray.getString(it) }
                } else obj.optString("aliasesEn", "")

                currentBatch.add(
                    ProductEntity(
                        id = obj.getString("id"),
                        nameBn = obj.getString("nameBn"),
                        nameEn = obj.optString("nameEn", ""),
                        aliasesBn = aliasesBn,
                        aliasesEn = aliasesEn,
                        categoryId = obj.getString("categoryId"),
                        defaultUnit = obj.optString("defaultUnit", "কেজি"),
                        defaultPrice = obj.optDouble("defaultPrice", 0.0),
                        isCommon = obj.optBoolean("isCommon", false),
                        isFavorite = false,
                        usageCount = 0,
                        lastUsedTimestamp = 0L,
                        isCustom = false,
                        ageRestricted = obj.optBoolean("ageRestricted", false)
                    )
                )

                if (currentBatch.size >= batchSize) {
                    database.productDao().insertOrUpdateProducts(currentBatch)
                    currentBatch = mutableListOf()
                }
            }

            if (currentBatch.isNotEmpty()) {
                database.productDao().insertOrUpdateProducts(currentBatch)
            }

            // 3. Load Templates
            val templatesCount = database.templateDao().getTemplateCount()
            if (templatesCount == 0 || forceReload) {
                val templatesJsonStr = context.assets.open("catalog/templates.json").bufferedReader().use { it.readText() }
                val templatesArray = JSONArray(templatesJsonStr)
                val templateEntities = mutableListOf<TemplateEntity>()
                val templateItemEntities = mutableListOf<TemplateItemEntity>()

                for (i in 0 until templatesArray.length()) {
                    val obj = templatesArray.getJSONObject(i)
                    val templateId = obj.getString("id")
                    val template = TemplateEntity(
                        id = templateId,
                        titleBn = obj.getString("titleBn"),
                        titleEn = obj.optString("titleEn", ""),
                        icon = obj.optString("icon", "shopping_cart"),
                        category = obj.optString("category", "PERSONAL"),
                        descriptionBn = obj.optString("descriptionBn", ""),
                        descriptionEn = obj.optString("descriptionEn", ""),
                        isCustom = false
                    )
                    templateEntities.add(template)

                    val itemsArray = obj.getJSONArray("items")
                    for (j in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(j)
                        templateItemEntities.add(
                            TemplateItemEntity(
                                templateId = templateId,
                                productId = itemObj.optString("productId", ""),
                                nameBn = itemObj.getString("nameBn"),
                                nameEn = itemObj.optString("nameEn", ""),
                                quantity = itemObj.optString("quantity", "১"),
                                unit = itemObj.optString("unit", "কেজি"),
                                defaultPrice = itemObj.optDouble("defaultPrice", 0.0),
                                categoryId = "rice_grains",
                                categoryBn = "মুদি",
                                sortOrder = j
                            )
                        )
                    }
                }
                database.templateDao().insertTemplates(templateEntities)
                database.templateDao().insertTemplateItems(templateItemEntities)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun exportProductsToJson(database: AppDatabase): String = withContext(Dispatchers.IO) {
        val products = database.productDao().getAllProductsList()
        val array = JSONArray()
        for (p in products) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("nameBn", p.nameBn)
            obj.put("nameEn", p.nameEn)
            obj.put("aliasesBn", p.aliasesBn)
            obj.put("aliasesEn", p.aliasesEn)
            obj.put("categoryId", p.categoryId)
            obj.put("defaultUnit", p.defaultUnit)
            obj.put("defaultPrice", p.defaultPrice)
            obj.put("isCommon", p.isCommon)
            obj.put("isCustom", p.isCustom)
            array.put(obj)
        }
        array.toString(2)
    }

    suspend fun importProductsFromJson(database: AppDatabase, jsonStr: String): Int = withContext(Dispatchers.IO) {
        try {
            val array = JSONArray(jsonStr.trim())
            val productEntities = mutableListOf<ProductEntity>()
            val batchSize = 500

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id").ifBlank { "custom_${System.currentTimeMillis()}_$i" }
                val nameBn = obj.optString("nameBn").ifBlank { obj.optString("name", "পণ্য") }
                val nameEn = obj.optString("nameEn", "")
                val aliasesBn = obj.optString("aliasesBn", "")
                val aliasesEn = obj.optString("aliasesEn", "")
                val catId = obj.optString("categoryId", "general")
                val unit = obj.optString("defaultUnit", "কেজি")
                val price = obj.optDouble("defaultPrice", 0.0)

                productEntities.add(
                    ProductEntity(
                        id = id,
                        nameBn = nameBn,
                        nameEn = nameEn,
                        aliasesBn = aliasesBn,
                        aliasesEn = aliasesEn,
                        categoryId = catId,
                        defaultUnit = unit,
                        defaultPrice = price,
                        isCommon = obj.optBoolean("isCommon", false),
                        isFavorite = false,
                        usageCount = 0,
                        lastUsedTimestamp = 0L,
                        isCustom = true
                    )
                )

                if (productEntities.size >= batchSize) {
                    database.productDao().insertOrUpdateProducts(productEntities)
                    productEntities.clear()
                }
            }

            if (productEntities.isNotEmpty()) {
                database.productDao().insertOrUpdateProducts(productEntities)
            }

            array.length()
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
}
