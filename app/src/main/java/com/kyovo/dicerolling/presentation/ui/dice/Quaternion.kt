package com.kyovo.dicerolling.presentation.ui.dice

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun times(scale: Float) = Vector3(x * scale, y * scale, z * scale)

    fun dot(other: Vector3) = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3) = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun normalized(): Vector3 {
        val length = sqrt(dot(this))
        return if (length == 0f) this else this * (1f / length)
    }
}

/** Unit quaternion describing a 3D rotation. `a * b` applies `b` first, then `a`. */
data class Quaternion(val w: Float, val x: Float, val y: Float, val z: Float) {

    operator fun times(other: Quaternion) = Quaternion(
        w * other.w - x * other.x - y * other.y - z * other.z,
        w * other.x + x * other.w + y * other.z - z * other.y,
        w * other.y - x * other.z + y * other.w + z * other.x,
        w * other.z + x * other.y - y * other.x + z * other.w
    )

    fun inverse() = Quaternion(w, -x, -y, -z)

    fun dot(other: Quaternion) = w * other.w + x * other.x + y * other.y + z * other.z

    /** Renormalizes, to stop rounding errors from accumulating over many successive rotations. */
    fun normalized(): Quaternion {
        val length = sqrt(dot(this))
        return Quaternion(w / length, x / length, y / length, z / length)
    }

    fun rotate(vector: Vector3): Vector3 {
        val axis = Vector3(x, y, z)
        val t = axis.cross(vector) * 2f
        return vector + t * w + axis.cross(t)
    }

    /** Axis and angle (in radians, within 0..π) of the shortest rotation equivalent to this one. */
    fun toAxisAngle(): Pair<Vector3, Float> {
        val sign = if (w < 0f) -1f else 1f
        val cosHalf = (w * sign).coerceIn(-1f, 1f)
        val sinHalf = sqrt(1f - cosHalf * cosHalf)
        val axis = if (sinHalf < 1e-4f) {
            Vector3(0f, 1f, 0f)
        } else {
            Vector3(x * sign / sinHalf, y * sign / sinHalf, z * sign / sinHalf)
        }
        return axis to 2f * acos(cosHalf)
    }

    companion object {
        val Identity = Quaternion(1f, 0f, 0f, 0f)

        fun fromAxisAngle(axis: Vector3, angleRadians: Float): Quaternion {
            val unitAxis = axis.normalized()
            val half = angleRadians / 2f
            val s = sin(half)
            return Quaternion(cos(half), unitAxis.x * s, unitAxis.y * s, unitAxis.z * s)
        }
    }
}
