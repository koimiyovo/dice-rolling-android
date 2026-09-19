package com.kyovo.dicerolling.presentation.ui

import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.FaceNumber
import com.kyovo.dicerolling.domain.model.Faces
import com.kyovo.dicerolling.domain.model.Probability
import com.kyovo.dicerolling.domain.model.WeightedDice
import com.kyovo.dicerolling.domain.ports.primary.ContinuousRoller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DiceViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val roller = FakeContinuousRoller()
    private val viewModel = DiceViewModel(roller, dice(), maxRollingDuration = 60.seconds)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        viewModel.stopRolling()
        Dispatchers.resetMain()
    }

    @Test
    fun `is rolling once started`() = runTest(dispatcher) {
        viewModel.startRolling()
        runCurrent()

        assertThat(viewModel.isRolling.value).isTrue()
        assertThat(viewModel.currentFace.value).isNotNull()
    }

    @Test
    fun `stops by itself once the maximum duration has elapsed`() = runTest(dispatcher) {
        viewModel.startRolling()
        runCurrent()

        advanceTimeBy(59.seconds)
        runCurrent()
        assertThat(viewModel.isRolling.value).isTrue()

        advanceTimeBy(1.seconds)
        runCurrent()
        assertThat(viewModel.isRolling.value).isFalse()
    }

    @Test
    fun `keeps the last face when it stops by itself`() = runTest(dispatcher) {
        viewModel.startRolling()
        advanceTimeBy(60.seconds)
        runCurrent()

        assertThat(viewModel.isRolling.value).isFalse()
        assertThat(viewModel.currentFace.value).isNotNull()
    }

    @Test
    fun `a manual stop cancels the timeout of that roll`() = runTest(dispatcher) {
        viewModel.startRolling()
        advanceTimeBy(30.seconds)
        viewModel.stopRolling()

        viewModel.startRolling()
        runCurrent()
        advanceTimeBy(45.seconds) // 75 seconds after the first start, but only 45 after the second.
        runCurrent()

        assertThat(viewModel.isRolling.value).isTrue()
    }

    @Test
    fun `a roll started after a stop gets the full duration`() = runTest(dispatcher) {
        viewModel.startRolling()
        advanceTimeBy(30.seconds)
        viewModel.stopRolling()

        viewModel.startRolling()
        runCurrent()
        advanceTimeBy(59.seconds)
        runCurrent()
        assertThat(viewModel.isRolling.value).isTrue()

        advanceTimeBy(1.seconds)
        runCurrent()
        assertThat(viewModel.isRolling.value).isFalse()
    }

    @Test
    fun `starting twice does not start a second roll`() = runTest(dispatcher) {
        viewModel.startRolling()
        viewModel.startRolling()
        runCurrent()

        assertThat(roller.collections).isEqualTo(1)
    }

    @Test
    fun `stops rolling on demand`() = runTest(dispatcher) {
        viewModel.startRolling()
        runCurrent()

        viewModel.stopRolling()

        assertThat(viewModel.isRolling.value).isFalse()
    }

    private class FakeContinuousRoller : ContinuousRoller {
        var collections = 0
            private set

        override fun continuousRoll(weightedDice: WeightedDice, delayMillis: Long): Flow<Face> = flow {
            collections++
            while (true) {
                emit(weightedDice.faces.first())
                delay(delayMillis)
            }
        }
    }

    private companion object {
        fun face(number: Int, probability: Int) = Face(FaceNumber(number), Probability(probability))

        fun dice() = WeightedDice(
            Faces(face(1, 10), face(2, 20), face(3, 30), face(4, 15), face(5, 15), face(6, 10))
        )
    }
}
