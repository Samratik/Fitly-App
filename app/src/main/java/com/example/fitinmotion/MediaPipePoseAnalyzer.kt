package com.example.fitinmotion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.ByteArrayOutputStream



class MediaPipePoseAnalyzer(
    context: android.content.Context,
    private val viewSizeProvider: () -> Size,
    private val onPose: (Map<Int, android.graphics.PointF>) -> Unit
) : ImageAnalysis.Analyzer {

    private val landmarker: PoseLandmarker by lazy {
        val base = BaseOptions.builder()
            .setModelAssetPath("models/pose_landmarker_lite.task")
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(base)
            .setRunningMode(RunningMode.VIDEO)
            .setNumPoses(1)
            .build()

        PoseLandmarker.createFromOptions(context, options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap().rotate(imageProxy.imageInfo.rotationDegrees)
        val mpImage: MPImage = BitmapImageBuilder(bitmap).build()

        val ts = android.os.SystemClock.uptimeMillis()
        val result: PoseLandmarkerResult = landmarker.detectForVideo(mpImage, ts)

        val landmarks = result.landmarks().firstOrNull().orEmpty()
        val size = viewSizeProvider()

        val map = HashMap<Int, android.graphics.PointF>(landmarks.size)
        for ((idx, lm) in landmarks.withIndex()) {
            map[idx] = android.graphics.PointF(lm.x() * size.width, lm.y() * size.height)
        }

        onPose(map)
        imageProxy.close()
    }
}

private fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)

    val chromaRowStride = planes[1].rowStride
    val chromaPixelStride = planes[1].pixelStride

    var offset = ySize
    val rowData = ByteArray(chromaRowStride)
    for (row in 0 until height / 2) {
        val rowStart = row * chromaRowStride
        vBuffer.position(rowStart)
        uBuffer.position(rowStart)
        vBuffer.get(rowData, 0, chromaRowStride)
        var col = 0
        while (col < width) {
            val v = rowData[col]
            val u = rowData[col + chromaPixelStride]
            nv21[offset++] = v
            nv21[offset++] = u
            col += chromaPixelStride * 2
        }
    }

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
    val jpegBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
}

private fun Bitmap.rotate(degrees: Int): Bitmap =
    if (degrees == 0) this
    else Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { postRotate(degrees.toFloat()) }, true)
