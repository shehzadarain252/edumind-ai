package com.edumind.ai.ads

import android.app.Activity
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Google UMP (User Messaging Platform) consent. Requests consent info on
 * first launch (from MainActivity) and shows the consent form when required
 * (e.g. EEA/UK). Failures are non-fatal — the app continues without blocking.
 */
object ConsentManager {

    private var consentForm: ConsentForm? = null

    fun requestConsent(activity: Activity) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    loadForm(activity)
                }
            },
            { /* Consent info update failed — continue without blocking. */ },
        )
    }

    private fun loadForm(activity: Activity) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { form -> consentForm = form },
            { /* Form load failed — continue without blocking. */ },
        )
    }

    /** Show the consent form if it is available and required. Safe to call anytime. */
    fun showFormIfRequired(activity: Activity) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        if (info.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
            consentForm?.show(activity) { /* Form dismissed. */ } ?: loadForm(activity)
        }
    }
}
