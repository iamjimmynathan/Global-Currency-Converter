package com.example.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack

object CalculatorEngine {

    /**
     * Evaluates a mathematical expression like "100 + 25 * 2" or "200 + 10%" or "500 / 4".
     * Returns the evaluated Double, or null if syntax is invalid or incomplete.
     */
    fun evaluate(expression: String): Double? {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")
            .trim()

        if (sanitized.isEmpty()) return 0.0

        return try {
            evalInternal(sanitized)
        } catch (e: Exception) {
            null
        }
    }

    private fun evalInternal(expr: String): Double {
        val tokens = tokenize(expr)
        if (tokens.isEmpty()) return 0.0
        return evaluateTokens(tokens)
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        val currentNum = StringBuilder()
        var i = 0

        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() || c == '.' -> {
                    currentNum.append(c)
                }
                c == '%' -> {
                    if (currentNum.isNotEmpty()) {
                        tokens.add(currentNum.toString())
                        currentNum.clear()
                    }
                    tokens.add("%")
                }
                c == '+' || c == '-' || c == '*' || c == '/' -> {
                    if (currentNum.isNotEmpty()) {
                        tokens.add(currentNum.toString())
                        currentNum.clear()
                    } else if (c == '-' && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/"))) {
                        // Unary minus
                        currentNum.append('-')
                        i++
                        continue
                    }
                    tokens.add(c.toString())
                }
            }
            i++
        }
        if (currentNum.isNotEmpty()) {
            tokens.add(currentNum.toString())
        }
        return tokens
    }

    private fun evaluateTokens(tokens: List<String>): Double {
        // First handle percentages:
        // Case A: number % (as standalone, e.g., 50% -> 0.5)
        // Case B: A + B% -> A + (A * (B/100))
        val resolved = mutableListOf<String>()
        var idx = 0
        while (idx < tokens.size) {
            val t = tokens[idx]
            if (t == "%") {
                if (resolved.isNotEmpty()) {
                    val prevVal = resolved.removeAt(resolved.lastIndex).toDoubleOrNull() ?: 0.0
                    if (resolved.size >= 2 && (resolved.last() == "+" || resolved.last() == "-")) {
                        val op = resolved.last()
                        val baseVal = resolved[resolved.size - 2].toDoubleOrNull() ?: prevVal
                        val percentVal = baseVal * (prevVal / 100.0)
                        resolved.add(percentVal.toString())
                    } else {
                        resolved.add((prevVal / 100.0).toString())
                    }
                }
            } else {
                resolved.add(t)
            }
            idx++
        }

        // Shunting-yard algorithm to RPN
        val values = Stack<Double>()
        val ops = Stack<String>()

        fun precedence(op: String): Int = when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            else -> 0
        }

        fun applyOp(op: String, b: Double, a: Double): Double = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else Double.NaN
            else -> 0.0
        }

        var i = 0
        while (i < resolved.size) {
            val token = resolved[i]
            val num = token.toDoubleOrNull()
            if (num != null) {
                values.push(num)
            } else if (token in listOf("+", "-", "*", "/")) {
                while (ops.isNotEmpty() && precedence(ops.peek()) >= precedence(token)) {
                    val op = ops.pop()
                    if (values.size >= 2) {
                        val b = values.pop()
                        val a = values.pop()
                        values.push(applyOp(op, b, a))
                    }
                }
                ops.push(token)
            }
            i++
        }

        while (ops.isNotEmpty()) {
            val op = ops.pop()
            if (values.size >= 2) {
                val b = values.pop()
                val a = values.pop()
                values.push(applyOp(op, b, a))
            }
        }

        return if (values.isNotEmpty()) values.pop() else 0.0
    }

    fun formatNumber(
        value: Double,
        decimals: Int = 2,
        formatType: String = "STANDARD"
    ): String {
        if (value.isNaN() || value.isInfinite()) return "0.00"

        val pattern = if (decimals > 0) {
            "#,##0." + "0".repeat(decimals)
        } else {
            "#,##0"
        }

        val symbols = if (formatType == "EUROPEAN") {
            DecimalFormatSymbols(Locale.GERMANY).apply {
                groupingSeparator = '.'
                decimalSeparator = ','
            }
        } else {
            DecimalFormatSymbols(Locale.US).apply {
                groupingSeparator = ','
                decimalSeparator = '.'
            }
        }

        val df = DecimalFormat(pattern, symbols)
        return df.format(value)
    }

    fun formatExchangeRate(rate: Double): String {
        return when {
            rate >= 1000 -> DecimalFormat("#,##0.00").format(rate)
            rate >= 1 -> DecimalFormat("#,##0.0000").format(rate)
            rate >= 0.001 -> DecimalFormat("0.00000").format(rate)
            else -> DecimalFormat("0.000000").format(rate)
        }
    }
}
