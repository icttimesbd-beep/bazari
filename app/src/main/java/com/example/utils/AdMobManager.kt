package com.example.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"

    // Google AdMob Ad Unit IDs
    const val BANNER_AD_UNIT_ID = "ca-app-pub-4405011631511984/4627012813"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-4405011631511984/2000849474"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4405011631511984/6239477389"

    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    private var isAdLoading = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context.applicationContext) { initializationStatus ->
                isInitialized = true
                Log.d(TAG, "AdMob SDK initialized successfully: $initializationStatus")
                try {
                    loadInterstitialAd(context.applicationContext)
                } catch (e: Throwable) {
                    Log.e(TAG, "Error loading interstitial ad", e)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize AdMob SDK", e)
        }
    }

    fun loadInterstitialAd(context: Context, adUnitId: String = INTERSTITIAL_AD_UNIT_ID) {
        if (isAdLoading || interstitialAd != null) return
        isAdLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isAdLoading = false
                        Log.d(TAG, "Interstitial ad loaded.")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isAdLoading = false
                        Log.w(TAG, "Interstitial ad failed to load: ${error.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            interstitialAd = null
            isAdLoading = false
            Log.e(TAG, "Exception during InterstitialAd.load", e)
        }
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        val currentAd = interstitialAd
        if (currentAd != null) {
            try {
                currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd(activity)
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                        loadInterstitialAd(activity)
                        onAdDismissed()
                    }
                }
                currentAd.show(activity)
            } catch (e: Throwable) {
                Log.e(TAG, "Exception showing interstitial ad", e)
                interstitialAd = null
                onAdDismissed()
            }
        } else {
            // If ad not ready, proceed smoothly without blocking the user
            loadInterstitialAd(activity)
            onAdDismissed()
        }
    }
}
