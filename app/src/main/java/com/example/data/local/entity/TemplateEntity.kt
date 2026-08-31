package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "templates",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isCustom"])
    ]
)
data class TemplateEntity(
    @PrimaryKey
    val id: String,
    val titleBn: String,
    val titleEn: String = "",
    val icon: String = "shopping_cart",
    val category: String = "PERSONAL", // "PERSONAL" or "STORE"
    val descriptionBn: String = "",
    val descriptionEn: String = "",
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "template_items",
    foreignKeys = [
        ForeignKey(
            entity = TemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["templateId"])
    ]
)
data class TemplateItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: String,
    val productId: String? = null,
    val nameBn: String,
    val nameEn: String = "",
    val quantity: String = "১",
    val unit: String = "কেজি",
    val defaultPrice: Double = 0.0,
    val categoryId: String = "rice_grains",
    val categoryBn: String = "মুদি",
    val sortOrder: Int = 0
)

@Entity(
    tableName = "product_memory",
    indices = [
        Index(value = ["productId"], unique = true),
        Index(value = ["useCount"]),
        Index(value = ["lastUsedTimestamp"])
    ]
)
data class ProductMemoryEntity(
    @PrimaryKey
    val productId: String,
    val lastQuantity: String = "১",
    val lastUnit: String = "কেজি",
    val lastPrice: Double = 0.0,
    val lastBrand: String = "",
    val useCount: Int = 1,
    val isFavorite: Boolean = false,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shopping_history",
    indices = [
        Index(value = ["completedDate"]),
        Index(value = ["mode"])
    ]
)
data class ShoppingHistoryEntity(
    @PrimaryKey
    val id: String,
    val listId: String,
    val title: String,
    val mode: String = "PERSONAL",
    val totalSpent: Double = 0.0,
    val budget: Double = 0.0,
    val totalItemCount: Int = 0,
    val boughtItemCount: Int = 0,
    val itemsSummary: String = "", // Comma separated items summary
    val completedDate: Long = System.currentTimeMillis()
)
