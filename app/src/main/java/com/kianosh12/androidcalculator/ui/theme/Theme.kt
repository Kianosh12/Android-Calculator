package com.kianosh12.androidcalculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppBackgroundTop = Color(0xFF0C0F15)
val AppBackgroundBottom = Color(0xFF171D28)

val KeyDigit = Color(0xFF252A34)
val KeyUtility = Color(0xFF3B414F)
val KeyOperator = Color(0xFFFF9F0A)
val KeyOperatorDark = Color(0xFFD97C05)

val TextPrimary = Color(0xFFF2F4F8)
val TextSecondary = Color(0xFF98A0AE)
val Danger = Color(0xFFFF5A4E)

private val CalculatorColorScheme = darkColorScheme(
    primary = KeyOperator,
    onPrimary = Color(0xFF11131A),
    primaryContainer = KeyOperatorDark,
    onPrimaryContainer = Color(0xFFFFFFFF),
    background = AppBackgroundTop,
    onBackground = TextPrimary,
    surface = KeyDigit,
    onSurface = TextPrimary,
    surfaceVariant = KeyUtility,
    onSurfaceVariant = TextSecondary,
    error = Danger,
    onError = Color.White,
    outline = Color(0xFF3B414F),
)

@Composable
fun CalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalculatorColorScheme,
        content = content,
    )
}
