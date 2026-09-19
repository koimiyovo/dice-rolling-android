package com.kyovo.dicerolling.domain.model

@JvmInline
value class Probability(val value: Int) {
    init {
        require(value >= 0) { "Probability cannot be negative" }
        require(value in 0..100) { "Probability must be between 0 and 100" }
    }
}