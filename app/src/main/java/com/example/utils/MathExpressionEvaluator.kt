package com.example.utils

import java.text.DecimalFormat

/**
 * Utility to parse and evaluate mathematical expressions (e.g. "45 * 5", "180 * 2", "500 / 2", "45 * 5 + 10")
 * supporting both Bengali digits and standard arithmetic operators (+, -, ×, *, ÷, /).
 */
object MathExpressionEvaluator {

    fun evaluate(expression: String): Double? {
        val engExpr = BengaliNumberUtils.toEnglishDigits(expression.trim())
        if (engExpr.isBlank()) return null

        // Normalize operators: replace × with *, ÷ with /
        var clean = engExpr
            .replace('×', '*')
            .replace('÷', '/')
            .replace("x", "*")
            .replace("X", "*")
            .replace(" ", "")

        // Trim trailing operators if user is still typing (e.g., "45*", "45+", "45-", "45/")
        while (clean.isNotEmpty() && (clean.last() in charArrayOf('+', '-', '*', '/', '.'))) {
            clean = clean.dropLast(1)
        }

        if (clean.isBlank()) return null

        return try {
            parseAndCompute(clean)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseAndCompute(expr: String): Double? {
        // Tokenize into numbers and operators
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()

        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            if (c in '0'..'9' || c == '.') {
                currentNumber.append(c)
            } else if (c in charArrayOf('+', '-', '*', '/')) {
                // Check for unary minus at the beginning or after another operator
                if (c == '-' && currentNumber.isEmpty() && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/"))) {
                    currentNumber.append(c)
                } else {
                    if (currentNumber.isNotEmpty()) {
                        tokens.add(currentNumber.toString())
                        currentNumber = StringBuilder()
                    }
                    tokens.add(c.toString())
                }
            }
            i++
        }
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
        }

        if (tokens.isEmpty()) return null

        // Pass 1: Handle multiplication (*) and division (/) with higher precedence
        val pass1Tokens = mutableListOf<String>()
        var idx = 0
        while (idx < tokens.size) {
            val token = tokens[idx]
            if (token == "*" || token == "/") {
                if (pass1Tokens.isEmpty() || idx + 1 >= tokens.size) return null
                val prevNum = pass1Tokens.removeAt(pass1Tokens.size - 1).toDoubleOrNull() ?: return null
                val nextNum = tokens[idx + 1].toDoubleOrNull() ?: return null
                val res = if (token == "*") {
                    prevNum * nextNum
                } else {
                    if (nextNum == 0.0) return null else prevNum / nextNum
                }
                pass1Tokens.add(res.toString())
                idx += 2
            } else {
                pass1Tokens.add(token)
                idx++
            }
        }

        // Pass 2: Handle addition (+) and subtraction (-)
        if (pass1Tokens.isEmpty()) return null
        var total = pass1Tokens[0].toDoubleOrNull() ?: return null
        var pIdx = 1
        while (pIdx < pass1Tokens.size) {
            val op = pass1Tokens[pIdx]
            if (pIdx + 1 >= pass1Tokens.size) break
            val nextVal = pass1Tokens[pIdx + 1].toDoubleOrNull() ?: return null
            total = when (op) {
                "+" -> total + nextVal
                "-" -> total - nextVal
                else -> total
            }
            pIdx += 2
        }

        return total
    }

    fun hasOperator(expr: String): Boolean {
        val eng = BengaliNumberUtils.toEnglishDigits(expr)
        return eng.contains("*") || eng.contains("×") || eng.contains("/") ||
                eng.contains("÷") || eng.contains("+") || (eng.contains("-") && !eng.startsWith("-"))
    }

    fun formatNumber(value: Double, useBn: Boolean = true): String {
        val isWhole = (value % 1.0 == 0.0)
        val df = if (isWhole) DecimalFormat("#,##0") else DecimalFormat("#,##0.##")
        val formatted = df.format(value)
        return if (useBn) BengaliNumberUtils.toBengaliDigits(formatted) else formatted
    }

    fun formatExpression(expr: String, useBn: Boolean = true): String {
        val normalized = expr
            .replace("*", " × ")
            .replace("x", " × ")
            .replace("X", " × ")
            .replace("/", " ÷ ")
            .replace("+", " + ")
            .replace("-", " - ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (useBn) BengaliNumberUtils.toBengaliDigits(normalized) else normalized
    }
}
