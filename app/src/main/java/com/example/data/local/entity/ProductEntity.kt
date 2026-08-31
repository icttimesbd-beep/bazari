package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["nameBn"]),
        Index(value = ["nameEn"]),
        Index(value = ["categoryId"]),
        Index(value = ["isCommon"]),
        Index(value = ["isFavorite"]),
        Index(value = ["usageCount"])
    ]
)
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val nameBn: String,
    val nameEn: String = "",
    val aliasesBn: String = "", // Comma-separated aliases
    val aliasesEn: String = "", // Comma-separated aliases
    val categoryId: String,
    val defaultUnit: String = "কেজি",
    val defaultPrice: Double = 0.0,
    val isCommon: Boolean = false,
    val isFavorite: Boolean = false,
    val usageCount: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val isCustom: Boolean = false,
    val ageRestricted: Boolean = false
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val icon: String,
    val color: String
)
