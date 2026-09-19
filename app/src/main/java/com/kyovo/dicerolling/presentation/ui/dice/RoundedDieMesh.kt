package com.kyovo.dicerolling.presentation.ui.dice

import kotlin.math.PI
import kotlin.math.tan

class DieVertex(val position: Vector3, val normal: Vector3)

/** The surface of one die face, as a grid of vertices: `grid[i][j]` runs along `u` then `v`. */
class DieFaceMesh(val face: DieFace, val grid: List<List<DieVertex>>)

/**
 * A cube of half-size 1 whose edges and corners are rounded off with the given [radius].
 *
 * Each face keeps a flat square in its middle, and the rest of its surface bends over the
 * neighbouring edges: quarter cylinders along the edges, eighths of a sphere at the corners.
 * Every face covers half of each rounded edge, so neighbouring faces meet at the 45° line.
 */
class RoundedDieMesh(val radius: Float, arcSteps: Int) {

    val faces: List<DieFaceMesh>

    init {
        require(radius > 0f && radius < 0.5f) { "Radius must be within 0 and 0.5 (currently: $radius)." }
        require(arcSteps >= 1) { "At least one step is needed to round an edge (currently: $arcSteps)." }

        val samples = samples(arcSteps)
        faces = DieFace.entries.map { face ->
            DieFaceMesh(face, samples.map { a -> samples.map { b -> vertex(face, a, b) } })
        }
    }

    /**
     * Coordinates along a face axis, from -1 to 1. The flat middle is a single step, and the
     * rounded parts on each side are cut at even angles, so the shading follows the curve.
     */
    private fun samples(arcSteps: Int): List<Float> {
        val inner = 1f - radius
        val negativeSide = (arcSteps downTo 0).map { step ->
            val angle = step * (PI.toFloat() / 4f) / arcSteps
            -(inner + radius * tan(angle))
        }
        return negativeSide + negativeSide.reversed().map { -it }
    }

    private fun vertex(face: DieFace, a: Float, b: Float): DieVertex {
        val inner = 1f - radius
        val flatA = a.coerceIn(-inner, inner)
        val flatB = b.coerceIn(-inner, inner)

        // The point of the flat middle closest to this spot, pushed out by `radius` along the
        // direction the surface leans towards.
        val core = face.normal * inner + face.u * flatA + face.v * flatB
        val lean = face.normal * radius + face.u * (a - flatA) + face.v * (b - flatB)
        val normal = lean.normalized()
        return DieVertex(core + normal * radius, normal)
    }
}
