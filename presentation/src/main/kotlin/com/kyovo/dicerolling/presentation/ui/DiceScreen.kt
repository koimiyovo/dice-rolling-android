package com.kyovo.dicerolling.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.kyovo.dicerolling.presentation.ui.dice.DiceView
import com.kyovo.dicerolling.presentation.ui.theme.DiceColors

private val MAX_DIE_SIZE = 280.dp
private val MAX_PORTRAIT_BUTTON_WIDTH = 400.dp
private val LANDSCAPE_BUTTON_WIDTH = 220.dp

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun DiceScreen(viewModel: DiceViewModel) {
    val currentFace by viewModel.currentFace.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val haptics = LocalHapticFeedback.current
    val activity = LocalContext.current.findActivity()

    // Leaving the app or locking the phone stops the roll. A configuration change (rotation...)
    // also stops the activity, but only to recreate it: the ViewModel survives, so keep rolling.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (activity?.isChangingConfigurations != true) viewModel.stopRolling()
    }

    val faceNumber = currentFace?.number?.value
    val onRollClick = {
        if (isRolling) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.stopRolling()
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
            viewModel.startRolling()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DiceColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        if (this.maxWidth > this.maxHeight) {
            // Landscape: the die on the left, taking the full height, the button on the right.
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Die(faceNumber, isRolling, Modifier
                    .weight(1f)
                    .fillMaxHeight())
                RollButton(isRolling, onRollClick, Modifier.width(LANDSCAPE_BUTTON_WIDTH))
            }
        } else {
            // Portrait: the die takes the space left above the button.
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Die(faceNumber, isRolling, Modifier
                    .weight(1f)
                    .fillMaxWidth())
                RollButton(
                    isRolling,
                    onRollClick,
                    Modifier
                        .widthIn(max = MAX_PORTRAIT_BUTTON_WIDTH)
                        .fillMaxWidth()
                )
            }
        }
    }
}

/** The die, as large as the space it is given allows, centered in it. */
@Composable
private fun Die(faceNumber: Int?, isRolling: Boolean, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        DiceView(
            faceNumber = faceNumber,
            isRolling = isRolling,
            modifier = Modifier.size(minOf(this.maxWidth, this.maxHeight, MAX_DIE_SIZE))
        )
    }
}

@Composable
private fun RollButton(isRolling: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val container by animateColorAsState(
        if (isRolling) DiceColors.Stop else DiceColors.Primary,
        label = "buttonColor"
    )

    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = DiceColors.OnPrimary
        )
    ) {
        Text(
            text = if (isRolling) "STOP" else "ROLL",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
