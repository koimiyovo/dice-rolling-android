package com.kyovo.dicerolling.infrastructure.adapters

import com.kyovo.dicerolling.domain.model.Roll
import com.kyovo.dicerolling.domain.ports.secondary.RollGenerator
import kotlin.random.Random

class DefaultRollGenerator : RollGenerator {
    override fun next(from: Int, until: Int): Roll {
        return Roll(Random.nextInt(from, until))
    }
}