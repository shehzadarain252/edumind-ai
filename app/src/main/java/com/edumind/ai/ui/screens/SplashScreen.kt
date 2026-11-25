package com.edumind.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edumind.ai.AppContainer
import com.edumind.ai.ui.components.RobotMascot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(container: AppContainer, onFinished: (Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        val seen = container.settingsRepository.onboardingSeen.first()
        onFinished(seen)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF7C3AED), Color(0xFF2563EB)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RobotMascot(size = 160.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "EduMind AI",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Learn Anything. Solve Everything.",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFEDE9FE),
            )
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}
