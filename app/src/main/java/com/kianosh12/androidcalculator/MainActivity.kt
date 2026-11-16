package com.kianosh12.androidcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kianosh12.androidcalculator.ui.theme.AppBackgroundBottom
import com.kianosh12.androidcalculator.ui.theme.AppBackgroundTop
import com.kianosh12.androidcalculator.ui.theme.CalculatorTheme
import com.kianosh12.androidcalculator.ui.theme.Danger
import com.kianosh12.androidcalculator.ui.theme.KeyDigit
import com.kianosh12.androidcalculator.ui.theme.KeyOperator
import com.kianosh12.androidcalculator.ui.theme.KeyUtility
import com.kianosh12.androidcalculator.ui.theme.TextPrimary
import com.kianosh12.androidcalculator.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                CalculatorScreen()
            }
        }
    }
}

private enum class KeyKind { Digit, Utility, Operator, Equals }

private data class Key(val label: String, val kind: KeyKind, val input: String)

private val KEY_ROWS = listOf(
    listOf(
        Key("C", KeyKind.Utility, "C"),
        Key("⌫", KeyKind.Utility, "⌫"),
        Key("%", KeyKind.Utility, "%"),
        Key("÷", KeyKind.Operator, "÷"),
    ),
    listOf(
        Key("7", KeyKind.Digit, "7"),
        Key("8", KeyKind.Digit, "8"),
        Key("9", KeyKind.Digit, "9"),
        Key("×", KeyKind.Operator, "×"),
    ),
    listOf(
        Key("4", KeyKind.Digit, "4"),
        Key("5", KeyKind.Digit, "5"),
        Key("6", KeyKind.Digit, "6"),
        Key("−", KeyKind.Operator, "−"),
    ),
    listOf(
        Key("1", KeyKind.Digit, "1"),
        Key("2", KeyKind.Digit, "2"),
        Key("3", KeyKind.Digit, "3"),
        Key("+", KeyKind.Operator, "+"),
    ),
    listOf(
        Key("0", KeyKind.Digit, "0"),
        Key(".", KeyKind.Digit, "."),
        Key("=", KeyKind.Equals, "="),
    ),
)

@Composable
fun CalculatorScreen() {
    var expression by rememberSaveable { mutableStateOf("0") }
    var justEvaluated by rememberSaveable { mutableStateOf(false) }
    var negateBase by rememberSaveable { mutableStateOf<String?>(null) }

    val haptic = LocalHapticFeedback.current

    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    fun onDigit(digit: String) {
        negateBase = null
        if (justEvaluated) {
            expression = digit
            justEvaluated = false
            return
        }
        if (expression == "0") {
            expression = digit
            return
        }
        expression += digit
    }

    fun onOperator(raw: String) {
        negateBase = null
        val symbol = when (raw) {
            "×" -> "*"
            "÷" -> "/"
            "−" -> "-"
            else -> raw
        }
        if (justEvaluated) {
            expression += symbol
            justEvaluated = false
            return
        }
        if (expression.isEmpty()) {
            expression = "0$symbol"
            return
        }
        val last = expression.last()
        expression = if (last == '+' || last == '-' || last == '*' || last == '/') {
            expression.dropLast(1) + symbol
        } else {
            expression + symbol
        }
    }

    fun onEquals() {
        val value = Calculator.evaluate(expression) ?: return
        expression = Calculator.format(value)
        justEvaluated = true
        negateBase = null
    }

    fun onClear() {
        expression = "0"
        justEvaluated = false
        negateBase = null
    }

    fun onBackspace() {
        negateBase = null
        if (justEvaluated) {
            expression = "0"
            justEvaluated = false
            return
        }
        expression = if (expression.length > 1) expression.dropLast(1) else "0"
    }

    fun onSign() {
        if (negateBase != null) {
            expression = negateBase!!
            negateBase = null
            return
        }
        val updated = Calculator.negateTrailingNumber(expression)
        if (updated == expression) return
        negateBase = expression
        expression = updated
    }

    fun onDecimal() {
        negateBase = null
        if (justEvaluated) {
            expression = "0."
            justEvaluated = false
            return
        }
        val currentNumber = expression.takeLastWhile { it.isDigit() || it == '.' }
        if (currentNumber.contains('.')) return
        val last = expression.lastOrNull()
        expression = if (last == null || last == '+' || last == '-' || last == '*' || last == '/') {
            expression + "0."
        } else {
            expression + "."
        }
    }

    fun onPercent() {
        negateBase = null
        if (justEvaluated) return
        val last = expression.lastOrNull() ?: return
        if (last == '+' || last == '-' || last == '*' || last == '/') return
        expression += "%"
    }

    fun onKey(input: String) {
        tick()
        when (input) {
            "C" -> onClear()
            "⌫" -> onBackspace()
            "±" -> onSign()
            "%" -> onPercent()
            "." -> onDecimal()
            "=" -> onEquals()
            "+", "-", "*", "/", "×", "÷", "−" -> onOperator(input)
            else -> onDigit(input)
        }
    }

    val display = expression.ifEmpty { "0" }
    val preview = remember(expression, justEvaluated) {
        if (justEvaluated) {
            null
        } else {
            Calculator.evaluate(expression)?.let { Calculator.format(it) }?.takeIf { it != expression }
        }
    }

    val displayFontSize = when {
        display.length > 18 -> 28.sp
        display.length > 13 -> 34.sp
        display.length > 9 -> 40.sp
        else -> 48.sp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppBackgroundTop, AppBackgroundBottom)))
            .padding(start = 20.dp, end = 20.dp, top = 36.dp, bottom = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = preview.orEmpty(),
                    color = TextSecondary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
                Text(
                    text = display,
                    color = TextPrimary,
                    fontSize = displayFontSize,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KEY_ROWS.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { key ->
                        val span = if (key.input == "0") 2f else 1f
                        CalculatorKey(
                            label = if (key.input == "C" && display == "0") "AC" else key.label,
                            kind = key.kind,
                            modifier = Modifier
                                .weight(span)
                                .aspectRatio(span),
                            onClick = { onKey(key.input) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorKey(
    label: String,
    kind: KeyKind,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = when (kind) {
        KeyKind.Digit -> KeyDigit
        KeyKind.Utility -> KeyUtility
        KeyKind.Operator -> KeyOperator
        KeyKind.Equals -> KeyOperator
    }
    val foreground = when (kind) {
        KeyKind.Digit -> TextPrimary
        KeyKind.Utility -> TextPrimary
        KeyKind.Operator -> Color(0xFF12141A)
        KeyKind.Equals -> Color(0xFF12141A)
    }
    val textColor = if (kind == KeyKind.Utility && label.startsWith("A")) Danger else foreground

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = if (label.length > 1) 22.sp else 28.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
