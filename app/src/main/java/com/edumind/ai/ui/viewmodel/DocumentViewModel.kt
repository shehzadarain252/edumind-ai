package com.edumind.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.ai.AppContainer
import com.edumind.ai.BuildConfig
import com.edumind.ai.data.local.SavedAnswerEntity
import com.edumind.ai.util.MAX_DOCUMENT_BYTES
import com.edumind.ai.util.PickedFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

data class DocumentResult(
    val fileName: String,
    val task: String,
    val output: String,
    val timestamp: Long = System.currentTimeMillis(),
)

data class DocumentUiState(
    val files: List<PickedFile> = emptyList(),
    val isUploading: Boolean = false,
    val results: List<DocumentResult> = emptyList(),
    val error: String? = null,
    val notConfigured: Boolean = false,
)

/**
 * Document upload: pick, preview, remove, upload to the backend for real
 * text extraction + AI analysis. Never pretends a document was processed
 * when extraction or AI fails — errors are shown honestly.
 */
class DocumentViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(DocumentUiState())
    val state: StateFlow<DocumentUiState> = _state.asStateFlow()

    private val backendConfigured: Boolean
        get() = !BuildConfig.BACKEND_URL.contains("your-backend.example.com")

    fun addFiles(files: List<PickedFile>) {
        val valid = files.filter { file ->
            file.sizeBytes <= MAX_DOCUMENT_BYTES &&
                file.extension in com.edumind.ai.util.SUPPORTED_DOCUMENT_EXTENSIONS
        }
        _state.update {
            it.copy(
                files = (it.files + valid).distinctBy { f -> f.uri },
                error = if (valid.size < files.size) {
                    "Some files were skipped: unsupported type or larger than 20 MB."
                } else {
                    it.error
                },
            )
        }
    }

    fun removeFile(file: PickedFile) {
        _state.update { it.copy(files = it.files - file) }
    }

    fun clearError() {
        _state.update { it.copy(error = null, notConfigured = false) }
    }

    /** Upload with bytes already read by the UI layer (avoids Context in the VM). */
    fun uploadBytes(file: PickedFile, bytes: ByteArray, task: String, question: String? = null) {
        if (!backendConfigured) {
            _state.update { it.copy(notConfigured = true) }
            return
        }
        if (bytes.size.toLong() > MAX_DOCUMENT_BYTES) {
            _state.update { it.copy(error = "File is larger than 20 MB.") }
            return
        }
        _state.update { it.copy(isUploading = true, error = null) }
        viewModelScope.launch {
            try {
                val mime = file.mimeType ?: "application/octet-stream"
                val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, body)
                val taskBody = (if (question.isNullOrBlank()) task else "$task::$question")
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val response = container.api.uploadDocument(part, taskBody)
                val result = DocumentResult(
                    fileName = file.name,
                    task = task,
                    output = response.answer,
                )
                _state.update {
                    it.copy(
                        isUploading = false,
                        results = listOf(result) + it.results,
                        files = it.files - file,
                    )
                }
                container.savedAnswersDao.insert(
                    SavedAnswerEntity(
                        id = UUID.randomUUID().toString(),
                        question = "${file.name} — $task",
                        answer = response.answer,
                        subject = null,
                        source = "document",
                        timestamp = System.currentTimeMillis(),
                    ),
                )
                container.settingsRepository.recordQuestionAnswered()
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    503 -> null // handled below as not-configured
                    422 -> "This file type could not be read. Try a PDF, DOCX, TXT or image file."
                    else -> "Document processing failed (${e.code()}). Please try again."
                }
                _state.update {
                    if (e.code() == 503) it.copy(isUploading = false, notConfigured = true)
                    else it.copy(isUploading = false, error = message)
                }
            } catch (e: IOException) {
                _state.update {
                    it.copy(
                        isUploading = false,
                        error = "No connection to the EduMind AI backend. Check your internet and backend URL.",
                    )
                }
            }
        }
    }
}
