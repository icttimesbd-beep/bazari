package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ProductMemoryEntity
import com.example.data.local.entity.ShoppingHistoryEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.TemplateItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Query("SELECT * FROM templates ORDER BY isCustom DESC, titleBn ASC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE category = :category ORDER BY isCustom DESC, titleBn ASC")
    fun getTemplatesByCategory(category: String): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: String): TemplateEntity?

    @Query("SELECT * FROM template_items WHERE templateId = :templateId ORDER BY sortOrder ASC, id ASC")
    fun getItemsForTemplate(templateId: String): Flow<List<TemplateItemEntity>>

    @Query("SELECT * FROM template_items WHERE templateId = :templateId ORDER BY sortOrder ASC, id ASC")
    suspend fun getItemsForTemplateSync(templateId: String): List<TemplateItemEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplates(templates: List<TemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateItems(items: List<TemplateItemEntity>)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)

    @Query("DELETE FROM template_items WHERE templateId = :templateId")
    suspend fun deleteItemsForTemplate(templateId: String)

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun getTemplateCount(): Int
}

@Dao
interface ProductMemoryDao {

    @Query("SELECT * FROM product_memory WHERE productId = :productId LIMIT 1")
    suspend fun getMemoryForProduct(productId: String): ProductMemoryEntity?

    @Query("SELECT * FROM product_memory ORDER BY lastUsedTimestamp DESC LIMIT :limit")
    fun getRecentMemories(limit: Int = 20): Flow<List<ProductMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMemory(memory: ProductMemoryEntity)

    @Query("DELETE FROM product_memory WHERE productId = :productId")
    suspend fun deleteMemory(productId: String)
}

@Dao
interface HistoryDao {

    @Query("SELECT * FROM shopping_history ORDER BY completedDate DESC")
    fun getAllHistory(): Flow<List<ShoppingHistoryEntity>>

    @Query("SELECT * FROM shopping_history WHERE mode = :mode ORDER BY completedDate DESC")
    fun getHistoryByMode(mode: String): Flow<List<ShoppingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ShoppingHistoryEntity)

    @Query("DELETE FROM shopping_history WHERE id = :id")
    suspend fun deleteHistory(id: String)

    @Query("DELETE FROM shopping_history")
    suspend fun clearAllHistory()
}
