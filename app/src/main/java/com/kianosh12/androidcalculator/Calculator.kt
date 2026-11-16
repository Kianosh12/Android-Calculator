package com.kianosh12.androidcalculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

/**
 * A tiny recursive-descent expression evaluator.
 *
 * Supported syntax: `+ - * /`, unary `+`/`-`, decimals, `%` (divides by 100),
 * and parentheses. Operator precedence follows the usual rules.
 */
object Calculator {

    /** Evaluates [expression] or returns `null` when it is not a complete valid expression. */
    fun evaluate(expression: String): Double? {
        if (expression.isBlank()) return null
        return try {
            val tokens = tokenize(expression)
            if (tokens.isEmpty()) return null
            val parser = Parser(tokens)
            val value = parser.parseExpression()
            if (!parser.atEnd()) return null
            if (value.isNaN() || value.isInfinite()) null else sanitize(value)
        } catch (_: Exception) {
            null
        }
    }

    /** Formats a number for display: no scientific notation, no floating point noise. */
    fun format(value: Double): String {
        if (value == 0.0) return "0"
        if (abs(value) >= 1e15 || (abs(value) < 1e-9 && value != 0.0)) {
            return BigDecimal(value).round(MathContext(12)).stripTrailingZeros().toPlainString()
        }
        if (value == Math.floor(value)) {
            return BigDecimal(value).setScale(0, RoundingMode.HALF_UP).toPlainString()
        }
        return BigDecimal(value)
            .round(MathContext(12))
            .setScale(10, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    }

    /**
     * Returns [expression] with the sign of its trailing number flipped.
     * A trailing number that is already being subtracted is wrapped in parentheses
     * so that `3-5` becomes `3-(-5)`.
     */
    fun negateTrailingNumber(expression: String): String {
        val trimmed = expression.trimEnd()
        if (trimmed.isEmpty()) return expression

        // The whole expression is a single number: just flip the leading sign.
        if (NUMBER_REGEX.matches(trimmed)) {
            return if (trimmed.startsWith("-")) trimmed.substring(1) else "-$trimmed"
        }

        var end = trimmed.length
        while (end > 0 && (trimmed[end - 1].isDigit() || trimmed[end - 1] == '.')) end--
        if (end == trimmed.length) return expression // no trailing number to negate

        val number = trimmed.substring(end)
        val prefix = trimmed.substring(0, end)

        // The trailing number follows a binary operator, so wrap it: `3-5` -> `3-(-5)`
        if (prefix.isNotEmpty() && endsWithBinaryOperator(prefix)) {
            return "$prefix(-$number)"
        }
        return "$prefix-$number"
    }

    /** True when [prefix] ends with an operator that acts as a binary operator. */
    private fun endsWithBinaryOperator(prefix: String): Boolean {
        val last = prefix.last()
        if (last != '+' && last != '-' && last != '*' && last != '/') return false
        val head = prefix.dropLast(1)
        if (head.isEmpty()) return false
        val before = head.last()
        return before.isDigit() || before == ')' || before == '.'
    }

    private fun sanitize(value: Double): Double =
        BigDecimal(value)
            .round(MathContext(12))
            .setScale(10, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toDouble()

    private sealed interface Token {
        data class Num(val value: Double) : Token
        data class Op(val symbol: Char) : Token
        data class Paren(val symbol: Char) : Token
    }

    private fun tokenize(expression: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0
        while (index < expression.length) {
            val c = expression[index]
            when {
                c.isDigit() || c == '.' -> {
                    val start = index
                    while (index < expression.length && (expression[index].isDigit() || expression[index] == '.')) index++
                    val text = expression.substring(start, index)
                    tokens += Token.Num(text.toDouble())
                }

                c in "+-*/%" -> {
                    tokens += Token.Op(c)
                    index++
                }

                c == '(' || c == ')' -> {
                    tokens += Token.Paren(c)
                    index++
                }

                c.isWhitespace() -> index++

                else -> throw IllegalArgumentException("Unexpected character '$c'")
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<Token>) {
        private var index = 0

        /**
         * When set, a `%` is interpreted relative to this value instead of being a
         * plain division by 100. That makes `100 + 5%` evaluate to `105` (like the
         * calculators on iOS and Android do) while a standalone `50%` stays `0.5`.
         */
        private var percentBase: Double? = null

        fun atEnd(): Boolean = index >= tokens.size

        private fun peek(): Token? = tokens.getOrNull(index)

        fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                val token = peek()
                if (token is Token.Op && (token.symbol == '+' || token.symbol == '-')) {
                    index++
                    percentBase = value
                    val right = parseTerm()
                    percentBase = null
                    value = if (token.symbol == '+') value + right else value - right
                } else {
                    break
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            // The percent context only applies to the operand directly after + or -.
            percentBase = null
            while (true) {
                val token = peek()
                if (token is Token.Op && (token.symbol == '*' || token.symbol == '/')) {
                    index++
                    val right = parseFactor()
                    value = if (token.symbol == '*') value * right else value / right
                } else {
                    break
                }
            }
            return value
        }

        private fun parseFactor(): Double {
            val token = peek()

            if (token is Token.Op && (token.symbol == '-' || token.symbol == '+')) {
                index++
                val value = parseFactor()
                return if (token.symbol == '-') -value else value
            }

            if (token is Token.Paren && token.symbol == '(') {
                index++
                val outerPercentBase = percentBase
                percentBase = null
                val value = parseExpression()
                percentBase = outerPercentBase
                val closing = peek()
                if (closing !is Token.Paren || closing.symbol != ')') {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
                index++
                return value
            }

            if (token is Token.Num) {
                index++
                var value = token.value
                while (true) {
                    val next = peek()
                    if (next is Token.Op && next.symbol == '%') {
                        index++
                        val base = percentBase
                        value = if (base != null) value * base / 100.0 else value / 100.0
                    } else {
                        break
                    }
                }
                return value
            }

            throw IllegalArgumentException("Unexpected token")
        }
    }

    private val NUMBER_REGEX = Regex("""-?\d*\.?\d+""")
}
