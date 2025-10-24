package com.example.fitinmotion

import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object Angles {
    data class P(val x: Float, val y: Float)

    private fun dot(ax: Float, ay: Float, bx: Float, by: Float) = ax * bx + ay * by
    private fun len(x: Float, y: Float) = sqrt(x * x + y * y)

    /** Угол A–B–C (между BA и BC) в градусах */
    fun angle(a: P, b: P, c: P): Float {
        val bax = a.x - b.x; val bay = a.y - b.y
        val bcx = c.x - b.x; val bcy = c.y - b.y
        val cos = dot(bax, bay, bcx, bcy) / max(1e-6f, len(bax, bay) * len(bcx, bcy))
        return Math.toDegrees(acos(min(1.0, max(-1.0, cos.toDouble())))).toFloat()
    }

    /** Удобный вариант: сразу координатами */
    fun angle(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float =
        angle(P(ax, ay), P(bx, by), P(cx, cy))
}