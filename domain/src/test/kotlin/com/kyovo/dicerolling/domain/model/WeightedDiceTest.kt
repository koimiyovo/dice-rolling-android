package com.kyovo.dicerolling.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class WeightedDiceTest {

    private fun face(number: Int, probability: Int) = Face(FaceNumber(number), Probability(probability))

    private fun faces(p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int) = Faces(
        face(1, p1), face(2, p2), face(3, p3), face(4, p4), face(5, p5), face(6, p6)
    )

    @Test
    fun `accepts faces with unique numbers whose probabilities add up to 100`() {
        assertThatCode { WeightedDice(faces(10, 20, 30, 15, 15, 10)) }.doesNotThrowAnyException()
    }

    @Test
    fun `accepts a fair die`() {
        assertThatCode { WeightedDice(faces(17, 17, 17, 17, 16, 16)) }.doesNotThrowAnyException()
    }

    @Test
    fun `accepts faces with a probability of 0`() {
        assertThatCode { WeightedDice(faces(0, 0, 0, 0, 0, 100)) }.doesNotThrowAnyException()
    }

    @Test
    fun `keeps the faces it was built with`() {
        val faces = faces(10, 20, 30, 15, 15, 10)

        assertThat(WeightedDice(faces).faces).isEqualTo(faces)
    }

    @Test
    fun `rejects probabilities adding up to less than 100`() {
        assertThatThrownBy { WeightedDice(faces(10, 20, 30, 15, 15, 9)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("currently: 99")
    }

    @Test
    fun `rejects probabilities adding up to more than 100`() {
        assertThatThrownBy { WeightedDice(faces(10, 20, 30, 15, 15, 11)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("currently: 101")
    }

    @Test
    fun `rejects two faces with the same number`() {
        val faces = Faces(
            face(1, 10), face(2, 20), face(3, 30), face(4, 15), face(5, 15), face(3, 10)
        )

        assertThatThrownBy { WeightedDice(faces) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("unique")
            .hasMessageContaining("FaceNumber(value=3)")
    }

    @Test
    fun `reports every duplicated number`() {
        val faces = Faces(
            face(1, 10), face(1, 20), face(2, 30), face(2, 15), face(3, 15), face(4, 10)
        )

        assertThatThrownBy { WeightedDice(faces) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("FaceNumber(value=1)")
            .hasMessageContaining("FaceNumber(value=2)")
            .hasMessageNotContaining("FaceNumber(value=3)")
    }

    @Test
    fun `checks duplicates before the sum of probabilities`() {
        // Both rules are broken here: the duplicate is the one reported.
        val faces = Faces(
            face(1, 50), face(1, 50), face(2, 50), face(3, 0), face(4, 0), face(5, 0)
        )

        assertThatThrownBy { WeightedDice(faces) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("unique")
    }
}
