package com.kyovo.dicerolling.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DiceColors {
    val BackgroundTop = Color(0xFF1A1547)
    val BackgroundBottom = Color(0xFF09081A)
    val Surface = Color(0x1AFFFFFF)
    val SurfaceBorder = Color(0x26FFFFFF)
    val Primary = Color(0xFF8B7CFF)
    val OnPrimary = Color(0xFF12103A)
    val Accent = Color(0xFFFFC857)
    val Stop = Color(0xFFFF6B7A)
    val TextPrimary = Color(0xFFF4F2FF)
    val TextSecondary = Color(0xFFA9A5D0)

    val background = Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))
}

private val colorScheme = darkColorScheme(
    primary = DiceColors.Primary,
    onPrimary = DiceColors.OnPrimary,
    secondary = DiceColors.Accent,
    error = DiceColors.Stop,
    onError = DiceColors.OnPrimary,
    background = DiceColors.BackgroundBottom,
    onBackground = DiceColors.TextPrimary,
    surface = DiceColors.BackgroundBottom,
    onSurface = DiceColors.TextPrimary,
    onSurfaceVariant = DiceColors.TextSecondary
)

private val typography = Typography(
    displayLarge = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    labelLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp),
    bodyMedium = TextStyle(fontSize = 14.sp)
)

private val shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun DiceRollingTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme, typography = typography, shapes = shapes, content = content)
}
