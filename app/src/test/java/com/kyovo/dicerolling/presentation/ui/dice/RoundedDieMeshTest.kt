package com.kyovo.dicerolling.presentation.ui.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class RoundedDieMeshTest {

    private val radius = 0.2f
    private val arcSteps = 3
    private val mesh = RoundedDieMesh(radius, arcSteps)
    private val delta = 1e-4f

    private val vertices = mesh.faces.flatMap { it.grid.flatten() }

    @Test
    fun `every face has a grid of the same size`() {
        for (face in mesh.faces) {
            assertEquals(2 * (arcSteps + 1), face.grid.size)
            assertTrue(face.grid.all { it.size == face.grid.size })
        }
    }

    @Test
    fun `every normal is a unit vector`() {
        for (vertex in vertices) {
            assertEquals(1f, sqrt(vertex.normal.dot(vertex.normal)), delta)
        }
    }

    @Test
    fun `the die never goes past the unit cube`() {
        for (vertex in vertices) {
            val p = vertex.position
            assertTrue(abs(p.x) <= 1f + delta && abs(p.y) <= 1f + delta && abs(p.z) <= 1f + delta)
        }
    }

    @Test
    fun `the middle of each face is flat and sits on the cube`() {
        for (face in mesh.faces) {
            val flat = face.grid[arcSteps][arcSteps]

            assertEquals(1f, flat.position.dot(face.face.normal), delta)
            assertEquals(1f, flat.normal.dot(face.face.normal), delta)
        }
    }

    @Test
    fun `corners are cut off compared to a sharp cube`() {
        val sharpCorner = sqrt(3f)
        val farthest = vertices.maxOf { sqrt(it.position.dot(it.position)) }

        assertTrue(farthest < sharpCorner - 0.1f)
    }

    @Test
    fun `each vertex sits exactly one radius from the flat core`() {
        val inner = 1f - radius

        for (vertex in vertices) {
            val p = vertex.position
            val core = Vector3(p.x.coerceIn(-inner, inner), p.y.coerceIn(-inner, inner), p.z.coerceIn(-inner, inner))
            val gap = p + core * -1f

            assertEquals(radius, sqrt(gap.dot(gap)), delta)
        }
    }
}
