package com.mlkit_facerecognition

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FaceDetectionScreen() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    if (!hasPermission) {
        LaunchedEffect(Unit) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.CAMERA),
                101
            )
        }
    }

    if (hasPermission) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = {
                CameraPreview(
                    onFacesDetected = { faces ->
                        faces.forEach { face ->
                            println("Detected face with bounds: ${face.boundingBox}")
                        }
                    },
                    onError = { error ->
                        println("Face detection error: $error")
                    }
                )
            }
        )
    }
}

