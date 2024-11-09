package com.mlkit_facerecognition

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.face.Face
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onFacesDetected: (List<Face>) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor = Executors.newSingleThreadExecutor()

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) { previewView ->

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(executor, FaceAnalyzer(object : FaceAnalyzerCallback {
                        override fun processFace(faces: List<Face>) {
                            onFacesDetected(faces)
                        }

                        override fun errorFace(error: String) {
                            onError(error)
                        }
                    }))
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {
                onError("Failed to bind camera use cases: ${e.message}")
            }
        }, executor)
    }
}

