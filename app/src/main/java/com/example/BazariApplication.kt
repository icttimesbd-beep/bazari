package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.utils.AdMobManager
import com.example.utils.LanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BazariApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Global crash guard to prevent abrupt process kills
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("BazariApplication", "Uncaught exception in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Initialize language configuration safely
        try {
            LanguageManager.init(this)
        } catch (e: Throwable) {
            Log.e("BazariApplication", "LanguageManager init error", e)
        }

        // Initialize AdMob asynchronously
        applicationScope.launch {
            try {
                AdMobManager.initialize(applicationContext)
            } catch (e: Throwable) {
                Log.e("BazariApplication", "AdMob initialization error", e)
            }
        }

        // Pre-warm database in background
        applicationScope.launch {
            try {
                AppDatabase.getDatabase(applicationContext)
            } catch (e: Throwable) {
                Log.e("BazariApplication", "Database initialization error", e)
            }
        }
    }
}
