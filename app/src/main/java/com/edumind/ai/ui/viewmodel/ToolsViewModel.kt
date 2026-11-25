package com.edumind.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.ai.AppContainer
import com.edumind.ai.BuildConfig
import com.edumind.ai.data.local.SavedAnswerEntity
import com.edumind.ai.data.remote.ToolRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

sealed class ToolUiState {
    data object Idle : ToolUiState()
    data object Loading : ToolUiState()
    data class Success(val output: String) : ToolUiState()
    data class NotConfigured(val message: String) : ToolUiState()
    data class Error(val message: String) : ToolUiState()
}

class ToolsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<ToolUiState>(ToolUiState.Idle)
    val state: StateFlow<ToolUiState> = _state.asStateFlow()

    private var lastToolId: String? = null
    private var lastInput: String? = null
    private var lastOutput: String? = null

    private val backendConfigured: Boolean
        get() = !BuildConfig.BACKEND_URL.contains("your-backend.example.com")

    fun runTool(toolId: String, input: String, level: String? = null) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        if (!backendConfigured) {
            _state.value = ToolUiState.NotConfigured(
                "AI is not configured. Add your Gemini API key to the backend and set the backend URL in local.properties.",
            )
            return
        }
        lastToolId = toolId
        lastInput = trimmed
        _state.value = ToolUiState.Loading
        viewModelScope.launch {
            try {
                val response = container.api.runTool(
                    ToolRequest(toolId = toolId, input = trimmed, level = level),
                )
                lastOutput = response.answer
                _state.value = ToolUiState.Success(response.answer)
                container.settingsRepository.recordQuestionAnswered()
            } catch (e: HttpException) {
                _state.value = if (e.code() == 503) {
                    ToolUiState.NotConfigured(
                        "AI is not configured. Add your Gemini API key to the backend.",
                    )
                } else {
                    ToolUiState.Error("The AI tool failed (${e.code()}). Please try again.")
                }
            } catch (e: IOException) {
                _state.value = ToolUiState.Error(
                    "No connection to the EduMind AI backend. Check your internet and backend URL.",
                )
            }
        }
    }

    fun regenerate() {
        val toolId = lastToolId ?: return
        val input = lastInput ?: return
        runTool(toolId, input)
    }

    fun saveOutput(toolId: String, toolName: String) {
        val output = lastOutput ?: return
        val input = lastInput.orEmpty()
        viewModelScope.launch {
            container.savedAnswersDao.insert(
                SavedAnswerEntity(
                    id = UUID.randomUUID().toString(),
                    question = input,
                    answer = output,
                    subject = toolName,
                    source = "tool",
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun reset() {
        _state.value = ToolUiState.Idle
        lastOutput = null
    }
}
