package don.t.connect.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import ir.myket.billingclient.IabHelper

import don.t.connect.BuildConfig

object MyketPurchaseManager {

    private const val TAG = "MyketPurchaseManager"
    private const val SKU_REMOVE_ADS = "remove_ads"

    private var mHelper: IabHelper? = null
    private var purchaseCallback: ((Boolean) -> Unit)? = null

    fun initialize(context: Context) {
        if (mHelper != null) return
        mHelper = IabHelper(context, BuildConfig.IAB_PUBLIC_KEY)
        // فعال‌سازی لاگ (اختیاری) - در صورت وجود متد
        try {
            mHelper?.enableDebugLogging(true)
        } catch (e: Exception) {
            // نادیده گرفته شود
        }
        startSetup()
    }

    private fun startSetup() {
        mHelper?.startSetup { result ->
            if (!result.isSuccess) {
                Log.e(TAG, "Problem setting up IAB: ${result.message}")
                return@startSetup
            }
            if (mHelper == null) return@startSetup
            Log.d(TAG, "IAB setup successful. Querying inventory.")
            try {
                // ✅ استفاده از نسخه صحیح queryInventoryAsync (3 پارامتر)
                mHelper?.queryInventoryAsync(true, listOf(SKU_REMOVE_ADS), inventoryListener)
            } catch (e: Exception) {
                Log.e(TAG, "Error querying inventory: ${e.message}")
            }
        }
    }

    private val inventoryListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        if (mHelper == null) return@QueryInventoryFinishedListener
        if (result.isFailure) {
            Log.e(TAG, "Failed to query inventory: ${result.message}")
            return@QueryInventoryFinishedListener
        }
        val purchase = inventory?.getPurchase(SKU_REMOVE_ADS)
        if (purchase != null) {
            Log.d(TAG, "User already purchased remove-ads.")
        } else {
            Log.d(TAG, "User has not purchased remove-ads yet.")
        }
    }

    fun launchPurchaseFlow(activity: Activity, onComplete: (Boolean) -> Unit) {
        if (mHelper == null) {
            onComplete(false)
            return
        }
        purchaseCallback = onComplete
        try {
            mHelper?.launchPurchaseFlow(activity, SKU_REMOVE_ADS, purchaseListener, "")
        } catch (e: Exception) {
            Log.e(TAG, "Error launching purchase flow: ${e.message}")
            onComplete(false)
        }
    }

    private val purchaseListener = IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
        if (mHelper == null) return@OnIabPurchaseFinishedListener
        if (result.isFailure) {
            Log.e(TAG, "Error purchasing: ${result.message}")
            purchaseCallback?.invoke(false)
            purchaseCallback = null
            return@OnIabPurchaseFinishedListener
        }
        if (purchase?.sku == SKU_REMOVE_ADS) {
            Log.d(TAG, "Purchase successful: ${purchase.sku}")
            purchaseCallback?.invoke(true)
        } else {
            purchaseCallback?.invoke(false)
        }
        purchaseCallback = null
    }

    fun dispose() {
        purchaseCallback = null
        try {
            // ✅ استفاده از dispose() به جای disposeWhenFinished()
            mHelper?.dispose()
            mHelper = null
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing IabHelper: ${e.message}")
        }
    }
}