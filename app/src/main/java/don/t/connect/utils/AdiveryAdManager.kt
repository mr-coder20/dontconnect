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
            Adivery::class.java.getMethod("setTestMode", Boolean::class.java).invoke(null, true)
        } catch (e: NoSuchMethodException) { /* ignore */ }
        Adivery.setLoggingEnabled(true)
        setupGlobalListener()
        Log.d(TAG, "Adivery initialized with APP_ID: $APP_ID")
    }

    private fun setupGlobalListener() {
        Adivery.addGlobalListener(object : AdiveryListener() {
            override fun onInterstitialAdLoaded(placementId: String) { Log.d(TAG, "Interstitial loaded: $placementId") }
            override fun onInterstitialAdShown(placementId: String) { Log.d(TAG, "Interstitial shown") }
            override fun onInterstitialAdClosed(placementId: String) { Log.d(TAG, "Interstitial closed") }
            override fun onRewardedAdLoaded(placementId: String) { Log.d(TAG, "✅ Rewarded ad loaded: $placementId") }
            override fun onRewardedAdShown(placementId: String) { Log.d(TAG, "🎥 Rewarded ad shown") }
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

    fun showRewardedWithFullWatchRequirement(activity: Activity, onRewarded: () -> Unit) {
        try {
            if (Adivery.isLoaded(REWARDED_PLACEMENT_ID)) {
                Log.d(TAG, "Rewarded ad is ready, showing...")
                var callbackInvoked = false
                val listener = object : AdiveryListener() {
                    override fun onRewardedAdClosed(placementId: String, isRewarded: Boolean) {
                        Log.d(TAG, "onRewardedAdClosed called: placementId=$placementId, isRewarded=$isRewarded, callbackInvoked=$callbackInvoked")
                        if (placementId == REWARDED_PLACEMENT_ID && !callbackInvoked) {
                            callbackInvoked = true
                            Adivery.removeGlobalListener(this)
                            if (isRewarded) {
                                Log.d(TAG, "✅ User watched full ad. Granting reward.")
                                onRewarded()
                            } else {
                                Log.w(TAG, "❌ User skipped ad. No reward. Connection will NOT proceed.")
                            }
                        }
                    }
                }
                Adivery.addGlobalListener(listener)
                Adivery.showAd(REWARDED_PLACEMENT_ID)
            } else {
                Log.w(TAG, "Rewarded ad not ready. Granting reward without ad.")
                onRewarded()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing rewarded ad: ${e.message}")
            onRewarded()
        }
    }

    fun showInterstitialOrRewardedWithFullWatch(activity: Activity, onFinished: () -> Unit) {
        if (Adivery.isLoaded(INTERSTITIAL_PLACEMENT_ID)) {
            Log.d(TAG, "Interstitial ad is ready, showing interstitial...")
            showInterstitialWithCallback(activity, onFinished)
            return
        }
        if (Adivery.isLoaded(REWARDED_PLACEMENT_ID)) {
            Log.d(TAG, "Interstitial not ready, showing rewarded ad with full watch requirement...")
            showRewardedWithFullWatchRequirement(activity, onFinished)
            return
        }
        Log.w(TAG, "No ad ready. Proceeding without ad.")
        onFinished()
    }
}