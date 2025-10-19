package com.example.fitinmotion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.PoseLandmark

class PoseOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val pt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 10f
        color = 0xFFFFFFFF.toInt()
    }
    private val ln = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = 0x80FFFFFF.toInt()
    }

    @Volatile private var points: Map<Int, PointF> = emptyMap()

    fun updateLandmarks(projected: Map<Int, PointF>) {
        points = projected
        postInvalidateOnAnimation()
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (points.isEmpty()) return
        fun link(a: Int, b: Int) {
            val p1 = points[a] ?: return
            val p2 = points[b] ?: return
            c.drawLine(p1.x, p1.y, p2.x, p2.y, ln)
        }
        // плечи/таз
        link(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER)
        link(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
        link(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP)
        link(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP)
        // руки
        link(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
        link(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
        link(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
        link(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)
        // ноги
        link(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE)
        link(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)
        link(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE)
        link(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)

        for ((_, p) in points) c.drawCircle(p.x, p.y, 6f, pt)
    }
}