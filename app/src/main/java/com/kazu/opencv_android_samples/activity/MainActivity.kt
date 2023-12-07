package com.kazu.opencv_android_samples.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kazu.opencv_android_samples.R
import com.kazu.opencv_android_samples.fragment.CameraFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // CameraFragmentを表示
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CameraFragment())
                .commit()
        }
    }
}