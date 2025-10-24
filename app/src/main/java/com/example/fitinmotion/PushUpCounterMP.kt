package com.example.fitinmotion

import android.graphics.PointF
import com.example.fitinmotion.Angles
import com.example.fitinmotion.PoseIdx

class PushUpCounterMP(
    private val downThresh: Float = 75f,   // низ: локоть ≤ 75°
    private val upThresh: Float = 165f,    // верх: локоть ≥ 165°
    private val stableN: Int = 3           // защита от дрожания: N кадров подряд
) {
    enum class State { TOP, DESC, BOTTOM, ASC }
    var state: State = State.TOP; private set
    var reps: Int = 0; private set

    private var stable = 0
    private var lastLandmarks: Map<Int, PointF>? = null

    fun onLandmarks(lm: Map<Int, PointF>) {
        val used = if (lm.isNotEmpty()) { lastLandmarks = lm; lm }
        else lastLandmarks ?: return

        // Достаём нужные точки
        fun p(i: Int) = used[i]

        val ls = p(PoseIdx.LEFT_SHOULDER)
        val le = p(PoseIdx.LEFT_ELBOW)
        val lw = p(PoseIdx.LEFT_WRIST)
        val rs = p(PoseIdx.RIGHT_SHOULDER)
        val re = p(PoseIdx.RIGHT_ELBOW)
        val rw = p(PoseIdx.RIGHT_WRIST)

        if (ls==null || le==null || lw==null || rs==null || re==null || rw==null) return

        // Углы в локтях
        val lElbow = Angles.angle(ls.x, ls.y, le.x, le.y, lw.x, lw.y)
        val rElbow = Angles.angle(rs.x, rs.y, re.x, re.y, rw.x, rw.y)
        val elbow  = minOf(lElbow, rElbow) // берём более согнутую руку

        fun goIf(cond: Boolean, next: () -> Unit) {
            if (cond) { stable++; if (stable >= stableN) { next(); stable = 0 } }
            else stable = 0
        }

        when (state) {
            State.TOP    -> goIf(elbow < 140f)          { state = State.DESC }
            State.DESC   -> goIf(elbow <= downThresh)   { state = State.BOTTOM }
            State.BOTTOM -> goIf(elbow > 110f)          { state = State.ASC }
            State.ASC    -> goIf(elbow >= upThresh)     { reps++; state = State.TOP }
        }
    }

    fun reset() { state = State.TOP; reps = 0; stable = 0; lastLandmarks = null }
}