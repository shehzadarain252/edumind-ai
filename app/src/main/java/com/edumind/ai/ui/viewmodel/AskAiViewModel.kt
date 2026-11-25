package com.edumind.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.ai.AppContainer
import com.edumind.ai.BuildConfig
import com.edumind.ai.data.ChatMessage
import com.edumind.ai.data.local.ChatMessageEntity
import com.edumind.ai.data.local.ConversationEntity
import com.edumind.ai.data.local.SavedAnswerEntity
import com.edumind.ai.data.remote.ChatMessageDto
import com.edumind.ai.data.remote.ChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

data class AskAiUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val notConfigured: Boolean = false,
    val conversationId: String = UUID.randomUUID().toString(),
)

class AskAiViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(AskAiUiState())
    val state: StateFlow<AskAiUiState> = _state.asStateFlow()

    private val backendConfigured: Boolean
        get() = !BuildConfig.BACKEND_URL.contains("your-backend.example.com")

    fun newConversation() {
        _state.update { AskAiUiState(conversationId = UUID.randomUUID().toString()) }
    }

    fun sendMessage(text: String, subject: String? = null, level: String? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _state.value.isLoading) return
        if (!backendConfigured) {
            _state.update {
                it.copy(notConfigured = true, error = null)
            }
            return
        }
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = _state.value.conversationId,
            role = ChatMessage.Role.USER,
            text = trimmed,
            timestamp = System.currentTimeMillis(),
            subject = subject,
        )
        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                isLoading = true,
                error = null,
                notConfigured = false,
            )
        }
        viewModelScope.launch {
            try {
                val history = _state.value.messages.map {
                    ChatMessageDto(
                        role = if (it.role == ChatMessage.Role.USER) "user" else "model",
                        text = it.text,
                    )
                }
                val response = container.api.chat(
                    ChatRequest(messages = history, subject = subject, level = level),
                )
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    conversationId = _state.value.conversationId,
                    role = ChatMessage.Role.AI,
                    text = response.answer,
                    timestamp = System.currentTimeMillis(),
                    subject = response.subject ?: subject,
                )
                _state.update {
                    it.copy(messages = it.messages + aiMessage, isLoading = false)
                }
                persistConversation(userMessage, aiMessage)
                container.settingsRepository.recordQuestionAnswered()
            } catch (e: HttpException) {
                if (e.code() == 503) {
                    _state.update { it.copy(isLoading = false, notConfigured = true) }
                } else {
                    _state.update {
                        it.copy(isLoading = false, error = "AI request failed (${e.code()}). Please try again.")
                    }
                }
            } catch (e: IOException) {
                _state.update {
                    it.copy(isLoading = false, error = "No connection to the EduMind AI backend. Check your internet and backend URL.")
                }
            }
        }
    }

    fun regenerate() {
        val lastUser = _state.value.messages.lastOrNull { it.role == ChatMessage.Role.USER }
            ?: return
        // Drop the last AI message (if any) and resend the last user message.
        _state.update { state ->
            val withoutLastAi = state.messages.dropLastWhile { it.role == ChatMessage.Role.AI }
            state.copy(messages = withoutLastAi)
        }
        sendMessage(lastUser.text, subject = lastUser.subject)
    }

    /** Send an attached image for AI vision analysis (Ask AI image attachments). */
    fun sendImage(jpegBytes: ByteArray, mimeType: String = "image/jpeg") {
        if (_state.value.isLoading) return
        if (!backendConfigured) {
            _state.update { it.copy(notConfigured = true) }
            return
        }
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = _state.value.conversationId,
            role = ChatMessage.Role.USER,
            text = "[Image attached]",
            timestamp = System.currentTimeMillis(),
        )
        _state.update {
            it.copy(messages = it.messages + userMessage, isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val base64 = com.edumind.ai.util.ImageUtils.toBase64(jpegBytes)
                val response = container.api.analyzeImage(
                    com.edumind.ai.data.remote.ImageAnalyzeRequest(
                        imageBase64 = base64,
                        mimeType = mimeType,
                    ),
                )
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    conversationId = _state.value.conversationId,
                    role = ChatMessage.Role.AI,
                    text = response.answer,
                    timestamp = System.currentTimeMillis(),
                )
                _state.update { it.copy(messages = it.messages + aiMessage, isLoading = false) }
                persistConversation(userMessage, aiMessage)
                container.settingsRepository.recordQuestionAnswered()
            } catch (e: HttpException) {
                if (e.code() == 503) {
                    _state.update { it.copy(isLoading = false, notConfigured = true) }
                } else {
                    _state.update {
                        it.copy(isLoading = false, error = "Image analysis failed (${e.code()}).")
                    }
                }
            } catch (e: IOException) {
                _state.update {
                    it.copy(isLoading = false, error = "No connection to the EduMind AI backend.")
                }
            }
        }
    }

    fun clearChat() {
        newConversation()
    }

    fun saveAnswer(message: ChatMessage) {
        val question = _state.value.messages
            .takeWhile { it.id != message.id }
            .lastOrNull { it.role == ChatMessage.Role.USER }?.text.orEmpty()
        viewModelScope.launch {
            container.savedAnswersDao.insert(
                SavedAnswerEntity(
                    id = UUID.randomUUID().toString(),
                    question = question,
                    answer = message.text,
                    subject = message.subject,
                    source = "ask",
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }

    private suspend fun persistConversation(user: ChatMessage, ai: ChatMessage) {
        val conversationId = _state.value.conversationId
        val title = user.text.take(60)
        container.chatDao.insertConversation(
            ConversationEntity(
                id = conversationId,
                title = title,
                lastUpdated = System.currentTimeMillis(),
            ),
        )
        container.chatDao.insertMessage(
            ChatMessageEntity(
                id = user.id,
                conversationId = conversationId,
                role = "USER",
                text = user.text,
                timestamp = user.timestamp,
                subject = user.subject,
            ),
        )
        container.chatDao.insertMessage(
            ChatMessageEntity(
                id = ai.id,
                conversationId = conversationId,
                role = "AI",
                text = ai.text,
                timestamp = ai.timestamp,
                subject = ai.subject,
            ),
        )
    }
}
