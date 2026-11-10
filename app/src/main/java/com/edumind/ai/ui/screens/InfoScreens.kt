package com.edumind.ai.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.AppContainer
import com.edumind.ai.data.local.MessageEntity
import com.edumind.ai.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Contact Us: validated form, stored in the on-device outbox. The app ships
 * without a support email address (no fake contact details), so messages are
 * stored locally; the user can also send the message through their own
 * email app (they choose the recipient — nothing is prefilled).
 */
@Composable
fun ContactScreen(container: AppContainer, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    fun isEmailValid(value: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("Contact Us", "Send us a message")
        Spacer(modifier = Modifier.height(16.dp))
        if (success) {
            Text(
                text = "Thank you! Your message has been saved on this device. " +
                    "It will be sent once a support channel is configured.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    val send = Intent(Intent.ACTION_SENDTO).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "EduMind AI — $subject")
                        putExtra(Intent.EXTRA_TEXT, "From: $name <$email>\n\n$message")
                    }
                    context.startActivity(Intent.createChooser(send, "Send via your email app"))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Send via my email app instead") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
            return
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Your email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Your message") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            minLines = 5,
        )
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                when {
                    name.isBlank() -> error = "Please enter your name."
                    !isEmailValid(email) -> error = "Please enter a valid email address."
                    subject.isBlank() -> error = "Please enter a subject."
                    message.trim().length < 10 -> error = "Please write a message of at least a few words."
                    else -> {
                        error = null
                        scope.launch {
                            container.messagesDao.insert(
                                MessageEntity(
                                    id = UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    email = email.trim(),
                                    subject = subject.trim(),
                                    message = message.trim(),
                                    type = "contact",
                                    timestamp = System.currentTimeMillis(),
                                ),
                            )
                            success = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Send Message") }
    }
}

private val faqs = listOf(
    "How do I ask a question?" to
        "Open Ask AI, type your question, choose a subject and level if you like, then tap Send. You can also attach a photo or speak your question.",
    "How does the camera scanner work?" to
        "Open Camera Scanner, allow camera permission, point the camera at a textbook page or handwritten question, tap Capture, then tap \"Solve This Question\". You can rotate or crop the photo first.",
    "Which document types can I upload?" to
        "PDF, DOC, DOCX, TXT, PPT, PPTX and images (JPG, PNG, WEBP), up to 20 MB each. Choose Summarize, Questions or Practice Test after selecting a file.",
    "Do I need an account?" to
        "No. All basic AI features work as a guest. Your data stays on your device.",
    "Why does it say \"AI is not configured\"?" to
        "The app needs a backend with a Gemini API key. Add the key to the backend .env file and set the backend URL in the app, then restart the app.",
    "Are AI answers always correct?" to
        "No. AI answers can contain errors. Always verify important work with your teacher or textbook. See the Disclaimer for details.",
    "How do ads work?" to
        "Interstitial ads appear only at natural breaks (for example when you finish a tool). Rewarded ads are optional — watch one if you want +50 XP. Ads never interrupt scanning, answers or uploads.",
    "How do I delete my data?" to
        "Go to Profile → \"Delete My Data\". This permanently removes saved answers, chat history, planner tasks and settings from this device.",
    "How do I change the theme?" to
        "Go to Profile and choose System, Light or Dark under Appearance.",
)

@Composable
fun FaqScreen(navController: NavHostController) {
    var expanded by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("FAQ", "Frequently asked questions")
        Spacer(modifier = Modifier.height(16.dp))
        faqs.forEach { (question, answer) ->
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = if (expanded == question) null else question }
                    .padding(vertical = 8.dp),
            )
            if (expanded == question) {
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GuidelinesScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("AI Usage Guidelines", "How to get the best — and safest — results from EduMind AI")
        Spacer(modifier = Modifier.height(16.dp))
        GuidelineItem(
            "Use AI to learn, not to copy blindly",
            "Read the explanation and the steps. Understanding the method matters more than the final answer.",
        )
        GuidelineItem(
            "Verify important work",
            "AI answers can be wrong. Check important homework, exam answers and assignments against your textbook or teacher.",
        )
        GuidelineItem(
            "Follow your school's rules",
            "Some schools and exams restrict AI use. Follow your institution's policy — the app is a study aid, not a way around the rules.",
        )
        GuidelineItem(
            "Ask clearly",
            "Include the full question, the subject and your level. Clear questions get better answers. For maths, include the whole equation.",
        )
        GuidelineItem(
            "Do not share sensitive information",
            "Do not include passwords, ID numbers, addresses or other private details in questions, photos or documents.",
        )
        GuidelineItem(
            "Respect copyright",
            "Only scan or upload material you are allowed to use for study.",
        )
        GuidelineItem(
            "Ads are optional for learning",
            "You never have to watch an ad to get an answer. Rewarded ads are voluntary and only give bonus XP.",
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GuidelineItem(title: String, body: String) {
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
