package com.kyovo.dicerolling.presentation.ui.dice

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Test
import kotlin.math.sqrt

class RoundedDieMeshTest {

    private val radius = 0.2f
    private val arcSteps = 3
    private val mesh = RoundedDieMesh(radius, arcSteps)
    private val delta = 1e-4f

    private val vertices = mesh.faces.flatMap { it.grid.flatten() }

    @Test
    fun `every face has a grid of the same size`() {
        val gridSize = 2 * (arcSteps + 1)

        for (face in mesh.faces) {
            assertThat(face.grid).hasSize(gridSize)
            assertThat(face.grid).allSatisfy { row -> assertThat(row).hasSize(gridSize) }
        }
    }

    @Test
    fun `every normal is a unit vector`() {
        for (vertex in vertices) {
            assertThat(sqrt(vertex.normal.dot(vertex.normal))).isCloseTo(1f, within(delta))
        }
    }

    @Test
    fun `the die never goes past the unit cube`() {
        for (vertex in vertices) {
            val p = vertex.position

            assertThat(listOf(p.x, p.y, p.z)).allSatisfy { coordinate ->
                assertThat(coordinate).isBetween(-1f - delta, 1f + delta)
            }
        }
    }

    @Test
    fun `the middle of each face is flat and sits on the cube`() {
        for (face in mesh.faces) {
            val flat = face.grid[arcSteps][arcSteps]

            assertThat(flat.position.dot(face.face.normal)).isCloseTo(1f, within(delta))
            assertThat(flat.normal.dot(face.face.normal)).isCloseTo(1f, within(delta))
        }
    }

    @Test
    fun `corners are cut off compared to a sharp cube`() {
        val sharpCorner = sqrt(3f)
        val farthest = vertices.maxOf { sqrt(it.position.dot(it.position)) }

        assertThat(farthest).isLessThan(sharpCorner - 0.1f)
    }

    @Test
    fun `each vertex sits exactly one radius from the flat core`() {
        val inner = 1f - radius

        for (vertex in vertices) {
            val p = vertex.position
            val core = Vector3(p.x.coerceIn(-inner, inner), p.y.coerceIn(-inner, inner), p.z.coerceIn(-inner, inner))
            val gap = p + core * -1f

            assertThat(sqrt(gap.dot(gap))).isCloseTo(radius, within(delta))
        }
    }
}
