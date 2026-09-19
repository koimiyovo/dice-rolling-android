package com.kyovo.dicerolling.infrastructure.adapters

import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.FaceNumber
import com.kyovo.dicerolling.domain.model.Faces
import com.kyovo.dicerolling.domain.model.Probability
import com.kyovo.dicerolling.domain.model.WeightedDice
import com.kyovo.dicerolling.domain.ports.secondary.DiceProvider

class DefaultDiceProvider : DiceProvider {
    override fun dice(): WeightedDice {
        return WeightedDice(
            Faces(
                face1 = Face(FaceNumber(1), Probability(10)),
                face2 = Face(FaceNumber(2), Probability(20)),
                face3 = Face(FaceNumber(3), Probability(30)),
                face4 = Face(FaceNumber(4), Probability(15)),
                face5 = Face(FaceNumber(5), Probability(15)),
                face6 = Face(FaceNumber(6), Probability(10))
            )
        )
    }
}
