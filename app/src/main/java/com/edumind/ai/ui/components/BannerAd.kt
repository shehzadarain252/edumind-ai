package com.edumind.ai.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.edumind.ai.AppContainer
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * LIVE banner ad shown at the bottom of the tab screens, above the bottom
 * navigation bar. Uses the owner's LIVE banner ad unit — never a test ad.
 * If the banner fails to load (e.g. no fill, or the unit belongs to a
 * different AdMob app entry than the manifest App ID), the space simply
 * stays empty and the app keeps working.
 */
@Composable
fun BannerAd(container: AppContainer, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val adUnitId = container.adMobManager.bannerAdUnitId

    // Anchored adaptive banner sized to the current screen width.
    val adSize = remember(configuration.screenWidthDp) {
        val width = configuration.screenWidthDp
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)
    }

    val adView = remember(adUnitId, adSize) {
        AdView(context).apply {
            setAdSize(adSize)
            adUnitId = adUnitId
            adListener = object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // Banner stays hidden; retry on next composition.
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        adView.resume()
        onDispose {
            adView.pause()
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier.fillMaxWidth(),
    )
}
