package com.edumind.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.ai.AppContainer
import com.edumind.ai.BuildConfig
import com.edumind.ai.data.local.SavedAnswerEntity
import com.edumind.ai.data.remote.ImageAnalyzeRequest
import com.edumind.ai.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

sealed class ScannerUiState {
    data object Idle : ScannerUiState()
    data class Preview(val jpegBytes: ByteArray) : ScannerUiState()
    data object Analyzing : ScannerUiState()
    data class Answered(val answer: String, val recognizedQuestion: String?) : ScannerUiState()
    data class NotConfigured(val message: String) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}

/**
 * Camera scanner + gallery image analysis. The loading message
 * "EduMind AI is analyzing your question..." is shown while analyzing and
 * stops as soon as the real answer arrives (or an error is shown).
 */
class ScannerViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    private var rotationDegrees: Float = 0f
    private var lastAnalyzedBytes: ByteArray? = null

    private val backendConfigured: Boolean
        get() = !BuildConfig.BACKEND_URL.contains("your-backend.example.com")

    fun onPhotoCaptured(jpegBytes: ByteArray) {
        lastAnalyzedBytes = jpegBytes
        rotationDegrees = 0f
        _state.value = ScannerUiState.Preview(jpegBytes)
    }

    fun rotatePreview() {
        val current = _state.value as? ScannerUiState.Preview ?: return
        rotationDegrees = (rotationDegrees + 90f) % 360f
        val bitmap = ImageUtils.decode(current.jpegBytes) ?: return
        val rotated = ImageUtils.rotate(bitmap, 90f)
        val bytes = ImageUtils.toJpegBytes(ImageUtils.scaleDown(rotated))
        lastAnalyzedBytes = bytes
        _state.value = ScannerUiState.Preview(bytes)
    }

    fun cropPreview() {
        val current = _state.value as? ScannerUiState.Preview ?: return
        val bitmap = ImageUtils.decode(current.jpegBytes) ?: return
        // Center-crop to a 4:3 frame (typical textbook page area).
        val cropped = ImageUtils.centerCrop(bitmap, 4f / 3f)
        val bytes = ImageUtils.toJpegBytes(ImageUtils.scaleDown(cropped))
        lastAnalyzedBytes = bytes
        _state.value = ScannerUiState.Preview(bytes)
    }

    fun retake() {
        lastAnalyzedBytes = null
        rotationDegrees = 0f
        _state.value = ScannerUiState.Idle
    }

    fun analyze(question: String? = null) {
        val bytes = lastAnalyzedBytes ?: return
        if (!backendConfigured) {
            _state.value = ScannerUiState.NotConfigured(
                "AI is not configured. Add your Gemini API key to the backend and set the backend URL in local.properties.",
            )
            return
        }
        _state.value = ScannerUiState.Analyzing
        viewModelScope.launch {
            try {
                val base64 = ImageUtils.toBase64(bytes)
                val response = container.api.analyzeImage(
                    ImageAnalyzeRequest(imageBase64 = base64, question = question),
                )
                _state.value = ScannerUiState.Answered(
                    answer = response.answer,
                    recognizedQuestion = response.subject,
                )
                container.settingsRepository.recordQuestionAnswered()
            } catch (e: HttpException) {
                _state.value = if (e.code() == 503) {
                    ScannerUiState.NotConfigured(
                        "AI is not configured. Add your Gemini API key to the backend.",
                    )
                } else {
                    ScannerUiState.Error("Image analysis failed (${e.code()}). Please retake the photo and try again.")
                }
            } catch (e: IOException) {
                _state.value = ScannerUiState.Error(
                    "No connection to the EduMind AI backend. Check your internet and backend URL.",
                )
            }
        }
    }

    fun saveAnswer(answer: String) {
        viewModelScope.launch {
            container.savedAnswersDao.insert(
                SavedAnswerEntity(
                    id = UUID.randomUUID().toString(),
                    question = "Scanned question",
                    answer = answer,
                    subject = null,
                    source = "scanner",
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }
}
