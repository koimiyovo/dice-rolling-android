package com.kyovo.dicerolling.presentation.ui.dice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val TUMBLE_RADIANS_PER_SECOND = 4f * PI.toFloat()
private const val SETTLE_MILLIS = 1100
private const val MIN_SETTLE_ANGLE = 0.01f

private const val CAMERA_DISTANCE = 8f
private const val FOCAL_LENGTH = 1.9f

// Rounding of the edges, as a fraction of half the die's width, and how many slices each rounded edge is cut in.
private const val ROUNDNESS = 0.2f
private const val ROUNDING_SLICES = 4

private const val PIP_RADIUS = 0.17f
private const val PIP_SEGMENTS = 16

private val bodyColor = Color(0xFFF7F3EA)
private val pipColor = Color(0xFF1E1E1E)
private val aceColor = Color(0xFFC62828)
private val lightDirection = Vector3(-0.35f, 0.65f, 0.68f).normalized()

private val mesh = RoundedDieMesh(ROUNDNESS, ROUNDING_SLICES)

/**
 * A 3D six-sided die. It tumbles while [isRolling] is true and, once it stops, spins down onto
 * [faceNumber] (face 1 while there is none yet).
 */
@Composable
fun DiceView(faceNumber: Int?, isRolling: Boolean, modifier: Modifier = Modifier) {
    val targetFace by rememberUpdatedState(DieFace.of(faceNumber ?: 1))
    var orientation by remember { mutableStateOf(DiceOrientation.resting(targetFace)) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            var previous = withFrameNanos { it }
            var elapsed = 0f
            while (true) {
                val now = withFrameNanos { it }
                val delta = (now - previous) / 1_000_000_000f
                previous = now
                elapsed += delta

                // The axis drifts over time so the die tumbles instead of spinning on one axis.
                val axis = Vector3(sin(elapsed * 1.7f), cos(elapsed * 1.1f), sin(elapsed * 0.6f) + 0.6f)
                orientation = (Quaternion.fromAxisAngle(axis, TUMBLE_RADIANS_PER_SECOND * delta) * orientation)
                    .normalized()
            }
        } else {
            val start = orientation
            val target = DiceOrientation.closestResting(targetFace, start)
            val (axis, angle) = (target * start.inverse()).toAxisAngle()
            if (angle > MIN_SETTLE_ANGLE) {
                // One extra full turn on top of the shortest path, so the die visibly slows
                // down while still spinning rather than just easing over to the final face.
                val totalAngle = angle + 2f * PI.toFloat()
                Animatable(0f).animateTo(1f, tween(SETTLE_MILLIS, easing = LinearOutSlowInEasing)) {
                    orientation = (Quaternion.fromAxisAngle(axis, totalAngle * value) * start).normalized()
                }
            }
            orientation = target
        }
    }

    Canvas(modifier) {
        drawDie(orientation)
    }
}

private fun DrawScope.drawDie(orientation: Quaternion) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val focal = FOCAL_LENGTH * min(size.width, size.height)
    val camera = Vector3(0f, 0f, CAMERA_DISTANCE)

    fun project(point: Vector3): Offset {
        val scale = focal / (CAMERA_DISTANCE - point.z)
        return Offset(center.x + point.x * scale, center.y - point.y * scale)
    }

    drawShadow(center)

    // The die is convex, so hiding the patches of its surface that look away from the camera
    // is enough: the ones left never overlap.
    val patch = Path()
    for (faceMesh in mesh.faces) {
        val positions = faceMesh.grid.map { row -> row.map { orientation.rotate(it.position) } }
        val normals = faceMesh.grid.map { row -> row.map { orientation.rotate(it.normal) } }

        for (i in 0 until positions.lastIndex) {
            for (j in 0 until positions.lastIndex) {
                val corners = listOf(positions[i][j], positions[i + 1][j], positions[i + 1][j + 1], positions[i][j + 1])
                val normal = (normals[i][j] + normals[i + 1][j] + normals[i + 1][j + 1] + normals[i][j + 1]).normalized()
                val middle = (corners[0] + corners[1] + corners[2] + corners[3]) * 0.25f
                if (normal.dot(camera + middle * -1f) <= 0f) continue

                val brightness = 0.6f + 0.4f * max(0f, normal.dot(lightDirection))
                val shaded = Color(bodyColor.red * brightness, bodyColor.green * brightness, bodyColor.blue * brightness)

                patch.reset()
                corners.forEachIndexed { index, corner ->
                    val point = project(corner)
                    if (index == 0) patch.moveTo(point.x, point.y) else patch.lineTo(point.x, point.y)
                }
                patch.close()

                drawPath(patch, shaded)
                // A hairline in the same color hides the gaps anti-aliasing leaves between patches.
                drawPath(patch, shaded, style = Stroke(width = 1f))
            }
        }
    }

    for (face in DieFace.entries) {
        val normal = orientation.rotate(face.normal)
        if (normal.dot(camera + normal * -1f) <= 0f) continue

        val color = if (face == DieFace.ONE) aceColor else pipColor
        for ((a, b) in face.pips) {
            val pip = Path().apply {
                repeat(PIP_SEGMENTS) { step ->
                    val angle = 2f * PI.toFloat() * step / PIP_SEGMENTS
                    val onFace = face.normal * 1.002f +
                        face.u * (a + PIP_RADIUS * cos(angle)) +
                        face.v * (b + PIP_RADIUS * sin(angle))
                    val point = project(orientation.rotate(onFace))
                    if (step == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
                close()
            }
            drawPath(pip, color)
        }
    }
}

private fun DrawScope.drawShadow(center: Offset) {
    val extent = min(size.width, size.height)
    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(center.x - extent * 0.28f, center.y + extent * 0.44f),
        size = Size(extent * 0.56f, extent * 0.07f)
    )
}
