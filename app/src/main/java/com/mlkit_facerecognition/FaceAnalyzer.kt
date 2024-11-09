package com.mlkit_facerecognition

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetectorOptions.CLASSIFICATION_MODE_ALL
import com.google.mlkit.vision.face.FaceDetectorOptions.CONTOUR_MODE_ALL
import com.google.mlkit.vision.face.FaceDetectorOptions.LANDMARK_MODE_NONE
import com.google.mlkit.vision.face.FaceDetectorOptions.PERFORMANCE_MODE_FAST

class FaceAnalyzer(private val callBack : FaceAnalyzerCallback) : ImageAnalysis.Analyzer {

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setContourMode(CONTOUR_MODE_ALL)
        .setPerformanceMode(PERFORMANCE_MODE_FAST)
        .setLandmarkMode(LANDMARK_MODE_NONE)
        .setClassificationMode(CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.20f)
        .build()


    private val detector = FaceDetection.getClient(realTimeOpts)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        mediaImage?.let {
            val inputImage =
                InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    callBack.processFace(faces)
                    image.close()
                }
                .addOnFailureListener {
                    callBack.errorFace(it.message.orEmpty())
                    image.close()
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }
}

interface FaceAnalyzerCallback {
    fun processFace(faces: List<Face>)
    fun errorFace(error: String)
}