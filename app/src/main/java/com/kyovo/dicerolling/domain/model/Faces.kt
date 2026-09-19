package com.kyovo.dicerolling.domain.model

data class Faces(
    val face1: Face,
    val face2: Face,
    val face3: Face,
    val face4: Face,
    val face5: Face,
    val face6: Face
) : Iterable<Face> {
    override fun iterator(): Iterator<Face> {
        return listOf(face1, face2, face3, face4, face5, face6).iterator()
    }
}