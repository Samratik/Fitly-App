package com.example.fitinmotion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.example.fitinmotion.PoseIdx

class PoseOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val pt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 8f
        color = 0xFFFFFFFF.toInt()
    }
    private val ln = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = 0x80FFFFFF.toInt()
    }

    @Volatile
    private var points: Map<Int, PointF> = emptyMap()

    fun updateLandmarks(map: Map<Int, PointF>) {
        points = map
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

        // Туловище
        link(PoseIdx.LEFT_SHOULDER, PoseIdx.RIGHT_SHOULDER)
        link(PoseIdx.LEFT_HIP, PoseIdx.RIGHT_HIP)
        link(PoseIdx.LEFT_SHOULDER, PoseIdx.LEFT_HIP)
        link(PoseIdx.RIGHT_SHOULDER, PoseIdx.RIGHT_HIP)

        // Руки
        link(PoseIdx.LEFT_SHOULDER, PoseIdx.LEFT_ELBOW)
        link(PoseIdx.LEFT_ELBOW, PoseIdx.LEFT_WRIST)
        link(PoseIdx.RIGHT_SHOULDER, PoseIdx.RIGHT_ELBOW)
        link(PoseIdx.RIGHT_ELBOW, PoseIdx.RIGHT_WRIST)

        // Ноги
        link(PoseIdx.LEFT_HIP, PoseIdx.LEFT_KNEE)
        link(PoseIdx.LEFT_KNEE, PoseIdx.LEFT_ANKLE)
        link(PoseIdx.RIGHT_HIP, PoseIdx.RIGHT_KNEE)
        link(PoseIdx.RIGHT_KNEE, PoseIdx.RIGHT_ANKLE)

        // Точки
        for ((_, p) in points) c.drawCircle(p.x, p.y, 6f, pt)
    }
}
