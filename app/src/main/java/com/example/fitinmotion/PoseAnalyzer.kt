package com.example.fitinmotion

import android.graphics.PointF
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.min

class PoseAnalyzer(
    private val viewSizeProvider: () -> Size,
    private val onPose: (Pose, Map<Int, PointF>) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: PoseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE) // реальное время
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val media = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { pose ->
                val rot = imageProxy.imageInfo.rotationDegrees
                val uprightW = if (rot == 90 || rot == 270) image.height else image.width
                val uprightH = if (rot == 90 || rot == 270) image.width  else image.height

                val projected = projectToView(pose, uprightW, uprightH)  // ← передаём исправленные W/H
                onPose(pose, projected)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun projectToView(
        pose: Pose, imgW: Int, imgH: Int
    ): Map<Int, PointF> {
        val (vw, vh) = viewSizeProvider().let { it.width to it.height }

        // FIT_CENTER проекция (вписываем без обрезки)
        val scale = kotlin.math.min(vw.toFloat() / imgW, vh.toFloat() / imgH)
        val dx = (vw - imgW * scale) / 2f
        val dy = (vh - imgH * scale) / 2f

        return buildMap {
            for (lm in pose.allPoseLandmarks) {
                val px = dx + lm.position.x * scale
                val py = dy + lm.position.y * scale
                put(lm.landmarkType, PointF(px, py))
            }
        }
    }


}