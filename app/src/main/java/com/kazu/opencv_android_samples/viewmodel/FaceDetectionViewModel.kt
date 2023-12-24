package com.kazu.opencv_android_samples.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.objdetect.CascadeClassifier

class FaceDetectionViewModel : ViewModel() {

    private lateinit var faceDetector: CascadeClassifier

    fun initializeFaceDetector(cascadeFilePath: String) {
        faceDetector = CascadeClassifier(cascadeFilePath)
        if (faceDetector.empty()) {
            Log.e("FaceDetection", "Cascade classifier could not be loaded")
        }
    }

    fun detectFaces(inputFrame: Mat): List<Rect> {
        val faces = MatOfRect()
        faceDetector.detectMultiScale(inputFrame, faces, 1.05, 3, 0, Size(80.0, 60.0), Size())
        faces.toArray().forEach { face ->
            Log.d("FaceDetection", "Detected face: X=${face.x}, Y=${face.y}, Width=${face.width}, Height=${face.height}")
        }
        Log.d("FaceDetection","Listです。 ${faces.toList()}")
        return faces.toList()
    }
}