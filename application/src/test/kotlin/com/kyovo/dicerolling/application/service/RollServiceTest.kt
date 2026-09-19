package com.kyovo.dicerolling.application.service

import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.FaceNumber
import com.kyovo.dicerolling.domain.model.Faces
import com.kyovo.dicerolling.domain.model.Probability
import com.kyovo.dicerolling.domain.model.Roll
import com.kyovo.dicerolling.domain.model.WeightedDice
import com.kyovo.dicerolling.domain.ports.secondary.RollGenerator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class RollServiceTest {

    /** Generator returning the given values in order, and remembering the bounds it was asked for. */
    private class FakeRollGenerator(vararg values: Int) : RollGenerator {
        private val remaining = values.toMutableList()
        val requestedBounds = mutableListOf<Pair<Int, Int>>()

        override fun next(from: Int, until: Int): Roll {
            requestedBounds += from to until
            return Roll(remaining.removeFirst())
        }
    }

    private fun diceOf(p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int): WeightedDice {
        fun face(number: Int, probability: Int) = Face(FaceNumber(number), Probability(probability))
        return WeightedDice(
            Faces(face(1, p1), face(2, p2), face(3, p3), face(4, p4), face(5, p5), face(6, p6))
        )
    }

    // Cumulative probabilities: 10, 30, 60, 75, 90, 100.
    private val dice = diceOf(10, 20, 30, 15, 15, 10)

    private fun rollFor(value: Int, weightedDice: WeightedDice = dice): Face =
        RollService(FakeRollGenerator(value)).roll(weightedDice)

    @Test
    fun `asks the generator for a value between 0 and 100`() {
        val generator = FakeRollGenerator(0)

        RollService(generator).roll(dice)

        assertThat(generator.requestedBounds).containsExactly(0 to 100)
    }

    @Test
    fun `asks the generator once per roll`() {
        val generator = FakeRollGenerator(0, 50, 99)
        val service = RollService(generator)

        repeat(3) { service.roll(dice) }

        assertThat(generator.requestedBounds).hasSize(3)
    }

    @Test
    fun `returns the face itself, with its probability`() {
        assertThat(rollFor(0)).isEqualTo(Face(FaceNumber(1), Probability(10)))
    }

    @Test
    fun `returns the face whose range contains the roll`() {
        // Every boundary of the ranges, on both sides: [0, 10) [10, 30) [30, 60) [60, 75) [75, 90) [90, 100).
        val expected = mapOf(
            0 to 1, 9 to 1,
            10 to 2, 29 to 2,
            30 to 3, 59 to 3,
            60 to 4, 74 to 4,
            75 to 5, 89 to 5,
            90 to 6, 99 to 6
        )

        for ((roll, faceNumber) in expected) {
            assertThat(rollFor(roll).number.value)
                .describedAs("face for a roll of $roll")
                .isEqualTo(faceNumber)
        }
    }

    @Test
    fun `never returns a face with a probability of 0`() {
        val dice = diceOf(0, 50, 0, 50, 0, 0)

        val faceNumbers = (0 until 100).map { rollFor(it, dice).number.value }.toSet()

        assertThat(faceNumbers).containsExactlyInAnyOrder(2, 4)
    }

    @Test
    fun `returns the only possible face of a certain die`() {
        val dice = diceOf(0, 0, 0, 0, 0, 100)

        assertThat((0 until 100).map { rollFor(it, dice).number.value }.toSet()).containsExactly(6)
    }

    @Test
    fun `picks each face as often as its probability over every possible roll`() {
        val counts = (0 until 100).groupingBy { rollFor(it).number.value }.eachCount()

        assertThat(counts).containsExactlyInAnyOrderEntriesOf(
            mapOf(1 to 10, 2 to 20, 3 to 30, 4 to 15, 5 to 15, 6 to 10)
        )
    }

    @Test
    fun `fails when the generator breaks its contract with a roll out of range`() {
        assertThatThrownBy { rollFor(100) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("no face was selected")
    }
}
