package com.example.fitinmotion

import android.graphics.PointF
import com.example.fitinmotion.Angles
import com.example.fitinmotion.PoseIdx

class SquatCounterMP(
    private val downThresh: Float = 80f,   // низ: колено ≤ 80°
    private val upThresh: Float = 165f,    // верх: колено ≥ 165°
    private val stableN: Int = 3
) {
    enum class State { TOP, DESC, BOTTOM, ASC }
    var state = State.TOP; private set
    var reps = 0; private set
    private var stable = 0
    private var last: Map<Int, PointF>? = null

    fun onLandmarks(lm: Map<Int, PointF>) {
        val used = if (lm.isNotEmpty()) { last = lm; lm } else last ?: return

        fun p(i:Int) = used[i]
        val lh = p(PoseIdx.LEFT_HIP); val lk = p(PoseIdx.LEFT_KNEE); val la = p(PoseIdx.LEFT_ANKLE)
        val rh = p(PoseIdx.RIGHT_HIP); val rk = p(PoseIdx.RIGHT_KNEE); val ra = p(PoseIdx.RIGHT_ANKLE)
        if (lh==null||lk==null||la==null||rh==null||rk==null||ra==null) return

        val left  = Angles.angle(lh.x, lh.y, lk.x, lk.y, la.x, la.y)
        val right = Angles.angle(rh.x, rh.y, rk.x, rk.y, ra.x, ra.y)
        val knee  = minOf(left, right)

        fun goIf(cond:Boolean, next:()->Unit){
            if (cond) { stable++; if (stable>=stableN) { next(); stable=0 } }
            else stable=0
        }

        when (state) {
            State.TOP    -> goIf(knee < 150f)          { state = State.DESC }
            State.DESC   -> goIf(knee <= downThresh)   { state = State.BOTTOM }
            State.BOTTOM -> goIf(knee > 120f)          { state = State.ASC }
            State.ASC    -> goIf(knee >= upThresh)     { reps++; state = State.TOP }
        }
    }

    fun reset(){ state = State.TOP; reps = 0; stable = 0; last = null }
}