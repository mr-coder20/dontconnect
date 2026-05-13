package don.t.connect.utils

import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.adivery.sdk.AdiveryAdListener
import com.adivery.sdk.AdiveryBannerAdView
import com.adivery.sdk.BannerSize

@Composable
fun AdiveryBannerAd(
    placementId: String,
    bannerSize: BannerSize = BannerSize.BANNER,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            AdiveryBannerAdView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPlacementId(placementId)
                setBannerSize(bannerSize)
                setBannerAdListener(object : AdiveryAdListener() {
                    override fun onAdLoaded() {
                        Log.d("AdiveryBanner", "Banner loaded successfully for $placementId")
                    }
                    override fun onError(reason: String) {
                        Log.e("AdiveryBanner", "Banner error for $placementId: $reason")
                    }
                    override fun onAdClicked() {
                        Log.d("AdiveryBanner", "Banner clicked")
                    }
                })
                loadAd()
            }
        },
        modifier = modifier
    )
}