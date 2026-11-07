package com.example.ui.components

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.monetization.AdManager
import com.example.ui.theme.CharcoalSecondaryBg
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Clean, dark-themed Native Ad Card for Google AdMob.
 * Adheres to Google Play ad policies: clearly marked "Ad", non-intrusive,
 * and completely disabled for premium VIP subscribers.
 */
@Composable
fun AdNativeCard(
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) {
        // Zero ads for premium subscribers
        return
    }

    val context = LocalContext.current
    val isTestMode by AdManager.isTestMode.collectAsState()
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(isTestMode, isPremium) {
        if (isPremium) return@LaunchedEffect

        val unitId = AdManager.getNativeAdUnitId()
        val adLoader = AdLoader.Builder(context, unitId)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
                isLoaded = true
                AdManager.recordNativeLoaded()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoaded = false
                    AdManager.recordNativeFailed(loadAdError)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        try {
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Log.e("AdNativeCard", "Error triggering native ad request: ${e.message}")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
        }
    }

    if (nativeAd != null) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 6.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = CharcoalSecondaryBg,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    createNativeAdLayout(ctx, nativeAd!!)
                },
                update = { view ->
                    nativeAd?.let { populateNativeAdView(it, view) }
                }
            )
        }
    }
}

private fun createNativeAdLayout(context: Context, ad: NativeAd): NativeAdView {
    val nativeAdView = NativeAdView(context)
    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dpToPx(context, 14), dpToPx(context, 12), dpToPx(context, 14), dpToPx(context, 12))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // Header Row: [AD Badge] + Headline + Advertiser
    val headerRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    val adBadge = TextView(context).apply {
        text = "AD"
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(android.graphics.Color.parseColor("#FF9500"))
        setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
        setPadding(dpToPx(context, 5), dpToPx(context, 1), dpToPx(context, 5), dpToPx(context, 1))
    }

    val iconView = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(dpToPx(context, 32), dpToPx(context, 32)).apply {
            marginStart = dpToPx(context, 8)
            marginEnd = dpToPx(context, 8)
        }
    }

    val headlineView = TextView(context).apply {
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(android.graphics.Color.WHITE)
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    headerRow.addView(adBadge)
    headerRow.addView(iconView)
    headerRow.addView(headlineView)
    rootLayout.addView(headerRow)

    // Body Text
    val bodyView = TextView(context).apply {
        textSize = 12f
        setTextColor(android.graphics.Color.parseColor("#A0A4B8"))
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
        setPadding(0, dpToPx(context, 6), 0, dpToPx(context, 8))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }
    rootLayout.addView(bodyView)

    // Call-To-Action Button
    val ctaButton = Button(context).apply {
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(android.graphics.Color.WHITE)
        setBackgroundColor(android.graphics.Color.parseColor("#FF9500"))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 38)
        )
    }
    rootLayout.addView(ctaButton)

    nativeAdView.addView(rootLayout)

    // Register views with NativeAdView
    nativeAdView.headlineView = headlineView
    nativeAdView.bodyView = bodyView
    nativeAdView.callToActionView = ctaButton
    nativeAdView.iconView = iconView

    populateNativeAdView(ad, nativeAdView)
    return nativeAdView
}

private fun populateNativeAdView(ad: NativeAd, adView: NativeAdView) {
    (adView.headlineView as? TextView)?.text = ad.headline

    if (ad.body != null) {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as? TextView)?.text = ad.body
    } else {
        adView.bodyView?.visibility = View.GONE
    }

    if (ad.callToAction != null) {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as? Button)?.text = ad.callToAction
    } else {
        adView.callToActionView?.visibility = View.GONE
    }

    if (ad.icon != null) {
        adView.iconView?.visibility = View.VISIBLE
        (adView.iconView as? ImageView)?.setImageDrawable(ad.icon?.drawable)
    } else {
        adView.iconView?.visibility = View.GONE
    }

    adView.setNativeAd(ad)
}

private fun dpToPx(context: Context, dp: Int): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}
