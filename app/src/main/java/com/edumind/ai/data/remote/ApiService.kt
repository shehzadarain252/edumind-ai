package com.edumind.ai.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

// ---------- Request / response DTOs ----------

data class ChatRequest(
    @SerializedName("messages") val messages: List<ChatMessageDto>,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("level") val level: String? = null,
    @SerializedName("systemHint") val systemHint: String? = null,
)

data class ChatMessageDto(
    @SerializedName("role") val role: String, // "user" | "model"
    @SerializedName("text") val text: String,
)

data class ChatResponse(
    @SerializedName("answer") val answer: String,
    @SerializedName("subject") val subject: String?,
    @SerializedName("model") val model: String?,
)

data class ToolRequest(
    @SerializedName("toolId") val toolId: String,
    @SerializedName("input") val input: String,
    @SerializedName("level") val level: String? = null,
)

data class SummarizeRequest(
    @SerializedName("text") val text: String,
    @SerializedName("style") val style: String? = null, // "brief" | "detailed" | "chapters"
)

data class ImageAnalyzeRequest(
    @SerializedName("imageBase64") val imageBase64: String,
    @SerializedName("mimeType") val mimeType: String = "image/jpeg",
    @SerializedName("question") val question: String? = null,
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("aiConfigured") val aiConfigured: Boolean,
    @SerializedName("model") val model: String?,
)

data class ApiErrorBody(
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?,
)

// ---------- API ----------

interface ApiService {

    @GET("api/health")
    suspend fun health(): HealthResponse

    /** General chat / solve endpoint (Ask AI screen and chat assistant). */
    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    /** One of the 20 AI study tools. */
    @POST("api/tools")
    suspend fun runTool(@Body request: ToolRequest): ChatResponse

    /** Summarize pasted text. */
    @POST("api/summarize")
    suspend fun summarize(@Body request: SummarizeRequest): ChatResponse

    /** Analyze a captured/scanned image with Gemini vision. */
    @POST("api/analyze-image")
    suspend fun analyzeImage(@Body request: ImageAnalyzeRequest): ChatResponse

    /** Upload a document (PDF/DOCX/TXT/PPTX/image) for extraction + AI analysis. */
    @Multipart
    @POST("api/document")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("task") task: okhttp3.RequestBody,
    ): ChatResponse

    companion object {
        fun create(baseUrl: String): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build()
            val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            return Retrofit.Builder()
                .baseUrl(normalized)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
