package com.kyovo.dicerolling.presentation.ui.dice

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Test
import kotlin.math.abs

class DiceOrientationTest {

    private val delta = 1e-4f

    private fun assertVectorEquals(actual: Vector3, expected: Vector3) {
        assertThat(actual.x).isCloseTo(expected.x, within(delta))
        assertThat(actual.y).isCloseTo(expected.y, within(delta))
        assertThat(actual.z).isCloseTo(expected.z, within(delta))
    }

    @Test
    fun `facing turns each face towards the camera, upright`() {
        for (face in DieFace.entries) {
            val rotate = face.facing::rotate

            assertVectorEquals(rotate(face.normal), Vector3(0f, 0f, 1f))
            assertVectorEquals(rotate(face.u), Vector3(1f, 0f, 0f))
            assertVectorEquals(rotate(face.v), Vector3(0f, 1f, 0f))
        }
    }

    @Test
    fun `local axes of every face are right-handed`() {
        for (face in DieFace.entries) {
            assertVectorEquals(face.u.cross(face.v), face.normal)
        }
    }

    @Test
    fun `opposite faces add up to seven`() {
        for (face in DieFace.entries) {
            val opposite = DieFace.entries.first { it.normal.dot(face.normal) < -0.5f }

            assertThat(face.value + opposite.value).isEqualTo(7)
        }
    }

    @Test
    fun `each face has as many pips as its value`() {
        for (face in DieFace.entries) {
            assertThat(face.pips).hasSize(face.value)
        }
    }

    @Test
    fun `resting orientation shows the requested face most towards the camera`() {
        for (face in DieFace.entries) {
            val facingCamera = DieFace.entries.maxBy {
                DiceOrientation.resting(face).rotate(it.normal).z
            }

            assertThat(facingCamera).isEqualTo(face)
        }
    }

    @Test
    fun `closest resting orientation is the current one when already at rest`() {
        val current = DiceOrientation.resting(DieFace.FOUR, quarterTurns = 2)

        val closest = DiceOrientation.closestResting(DieFace.FOUR, current)

        // A quaternion and its opposite describe the same rotation.
        assertThat(abs(closest.dot(current))).isGreaterThan(0.9999f)
    }

    @Test
    fun `axis angle round trip rebuilds the same rotation`() {
        val original = Quaternion.fromAxisAngle(Vector3(1f, 2f, 3f), 1.2f)

        val (axis, angle) = original.toAxisAngle()
        val rebuilt = Quaternion.fromAxisAngle(axis, angle)

        assertVectorEquals(
            rebuilt.rotate(Vector3(1f, 0f, 0f)),
            original.rotate(Vector3(1f, 0f, 0f))
        )
        assertVectorEquals(
            rebuilt.rotate(Vector3(0f, 0f, 1f)),
            original.rotate(Vector3(0f, 0f, 1f))
        )
    }
}
