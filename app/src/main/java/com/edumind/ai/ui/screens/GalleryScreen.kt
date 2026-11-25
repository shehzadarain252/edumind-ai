package com.edumind.ai.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.edumind.ai.AppContainer
import com.edumind.ai.ui.components.AnswerView
import com.edumind.ai.ui.components.LoadingView
import com.edumind.ai.ui.components.NotConfiguredCard
import com.edumind.ai.ui.components.SectionTitle
import com.edumind.ai.ui.navigation.vmFactory
import com.edumind.ai.ui.viewmodel.ScannerUiState
import com.edumind.ai.ui.viewmodel.ScannerViewModel

/**
 * Gallery upload: pick an image from the device gallery and have
 * EduMind AI read and solve the question in it.
 */
@Composable
fun GalleryScreen(container: AppContainer, navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ScannerViewModel = viewModel(
        factory = vmFactory { ScannerViewModel(container) },
    )
    val state by viewModel.state.collectAsState()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
            if (bytes != null) viewModel.onPhotoCaptured(bytes)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        SectionTitle("Gallery Upload", "Pick a photo of a question to solve it")

        when (val s = state) {
            is ScannerUiState.Idle -> {
                Text(
                    text = "Choose a photo from your gallery — a textbook page, a handwritten " +
                        "question, an equation or an exam paper.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                Button(
                    onClick = { picker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Pick an Image") }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { navController.navigate(com.edumind.ai.ui.navigation.Routes.SCANNER) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Camera Scanner Instead") }
            }

            is ScannerUiState.Preview -> {
                AsyncImage(
                    model = s.jpegBytes,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.analyze() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Solve This Question") }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.retake() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Choose a Different Image") }
            }

            is ScannerUiState.Analyzing -> {
                LoadingView("EduMind AI is analyzing your question…")
            }

            is ScannerUiState.Answered -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    AnswerView(
                        answerText = s.answer,
                        onSave = { viewModel.saveAnswer(s.answer) },
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.retake() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Analyze Another Image") }
            }

            is ScannerUiState.NotConfigured -> {
                NotConfiguredCard()
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { viewModel.retake() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Pick Another Image")
                }
            }

            is ScannerUiState.Error -> {
                Text(
                    text = s.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                Button(onClick = { viewModel.retake() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Try Again")
                }
            }
        }
    }
}
