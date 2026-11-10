package com.edumind.ai.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.ui.components.AnswerView
import com.edumind.ai.ui.components.NotConfiguredCard
import com.edumind.ai.ui.components.SectionTitle
import com.edumind.ai.ui.viewmodel.DocumentViewModel
import com.edumind.ai.util.fileInfo
import com.edumind.ai.util.readBytes

private val documentMimeTypes = arrayOf(
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "text/plain",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "image/jpeg",
    "image/png",
    "image/webp",
)

/**
 * Document upload: PDF / DOC / DOCX / TXT / PPT / PPTX / images.
 * Multi-select, preview (name + size), remove, then real extraction +
 * AI analysis on the backend. Never pretends a document was processed
 * when extraction or AI fails.
 */
@Composable
fun DocumentUploadScreen(viewModel: DocumentViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var task by remember { mutableStateOf("summarize") }
    var question by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        val files = uris.map { uri -> context.fileInfo(uri) }
        viewModel.addFiles(files)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle(
            "Document Upload",
            "PDF, DOC, DOCX, TXT, PPT, PPTX and images (max 20 MB each)",
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { picker.launch(documentMimeTypes) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Select Documents") }

        if (state.files.isEmpty()) {
            Text(
                text = "No documents selected yet. Pick one or more files to summarise them, " +
                    "extract their questions, or generate a practice test.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            state.files.forEach { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(text = file.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${file.sizeText} • .${file.extension}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { viewModel.removeFile(file) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "What should EduMind AI do?", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("summarize" to "Summarize", "questions" to "Questions", "quiz" to "Practice Test").forEach { (id, label) ->
                    androidx.compose.material3.FilterChip(
                        selected = task == id,
                        onClick = { task = id },
                        label = { Text(label) },
                    )
                }
            }
            if (task == "questions") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    placeholder = { Text("Optional: ask a specific question about the document…") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val file = state.files.firstOrNull() ?: return@Button
                    val bytes = context.readBytes(file.uri)
                    viewModel.uploadBytes(file, bytes, task, question.ifBlank { null })
                },
                enabled = !state.isUploading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Process Document") }
        }

        if (state.isUploading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                text = "Uploading and analyzing…",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (state.notConfigured) {
            Spacer(modifier = Modifier.height(16.dp))
            NotConfiguredCard()
        }
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        state.results.forEach { result ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${result.fileName} — ${result.task}",
                style = MaterialTheme.typography.titleMedium,
            )
            AnswerView(answerText = result.output)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
