package com.edumind.ai.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.ui.components.SectionTitle

/** Shared scrollable legal-page layout. */
@Composable
private fun LegalPage(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle(title, subtitle)
        Spacer(modifier = Modifier.height(16.dp))
        content()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun H(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun P(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun PrivacyScreen(navController: NavHostController) {
    LegalPage("Privacy Policy", "Last updated: November 2026") {
        P("EduMind AI is an AI-powered education app. This policy explains what data the app collects, how it is used, and your rights.")
        H("1. Data Collection")
        P("The app does not require an account. Your questions, saved answers, chat history, favorites and study planner tasks are stored only on your device. The app does not send your personal identity to us.")
        H("2. Camera Permissions")
        P("Camera access is used only when you open the Camera Scanner, to photograph a question. Photos are sent to the EduMind AI backend only when you tap \"Solve This Question\". Photos are not stored on the server after processing.")
        H("3. Gallery Permissions")
        P("Gallery access is used only when you choose an image to analyze. The selected image is sent to the backend for AI analysis and is not stored on the server after processing.")
        H("4. Document Processing")
        P("Documents you upload are sent to the EduMind AI backend so their text can be extracted and analyzed by the AI. Uploads are held in memory only and are not kept on the server. Files are limited to 20 MB.")
        H("5. AI API Processing")
        P("Your questions and document text are processed by Google Gemini through the EduMind AI backend. The backend does not log your API key or your personal data. Please do not include passwords or highly sensitive personal information in your questions.")
        H("6. Advertising and AdMob")
        P("The app uses Google AdMob to show interstitial and rewarded ads. AdMob may collect device information and advertising identifiers to serve ads, subject to Google's privacy policy and your consent choices (see \"Ad Choices\" in Profile). The app does not show banner ads.")
        H("7. Device Information")
        P("The app may access basic device information (such as Android version) only as needed to run correctly. It does not collect your precise location.")
        H("8. Data Retention")
        P("Your learning data stays on your device until you delete it. The backend does not retain your questions or documents after processing.")
        H("9. Data Deletion")
        P("You can delete all locally stored data at any time from Profile → \"Delete My Data\". This permanently removes saved answers, chat history, planner tasks and settings from this device.")
        H("10. Third-Party Services")
        P("The app uses Google Gemini (AI), Google AdMob (advertising) and Google UMP (consent). Each is governed by its own privacy policy.")
        H("11. Your Privacy Rights")
        P("Because the app needs no account, your data is yours and stays on your device. You can delete it at any time (see section 9). You can change ad consent choices from Profile → \"Ad Choices\".")
        H("12. Contact")
        P("Questions about this policy can be sent through the in-app Contact Us form.")
    }
}

@Composable
fun TermsScreen(navController: NavHostController) {
    LegalPage("Terms and Conditions", "Last updated: November 2026") {
        P("By using EduMind AI you agree to these terms.")
        H("1. Application Usage Rules")
        P("Use the app for learning. Do not use it to cheat on live examinations where that is prohibited, to harass others, or to break the law.")
        H("2. AI-Generated Content Limitations")
        P("AI answers are generated automatically and may contain errors. Always verify important academic work with your teacher, textbook or another reliable source. The app is a study aid, not a substitute for professional advice (including medical, legal or financial advice).")
        H("3. Acceptable Use")
        P("Do not upload illegal, harmful or infringing material. Do not try to bypass usage limits or abuse the AI service.")
        H("4. Intellectual Property")
        P("The EduMind AI name, logo and app design belong to the app owner. You may use AI-generated answers for your own study. Respect the copyright of textbooks and materials you scan or upload.")
        H("5. Service Availability")
        P("The app needs an internet connection and a configured backend to answer questions. The service may be unavailable at times (for example, if the AI service is busy). No uptime guarantee is given.")
        H("6. Limitation of Liability")
        P("The app is provided \"as is\". The owner is not liable for academic results, decisions made from AI answers, or any loss arising from use of the app, to the extent permitted by law.")
    }
}

@Composable
fun DisclaimerScreen(navController: NavHostController) {
    LegalPage("Disclaimer", "Please read carefully") {
        P("EduMind AI provides AI-generated educational answers that may contain errors, inaccuracies or outdated information. AI answers should be verified before being used for important academic work, examinations, assignments or professional decisions.")
        H("Education, Not Professional Advice")
        P("Content about medicine, health, law, finance or other professional topics is educational information only. It is not a substitute for advice from a qualified professional.")
        H("Your Responsibility")
        P("You are responsible for checking AI answers against your textbooks, teachers and other reliable sources, and for following your school or institution's rules on AI use.")
        H("No Guarantee")
        P("No guarantee is given that any answer is complete, correct or suitable for a particular purpose.")
    }
}

@Composable
fun AboutScreen(navController: NavHostController) {
    LegalPage("About Us", "EduMind AI — Learn Anything. Solve Everything.") {
        P("EduMind AI is an AI-powered educational learning platform. It helps students of all levels — school, college, university and beyond — to ask academic questions, scan textbook problems, upload documents and get intelligent, detailed explanations.")
        H("What the app does")
        P("• Answers questions across 16 subject categories.\n• Scans printed and handwritten questions with the camera.\n• Summarizes documents and generates practice tests.\n• Offers 20 AI study tools, from homework help to quiz generation.\n• Works as a guest — no account is required for basic AI features.")
        H("Our goal")
        P("To make quality learning help available to every student, anywhere, at any time.")
    }
}
