package com.mlkit_facerecognition

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.face.Face

@Composable
fun CameraPreview(
    onFacesDetected: (List<Face>) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val detectedFaces = remember { mutableStateOf<List<Face>>(emptyList()) }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val analyzer = FaceAnalyzer(object : FaceAnalyzerCallback {
                override fun processFace(faces: List<Face>) {
                    detectedFaces.value = faces
                    onFacesDetected(faces)
                }

                override fun errorFace(error: String) {
                    onError(error)
                }
            })

            imageAnalysis.setAnalyzer(mainExecutor, analyzer)

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {
                onError("Failed to bind camera use cases: ${e.message}")
            }
        }, mainExecutor)
    }

    // Overlay a Box on top of the camera preview to draw bounding boxes for detected faces
    Box(modifier = Modifier.fillMaxSize()) {
        FaceDetectionOverlay(faces = detectedFaces.value)
    }
}

@Composable
fun FaceDetectionOverlay(faces: List<Face>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        faces.forEach { face ->
            val bounds = face.boundingBox
            // Draw a rectangle for each detected face
            drawRect(
                color = Color.Green,
                topLeft = Offset(bounds.left.toFloat(), bounds.top.toFloat()),
                size = androidx.compose.ui.geometry.Size(bounds.width().toFloat(), bounds.height().toFloat()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
        }
    }
}

