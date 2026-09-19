package com.kyovo.dicerolling.application.service

import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.FaceNumber
import com.kyovo.dicerolling.domain.model.Faces
import com.kyovo.dicerolling.domain.model.Probability
import com.kyovo.dicerolling.domain.model.WeightedDice
import com.kyovo.dicerolling.domain.ports.primary.Roller
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ContinuousRollServiceTest {

    private fun face(number: Int) =
        Face(FaceNumber(number), Probability(if (number == 6) 50 else 10))

    private val dice = WeightedDice(
        Faces(face(1), face(2), face(3), face(4), face(5), face(6))
    )

    /** Roller returning face 1, then 2, then 3… and remembering the dice it was given. */
    private class FakeRoller : Roller {
        val receivedDice = mutableListOf<WeightedDice>()

        override fun roll(weightedDice: WeightedDice): Face {
            receivedDice += weightedDice
            return weightedDice.faces.elementAt((receivedDice.size - 1) % 6)
        }
    }

    @Test
    fun `emits the faces rolled by the roller, in order`() = runTest {
        val service = ContinuousRollService(FakeRoller())

        val faces = service.continuousRoll(dice, delayMillis = 100).take(4).toList()

        assertThat(faces.map { it.number.value }).containsExactly(1, 2, 3, 4)
    }

    @Test
    fun `rolls the dice it was given`() = runTest {
        val roller = FakeRoller()

        ContinuousRollService(roller).continuousRoll(dice, delayMillis = 100).take(3).toList()

        assertThat(roller.receivedDice).hasSize(3).allSatisfy { assertThat(it).isSameAs(dice) }
    }

    @Test
    fun `rolls right away, then once per delay`() = runTest {
        val service = ContinuousRollService(FakeRoller())

        val emissionTimes = service.continuousRoll(dice, delayMillis = 100)
            .map { currentTime }
            .take(3)
            .toList()

        assertThat(emissionTimes).containsExactly(0L, 100L, 200L)
    }

    @Test
    fun `waits as long as the delay it was given`() = runTest {
        val service = ContinuousRollService(FakeRoller())

        val emissionTimes = service.continuousRoll(dice, delayMillis = 250)
            .map { currentTime }
            .take(3)
            .toList()

        assertThat(emissionTimes).containsExactly(0L, 250L, 500L)
    }

    @Test
    fun `does not roll until the flow is collected`() = runTest {
        val roller = FakeRoller()

        val flow = ContinuousRollService(roller).continuousRoll(dice, delayMillis = 100)

        assertThat(roller.receivedDice).isEmpty()

        flow.take(1).toList()

        assertThat(roller.receivedDice).hasSize(1)
    }

    @Test
    fun `rolls again for every new collection`() = runTest {
        val roller = FakeRoller()
        val flow = ContinuousRollService(roller).continuousRoll(dice, delayMillis = 100)

        flow.take(2).toList()
        flow.take(2).toList()

        assertThat(roller.receivedDice).hasSize(4)
    }

    @Test
    fun `keeps rolling until the collection is cancelled`() = runTest {
        val roller = FakeRoller()
        val collection = launch {
            ContinuousRollService(roller).continuousRoll(dice, delayMillis = 100).collect { }
        }

        // Rolls at 0, 100 and 200 ms.
        advanceTimeBy(250.milliseconds)
        runCurrent()
        assertThat(roller.receivedDice).hasSize(3)

        collection.cancel()
        advanceTimeBy(1_000.milliseconds)
        runCurrent()

        assertThat(roller.receivedDice).hasSize(3)
    }
}
