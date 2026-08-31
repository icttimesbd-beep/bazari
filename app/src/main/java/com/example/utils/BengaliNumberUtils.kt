package com.example.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object BengaliNumberUtils {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(numberStr: String): String {
        val sb = StringBuilder()
        for (c in numberStr) {
            if (c in '0'..'9') {
                sb.append(banglaDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun toBengaliDigits(number: Int): String {
        return toBengaliDigits(number.toString())
    }

    fun toBengaliDigits(number: Double): String {
        val df = DecimalFormat("#.##")
        return toBengaliDigits(df.format(number))
    }

    fun toEnglishDigits(banglaStr: String): String {
        val sb = StringBuilder()
        for (c in banglaStr) {
            when (c) {
                '০' -> sb.append('0')
                '১' -> sb.append('1')
                '২' -> sb.append('2')
                '৩' -> sb.append('3')
                '৪' -> sb.append('4')
                '৫' -> sb.append('5')
                '৬' -> sb.append('6')
                '৭' -> sb.append('7')
                '৮' -> sb.append('8')
                '৯' -> sb.append('9')
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun formatPriceTaka(amount: Double, useBn: Boolean = true): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##0")
        val formatted = formatter.format(amount)
        return if (useBn) {
            "৳${toBengaliDigits(formatted)}"
        } else {
            "৳$formatted"
        }
    }

    fun formatPriceTaka(amount: Int, useBn: Boolean = true): String {
        return formatPriceTaka(amount.toDouble(), useBn)
    }
}
