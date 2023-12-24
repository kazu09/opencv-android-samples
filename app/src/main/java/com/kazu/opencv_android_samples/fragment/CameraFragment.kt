package com.kazu.opencv_android_samples.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kazu.opencv_android_samples.R
import com.kazu.opencv_android_samples.viewmodel.FaceDetectionViewModel
import androidx.fragment.app.viewModels
import com.kazu.opencv_android_samples.view.FaceOverlayView
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream

class CameraFragment : Fragment() {

    private lateinit var viewFinder: PreviewView
    private val viewModel: FaceDetectionViewModel by viewModels()
    // 顔の位置を表示するためのビューを参照
    private lateinit var faceOverlayView: FaceOverlayView


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
            faceOverlayView = view.findViewById(R.id.faceOverlayView)
            startCamera()
            val cascadeFilePath = loadCascadeFile("haarcascade_frontalface_default.xml")
            viewModel.initializeFaceDetector(cascadeFilePath)
            // ビューのサイズを取得してログに出力する
            viewFinder.post {
                val viewSize = Size(viewFinder.width, viewFinder.height)
                Log.d("ViewSize", "ビューサイズの確認　Width: ${viewSize.width}, Height: ${viewSize.height}")
            }
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
            val imageAnalysis = ImageAnalysis.Builder()
                // ...
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                        val frame = convertImageProxyToMat(imageProxy) // ImageProxyをMatに変換
                        val faces = viewModel.detectFaces(frame)

                        val cameraResolution = Size(imageProxy.width, imageProxy.height)
                        val viewSize = Size(faceOverlayView.width, faceOverlayView.height)
                        Log.d("CameraResolution", "カメラの解像度　Width: ${cameraResolution.width}, Height: ${cameraResolution.height}")

                        // 検出された顔の情報をUIに反映する
                        faceOverlayView.post {
                            faceOverlayView.updateFaces(faces, cameraResolution, viewSize)
                        }

                        // 検出された顔に対する処理（UIスレッドで実行する必要があります）
                        imageProxy.close()
                    }
                }
            try {
                // 既存のカメラをアンバインドして新しいカメラをバインド
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis)
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

    private fun convertImageProxyToMat(imageProxy: ImageProxy): Mat {
        val yBuffer = imageProxy.planes[0].buffer // Y
        val vuBuffer = imageProxy.planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvMat = Mat(imageProxy.height + imageProxy.height / 2, imageProxy.width, CvType.CV_8UC1)
        yuvMat.put(0, 0, nv21)

        val rgbMat = Mat()
        Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21, 3)

        yuvMat.release()

        return rgbMat
    }

    private fun loadCascadeFile(filename: String): String {
        val inputStream = requireContext().assets.open(filename)
        val cascadeDir = requireContext().getDir("cascade", Context.MODE_PRIVATE)
        val cascadeFile = File(cascadeDir, filename)
        val outputStream = FileOutputStream(cascadeFile)

        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        return cascadeFile.absolutePath
    }
}
