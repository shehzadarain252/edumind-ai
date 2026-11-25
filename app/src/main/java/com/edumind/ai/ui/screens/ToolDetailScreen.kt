package com.edumind.ai.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.AppContainer
import com.edumind.ai.data.findTool
import com.edumind.ai.ui.components.AnswerView
import com.edumind.ai.ui.components.LoadingView
import com.edumind.ai.ui.components.NotConfiguredCard
import com.edumind.ai.ui.viewmodel.ToolUiState
import com.edumind.ai.ui.viewmodel.ToolsViewModel

/**
 * One screen per AI study tool, driven by the tool's config.
 * Input -> loading -> output (with Copy/Share/Regenerate/Save).
 *
 * No ad is shown from this screen. Interstitials are handled centrally:
 * any option tap schedules an ad 5 seconds later (see EduMindNavHost).
 * Ads never interrupt tool input or generation.
 */
@Composable
fun ToolDetailScreen(
    toolId: String,
    viewModel: ToolsViewModel,
    navController: NavHostController,
) {
    val tool = findTool(toolId)
    val state by viewModel.state.collectAsState()
    var input by remember { mutableStateOf("") }

    if (tool == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Tool not found.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    fun goBackWithAd() {
        // Interstitials are handled centrally: any option tap schedules an
        // ad 5 seconds later (see EduMindNavHost). No ad is shown here, so
        // leaving a tool never triggers a second ad.
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = tool.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = tool.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(tool.inputLabel) },
            placeholder = { Text(tool.inputPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 4,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { viewModel.runTool(tool.id, input) },
            enabled = input.isNotBlank() && state !is ToolUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Run ${tool.name}") }

        Spacer(modifier = Modifier.height(16.dp))
        when (val s = state) {
            is ToolUiState.Idle -> {
                Text(
                    text = "Enter your input above and tap Run. The AI will ${tool.outputTitle.lowercase()} for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            is ToolUiState.Loading -> LoadingView("EduMind AI is working on it…")
            is ToolUiState.Success -> {
                Text(
                    text = tool.outputTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                AnswerView(
                    answerText = s.output,
                    onRegenerate = { viewModel.regenerate() },
                    onSave = { viewModel.saveOutput(tool.id, tool.name) },
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { goBackWithAd() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Back to Tools") }
            }
            is ToolUiState.NotConfigured -> NotConfiguredCard()
            is ToolUiState.Error -> {
                Text(text = s.message, color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
