package com.kazu.opencv_android_samples.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.View
import org.opencv.core.Rect
import kotlin.math.min


class FaceOverlayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var faces: List<Rect> = emptyList()
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 描画ロジック
        faces.forEach { face ->
            Log.d("FaceDetection","face。 $face")
            canvas.drawRect(face.toRectF(), paint)
        }
    }

    private fun Rect.toRectF(): RectF {
        return RectF(this.x.toFloat(), this.y.toFloat(), (this.x + this.width).toFloat(), (this.y + this.height).toFloat())
    }

    fun updateFaces(detectedFaces: List<Rect>, cameraResolution: Size, viewSize: Size) {
        val scaleX = viewSize.width.toFloat() / cameraResolution.width
        val scaleY = viewSize.height.toFloat() / cameraResolution.height
        val scaleForSize = 0.4 // 枠のサイズを調整するための係数

        faces = detectedFaces.map { rect ->
            val scaledX = rect.x * scaleX
            val scaledY = rect.y * scaleY
            val scaledWidth = rect.width * scaleX * scaleForSize
            val scaledHeight = rect.height * scaleY * scaleForSize

            Rect(
                scaledX.toInt(),
                scaledY.toInt(),
                (scaledX + scaledWidth).toInt(),
                (scaledY + scaledHeight).toInt()
            )
        }

        invalidate() // ビューの再描画を要求
    }


}