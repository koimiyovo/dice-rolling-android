package com.kyovo.dicerolling.domain.model

class WeightedDice(val faces: Faces) {
    init {
        val duplicated = faces
            .groupingBy { it.number }
            .eachCount()
            .filter { it.value > 1 }
            .keys

        require(duplicated.isEmpty()) {
            "Every face must have a unique value. Duplicates found: $duplicated."
        }

        val total = faces.sumOf { it.probability.value }
        require(total == 100) {
            "Sum of probabilities must be equal to 100 (currently: $total)."
        }
    }
}