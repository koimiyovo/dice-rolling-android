package com.kyovo.dicerolling.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.kyovo.dicerolling.presentation.ui.dice.DiceView
import com.kyovo.dicerolling.presentation.ui.theme.DiceColors

@Composable
fun DiceScreen(viewModel: DiceViewModel) {
    val currentFace by viewModel.currentFace.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiceColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        DiceView(
            faceNumber = currentFace?.number?.value,
            isRolling = isRolling,
            modifier = Modifier.size(280.dp)
        )

        Spacer(Modifier.weight(1f))

        RollButton(isRolling = isRolling) {
            if (isRolling) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.stopRolling()
            } else {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                viewModel.startRolling()
            }
        }
    }
}

@Composable
private fun RollButton(isRolling: Boolean, onClick: () -> Unit) {
    val container by animateColorAsState(
        if (isRolling) DiceColors.Stop else DiceColors.Primary,
        label = "buttonColor"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
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
