package com.edumind.ai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.edumind.ai.ui.components.AnswerView
import com.edumind.ai.ui.components.LoadingView
import com.edumind.ai.ui.components.NotConfiguredCard
import com.edumind.ai.ui.components.SectionTitle
import com.edumind.ai.ui.viewmodel.ScannerUiState
import com.edumind.ai.ui.viewmodel.ScannerViewModel
import java.io.File

/**
 * Camera scanner (CameraX). Captures textbook pages, handwritten questions,
 * equations and exam papers, then sends the image to Gemini for analysis.
 *
 * Ads are NEVER shown during scanning — no AdMob call is made here.
 */
@Composable
fun ScannerScreen(viewModel: ScannerViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsState()
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var captureError by remember { mutableStateOf<String?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(lensFacing, hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
        } catch (e: Exception) {
            captureError = "Camera could not be started: ${e.message}"
        }
    }

    fun capturePhoto() {
        val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModel.onPhotoCaptured(file.readBytes())
                }

                override fun onError(exception: ImageCaptureException) {
                    captureError = "Photo capture failed: ${exception.message}"
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        SectionTitle("Camera Scanner", "Point the camera at a question and tap capture")

        when (val s = state) {
            is ScannerUiState.Idle -> {
                if (!hasPermission) {
                    Text(
                        text = "Camera permission is needed to scan questions.",
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Allow Camera")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier.fillMaxSize(),
                        )
                        IconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                        ) {
                            Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch camera")
                        }
                    }
                    captureError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { capturePhoto() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Text("  Capture Question")
                    }
                }
            }

            is ScannerUiState.Preview -> {
                AsyncImage(
                    model = s.jpegBytes,
                    contentDescription = "Captured question",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { viewModel.rotatePreview() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.RotateRight, contentDescription = null)
                        Text("Rotate")
                    }
                    OutlinedButton(onClick = { viewModel.cropPreview() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Crop, contentDescription = null)
                        Text("Crop")
                    }
                    OutlinedButton(onClick = { viewModel.retake() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Text("Retake")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.analyze() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Solve This Question") }
            }

            is ScannerUiState.Analyzing -> {
                // The exact loading message from the specification. It stops
                // as soon as the real answer (or an error) arrives.
                LoadingView("EduMind AI is analyzing your question…")
            }

            is ScannerUiState.Answered -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    s.recognizedQuestion?.let {
                        Text(
                            text = "Recognized: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    AnswerView(
                        answerText = s.answer,
                        onSave = { viewModel.saveAnswer(s.answer) },
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.retake() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Scan Another Question") }
            }

            is ScannerUiState.NotConfigured -> {
                NotConfiguredCard()
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { viewModel.retake() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Camera")
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
