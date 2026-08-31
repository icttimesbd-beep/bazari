package com.example.domain.model

enum class AppLanguage(
    val code: String,
    val titleBn: String,
    val titleEn: String,
    val shortLabel: String
) {
    BN("bn", "বাংলা", "Bengali", "বাং"),
    EN("en", "ইংরেজি", "English", "EN")
}
