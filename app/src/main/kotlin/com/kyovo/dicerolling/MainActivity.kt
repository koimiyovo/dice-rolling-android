package com.kyovo.dicerolling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kyovo.dicerolling.application.service.ContinuousRollService
import com.kyovo.dicerolling.application.service.RollService
import com.kyovo.dicerolling.domain.model.Face
import com.kyovo.dicerolling.domain.model.FaceNumber
import com.kyovo.dicerolling.domain.model.Faces
import com.kyovo.dicerolling.domain.model.Probability
import com.kyovo.dicerolling.domain.model.WeightedDice
import com.kyovo.dicerolling.infrastructure.adapters.DefaultRollGenerator
import com.kyovo.dicerolling.presentation.ui.DiceScreen
import com.kyovo.dicerolling.presentation.ui.DiceViewModel

/** Composition root: the only place that knows the concrete implementations of the ports. */
class MainActivity : ComponentActivity() {

    private val viewModel: DiceViewModel by viewModels {
        viewModelFactory {
            initializer {
                val roller = RollService(DefaultRollGenerator())
                val weightedDice = WeightedDice(
                    Faces(
                        face1 = Face(FaceNumber(1), Probability(10)),
                        face2 = Face(FaceNumber(2), Probability(20)),
                        face3 = Face(FaceNumber(3), Probability(30)),
                        face4 = Face(FaceNumber(4), Probability(15)),
                        face5 = Face(FaceNumber(5), Probability(15)),
                        face6 = Face(FaceNumber(6), Probability(10))
                    )
                )
                DiceViewModel(ContinuousRollService(roller), weightedDice)
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
