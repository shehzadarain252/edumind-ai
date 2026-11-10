package com.edumind.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.edumind.ai.ads.ConsentManager
import com.edumind.ai.ui.navigation.EduMindNavHost
import com.edumind.ai.ui.theme.EduMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request UMP consent info (non-blocking; failures are ignored).
        ConsentManager.requestConsent(this)
        val container = (application as EduMindApp).container
        setContent {
            val themeMode by container.settingsRepository.themeMode
                .collectAsState(initial = com.edumind.ai.ui.theme.ThemeMode.SYSTEM)
            EduMindTheme(themeMode = themeMode) {
                EduMindNavHost(container = container)
            }
        }
    }
}
