package com.kyovo.dicerolling

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kyovo.dicerolling.application.service.ContinuousRollService
import com.kyovo.dicerolling.application.service.RollService
import com.kyovo.dicerolling.infrastructure.adapters.DefaultDiceProvider
import com.kyovo.dicerolling.infrastructure.adapters.DefaultRollGenerator
import com.kyovo.dicerolling.presentation.ui.DiceScreen
import com.kyovo.dicerolling.presentation.ui.DiceViewModel
import com.kyovo.dicerolling.presentation.ui.theme.DiceRollingTheme

/** Composition root: the only place that knows the concrete implementations of the ports. */
class MainActivity : ComponentActivity() {

    private val viewModel: DiceViewModel by viewModels {
        viewModelFactory {
            initializer {
                val roller = RollService(DefaultRollGenerator())
                val weightedDice = DefaultDiceProvider().dice()
                DiceViewModel(ContinuousRollService(roller), weightedDice)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            DiceRollingTheme {
                DiceScreen(viewModel)
            }
        }
    }
}
