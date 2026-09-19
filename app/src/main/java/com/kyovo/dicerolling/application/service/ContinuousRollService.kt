package com.kyovo.dicerolling.application.service

import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.WeightedDice
import com.kyovo.dicerolling.domain.ports.primary.ContinuousRoller
import com.kyovo.dicerolling.domain.ports.primary.Roller
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

class ContinuousRollService(private val roller: Roller) : ContinuousRoller {

    override fun continuousRoll(weightedDice: WeightedDice, delayMillis: Long): Flow<Face> = flow {
        while (true) {
            emit(roller.roll(weightedDice))
            delay(delayMillis.milliseconds)
        }
    }
}