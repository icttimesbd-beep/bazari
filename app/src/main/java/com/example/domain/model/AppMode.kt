package com.example.domain.model

enum class AppMode(val titleBn: String, val titleEn: String) {
    PERSONAL("ব্যক্তিগত", "Personal / Family"),
    STORE("দোকান", "Store / Wholesale")
}

data class ParsedQuickEntry(
    val rawText: String,
    val quantity: String,
    val unit: String,
    val productName: String,
    val matchedProductId: String? = null
)
