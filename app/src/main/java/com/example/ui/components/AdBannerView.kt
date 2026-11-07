package com.example.ui.components

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.monetization.AdManager
import com.example.ui.theme.CharcoalDeepBlack
import com.example.ui.theme.TextLightGreySecondary
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdBannerView(
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) {
        // Zero advertisements for premium subscribers
        return
    }

    val isTestMode by AdManager.isTestMode.collectAsState()
    val lastError by AdManager.lastBannerError.collectAsState()
    var isAdLoaded by remember(isTestMode) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(CharcoalDeepBlack),
        contentAlignment = Alignment.Center
    ) {
        if (!isAdLoaded) {
            val statusText = if (isTestMode) {
                "AdMob Test Banner Slot • ${if (lastError != null) "Retrying..." else "Active"}"
            } else {
                "Advertisement Slot • Free Tier"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = TextLightGreySecondary.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdManager.getBannerAdUnitId()
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isAdLoaded = true
                            AdManager.recordBannerLoaded()
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            isAdLoaded = false
                            AdManager.recordBannerFailed(loadAdError)
                        }
                    }
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    try {
                        loadAd(AdRequest.Builder().build())
                    } catch (e: Exception) {
                        Log.e("AdBannerView", "Error loading banner ad: ${e.message}")
                    }
                }
            },
            update = { adView ->
                val currentUnitId = AdManager.getBannerAdUnitId()
                if (adView.adUnitId != currentUnitId) {
                    adView.adUnitId = currentUnitId
                    try {
                        adView.loadAd(AdRequest.Builder().build())
                    } catch (e: Exception) {
                        Log.e("AdBannerView", "Error updating banner ad: ${e.message}")
                    }
                }
            }
        )
    }
}
