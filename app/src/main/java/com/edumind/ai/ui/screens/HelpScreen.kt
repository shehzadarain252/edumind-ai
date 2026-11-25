package com.edumind.ai.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.ui.components.SectionTitle
import com.edumind.ai.ui.navigation.Routes

/** Help and Support: how each feature works, with links to FAQ and Contact. */
@Composable
fun HelpScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("Help and Support", "How EduMind AI works")
        Spacer(modifier = Modifier.height(16.dp))
        HelpItem(
            title = "Ask AI",
            body = "Type any academic question. Choose a subject and level for better answers. " +
                "Tap the image button to attach a photo, or the microphone to speak your question.",
        )
        HelpItem(
            title = "Camera Scanner",
            body = "Allow camera permission, point the camera at a textbook page or handwritten " +
                "question, and tap Capture. You can rotate or crop the photo, then tap " +
                "\"Solve This Question\".",
        )
        HelpItem(
            title = "Document Upload",
            body = "Select a PDF, Word, text, PowerPoint or image file (max 20 MB). Choose " +
                "Summarize, Questions or Practice Test, then tap Process Document.",
        )
        HelpItem(
            title = "AI Study Tools",
            body = "Open Study Tools, pick a tool (for example Quiz Generator or Essay Writer), " +
                "enter your input and tap Run.",
        )
        HelpItem(
            title = "Saved Answers and Favorites",
            body = "Tap Save on any AI answer to keep it. Star a saved answer to add it to Favorites.",
        )
        HelpItem(
            title = "Ads",
            body = "EduMind AI shows interstitial ads only at natural breaks (for example when " +
                "you finish a tool), and rewarded ads only when you choose to watch one for +50 XP. " +
                "Ads never interrupt scanning, AI answers or uploads.",
        )
        HelpItem(
            title = "AI not configured",
            body = "If you see \"AI is not configured\", the backend has no Gemini API key yet. " +
                "Add the key to the backend .env file and set the backend URL in the app, " +
                "then restart the app.",
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(Routes.FAQ) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Read the FAQ") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate(Routes.CONTACT) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Contact Us") }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HelpItem(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp),
    )
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
