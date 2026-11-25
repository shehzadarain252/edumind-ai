package com.edumind.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.ai.AppContainer
import com.edumind.ai.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        container.settingsRepository.themeMode
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val dayStreak: StateFlow<Int> =
        container.settingsRepository.dayStreak
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val questionsAsked: StateFlow<Int> =
        container.settingsRepository.questionsAsked
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val xp: StateFlow<Int> =
        container.settingsRepository.xp
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { container.settingsRepository.setThemeMode(mode) }
    }

    /**
     * Working data-deletion: clears local settings and all locally stored
     * answers, chat history and planner tasks. This is the process the
     * Privacy Policy refers to — it really deletes the data.
     */
    fun deleteAllMyData(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            container.settingsRepository.clearAll()
            // Delete all locally stored answers, chat history and planner tasks.
            container.database.clearAllTables()
            onDone()
        }
    }
}
