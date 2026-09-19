package com.kyovo.dicerolling.domain.ports.primary

import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.WeightedDice
import kotlinx.coroutines.flow.Flow

interface ContinuousRoller {
    fun continuousRoll(weightedDice: WeightedDice, delayMillis: Long): Flow<Face>
}