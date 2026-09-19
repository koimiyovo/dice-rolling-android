package com.kyovo.dicerolling.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyovo.dicerolling.presentation.ui.dice.DiceView

@Composable
fun DiceScreen(viewModel: DiceViewModel) {
    val currentFace by viewModel.currentFace.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DiceView(
            faceNumber = currentFace?.number?.value,
            isRolling = isRolling,
            modifier = Modifier.size(280.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { if (isRolling) viewModel.stopRolling() else viewModel.startRolling() }) {
            Text(if (isRolling) "Stop" else "Roll")
        }
    }
}
