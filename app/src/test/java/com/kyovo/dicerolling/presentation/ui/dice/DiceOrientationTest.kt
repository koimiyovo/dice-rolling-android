package com.kyovo.dicerolling.presentation.ui.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceOrientationTest {

    private val delta = 1e-4f

    private fun assertVectorEquals(expected: Vector3, actual: Vector3) {
        assertEquals(expected.x, actual.x, delta)
        assertEquals(expected.y, actual.y, delta)
        assertEquals(expected.z, actual.z, delta)
    }

    @Test
    fun `facing turns each face towards the camera, upright`() {
        for (face in DieFace.entries) {
            val rotate = face.facing::rotate

            assertVectorEquals(Vector3(0f, 0f, 1f), rotate(face.normal))
            assertVectorEquals(Vector3(1f, 0f, 0f), rotate(face.u))
            assertVectorEquals(Vector3(0f, 1f, 0f), rotate(face.v))
        }
    }

    @Test
    fun `local axes of every face are right-handed`() {
        for (face in DieFace.entries) {
            assertVectorEquals(face.normal, face.u.cross(face.v))
        }
    }

    @Test
    fun `opposite faces add up to seven`() {
        for (face in DieFace.entries) {
            val opposite = DieFace.entries.first { it.normal.dot(face.normal) < -0.5f }
            assertEquals(7, face.value + opposite.value)
        }
    }

    @Test
    fun `each face has as many pips as its value`() {
        for (face in DieFace.entries) {
            assertEquals(face.value, face.pips.size)
        }
    }

    @Test
    fun `resting orientation shows the requested face most towards the camera`() {
        for (face in DieFace.entries) {
            val facingCamera = DieFace.entries.maxBy {
                DiceOrientation.resting(face).rotate(it.normal).z
            }

            assertEquals(face, facingCamera)
        }
    }

    @Test
    fun `closest resting orientation is the current one when already at rest`() {
        val current = DiceOrientation.resting(DieFace.FOUR, quarterTurns = 2)

        val closest = DiceOrientation.closestResting(DieFace.FOUR, current)

        assertTrue(closest.dot(current) > 0.9999f || closest.dot(current) < -0.9999f)
    }

    @Test
    fun `axis angle round trip rebuilds the same rotation`() {
        val original = Quaternion.fromAxisAngle(Vector3(1f, 2f, 3f), 1.2f)

        val (axis, angle) = original.toAxisAngle()
        val rebuilt = Quaternion.fromAxisAngle(axis, angle)

        assertVectorEquals(original.rotate(Vector3(1f, 0f, 0f)), rebuilt.rotate(Vector3(1f, 0f, 0f)))
        assertVectorEquals(original.rotate(Vector3(0f, 0f, 1f)), rebuilt.rotate(Vector3(0f, 0f, 1f)))
    }
}
