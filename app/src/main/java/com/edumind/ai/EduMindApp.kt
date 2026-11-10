package com.edumind.ai

import android.app.Application
import com.edumind.ai.ads.AdMobManager
import com.edumind.ai.ads.ConsentManager
import com.edumind.ai.data.SettingsRepository
import com.edumind.ai.data.local.EduMindDatabase
import com.edumind.ai.data.remote.ApiService
import com.google.android.gms.ads.MobileAds

/**
 * Application class. Owns the [AppContainer] (Retrofit, Room, repositories,
 * AdMob) and initializes Google Mobile Ads exactly once.
 *
 * No API keys or credentials live here — the Gemini key belongs to the
 * backend only (see backend/.env.example).
 */
class EduMindApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Initialize Google Mobile Ads on a background thread.
        // (UMP consent is requested from MainActivity, which is an Activity.)
        Thread {
            MobileAds.initialize(this) {}
        }.start()
    }
}

/** Simple manual dependency container (no DI framework, keeps the build light). */
class AppContainer(app: Application) {
    val settingsRepository = SettingsRepository(app.applicationContext)
    /** Question typed on Home and handed to the Ask AI screen (cleared on use). */
    var pendingQuestion: String? = null
    /** Subject chosen on a Subject Detail screen (cleared on use). */
    var pendingSubject: String? = null
    val database: EduMindDatabase = EduMindDatabase.getInstance(app.applicationContext)
    val api: ApiService = ApiService.create(BuildConfig.BACKEND_URL)
    val adMobManager = AdMobManager(app.applicationContext)

    val savedAnswersDao get() = database.savedAnswersDao()
    val chatDao get() = database.chatDao()
    val plannerDao get() = database.plannerDao()
    val messagesDao get() = database.messagesDao()
}
