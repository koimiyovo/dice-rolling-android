package com.kyovo.dicerolling.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.WeightedDice
import com.kyovo.dicerolling.domain.ports.primary.ContinuousRoller
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** A roll nobody stops by hand is stopped after this long, so the die never spins forever. */
val DEFAULT_MAX_ROLLING_DURATION: Duration = 1.minutes

class DiceViewModel(
    private val continuousRoller: ContinuousRoller,
    private val weightedDice: WeightedDice,
    private val maxRollingDuration: Duration = DEFAULT_MAX_ROLLING_DURATION
) : ViewModel() {

    private val _currentFace = MutableStateFlow<Face?>(null)
    val currentFace: StateFlow<Face?> = _currentFace.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    // Holds the coroutine collecting the Flow, so it can be cancelled on stop().
    private var rollingJob: Job? = null

    // Stops the roll once maxRollingDuration has elapsed; cancelled with the roll itself.
    private var timeoutJob: Job? = null

    fun startRolling() {
        if (rollingJob != null) return // A roll is already in progress.

        _isRolling.value = true

        rollingJob = viewModelScope.launch {
            continuousRoller.continuousRoll(weightedDice, delayMillis = 100)
                .collect { face -> _currentFace.value = face }
        }

        timeoutJob = viewModelScope.launch {
            delay(maxRollingDuration)
            stopRolling()
        }
    }

    fun stopRolling() {
        // Cancelling the Job stops collection, which cancels the Flow's
        // coroutine, which stops the `while (true)` loop inside it.
        // _currentFace already holds the last collected face.
        rollingJob?.cancel()
        rollingJob = null
        timeoutJob?.cancel()
        timeoutJob = null
        _isRolling.value = false
    }

    override fun onCleared() {
        // Safety net: if the ViewModel is destroyed while rolling,
        // viewModelScope is cancelled automatically anyway, but this
        // keeps state consistent if onCleared is reached some other way.
        rollingJob?.cancel()
        timeoutJob?.cancel()
    }
}
