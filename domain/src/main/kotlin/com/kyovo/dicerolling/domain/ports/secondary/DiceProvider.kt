package com.kyovo.dicerolling.domain.ports.secondary

import com.kyovo.dicerolling.domain.model.WeightedDice

interface DiceProvider {
    fun dice(): WeightedDice
}
