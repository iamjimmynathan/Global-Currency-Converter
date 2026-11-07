package com.example.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Centralized AdMob Manager
 * Integrates official user AdMob account with strict premium subscriber protection,
 * non-intrusive placement, frequency capping, and testing safety controls.
 */
object AdManager {
    private const val TAG = "AdManager"

    // User's Configured Google AdMob Account
    const val ADMOB_APP_ID = "ca-app-pub-5418146471778034~4026840410"
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-5418146471778034/3779428402"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5418146471778034/2409460233"
    const val PROD_NATIVE_AD_UNIT_ID = "ca-app-pub-5418146471778034/1101294170"

    // Official Google AdMob Test Ad Units (safe for development & policy compliance)
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // Test mode toggle (safe development & testing mode enabled by default to prevent accidental invalid traffic strikes)
    private val _isTestMode = MutableStateFlow(true)
    val isTestMode: StateFlow<Boolean> = _isTestMode.asStateFlow()

    private val _isAdsInitialized = MutableStateFlow(false)
    val isAdsInitialized: StateFlow<Boolean> = _isAdsInitialized.asStateFlow()

    private val _isInterstitialLoaded = MutableStateFlow(false)
    val isInterstitialLoaded: StateFlow<Boolean> = _isInterstitialLoaded.asStateFlow()

    private val _lastBannerError = MutableStateFlow<String?>(null)
    val lastBannerError: StateFlow<String?> = _lastBannerError.asStateFlow()

    private val _lastInterstitialError = MutableStateFlow<String?>(null)
    val lastInterstitialError: StateFlow<String?> = _lastInterstitialError.asStateFlow()

    private val _lastNativeError = MutableStateFlow<String?>(null)
    val lastNativeError: StateFlow<String?> = _lastNativeError.asStateFlow()

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTime = 0L
    private var actionCounter = 0

    // Frequency Limits: Minimum 60s cooldown and at least 4 actions between interstitial ads
    private const val MIN_INTERSTITIAL_INTERVAL_MS = 60_000L
    private const val ACTIONS_PER_INTERSTITIAL = 4

    fun setTestMode(enabled: Boolean) {
        if (_isTestMode.value != enabled) {
            _isTestMode.value = enabled
            interstitialAd = null
            _isInterstitialLoaded.value = false
            Log.d(TAG, "AdMob Test Mode changed to: $enabled (Live IDs active: ${!enabled})")
        }
    }

    fun getBannerAdUnitId(): String =
        if (_isTestMode.value) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID

    fun getInterstitialAdUnitId(): String =
        if (_isTestMode.value) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    fun getNativeAdUnitId(): String =
        if (_isTestMode.value) TEST_NATIVE_AD_UNIT_ID else PROD_NATIVE_AD_UNIT_ID

    fun initialize(context: Context) {
        if (_isAdsInitialized.value) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Configure test device list to include emulator for policy safety
                val configuration = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                MobileAds.setRequestConfiguration(configuration)

                MobileAds.initialize(context) { status ->
                    _isAdsInitialized.value = true
                    Log.d(TAG, "Google Mobile Ads SDK initialized successfully. Status: ${status.adapterStatusMap}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Google Mobile Ads SDK: ${e.message}")
            }
        }
    }

    fun recordBannerLoaded() {
        _lastBannerError.value = null
        Log.d(TAG, "Banner ad loaded successfully with unit: ${getBannerAdUnitId()}")
    }

    fun recordBannerFailed(error: LoadAdError) {
        val msg = "${error.message} (Code ${error.code})"
        _lastBannerError.value = msg
        Log.w(TAG, "Banner ad failed to load: $msg")
    }

    fun recordNativeLoaded() {
        _lastNativeError.value = null
        Log.d(TAG, "Native ad loaded successfully with unit: ${getNativeAdUnitId()}")
    }

    fun recordNativeFailed(error: LoadAdError) {
        val msg = "${error.message} (Code ${error.code})"
        _lastNativeError.value = msg
        Log.w(TAG, "Native ad failed to load: $msg")
    }

    fun preloadInterstitial(context: Context, isPremium: Boolean) {
        if (isPremium || isInterstitialLoading || interstitialAd != null) return

        isInterstitialLoading = true
        val unitId = getInterstitialAdUnitId()
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            unitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    _isInterstitialLoaded.value = true
                    _lastInterstitialError.value = null
                    Log.d(TAG, "Interstitial ad loaded successfully ($unitId)")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    _isInterstitialLoaded.value = false
                    val msg = "${loadAdError.message} (Code ${loadAdError.code})"
                    _lastInterstitialError.value = msg
                    Log.w(TAG, "Interstitial ad failed to load: $msg")
                }
            }
        )
    }

    /**
     * Increments action counter and shows interstitial if frequency limits are met.
     */
    fun recordActionAndShowInterstitial(
        activity: Activity,
        isPremium: Boolean,
        onDismissed: () -> Unit = {}
    ) {
        if (isPremium) {
            onDismissed()
            return
        }

        actionCounter++
        val now = System.currentTimeMillis()
        val timeSinceLast = now - lastInterstitialShownTime

        if (actionCounter >= ACTIONS_PER_INTERSTITIAL && timeSinceLast >= MIN_INTERSTITIAL_INTERVAL_MS) {
            actionCounter = 0
            showInterstitialIfAllowed(activity, isPremium, onDismissed)
        } else {
            // Preload next if not ready
            if (interstitialAd == null && !isInterstitialLoading) {
                preloadInterstitial(activity, isPremium)
            }
            onDismissed()
        }
    }

    fun showInterstitialIfAllowed(
        activity: Activity,
        isPremium: Boolean,
        onDismissed: () -> Unit = {}
    ) {
        if (isPremium) {
            onDismissed()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastInterstitialShownTime < MIN_INTERSTITIAL_INTERVAL_MS) {
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    _isInterstitialLoaded.value = false
                    lastInterstitialShownTime = System.currentTimeMillis()
                    preloadInterstitial(activity, isPremium)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    _isInterstitialLoaded.value = false
                    Log.w(TAG, "Interstitial failed to show: ${adError.message}")
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            preloadInterstitial(activity, isPremium)
            onDismissed()
        }
    }
}
