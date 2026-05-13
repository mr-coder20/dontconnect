package don.t.connect.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryListener

object AdiveryAdManager {
    private const val TAG = "AdiveryAdManager"

    private const val APP_ID = "074d4d1f-b651-4055-957a-b203e8b17ffb"
    private const val BANNER_PLACEMENT_ID = "c77a029d-a8e0-4a0e-90be-e9af2515cd28"
    private const val INTERSTITIAL_PLACEMENT_ID = "cc7843ef-e34b-496a-82ab-a1c7b61f7b0a"
    private const val REWARDED_PLACEMENT_ID = "c6bdc828-23a0-4c4b-a156-287c5a6d0e97"

    fun getBannerPlacementId(): String = BANNER_PLACEMENT_ID

    fun initialize(application: Application) {
        Adivery.configure(application, APP_ID)
        try {
            // اگر متد setTestMode وجود داشت، فعال کن
            Adivery::class.java.getMethod("setTestMode", Boolean::class.java).invoke(null, true)
        } catch (e: NoSuchMethodException) {
            // نادیده گرفته شود (نسخه قدیمی SDK)
        }
        Adivery.setLoggingEnabled(true)
        setupGlobalListener()
        Log.d(TAG, "Adivery initialized with APP_ID: $APP_ID")
    }

    private fun setupGlobalListener() {
        Adivery.addGlobalListener(object : AdiveryListener() {
            override fun onInterstitialAdLoaded(placementId: String) {
                Log.d(TAG, "Interstitial loaded: $placementId")
            }

            override fun onInterstitialAdShown(placementId: String) {
                Log.d(TAG, "Interstitial shown")
            }

            override fun onInterstitialAdClosed(placementId: String) {
                Log.d(TAG, "Interstitial closed")
            }

            override fun onRewardedAdLoaded(placementId: String) {
                Log.d(TAG, "✅ Rewarded ad loaded: $placementId")
            }

            override fun onRewardedAdShown(placementId: String) {
                Log.d(TAG, "🎥 Rewarded ad shown")
            }

            override fun onRewardedAdClosed(placementId: String, isRewarded: Boolean) {
                Log.d(TAG, "Rewarded ad closed, rewarded: $isRewarded")
            }

            fun onError(placementId: String, error: String) {
                Log.e(TAG, "❌ Error for placement $placementId: $error")
            }

            override fun log(placementId: String, message: String) {
                Log.d(TAG, "Log: $placementId -> $message")
            }
        })
    }

    fun prepareInterstitial(context: Context) {
        Adivery.prepareInterstitialAd(context, INTERSTITIAL_PLACEMENT_ID)
        Log.d(TAG, "Interstitial prepared for $INTERSTITIAL_PLACEMENT_ID")
    }

    fun prepareRewarded(context: Context) {
        Adivery.prepareRewardedAd(context, REWARDED_PLACEMENT_ID)
        Log.d(TAG, "Rewarded ad prepared for $REWARDED_PLACEMENT_ID")
    }

    // نمایش اینترستیشیال با callback پس از بسته شدن
    fun showInterstitialWithCallback(activity: Activity, onClosed: () -> Unit) {
        if (Adivery.isLoaded(INTERSTITIAL_PLACEMENT_ID)) {
            var callbackInvoked = false
            val listener = object : AdiveryListener() {
                override fun onInterstitialAdClosed(placementId: String) {
                    if (placementId == INTERSTITIAL_PLACEMENT_ID && !callbackInvoked) {
                        callbackInvoked = true
                        Adivery.removeGlobalListener(this)
                        onClosed()
                    }
                }
            }
            Adivery.addGlobalListener(listener)
            Adivery.showAd(INTERSTITIAL_PLACEMENT_ID)
            Log.d(TAG, "Showing interstitial ad")
        } else {
            Log.w(TAG, "Interstitial ad not ready, skipping")
            onClosed()
        }
    }

    // نمایش ریوارد یا در صورت عدم آمادگی، اینترستیشیال و سپس عملیات اصلی
    fun showRewardedOrInterstitialWithCallback(activity: Activity, onFinished: () -> Unit) {
        // اولویت با ریوارد است
        if (Adivery.isLoaded(REWARDED_PLACEMENT_ID)) {
            Log.d(TAG, "Rewarded ad is ready, showing rewarded...")
            var callbackInvoked = false
            val listener = object : AdiveryListener() {
                override fun onRewardedAdClosed(placementId: String, isRewarded: Boolean) {
                    if (placementId == REWARDED_PLACEMENT_ID && !callbackInvoked) {
                        callbackInvoked = true
                        Adivery.removeGlobalListener(this)
                        Log.d(TAG, "Rewarded ad finished, rewarded=$isRewarded")
                        onFinished()
                    }
                }
            }
            Adivery.addGlobalListener(listener)
            Adivery.showAd(REWARDED_PLACEMENT_ID)
        } else {
            // ریوارد آماده نیست، سعی می‌کنیم اینترستیشیال نمایش دهیم
            Log.w(TAG, "Rewarded ad not ready, trying interstitial instead")
            showInterstitialWithCallback(activity, onFinished)
        }
    }

    fun showInterstitialIfReady(activity: Activity) {
        if (Adivery.isLoaded(INTERSTITIAL_PLACEMENT_ID)) {
            Adivery.showAd(INTERSTITIAL_PLACEMENT_ID)
        } else {
            prepareInterstitial(activity)
        }
    }
}