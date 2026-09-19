package com.kyovo.dicerolling.presentation.ui.dice

import kotlin.math.PI
import kotlin.math.abs

private val X_AXIS = Vector3(1f, 0f, 0f)
private val Y_AXIS = Vector3(0f, 1f, 0f)
private val Z_AXIS = Vector3(0f, 0f, 1f)

private fun degrees(value: Float) = value * (PI.toFloat() / 180f)

private const val PIP = 0.5f

/**
 * One face of a unit cube (half-size 1) centered on the origin.
 *
 * [normal] points outwards; [u] and [v] are the face's local right and up axes
 * (`u × v == normal`) and the pips are laid out on them. [facing] rotates the die so that
 * this face looks at the camera (the +Z axis), upright. Opposite faces add up to 7.
 */
enum class DieFace(
    val value: Int,
    val normal: Vector3,
    val u: Vector3,
    val v: Vector3,
    val facing: Quaternion,
    val pips: List<Pair<Float, Float>>
) {
    ONE(
        1, Z_AXIS, X_AXIS, Y_AXIS,
        Quaternion.Identity,
        listOf(0f to 0f)
    ),
    TWO(
        2, Y_AXIS, X_AXIS, Vector3(0f, 0f, -1f),
        Quaternion.fromAxisAngle(X_AXIS, degrees(90f)),
        listOf(-PIP to PIP, PIP to -PIP)
    ),
    THREE(
        3, X_AXIS, Vector3(0f, 0f, -1f), Y_AXIS,
        Quaternion.fromAxisAngle(Y_AXIS, degrees(-90f)),
        listOf(-PIP to PIP, 0f to 0f, PIP to -PIP)
    ),
    FOUR(
        4, Vector3(-1f, 0f, 0f), Z_AXIS, Y_AXIS,
        Quaternion.fromAxisAngle(Y_AXIS, degrees(90f)),
        listOf(-PIP to -PIP, -PIP to PIP, PIP to -PIP, PIP to PIP)
    ),
    FIVE(
        5, Vector3(0f, -1f, 0f), X_AXIS, Z_AXIS,
        Quaternion.fromAxisAngle(X_AXIS, degrees(-90f)),
        listOf(-PIP to -PIP, -PIP to PIP, 0f to 0f, PIP to -PIP, PIP to PIP)
    ),
    SIX(
        6, Vector3(0f, 0f, -1f), Vector3(-1f, 0f, 0f), Y_AXIS,
        Quaternion.fromAxisAngle(Y_AXIS, degrees(180f)),
        listOf(-PIP to -PIP, -PIP to 0f, -PIP to PIP, PIP to -PIP, PIP to 0f, PIP to PIP)
    );

    companion object {
        fun of(value: Int): DieFace = entries.first { it.value == value }
    }
}

object DiceOrientation {

    // Slight tilt so the die at rest shows its top and right sides too, and not a flat square.
    private val tilt =
        Quaternion.fromAxisAngle(Y_AXIS, degrees(-20f)) * Quaternion.fromAxisAngle(X_AXIS, degrees(20f))

    /** Orientation at rest showing [face] to the camera, turned by [quarterTurns] × 90° in the screen plane. */
    fun resting(face: DieFace, quarterTurns: Int = 0): Quaternion {
        val inPlane = Quaternion.fromAxisAngle(Z_AXIS, degrees(90f * quarterTurns))
        return tilt * inPlane * face.facing
    }

    /**
     * The resting orientation of [face] that needs the least rotation from [from]. The pips of
     * every face are symmetric under a quarter turn, so any of the four is a correct result.
     */
    fun closestResting(face: DieFace, from: Quaternion): Quaternion =
        (0..3).map { resting(face, it) }.maxBy { abs(it.dot(from)) }
}
