package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_lists",
    indices = [
        Index(value = ["mode"]),
        Index(value = ["isCompleted"]),
        Index(value = ["updatedAt"])
    ]
)
data class ShoppingListEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val mode: String = "PERSONAL", // "PERSONAL" or "STORE"
    val budget: Double = 0.0,
    val targetDate: Long = 0L,
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val colorIndex: Int = 0,
    val iconName: String = "shopping_cart",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"]),
        Index(value = ["isBought"]),
        Index(value = ["categoryId"])
    ]
)
data class ShoppingItemEntity(
    @PrimaryKey
    val id: String,
    val listId: String,
    val productId: String? = null,
    val nameBn: String,
    val nameEn: String = "",
    val quantity: String = "১",
    val unit: String = "কেজি",
    val unitPrice: Double = 0.0,
    val isBought: Boolean = false,
    val boughtAt: Long = 0L,
    val categoryId: String = "rice_grains",
    val categoryBn: String = "মুদি",
    val brand: String = "",
    val notes: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
