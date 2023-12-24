package com.kazu.opencv_android_samples.application

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate();
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV loaded successfully");
        }
    }
}