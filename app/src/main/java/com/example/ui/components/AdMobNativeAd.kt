package com.example.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.utils.AdMobManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun AdMobNativeAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.NATIVE_AD_UNIT_ID
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isFailed by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f).toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    DisposableEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
                isFailed = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isFailed = true
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    if (nativeAd != null && !isFailed) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .testTag("admob_native_ad_box")
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    createNativeAdView(
                        ctx,
                        nativeAd!!,
                        primaryColor,
                        surfaceVariantColor,
                        onSurfaceColor,
                        onSurfaceVariantColor
                    )
                },
                update = { nativeAdView ->
                    nativeAd?.let { populateNativeAdView(nativeAdView, it) }
                }
            )
        }
    }
}

private fun createNativeAdView(
    context: Context,
    nativeAd: NativeAd,
    primaryColor: Int,
    surfaceVariantColor: Int,
    onSurfaceColor: Int,
    onSurfaceVariantColor: Int
): NativeAdView {
    val nativeAdView = NativeAdView(context)
    val density = context.resources.displayMetrics.density

    fun dp(value: Float) = (value * density).toInt()

    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12f), dp(12f), dp(12f), dp(12f))
        setBackgroundColor(surfaceVariantColor)
    }

    // Top Header: Ad Attribution Badge + Headline + Icon
    val topRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    val iconView = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(dp(44f), dp(44f)).apply {
            marginEnd = dp(10f)
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
    }
    nativeAdView.iconView = iconView
    topRow.addView(iconView)

    val headlineCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val adBadge = TextView(context).apply {
        text = "Ad"
        textSize = 10f
        setTypeface(null, Typeface.BOLD)
        setTextColor(AndroidColor.WHITE)
        setBackgroundColor(primaryColor)
        setPadding(dp(4f), dp(1f), dp(4f), dp(1f))
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(2f)
        }
    }
    headlineCol.addView(adBadge)

    val headlineView = TextView(context).apply {
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        setTextColor(onSurfaceColor)
        maxLines = 1
    }
    nativeAdView.headlineView = headlineView
    headlineCol.addView(headlineView)

    val advertiserView = TextView(context).apply {
        textSize = 11f
        setTextColor(onSurfaceVariantColor)
        maxLines = 1
    }
    nativeAdView.advertiserView = advertiserView
    headlineCol.addView(advertiserView)

    topRow.addView(headlineCol)
    rootLayout.addView(topRow)

    // Body Text
    val bodyView = TextView(context).apply {
        textSize = 12f
        setTextColor(onSurfaceVariantColor)
        maxLines = 2
        setPadding(0, dp(6f), 0, dp(8f))
    }
    nativeAdView.bodyView = bodyView
    rootLayout.addView(bodyView)

    // Call To Action Button
    val callToActionView = Button(context).apply {
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        setTextColor(AndroidColor.WHITE)
        setBackgroundColor(primaryColor)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40f))
    }
    nativeAdView.callToActionView = callToActionView
    rootLayout.addView(callToActionView)

    nativeAdView.addView(rootLayout)
    populateNativeAdView(nativeAdView, nativeAd)

    return nativeAdView
}

private fun populateNativeAdView(nativeAdView: NativeAdView, nativeAd: NativeAd) {
    (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline

    if (nativeAd.body != null) {
        nativeAdView.bodyView?.visibility = View.VISIBLE
        (nativeAdView.bodyView as? TextView)?.text = nativeAd.body
    } else {
        nativeAdView.bodyView?.visibility = View.GONE
    }

    if (nativeAd.callToAction != null) {
        nativeAdView.callToActionView?.visibility = View.VISIBLE
        (nativeAdView.callToActionView as? Button)?.text = nativeAd.callToAction
    } else {
        nativeAdView.callToActionView?.visibility = View.GONE
    }

    if (nativeAd.icon != null) {
        nativeAdView.iconView?.visibility = View.VISIBLE
        (nativeAdView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
    } else {
        nativeAdView.iconView?.visibility = View.GONE
    }

    if (nativeAd.advertiser != null) {
        nativeAdView.advertiserView?.visibility = View.VISIBLE
        (nativeAdView.advertiserView as? TextView)?.text = nativeAd.advertiser
    } else {
        nativeAdView.advertiserView?.visibility = View.GONE
    }

    nativeAdView.setNativeAd(nativeAd)
}
