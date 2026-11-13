package com.edumind.ai.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Google Mobile Ads for EduMind AI.
 *
 * Ad unit IDs (supplied by the app owner):
 * - Interstitial: ca-app-pub-4510714397469784/7398560475
 * - Rewarded:     ca-app-pub-4510714397469784/2417696533
 * - Banner:       ca-app-pub-4510714397469784/7211153363
 *
 * The PRIMARY AdMob App ID (ca-app-pub-4510714397469784~5758175741) is
 * registered in AndroidManifest.xml. It is the ONLY App ID registered —
 * the interstitial and rewarded units were verified LIVE under it.
 *
 * Additional App IDs provided by the owner (REFERENCE ONLY — do NOT register
 * them in the manifest or anywhere else in the app):
 *   ca-app-pub-4510714397469784~6495647157
 *   ca-app-pub-4510714397469784~3913557713 (shown on the banner ad unit's page)
 * Confirm the correct App ID in the AdMob dashboard before release.
 * NOTE: if the banner does not load, it is probably because the banner unit
 * was created under a different AdMob app entry (~3913557713) than the
 * manifest App ID (~5758175741). In that case create the banner unit again
 * under the same EduMind AI app entry as the interstitial/rewarded units.
 *
 * Rules enforced here:
 * - Banner ads show at the bottom of the tab screens (Home, Ask AI,
 *   Subjects, Tools, Profile), above the bottom navigation bar.
 * - Interstitials: when the user taps any option (any navigation), an
 *   interstitial is scheduled 5 seconds later, and shows only if an ad is
 *   loaded and the frequency cooldown has passed. The UI layer must NEVER
 *   show an interstitial during question scanning, AI answer generation,
 *   or document uploads.
 * - Frequency limit: at most one interstitial every 60 seconds.
 * - Rewarded ads are voluntary only and are never required to access
 *   essential educational answers.
 * - LIVE ad units are used in ALL builds (including debug) — the owner
 *   explicitly requires live AdMob ads, never Google's test ads.
 */
class AdMobManager(private val context: Context) {

    // LIVE ad unit IDs (supplied by the app owner) — used in every build.
    // Test ad units are NOT used anywhere in this app.
    private val interstitialAdUnitId: String =
        "ca-app-pub-4510714397469784/7398560475"

    private val rewardedAdUnitId: String =
        "ca-app-pub-4510714397469784/2417696533"

    /** LIVE banner ad unit ID (supplied by the app owner). */
    val bannerAdUnitId: String =
        "ca-app-pub-4510714397469784/7211153363"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var lastInterstitialShownAt: Long = 0L
    private var interstitialLoading = false
    private var rewardedLoading = false

    /** Minimum time between two interstitial ads (60 seconds). */
    private val interstitialCooldownMs = 60 * 1000L

    /** Delay after an option tap before the interstitial shows (5 seconds). */
    private val interstitialDelayMs = 5 * 1000L

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingInterstitialRunnable: Runnable? = null

    fun preloadAds() {
        loadInterstitial()
        loadRewarded()
    }

    // ---------- Interstitial ----------

    fun loadInterstitial() {
        if (interstitialLoading || interstitialAd != null) return
        interstitialLoading = true
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    interstitialLoading = false
                }
            },
        )
    }

    /** True only if an ad is ready AND the frequency cooldown has passed. */
    fun canShowInterstitial(): Boolean {
        val cooledDown = System.currentTimeMillis() - lastInterstitialShownAt >= interstitialCooldownMs
        return interstitialAd != null && cooledDown
    }

    /**
     * Called when the user taps any option (any navigation). Schedules the
     * interstitial to show 5 seconds after the tap, if an ad is loaded and
     * the cooldown has passed. A newer tap cancels the pending one and
     * restarts the 5-second wait. If the activity is gone by then, or no ad
     * is loaded, nothing shows. Call only from a safe point (NOT during
     * scanning, AI generation, or uploads).
     */
    fun scheduleInterstitialAfterDelay(activity: Activity) {
        pendingInterstitialRunnable?.let { mainHandler.removeCallbacks(it) }
        val runnable = Runnable {
            pendingInterstitialRunnable = null
            if (activity.isFinishing || activity.isDestroyed) return@Runnable
            if (canShowInterstitial()) {
                showInterstitial(activity)
            } else {
                // No ad ready yet — make sure one is loading for next time.
                loadInterstitial()
            }
        }
        pendingInterstitialRunnable = runnable
        mainHandler.postDelayed(runnable, interstitialDelayMs)
    }

    /** Cancel a pending delayed interstitial (e.g. when leaving the app). */
    fun cancelPendingInterstitial() {
        pendingInterstitialRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingInterstitialRunnable = null
    }

    /**
     * Show an interstitial at a natural transition. Call only from a safe
     * point (NOT during scanning, AI generation, or uploads). The [onClosed]
     * callback runs when the ad is dismissed or fails to show.
     */
    fun showInterstitial(activity: Activity, onClosed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            onClosed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastInterstitialShownAt = System.currentTimeMillis()
                loadInterstitial()
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitial()
                onClosed()
            }
        }
        ad.show(activity)
    }

    // ---------- Rewarded ----------

    fun loadRewarded() {
        if (rewardedLoading || rewardedAd != null) return
        rewardedLoading = true
        RewardedAd.load(
            context,
            rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    rewardedLoading = false
                }
            },
        )
    }

    fun isRewardedReady(): Boolean = rewardedAd != null

    /**
     * Show a rewarded ad the user chose to watch. [onRewardEarned] runs only
     * when the reward callback is verified by the SDK. [onClosed] runs when
     * the ad closes with or without a reward.
     */
    fun showRewarded(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onClosed: () -> Unit = {},
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onClosed()
            return
        }
        var rewarded = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded()
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewarded()
                onClosed()
            }
        }
        ad.show(activity) { rewardItem: RewardItem ->
            rewarded = true
            onRewardEarned(rewardItem)
        }
        @Suppress("UNUSED_VARIABLE")
        val ignored = rewarded
    }
}
