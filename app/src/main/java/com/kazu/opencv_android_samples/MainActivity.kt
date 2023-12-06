package com.kazu.opencv_android_samples

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val versionString: TextView = findViewById(R.id.text_view)
        versionString.text = openCVVersionDisplay(OpenCVLoader.initDebug())
    }

    private fun openCVVersionDisplay(arg: Boolean): String {
        return when (arg) {
            true -> "OpenCV Version: " + OpenCVLoader.OPENCV_VERSION
            false -> "ERROR: OpenCV Load Failed"
        }
    }
}