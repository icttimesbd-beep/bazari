package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LanguageManager {
    private const val PREFS_NAME = "fordo_preferences"
    private const val KEY_LANGUAGE = "app_language"

    private var sharedPreferences: SharedPreferences? = null
    private val _currentLanguage = MutableStateFlow(AppLanguage.BN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        if (sharedPreferences == null) {
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences = prefs
            val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.BN.code)
            val lang = if (savedCode == AppLanguage.EN.code) AppLanguage.EN else AppLanguage.BN
            _currentLanguage.value = lang
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        sharedPreferences?.edit()?.putString(KEY_LANGUAGE, language.code)?.apply()
    }

    fun toggleLanguage(): AppLanguage {
        val next = if (_currentLanguage.value == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN
        setLanguage(next)
        return next
    }

    fun isBangla(): Boolean = _currentLanguage.value == AppLanguage.BN
    fun isEnglish(): Boolean = _currentLanguage.value == AppLanguage.EN
}
