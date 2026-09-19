package com.kyovo.dicerolling.domain.ports.secondary

import com.kyovo.dicerolling.domain.model.Roll

interface RollGenerator {
    fun next(from: Int, until: Int): Roll
}