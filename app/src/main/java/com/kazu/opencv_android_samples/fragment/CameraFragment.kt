package com.kazu.opencv_android_samples.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kazu.opencv_android_samples.R
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private lateinit var viewFinder: PreviewView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewFinder = view.findViewById(R.id.view_finder)

        // カメラパーミッションのリクエスト
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    /**
     * パーミッション
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     *  permissionLauncher
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // すべてのパーミッションが許可されたかチェック
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startCamera()
        } else {
            showDialog()
        }
    }

    /**
     * カメラを起動する
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // カメラプロバイダーの取得
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // プレビューの構築
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // デフォルトのカメラを選択
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val imageAnalysis = ImageAnalysis.Builder().build()
            try {
                imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    ImageAnalysis.Analyzer { image ->

                    })
                // 既存のカメラをアンバインドして新しいカメラをバインド
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e("CameraFragment", "カメラの起動に失敗しました", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * ダイアログを表示する
     */
    private fun showDialog() {
        AlertDialog.Builder(context)
            .setTitle("カメラのアクセス権限を許可してください")
            .setMessage("このアプリを使用するにはカメラの権限を許可しないと使用することができません。")
            .setPositiveButton("設定画面へ移動") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("アプリを終了する", ){ _, _ ->
                activity?.finish()
            }
            .show()
    }

    /**
     * 設定画面を開く
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }
}
