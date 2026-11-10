package com.edumind.ai.ads

import android.app.Activity
import android.content.Context
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
 *
 * The PRIMARY AdMob App ID (ca-app-pub-4510714397469784~5758175741) is
 * registered in AndroidManifest.xml. It is the ONLY App ID registered.
 *
 * Additional App ID provided by the owner (REFERENCE ONLY — do NOT register
 * it in the manifest or anywhere else in the app):
 *   ca-app-pub-4510714397469784~6495647157
 * Confirm the correct App ID in the AdMob dashboard before release.
 *
 * Rules enforced here:
 * - No banner ads anywhere in the app.
 * - Interstitials load before they are needed and show ONLY at natural
 *   transitions (e.g. after finishing a tool result, returning home).
 *   The UI layer must NEVER call showInterstitial() during question
 *   scanning, AI answer generation, or document uploads.
 * - Frequency limit: at most one interstitial every 3 minutes.
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

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var lastInterstitialShownAt: Long = 0L
    private var interstitialLoading = false
    private var rewardedLoading = false

    /** Minimum time between two interstitial ads (3 minutes). */
    private val interstitialCooldownMs = 3 * 60 * 1000L

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
