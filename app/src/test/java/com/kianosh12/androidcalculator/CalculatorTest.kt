package com.kianosh12.androidcalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for the expression engine. These run on the JVM (`./gradlew test`),
 * no Android device or emulator required.
 */
class CalculatorTest {

    private fun eval(expression: String): Double? = Calculator.evaluate(expression)

    private fun formatted(expression: String): String =
        Calculator.evaluate(expression)?.let { Calculator.format(it) } ?: "INVALID"

    @Test
    fun `adds numbers`() {
        assertEquals(8.0, eval("3+5")!!, 1e-9)
        assertEquals(0.0, eval("3+(-3)")!!, 1e-9)
        assertEquals(1.25, eval("0.25+1")!!, 1e-9)
    }

    @Test
    fun `subtracts numbers`() {
        assertEquals(-2.0, eval("3-5")!!, 1e-9)
        assertEquals(8.0, eval("3-(-5)")!!, 1e-9)
    }

    @Test
    fun `multiplies and divides`() {
        assertEquals(15.0, eval("3*5")!!, 1e-9)
        assertEquals(2.5, eval("5/2")!!, 1e-9)
        assertEquals(0.5, eval("1/2")!!, 1e-9)
    }

    @Test
    fun `respects operator precedence`() {
        assertEquals(14.0, eval("2+3*4")!!, 1e-9)
        assertEquals(20.0, eval("(2+3)*4")!!, 1e-9)
        assertEquals(1.0, eval("8/2/4")!!, 1e-9)
        assertEquals(2.0, eval("4/2*4")!!, 1e-9)
    }

    @Test
    fun `handles unary minus`() {
        assertEquals(-5.0, eval("-5")!!, 1e-9)
        assertEquals(8.0, eval("3--5")!!, 1e-9)
        assertEquals(-15.0, eval("3*-5")!!, 1e-9)
        assertEquals(2.0, eval("-3+5")!!, 1e-9)
    }

    @Test
    fun `percent divides by one hundred`() {
        assertEquals(0.5, eval("50%")!!, 1e-9)
        assertEquals(105.0, eval("100+5%")!!, 1e-9)
    }

    @Test
    fun `handles decimals`() {
        assertEquals(58.0, eval("12.5*4+8")!!, 1e-9)
        assertEquals(0.3, eval("0.1+0.2")!!, 1e-9)
    }

    @Test
    fun `rejects incomplete or invalid expressions`() {
        assertNull(eval("3+"))
        assertNull(eval("*5"))
        assertNull(eval("(2+3"))
        assertNull(eval(""))
        assertNull(eval("1.2.3"))
    }

    @Test
    fun `formats results without floating point noise`() {
        assertEquals("0.3", formatted("0.1+0.2"))
        assertEquals("8", formatted("3+5"))
        assertEquals("2.5", formatted("5/2"))
        assertEquals("-2", formatted("3-5"))
        assertEquals("0", formatted("0"))
    }

    @Test
    fun `negates the trailing number`() {
        assertEquals("-5", Calculator.negateTrailingNumber("5"))
        assertEquals("3-(-5)", Calculator.negateTrailingNumber("3-5"))
        assertEquals("3+(-5)", Calculator.negateTrailingNumber("3+5"))
    }

    @Test
    fun `negated expressions still evaluate correctly`() {
        assertEquals(8.0, eval(Calculator.negateTrailingNumber("3-5"))!!, 1e-9)
        assertEquals(-2.0, eval(Calculator.negateTrailingNumber("3+5"))!!, 1e-9)
        assertEquals(-5.0, eval(Calculator.negateTrailingNumber("5"))!!, 1e-9)
    }
}
