package com.kyovo.dicerolling.infrastructure.adapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DefaultDiceProviderTest {

    private val dice = DefaultDiceProvider().dice()

    @Test
    fun `provides a die with six faces numbered 1 to 6`() {
        assertThat(dice.faces.map { it.number.value }).containsExactly(1, 2, 3, 4, 5, 6)
    }

    @Test
    fun `provides a die whose probabilities add up to 100`() {
        assertThat(dice.faces.sumOf { it.probability.value }).isEqualTo(100)
    }
}
