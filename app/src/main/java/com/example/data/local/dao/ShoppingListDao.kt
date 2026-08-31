package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

data class ShoppingListWithItems(
    val list: ShoppingListEntity,
    val items: List<ShoppingItemEntity>
)

@Dao
interface ShoppingListDao {

    @Query("SELECT * FROM shopping_lists WHERE mode = :mode ORDER BY isCompleted ASC, updatedAt DESC")
    fun getListsByMode(mode: String): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE isCompleted = 0 ORDER BY updatedAt DESC")
    fun getAllActiveLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id LIMIT 1")
    fun getListById(id: String): Flow<ShoppingListEntity?>

    @Query("SELECT * FROM shopping_lists WHERE id = :id LIMIT 1")
    suspend fun getListByIdSync(id: String): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity)

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Query("UPDATE shopping_lists SET isCompleted = :isCompleted, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setListCompleted(id: String, isCompleted: Boolean, completedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE shopping_lists SET budget = :budget, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBudget(id: String, budget: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteList(id: String)

    @Query("SELECT COUNT(*) FROM shopping_lists WHERE isCompleted = 0")
    fun getActiveListCount(): Flow<Int>
}

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isBought ASC, sortOrder ASC, createdAt DESC")
    fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE listId = :listId")
    suspend fun getItemsForListSync(listId: String): List<ShoppingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET isBought = :isBought, boughtAt = :boughtAt WHERE id = :id")
    suspend fun setItemBought(id: String, isBought: Boolean, boughtAt: Long = System.currentTimeMillis())

    @Query("UPDATE shopping_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: String)

    @Query("UPDATE shopping_items SET unitPrice = :unitPrice WHERE id = :id")
    suspend fun updatePrice(id: String, unitPrice: Double)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("DELETE FROM shopping_items WHERE listId = :listId AND isBought = 1")
    suspend fun clearBoughtItems(listId: String)

    @Query("DELETE FROM shopping_items WHERE listId = :listId")
    suspend fun deleteAllItemsForList(listId: String)

    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId")
    fun getItemCountForList(listId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId AND isBought = 1")
    fun getBoughtItemCountForList(listId: String): Flow<Int>
}
