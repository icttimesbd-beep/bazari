package com.example.utils

import com.example.domain.model.ParsedQuickEntry

object SmartEntryParser {

    private val units = listOf(
        "কেজি", "কে.জি", "কেজি.", "গ্রাম", "লিটার", "মি.লি", "মিলি",
        "পিস", "টা", "টি", "হালি", "ডজন", "প্যাকেট", "প্যাক",
        "বক্স", "কার্টন", "কার্টুন", "বস্তা", "বোতল", "কৌটা", "আঁটি", "জোড়া", "রিম", "রোল",
        "kg", "gm", "g", "ltr", "liter", "l", "ml", "pcs", "pc", "piece", "dozen", "pack", "pkt", "box", "carton", "sack"
    )

    private val bengaliWordNumberMap = mapOf(
        "এক" to "1", "দুই" to "2", "তিন" to "3", "চার" to "4", "পাঁচ" to "5",
        "ছয়" to "6", "ছয়" to "6", "সাত" to "7", "আট" to "8", "নয়" to "9", "নয়" to "9", "দশ" to "10",
        "বারো" to "12", "কুড়ি" to "20", "বিশ" to "20", "দেড়" to "1.5", "আড়াই" to "2.5",
        "one" to "1", "two" to "2", "three" to "3", "four" to "4", "five" to "5",
        "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9", "ten" to "10", "dozen" to "12"
    )

    fun parseMultiple(input: String): List<ParsedQuickEntry> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Split by comma, semicolon, newline, or ' এবং ' / ' ও ' / ' and '
        val delimiters = Regex("""[,;\n\r]|(\s+এবং\s+)|(\s+ও\s+)|(\s+and\s+)""", RegexOption.IGNORE_CASE)
        val parts = trimmed.split(delimiters).map { it.trim() }.filter { it.isNotBlank() }

        return if (parts.isNotEmpty()) {
            parts.map { parse(it) }
        } else {
            listOf(parse(trimmed))
        }
    }

    fun parse(input: String): ParsedQuickEntry {
        var trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ParsedQuickEntry(rawText = "", quantity = "১", unit = "পিস", productName = "")
        }

        // Handle fractional words like "আধা কেজি" or "পোয়া"
        if (trimmed.startsWith("আধা কেজি", ignoreCase = true) || trimmed.startsWith("half kg", ignoreCase = true)) {
            val name = trimmed.substringAfter("কেজি").substringAfter("kg").trim()
            return ParsedQuickEntry(
                rawText = input,
                quantity = "৫০০",
                unit = "গ্রাম",
                productName = name.ifBlank { "পণ্য" }
            )
        }
        if (trimmed.startsWith("পোয়া", ignoreCase = true) || trimmed.startsWith("পোয়া", ignoreCase = true)) {
            val name = trimmed.substringAfter("পোয়া").substringAfter("পোয়া").trim()
            return ParsedQuickEntry(
                rawText = input,
                quantity = "২৫০",
                unit = "গ্রাম",
                productName = name.ifBlank { "পণ্য" }
            )
        }

        // Replace word numbers at the beginning (e.g. "দুই কেজি চাল" -> "2 কেজি চাল")
        bengaliWordNumberMap.forEach { (word, digit) ->
            if (trimmed.startsWith("$word ", ignoreCase = true)) {
                trimmed = "$digit " + trimmed.substring(word.length + 1).trim()
            }
        }

        // Convert any Bengali digits to English for regex extraction
        val engInput = BengaliNumberUtils.toEnglishDigits(trimmed)

        // Pattern 1: Number + Optional Unit + Product Name (e.g., "5 কেজি চাল" or "5kg rice" or "১২টা ডিম")
        val unitPattern = units.joinToString("|") { Regex.escape(it) }
        val regex = Regex("""^(\d+(?:\.\d+)?)\s*($unitPattern)?\s*(.*)$""", RegexOption.IGNORE_CASE)

        val match = regex.find(engInput)
        if (match != null) {
            val numStr = match.groupValues[1]
            val matchedUnitEng = match.groupValues[2].trim()
            val rawRemaining = match.groupValues[3].trim()

            // Find matching slice in original Bengali input to preserve original script for name
            val originalQtyBn = BengaliNumberUtils.toBengaliDigits(numStr)
            var unitBn = mapUnitToStandardBn(matchedUnitEng)
            var productName = rawRemaining

            if (productName.isEmpty() && matchedUnitEng.isNotEmpty()) {
                // E.g., user typed "5 kg" or "5 চাল"
                if (!isKnownUnit(matchedUnitEng)) {
                    productName = matchedUnitEng
                    unitBn = "কেজি"
                }
            }

            if (productName.isEmpty()) {
                productName = trimmed
            }

            return ParsedQuickEntry(
                rawText = input,
                quantity = originalQtyBn,
                unit = unitBn,
                productName = productName.removePrefix("-").removePrefix("—").trim()
            )
        }

        // Pattern 2: Product Name + Number + Unit (e.g., "চাল ৫ কেজি" or "ডিম ১২টা")
        val revRegex = Regex("""^(.*?)\s*(\d+(?:\.\d+)?)\s*($unitPattern)?$""", RegexOption.IGNORE_CASE)
        val revMatch = revRegex.find(engInput)
        if (revMatch != null && revMatch.groupValues[1].isNotBlank()) {
            val name = revMatch.groupValues[1].trim()
            val numStr = revMatch.groupValues[2]
            val matchedUnitEng = revMatch.groupValues[3].trim()

            return ParsedQuickEntry(
                rawText = input,
                quantity = BengaliNumberUtils.toBengaliDigits(numStr),
                unit = mapUnitToStandardBn(matchedUnitEng),
                productName = name.removeSuffix("-").removeSuffix("—").trim()
            )
        }

        // Fallback: Default quantity "১", unit "পিস"
        return ParsedQuickEntry(
            rawText = input,
            quantity = "১",
            unit = "পিস",
            productName = trimmed
        )
    }

    private fun isKnownUnit(unit: String): Boolean {
        return units.any { it.equals(unit, ignoreCase = true) }
    }

    fun mapUnitToStandardBn(unit: String): String {
        return when (unit.lowercase()) {
            "কেজি", "কে.জি", "কেজি.", "kg", "kgs", "kilo" -> "কেজি"
            "গ্রাম", "gm", "g", "gram", "grams" -> "গ্রাম"
            "লিটার", "লি.", "ltr", "l", "liter", "litres" -> "লিটার"
            "মিলি", "মি.লি", "ml" -> "মিলি"
            "পিস", "টা", "টি", "pc", "pcs", "piece", "pieces" -> "পিস"
            "হালি" -> "হালি"
            "ডজন", "dozen", "doz" -> "ডজন"
            "প্যাকেট", "প্যাক", "pack", "pkt", "packet" -> "প্যাকেট"
            "বক্স", "box" -> "বক্স"
            "কার্টন", "কার্টুন", "carton" -> "কার্টন"
            "বস্তা", "sack", "bosta" -> "বস্তা"
            "বোতল", "bottle", "btl" -> "বোতল"
            "কৌটা", "can", "jar", "ক্যান" -> "কৌটা"
            "আঁটি", "bundle" -> "আঁটি"
            "জোড়া", "pair" -> "জোড়া"
            "রিম", "ream" -> "রিম"
            "রোল", "roll" -> "রোল"
            else -> if (unit.isNotBlank()) unit else "পিস"
        }
    }
}

