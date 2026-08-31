package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY isCommon DESC, usageCount DESC, nameBn ASC LIMIT :limit")
    fun getAllProducts(limit: Int = 200): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isCommon = 1 ORDER BY usageCount DESC, nameBn ASC LIMIT :limit")
    fun getCommonProducts(limit: Int = 12): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isFavorite = 1 ORDER BY usageCount DESC, nameBn ASC")
    fun getFavoriteProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE usageCount > 0 ORDER BY lastUsedTimestamp DESC LIMIT :limit")
    fun getRecentProducts(limit: Int = 15): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE (categoryId = :categoryId OR :categoryId = '')
        AND (
            nameBn LIKE '%' || :query || '%'
            OR nameEn LIKE '%' || :query || '%'
            OR aliasesBn LIKE '%' || :query || '%'
            OR aliasesEn LIKE '%' || :query || '%'
        )
        ORDER BY 
            CASE 
                WHEN nameBn = :query THEN 1
                WHEN aliasesBn = :query THEN 2
                WHEN aliasesBn LIKE :query || ',%' OR aliasesBn LIKE '%,' || :query || ',%' OR aliasesBn LIKE '%,' || :query THEN 3
                WHEN nameBn LIKE :query || '%' THEN 4
                WHEN nameEn = :query THEN 5
                WHEN aliasesEn = :query THEN 6
                WHEN aliasesEn LIKE :query || ',%' OR aliasesEn LIKE '%,' || :query || ',%' OR aliasesEn LIKE '%,' || :query THEN 7
                WHEN nameEn LIKE :query || '%' THEN 8
                ELSE 9
            END,
            isCommon DESC,
            usageCount DESC
        LIMIT :limit
    """)
    fun searchProducts(query: String, categoryId: String = "", limit: Int = 50): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE nameBn = :query 
           OR nameEn = :query 
           OR aliasesBn = :query
           OR aliasesBn LIKE :query || ',%'
           OR aliasesBn LIKE '%,' || :query || ',%'
           OR aliasesBn LIKE '%,' || :query
           OR aliasesEn = :query
           OR aliasesEn LIKE :query || ',%'
           OR aliasesEn LIKE '%,' || :query || ',%'
           OR aliasesEn LIKE '%,' || :query
        LIMIT 1
    """)
    suspend fun findExactMatch(query: String): ProductEntity?

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY isCommon DESC, usageCount DESC, nameBn ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE products SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementUsage(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT * FROM products ORDER BY nameBn ASC")
    suspend fun getAllProductsList(): List<ProductEntity>

    @Query("DELETE FROM products WHERE isCustom = 0")
    suspend fun clearDefaultProducts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProducts(products: List<ProductEntity>)

    // Categories
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
}
