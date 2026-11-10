package com.edumind.ai.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.AppContainer
import com.edumind.ai.data.ChatMessage
import com.edumind.ai.data.LearningLevels
import com.edumind.ai.data.SubjectCategories
import com.edumind.ai.ui.components.AnswerView
import com.edumind.ai.ui.components.LoadingView
import com.edumind.ai.ui.components.NotConfiguredCard
import com.edumind.ai.ui.components.RobotMascot
import com.edumind.ai.ui.viewmodel.AskAiViewModel
import java.util.Locale

private val suggestions = listOf(
    "Explain photosynthesis simply",
    "Solve 2x² + 5x − 3 = 0",
    "What is the difference between mitosis and meiosis?",
    "Give me 5 tips to improve my English writing",
)

@Composable
fun AskAiScreen(
    container: AppContainer,
    viewModel: AskAiViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var input by remember { mutableStateOf("") }
    var subject by remember {
        mutableStateOf(container.pendingSubject?.also { container.pendingSubject = null })
    }
    var level by remember { mutableStateOf<String?>(null) }
    var subjectMenuOpen by remember { mutableStateOf(false) }
    var levelMenuOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Text-to-speech for reading answers aloud.
    val tts = remember {
        TextToSpeech(context) { /* status ignored */ }
    }
    DisposableEffect(Unit) {
        onDispose { tts.stop(); tts.shutdown() }
    }
    fun speak(text: String) {
        tts.language = Locale.US
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "edumind-ai")
    }

    // Image attachment (gallery).
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
            if (bytes != null) viewModel.sendImage(bytes)
        }
    }

    // Voice input.
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) input = spoken
        }
    }

    // Question handed over from the Home screen.
    LaunchedEffect(Unit) {
        container.pendingQuestion?.let { pending ->
            container.pendingQuestion = null
            viewModel.sendMessage(pending, subject = subject, level = level)
        }
    }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Subject + level selectors.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box {
                OutlinedButton(onClick = { subjectMenuOpen = true }) {
                    Text(subject ?: "Subject: General")
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = subjectMenuOpen, onDismissRequest = { subjectMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("General") }, onClick = { subject = null; subjectMenuOpen = false })
                    SubjectCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = { subject = category.name; subjectMenuOpen = false },
                        )
                    }
                }
            }
            Box {
                OutlinedButton(onClick = { levelMenuOpen = true }) {
                    Text(level ?: "Level")
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = levelMenuOpen, onDismissRequest = { levelMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("Any level") }, onClick = { level = null; levelMenuOpen = false })
                    LearningLevels.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = { level = item; levelMenuOpen = false },
                        )
                    }
                }
            }
        }

        if (state.notConfigured) {
            NotConfiguredCard(modifier = Modifier.padding(horizontal = 16.dp))
        }
        state.error?.let { error ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (state.messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RobotMascot(size = 120.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Hi, I'm Lumina",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "How can I help you learn today?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))
                suggestions.forEach { suggestion ->
                    Button(
                        onClick = {
                            viewModel.sendMessage(suggestion, subject = subject, level = level)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) { Text(suggestion) }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.messages) { message ->
                    if (message.role == ChatMessage.Role.USER) {
                        Surface(
                            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(0.85f),
                        ) {
                            Text(
                                text = message.text,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    } else {
                        AnswerView(
                            answerText = message.text,
                            onRegenerate = { viewModel.regenerate() },
                            onSave = { viewModel.saveAnswer(message) },
                            onSpeak = { speak(message.text) },
                        )
                    }
                }
                if (state.isLoading) {
                    item { LoadingView("EduMind AI is thinking…") }
                }
            }
        }

        // Input row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { imagePicker.launch("image/*") }) {
                Icon(Icons.Filled.Image, contentDescription = "Attach an image")
            }
            IconButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask EduMind AI…")
                }
                speechLauncher.launch(intent)
            }) {
                Icon(Icons.Filled.Mic, contentDescription = "Voice input")
            }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Ask anything…") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input, subject = subject, level = level)
                        input = ""
                    }
                },
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
