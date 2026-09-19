package com.kyovo.dicerolling.domain.ports.primary

import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.WeightedDice

interface Roller {
    fun roll(weightedDice: WeightedDice): Face
}