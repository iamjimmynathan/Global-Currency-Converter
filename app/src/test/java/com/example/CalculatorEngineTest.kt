package com.example

import com.example.domain.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testSimpleAdditionAndMultiplication() {
        val result = CalculatorEngine.evaluate("100 + 50 * 2")
        assertNotNull(result)
        assertEquals(200.0, result!!, 0.001)
    }

    @Test
    fun testPercentageCalculation() {
        val standalonePercent = CalculatorEngine.evaluate("50%")
        assertNotNull(standalonePercent)
        assertEquals(0.5, standalonePercent!!, 0.001)

        val additivePercent = CalculatorEngine.evaluate("200 + 10%")
        assertNotNull(additivePercent)
        assertEquals(220.0, additivePercent!!, 0.001)
    }

    @Test
    fun testDivisionAndSubtraction() {
        val result = CalculatorEngine.evaluate("1000 ÷ 4 - 50")
        assertNotNull(result)
        assertEquals(200.0, result!!, 0.001)
    }

    @Test
    fun testNumberFormatting() {
        val formattedStandard = CalculatorEngine.formatNumber(1234567.89, decimals = 2, formatType = "STANDARD")
        assertEquals("1,234,567.89", formattedStandard)

        val formattedEuropean = CalculatorEngine.formatNumber(1234567.89, decimals = 2, formatType = "EUROPEAN")
        assertEquals("1.234.567,89", formattedEuropean)
    }
}
