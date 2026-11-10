package com.edumind.ai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.edumind.ai.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "edumind_settings")

/**
 * App settings stored locally (DataStore): theme, onboarding, streak,
 * usage counters. No account or login is required — guest access.
 */
class SettingsRepository(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_mode")
    private val onboardingKey = booleanPreferencesKey("onboarding_seen")
    private val streakKey = intPreferencesKey("day_streak")
    private val lastActiveDayKey = longPreferencesKey("last_active_day")
    private val questionsAskedKey = intPreferencesKey("questions_asked")
    private val xpKey = intPreferencesKey("xp")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[themeKey]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val onboardingSeen: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingKey] == true
    }

    val dayStreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[streakKey] ?: 0
    }

    val questionsAsked: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[questionsAskedKey] ?: 0
    }

    val xp: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[xpKey] ?: 0
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }

    suspend fun setOnboardingSeen() {
        context.dataStore.edit { prefs ->
            prefs[onboardingKey] = true
        }
    }

    /** Record one answered question: increments XP and question count, updates streak. */
    suspend fun recordQuestionAnswered() {
        val today = System.currentTimeMillis() / 86_400_000L
        context.dataStore.edit { prefs ->
            prefs[questionsAskedKey] = (prefs[questionsAskedKey] ?: 0) + 1
            prefs[xpKey] = (prefs[xpKey] ?: 0) + 10
            val lastDay = prefs[lastActiveDayKey]
            val streak = prefs[streakKey] ?: 0
            prefs[streakKey] = when {
                lastDay == null -> 1
                today == lastDay -> streak
                today == lastDay + 1 -> streak + 1
                else -> 1
            }
            prefs[lastActiveDayKey] = today
        }
    }

    /** Add bonus XP (for example, the rewarded-ad benefit). */
    suspend fun addXp(amount: Int) {
        context.dataStore.edit { prefs ->
            prefs[xpKey] = (prefs[xpKey] ?: 0) + amount
        }
    }

    /** Delete all locally stored app data (used by the in-app data-deletion action). */
    suspend fun clearAll() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}
