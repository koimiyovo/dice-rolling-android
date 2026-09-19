package com.kyovo.dicerolling.application.service

import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.WeightedDice
import com.kyovo.dicerolling.domain.ports.primary.Roller
import com.kyovo.dicerolling.domain.ports.secondary.RollGenerator

class RollService(private val rollGenerator: RollGenerator) : Roller {

    override fun roll(weightedDice: WeightedDice): Face {
        val roll = rollGenerator.next(0, 100)
        var accumulation = 0

        for (face in weightedDice.faces) {
            accumulation += face.probability.value
            if (roll.value < accumulation) {
                return face
            }
        }

        // This point should never be reached: the sum of probabilities
        // is 100 and the roll is always strictly less than 100,
        // so the last face will always trigger the return above.
        error("Internal error: no face was selected for the roll $roll.")
    }
}