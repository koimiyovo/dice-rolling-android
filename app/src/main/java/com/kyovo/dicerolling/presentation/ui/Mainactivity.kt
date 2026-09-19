package com.kyovo.dicerolling.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kyovo.dicerolling.application.service.ContinuousRollService
import com.kyovo.dicerolling.application.service.RollService
import com.kyovo.dicerolling.infrastructure.adapters.DefaultRollGenerator

class MainActivity : ComponentActivity() {

    private val viewModel: DiceViewModel by viewModels {
        viewModelFactory {
            initializer {
                val roller = RollService(DefaultRollGenerator())
                DiceViewModel(ContinuousRollService(roller))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    DiceScreen(viewModel)
                }
            }
        }
    }
}

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
        Text(
            text = currentFace?.number?.value?.toString() ?: "–",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { if (isRolling) viewModel.stopRolling() else viewModel.startRolling() }) {
            Text(if (isRolling) "Stop" else "Roll")
        }
    }
}