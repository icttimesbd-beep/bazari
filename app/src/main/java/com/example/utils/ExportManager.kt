package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.EventExpenseEntity
import com.example.data.local.entity.EventMemberEntity
import com.example.data.local.entity.EventPlanEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.data.repository.EventInvoiceSummary
import com.example.domain.model.AppLanguage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    private fun getExportDir(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getFormattedDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun getDisplayDate(timestamp: Long, lang: AppLanguage): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", if (lang == AppLanguage.BN) Locale("bn", "BD") else Locale.US)
        return sdf.format(Date(timestamp))
    }

    // 1. TEXT SHARE (NO EMOJIS)
    fun generatePlainText(
        list: ShoppingListEntity,
        items: List<ShoppingItemEntity>,
        lang: AppLanguage,
        includePrices: Boolean = true
    ): String {
        val sb = StringBuilder()
        val brand = L10n.appName(lang)
        val dateStr = getDisplayDate(list.createdAt, lang)

        sb.appendLine("[$brand] ${list.title}")
        sb.appendLine(if (lang == AppLanguage.BN) "তারিখ: $dateStr" else "Date: $dateStr")
        if (list.budget > 0) {
            val budgetStr = L10n.price(list.budget, lang)
            sb.appendLine(if (lang == AppLanguage.BN) "বাজেট: $budgetStr" else "Budget: $budgetStr")
        }
        sb.appendLine("================================")

        val unbought = items.filter { !it.isBought }
        val bought = items.filter { it.isBought }
        var totalCost = 0.0

        if (unbought.isNotEmpty()) {
            sb.appendLine(if (lang == AppLanguage.BN) "-- কেনা বাকি --" else "-- Remaining Items --")
            unbought.forEachIndexed { index, item ->
                val num = L10n.digits(index + 1, lang)
                val qtyUnit = L10n.quantityWithUnit(item.quantity, item.unit, lang)
                val line = StringBuilder("[ ] $num. ${item.nameBn} - $qtyUnit")
                if (includePrices && item.unitPrice > 0) {
                    val qtyNum = BengaliNumberUtils.toEnglishDigits(item.quantity).toDoubleOrNull() ?: 1.0
                    val itemTotal = qtyNum * item.unitPrice
                    totalCost += itemTotal
                    line.append(" (${L10n.price(itemTotal, lang)})")
                }
                if (item.brand.isNotBlank()) {
                    line.append(" [${item.brand}]")
                }
                sb.appendLine(line.toString())
            }
        }

        if (bought.isNotEmpty()) {
            if (unbought.isNotEmpty()) sb.appendLine()
            sb.appendLine(if (lang == AppLanguage.BN) "-- কেনা সম্পন্ন --" else "-- Completed Items --")
            bought.forEachIndexed { index, item ->
                val num = L10n.digits(index + 1, lang)
                val qtyUnit = L10n.quantityWithUnit(item.quantity, item.unit, lang)
                val line = StringBuilder("[X] $num. ${item.nameBn} - $qtyUnit")
                if (includePrices && item.unitPrice > 0) {
                    val qtyNum = BengaliNumberUtils.toEnglishDigits(item.quantity).toDoubleOrNull() ?: 1.0
                    val itemTotal = qtyNum * item.unitPrice
                    totalCost += itemTotal
                    line.append(" (${L10n.price(itemTotal, lang)})")
                }
                sb.appendLine(line.toString())
            }
        }

        sb.appendLine("================================")
        if (includePrices && totalCost > 0) {
            val totalStr = L10n.price(totalCost, lang)
            sb.appendLine(if (lang == AppLanguage.BN) "সর্বমোট আনুমানিক খরচ: $totalStr" else "Total Est. Cost: $totalStr")
        }
        sb.appendLine(if (lang == AppLanguage.BN) "তৈরি: বাজারি অ্যাপ" else "Created via Bazari App")

        return sb.toString()
    }

    fun shareText(context: Context, title: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // 2. DOCUMENT EXPORT (.doc / .txt)
    fun exportDocument(
        context: Context,
        list: ShoppingListEntity,
        items: List<ShoppingItemEntity>,
        lang: AppLanguage
    ): File? {
        return try {
            val fileName = "bazari-${list.title.replace("\\s+".toRegex(), "_")}-${getFormattedDate()}.doc"
            val file = File(getExportDir(context), fileName)
            val content = generatePlainText(list, items, lang, includePrices = true)
            file.writeText(content, Charsets.UTF_8)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3. IMAGE RECEIPT EXPORT (Clean vertical graphic)
    fun exportImageReceipt(
        context: Context,
        list: ShoppingListEntity,
        items: List<ShoppingItemEntity>,
        lang: AppLanguage
    ): File? {
        return try {
            val width = 720
            val rowHeight = 44
            val headerHeight = 180
            val footerHeight = 140
            val totalHeight = headerHeight + (items.size.coerceAtLeast(1) * rowHeight) + footerHeight + 40

            val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background
            canvas.drawColor(Color.parseColor("#F9FBF9"))

            // Card background
            val bgPaint = Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
            }
            val borderPaint = Paint().apply {
                color = Color.parseColor("#E0E6E0")
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            val cardRect = RectF(16f, 16f, width - 16f, totalHeight - 16f)
            canvas.drawRoundRect(cardRect, 16f, 16f, bgPaint)
            canvas.drawRoundRect(cardRect, 16f, 16f, borderPaint)

            // Header Banner
            val headerPaint = Paint().apply {
                color = Color.parseColor("#2E7D32") // Forest Green
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val topHeaderRect = RectF(16f, 16f, width - 16f, 100f)
            canvas.drawRoundRect(topHeaderRect, 16f, 16f, headerPaint)
            canvas.drawRect(RectF(16f, 60f, width - 16f, 100f), headerPaint)

            // Header Texts
            val brandPaint = Paint().apply {
                color = Color.WHITE
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(L10n.appName(lang), 36f, 60f, brandPaint)

            val taglinePaint = Paint().apply {
                color = Color.parseColor("#E8F5E9")
                textSize = 14f
                isAntiAlias = true
            }
            canvas.drawText(L10n.appTagline(lang), 36f, 85f, taglinePaint)

            // List Title and Date
            val titlePaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(list.title, 36f, 135f, titlePaint)

            val metaPaint = Paint().apply {
                color = Color.parseColor("#5A6B5C")
                textSize = 14f
                isAntiAlias = true
            }
            val dateLabel = if (lang == AppLanguage.BN) "তারিখ: " else "Date: "
            val dateStr = dateLabel + getDisplayDate(list.createdAt, lang)
            canvas.drawText(dateStr, 36f, 160f, metaPaint)

            if (list.budget > 0) {
                val bLabel = if (lang == AppLanguage.BN) "বাজেট: " else "Budget: "
                val bStr = bLabel + L10n.price(list.budget, lang)
                canvas.drawText(bStr, width - 220f, 160f, metaPaint)
            }

            // Divider
            val divPaint = Paint().apply {
                color = Color.parseColor("#E0E6E0")
                strokeWidth = 1.5f
            }
            canvas.drawLine(36f, 175f, width - 36f, 175f, divPaint)

            // Column Headers
            val colHeaderPaint = Paint().apply {
                color = Color.parseColor("#495E4B")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val yTableStart = 205f
            canvas.drawText(if (lang == AppLanguage.BN) "ক্রম ও পণ্য" else "No. & Product", 40f, yTableStart, colHeaderPaint)
            canvas.drawText(if (lang == AppLanguage.BN) "পরিমাণ" else "Quantity", 420f, yTableStart, colHeaderPaint)
            canvas.drawText(if (lang == AppLanguage.BN) "দাম" else "Price", width - 120f, yTableStart, colHeaderPaint)

            canvas.drawLine(36f, yTableStart + 12f, width - 36f, yTableStart + 12f, divPaint)

            // Items Loop
            var currentY = yTableStart + 36f
            val itemPaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 16f
                isAntiAlias = true
            }
            val boughtItemPaint = Paint().apply {
                color = Color.parseColor("#8C9E8E")
                textSize = 16f
                isAntiAlias = true
                flags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
            }
            val qtyPaint = Paint().apply {
                color = Color.parseColor("#2E4430")
                textSize = 15f
                isAntiAlias = true
            }
            val pricePaint = Paint().apply {
                color = Color.parseColor("#1E3821")
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val checkPaint = Paint().apply {
                color = Color.parseColor("#2E7D32")
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }

            var totalEstimated = 0.0

            if (items.isEmpty()) {
                val emptyPaint = Paint().apply {
                    color = Color.parseColor("#8C9E8E")
                    textSize = 15f
                    isAntiAlias = true
                }
                canvas.drawText(L10n.emptyListTitle(lang), 40f, currentY + 10f, emptyPaint)
                currentY += rowHeight
            } else {
                items.forEachIndexed { idx, item ->
                    val isBought = item.isBought
                    val paint = if (isBought) boughtItemPaint else itemPaint
                    val numBn = L10n.digits(idx + 1, lang)
                    val nameText = "$numBn. ${item.nameBn}"

                    // Draw Checkbox indicator
                    val checkY = currentY - 10f
                    if (isBought) {
                        val filledCheckPaint = Paint().apply {
                            color = Color.parseColor("#2E7D32")
                            style = Paint.Style.FILL
                            isAntiAlias = true
                        }
                        canvas.drawRoundRect(RectF(38f, checkY - 12f, 54f, checkY + 4f), 4f, 4f, filledCheckPaint)
                        // Checkmark tick
                        val tickPaint = Paint().apply {
                            color = Color.WHITE
                            strokeWidth = 2f
                            style = Paint.Style.STROKE
                            isAntiAlias = true
                        }
                        canvas.drawLine(41f, checkY - 4f, 45f, checkY + 1f, tickPaint)
                        canvas.drawLine(45f, checkY + 1f, 51f, checkY - 8f, tickPaint)
                    } else {
                        canvas.drawRoundRect(RectF(38f, checkY - 12f, 54f, checkY + 4f), 4f, 4f, checkPaint)
                    }

                    // Product Name
                    val clippedName = if (nameText.length > 28) nameText.take(26) + "..." else nameText
                    canvas.drawText(clippedName, 64f, currentY, paint)

                    // Quantity
                    val qtyStr = L10n.quantityWithUnit(item.quantity, item.unit, lang)
                    canvas.drawText(qtyStr, 420f, currentY, if (isBought) boughtItemPaint else qtyPaint)

                    // Price
                    val qtyNum = BengaliNumberUtils.toEnglishDigits(item.quantity).toDoubleOrNull() ?: 1.0
                    val linePrice = qtyNum * item.unitPrice
                    totalEstimated += linePrice

                    val priceStr = if (item.unitPrice > 0) L10n.price(linePrice, lang) else "-"
                    canvas.drawText(priceStr, width - 120f, currentY, if (isBought) boughtItemPaint else pricePaint)

                    // Sub-row line
                    canvas.drawLine(36f, currentY + 12f, width - 36f, currentY + 12f, divPaint)

                    currentY += rowHeight
                }
            }

            // Summary Section
            val summaryY = currentY + 20f
            val summaryBgPaint = Paint().apply {
                color = Color.parseColor("#F1F8F1")
                isAntiAlias = true
            }
            val summaryRect = RectF(36f, summaryY, width - 36f, summaryY + 60f)
            canvas.drawRoundRect(summaryRect, 10f, 10f, summaryBgPaint)

            val sumTextPaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val boughtCount = items.count { it.isBought }
            val statsText = if (lang == AppLanguage.BN) {
                "মোট: ${L10n.digits(items.size, lang)}টি | কেনা: ${L10n.digits(boughtCount, lang)}টি"
            } else {
                "Total: ${items.size} | Bought: $boughtCount"
            }
            canvas.drawText(statsText, 52f, summaryY + 36f, sumTextPaint)

            if (totalEstimated > 0) {
                val totalLabel = if (lang == AppLanguage.BN) "সর্বমোট: " else "Total: "
                val totalText = totalLabel + L10n.price(totalEstimated, lang)
                canvas.drawText(totalText, width - 260f, summaryY + 36f, sumTextPaint)
            }

            // Footer Branding
            val footerPaint = Paint().apply {
                color = Color.parseColor("#7A8F7D")
                textSize = 12f
                isAntiAlias = true
            }
            val footerText = if (lang == AppLanguage.BN) "বাজারি অ্যাপ দিয়ে তৈরি — বাজার সহজ, ফর্দ আরও সহজ"
            else "Created with Bazari — Shopping made easy, lists made smarter"
            canvas.drawText(footerText, 52f, totalHeight - 32f, footerPaint)

            // Save to file
            val fileName = "bazari-${list.title.replace("\\s+".toRegex(), "_")}-${getFormattedDate()}.png"
            val file = File(getExportDir(context), fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 4. REAL PDF EXPORT (Native Android PdfDocument)
    fun exportPdfDocument(
        context: Context,
        list: ShoppingListEntity,
        items: List<ShoppingItemEntity>,
        lang: AppLanguage
    ): File? {
        val pdfDocument = PdfDocument()
        return try {
            val pageWidth = 595 // A4 standard pt
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Page Background
            canvas.drawColor(Color.WHITE)

            // Header Banner
            val headerPaint = Paint().apply {
                color = Color.parseColor("#1B5E20") // Deep Forest Green
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, headerPaint)

            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(L10n.appName(lang), 30f, 42f, titlePaint)

            val taglinePaint = Paint().apply {
                color = Color.parseColor("#C8E6C9")
                textSize = 11f
                isAntiAlias = true
            }
            canvas.drawText(L10n.appTagline(lang), 30f, 58f, taglinePaint)

            // Document Info
            val docTitlePaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(list.title, 30f, 105f, docTitlePaint)

            val metaPaint = Paint().apply {
                color = Color.parseColor("#556B57")
                textSize = 11f
                isAntiAlias = true
            }
            val dateStr = (if (lang == AppLanguage.BN) "তারিখ: " else "Date: ") + getDisplayDate(list.createdAt, lang)
            canvas.drawText(dateStr, 30f, 125f, metaPaint)

            if (list.budget > 0) {
                val budgetStr = (if (lang == AppLanguage.BN) "বাজেট: " else "Budget: ") + L10n.price(list.budget, lang)
                canvas.drawText(budgetStr, pageWidth - 160f, 125f, metaPaint)
            }

            // Divider
            val divPaint = Paint().apply {
                color = Color.parseColor("#DDE5DD")
                strokeWidth = 1f
            }
            canvas.drawLine(30f, 140f, pageWidth - 30f, 140f, divPaint)

            // Table Headers
            val thPaint = Paint().apply {
                color = Color.parseColor("#2E4F32")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(if (lang == AppLanguage.BN) "ক্রম ও পণ্যের নাম" else "No. & Item", 32f, 160f, thPaint)
            canvas.drawText(if (lang == AppLanguage.BN) "পরিমাণ" else "Quantity", 330f, 160f, thPaint)
            canvas.drawText(if (lang == AppLanguage.BN) "একক দর" else "Unit Price", 420f, 160f, thPaint)
            canvas.drawText(if (lang == AppLanguage.BN) "মোট টাকা" else "Total", pageWidth - 80f, 160f, thPaint)

            canvas.drawLine(30f, 168f, pageWidth - 30f, 168f, divPaint)

            var rowY = 190f
            val itemTextPaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 12f
                isAntiAlias = true
            }
            val boughtPaint = Paint().apply {
                color = Color.parseColor("#8E9F90")
                textSize = 12f
                flags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
            }
            val checkPaint = Paint().apply {
                color = Color.parseColor("#2E7D32")
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                isAntiAlias = true
            }

            var grandTotal = 0.0

            items.forEachIndexed { index, item ->
                if (rowY > pageHeight - 120f) return@forEachIndexed // Single page safety

                val isBought = item.isBought
                val p = if (isBought) boughtPaint else itemTextPaint
                val numStr = L10n.digits(index + 1, lang)

                // Checkbox
                if (isBought) {
                    val fillCheck = Paint().apply {
                        color = Color.parseColor("#2E7D32")
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(RectF(32f, rowY - 10f, 44f, rowY + 2f), 2f, 2f, fillCheck)
                } else {
                    canvas.drawRoundRect(RectF(32f, rowY - 10f, 44f, rowY + 2f), 2f, 2f, checkPaint)
                }

                // Name
                val name = "$numStr. ${item.nameBn}"
                val safeName = if (name.length > 32) name.take(30) + "..." else name
                canvas.drawText(safeName, 52f, rowY, p)

                // Qty
                val qtyStr = L10n.quantityWithUnit(item.quantity, item.unit, lang)
                canvas.drawText(qtyStr, 330f, rowY, p)

                // Unit Price
                val unitPriceStr = if (item.unitPrice > 0) L10n.price(item.unitPrice, lang) else "-"
                canvas.drawText(unitPriceStr, 420f, rowY, p)

                // Total
                val qtyNum = BengaliNumberUtils.toEnglishDigits(item.quantity).toDoubleOrNull() ?: 1.0
                val lineTotal = qtyNum * item.unitPrice
                grandTotal += lineTotal
                val totalStr = if (item.unitPrice > 0) L10n.price(lineTotal, lang) else "-"
                canvas.drawText(totalStr, pageWidth - 80f, rowY, p)

                canvas.drawLine(30f, rowY + 8f, pageWidth - 30f, rowY + 8f, divPaint)
                rowY += 26f
            }

            // Summary Box
            val sumY = (rowY + 16f).coerceAtMost(pageHeight - 90f)
            val sumBg = Paint().apply {
                color = Color.parseColor("#F4F8F4")
            }
            canvas.drawRoundRect(RectF(30f, sumY, pageWidth - 30f, sumY + 45f), 6f, 6f, sumBg)

            val summaryTextPaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val boughtCount = items.count { it.isBought }
            val countInfo = if (lang == AppLanguage.BN) "আইটেম: ${L10n.digits(items.size, lang)}টি (কেনা: ${L10n.digits(boughtCount, lang)}টি)"
            else "Items: ${items.size} (Bought: $boughtCount)"
            canvas.drawText(countInfo, 45f, sumY + 26f, summaryTextPaint)

            if (grandTotal > 0) {
                val gTotalStr = (if (lang == AppLanguage.BN) "সর্বমোট: " else "Total: ") + L10n.price(grandTotal, lang)
                canvas.drawText(gTotalStr, pageWidth - 190f, sumY + 26f, summaryTextPaint)
            }

            // Footer
            val footerPaint = Paint().apply {
                color = Color.parseColor("#7A8F7D")
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText(if (lang == AppLanguage.BN) "বাজারি — বাজার সহজ, ফর্দ আরও সহজ।" else "Bazari — Shopping made easy, lists made smarter.", 30f, pageHeight - 30f, footerPaint)

            pdfDocument.finishPage(page)

            val fileName = "bazari-${list.title.replace("\\s+".toRegex(), "_")}-${getFormattedDate()}.pdf"
            val file = File(getExportDir(context), fileName)
            val out = FileOutputStream(file)
            pdfDocument.writeTo(out)
            out.flush()
            out.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }

    // 5. SHARE VIA FILE PROVIDER
    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // 6. PRINT PDF
    fun printPdf(context: Context, file: File, jobName: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Print service unavailable", Toast.LENGTH_SHORT).show()
                return
            }

            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val pdi = PrintDocumentInfo.Builder(jobName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(pdi, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    var input: FileInputStream? = null
                    var output: FileOutputStream? = null
                    try {
                        input = FileInputStream(file)
                        output = FileOutputStream(destination?.fileDescriptor)
                        val buf = ByteArray(1024)
                        var bytesRead: Int
                        while (input.read(buf).also { bytesRead = it } > 0) {
                            output.write(buf, 0, bytesRead)
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    } finally {
                        input?.close()
                        output?.close()
                    }
                }
            }

            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Print error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // 7. EVENT & GROUP BUDGET INVOICE (PLAIN TEXT SHARE)
    // =========================================================================
    fun generateEventInvoicePlainText(
        summary: EventInvoiceSummary,
        lang: AppLanguage
    ): String {
        val sb = StringBuilder()
        val ev = summary.event
        val dateStr = getDisplayDate(ev.eventDate, lang)
        val brand = L10n.appName(lang)

        sb.appendLine("==========================================")
        sb.appendLine("★ $brand — ইভেন্ট ও বাজেট হিসাব মেমো ★")
        sb.appendLine("==========================================")
        sb.appendLine("ইভেন্ট: ${ev.title}")
        sb.appendLine("তারিখ: $dateStr")
        if (ev.organizerName.isNotBlank()) {
            sb.appendLine("আয়োজক/হিসাব রক্ষক: ${ev.organizerName}")
        }
        if (ev.location.isNotBlank()) {
            sb.appendLine("স্থান: ${ev.location}")
        }
        if (ev.targetBudget > 0) {
            sb.appendLine("বাজেট লক্ষ্যমাত্রা: ${L10n.price(ev.targetBudget, lang)}")
        }
        sb.appendLine("------------------------------------------")

        // 1. Members section
        sb.appendLine("\n[১] সদস্য ও চাঁদা কালেকশন (${summary.members.size} জন):")
        if (summary.members.isEmpty()) {
            sb.appendLine("(কোন সদস্য তালিকাভুক্ত নেই)")
        } else {
            summary.members.forEachIndexed { idx, m ->
                val num = L10n.digits(idx + 1, lang)
                val status = if (m.isPaid) "[পরিশোধিত]" else "[বাকি: ${L10n.price(m.targetAmount - m.paidAmount, lang)}]"
                sb.appendLine("$num. ${m.name} — জমা: ${L10n.price(m.paidAmount, lang)} $status")
            }
        }
        sb.appendLine("মোট সংগৃহীত চাঁদা: ${L10n.price(summary.totalPaidCollection, lang)}")
        if (summary.totalPendingCollection > 0) {
            sb.appendLine("বাকি চাঁদা: ${L10n.price(summary.totalPendingCollection, lang)}")
        }

        // 2. Expenses section
        sb.appendLine("\n[২] বাজার ও খরচ বিবরণী (${summary.expenses.size}টি খাত):")
        if (summary.expenses.isEmpty()) {
            sb.appendLine("(কোন খরচের হিসাব যুক্ত নেই)")
        } else {
            summary.expenses.forEachIndexed { idx, exp ->
                val num = L10n.digits(idx + 1, lang)
                val qtyUnit = if (exp.unitPrice > 0) " (${exp.quantity} ${exp.unit} × ${L10n.price(exp.unitPrice, lang)})" else ""
                sb.appendLine("$num. ${exp.title}$qtyUnit — ${L10n.price(exp.amount, lang)} [পরিশোধ: ${exp.paidBy}]")
            }
        }
        sb.appendLine("মোট বাজার ও খরচ: ${L10n.price(summary.totalExpenses, lang)}")

        // 3. Final summary
        sb.appendLine("==========================================")
        sb.appendLine("★ ফাইনাল হিসাব সারাংশ ★")
        sb.appendLine("মোট কালেকশন: ${L10n.price(summary.totalPaidCollection, lang)}")
        sb.appendLine("মোট খরচ: ${L10n.price(summary.totalExpenses, lang)}")
        val net = summary.netBalance
        if (net >= 0) {
            sb.appendLine("উদ্বৃত্ত / হাতে আছে: (+) ${L10n.price(net, lang)}")
        } else {
            sb.appendLine("ঘাটতি / অতিরিক্ত খরচ: (-) ${L10n.price(-net, lang)}")
        }
        sb.appendLine("==========================================")
        sb.appendLine("তৈরি হয়েছে বাজারি (Bazari) অ্যাপ দিয়ে।")

        return sb.toString()
    }

    // =========================================================================
    // 8. EVENT INVOICE IMAGE RECEIPT (PNG)
    // =========================================================================
    fun exportEventInvoiceImage(
        context: Context,
        summary: EventInvoiceSummary,
        lang: AppLanguage
    ): File? {
        return try {
            val width = 800
            val ev = summary.event
            val memberRows = summary.members.size.coerceAtLeast(1)
            val expenseRows = summary.expenses.size.coerceAtLeast(1)
            val totalHeight = 360 + (memberRows * 36) + (expenseRows * 36) + 260

            val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            canvas.drawColor(Color.parseColor("#F4F7F4"))

            // Card
            val bgPaint = Paint().apply { color = Color.WHITE; isAntiAlias = true }
            val borderPaint = Paint().apply {
                color = Color.parseColor("#CBD8CB")
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            val cardRect = RectF(16f, 16f, width - 16f, totalHeight - 16f)
            canvas.drawRoundRect(cardRect, 16f, 16f, bgPaint)
            canvas.drawRoundRect(cardRect, 16f, 16f, borderPaint)

            // Header Banner
            val headerPaint = Paint().apply { color = Color.parseColor("#1B5E20"); style = Paint.Style.FILL; isAntiAlias = true }
            canvas.drawRoundRect(RectF(16f, 16f, width - 16f, 110f), 16f, 16f, headerPaint)
            canvas.drawRect(RectF(16f, 70f, width - 16f, 110f), headerPaint)

            // Brand & Title
            val brandPaint = Paint().apply {
                color = Color.WHITE
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("বাজারি — ইভেন্ট ও বাজেট মেমো", 36f, 62f, brandPaint)

            val taglinePaint = Paint().apply { color = Color.parseColor("#E8F5E9"); textSize = 14f; isAntiAlias = true }
            canvas.drawText("Bazari Event & Expense Invoice | পিকনিক, বিয়ে ও গ্রুপ হিসাব", 36f, 92f, taglinePaint)

            // Event Meta Info
            var yPos = 145f
            val eventTitlePaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(ev.title, 36f, yPos, eventTitlePaint)

            yPos += 26f
            val metaPaint = Paint().apply { color = Color.parseColor("#5A6B5C"); textSize = 13f; isAntiAlias = true }
            val metaLine1 = "তারিখ: ${getDisplayDate(ev.eventDate, lang)}" +
                    (if (ev.location.isNotBlank()) " | স্থান: ${ev.location}" else "") +
                    (if (ev.organizerName.isNotBlank()) " | আয়োজক: ${ev.organizerName}" else "")
            canvas.drawText(metaLine1, 36f, yPos, metaPaint)

            val divPaint = Paint().apply { color = Color.parseColor("#E0E6E0"); strokeWidth = 1.5f }

            // Section 1: Member Collections
            yPos += 30f
            val secHeaderPaint = Paint().apply {
                color = Color.parseColor("#2E7D32")
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("১. সদস্য ও চাঁদা কালেকশন হিসাব (${L10n.digits(summary.members.size, lang)} জন)", 36f, yPos, secHeaderPaint)

            yPos += 14f
            canvas.drawLine(36f, yPos, width - 36f, yPos, divPaint)

            val textPaint = Paint().apply { color = Color.parseColor("#1B2E1B"); textSize = 14f; isAntiAlias = true }
            val paidStatusPaint = Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            val dueStatusPaint = Paint().apply { color = Color.parseColor("#C62828"); textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }

            yPos += 24f
            if (summary.members.isEmpty()) {
                canvas.drawText("(কোন সদস্য তালিকাভুক্ত নেই)", 40f, yPos, metaPaint)
                yPos += 30f
            } else {
                summary.members.forEachIndexed { idx, m ->
                    val num = L10n.digits(idx + 1, lang)
                    canvas.drawText("$num. ${m.name}", 40f, yPos, textPaint)
                    canvas.drawText("জমা: ${L10n.price(m.paidAmount, lang)}", 440f, yPos, textPaint)
                    if (m.isPaid) {
                        canvas.drawText("✓ পরিশোধিত", width - 150f, yPos, paidStatusPaint)
                    } else {
                        val due = (m.targetAmount - m.paidAmount).coerceAtLeast(0.0)
                        canvas.drawText("বাকি: ${L10n.price(due, lang)}", width - 170f, yPos, dueStatusPaint)
                    }
                    yPos += 28f
                }
            }

            // Subtotal for Collection
            val subSumPaint = Paint().apply { color = Color.parseColor("#1B2E1B"); textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            canvas.drawText("মোট সংগৃহীত চাঁদা: ${L10n.price(summary.totalPaidCollection, lang)}", 40f, yPos, subSumPaint)
            if (summary.totalPendingCollection > 0) {
                canvas.drawText("বাকি চাঁদা: ${L10n.price(summary.totalPendingCollection, lang)}", 440f, yPos, dueStatusPaint)
            }

            // Section 2: Expenses
            yPos += 36f
            canvas.drawText("২. বাজার ও খরচ বিবরণী (${L10n.digits(summary.expenses.size, lang)}টি খাত)", 36f, yPos, secHeaderPaint)

            yPos += 14f
            canvas.drawLine(36f, yPos, width - 36f, yPos, divPaint)

            yPos += 24f
            if (summary.expenses.isEmpty()) {
                canvas.drawText("(কোন খরচ যুক্ত নেই)", 40f, yPos, metaPaint)
                yPos += 30f
            } else {
                summary.expenses.forEachIndexed { idx, exp ->
                    val num = L10n.digits(idx + 1, lang)
                    val title = if (exp.unitPrice > 0) "${exp.title} (${exp.quantity} ${exp.unit})" else exp.title
                    canvas.drawText("$num. $title", 40f, yPos, textPaint)
                    canvas.drawText("[পরিশোধ: ${exp.paidBy}]", 440f, yPos, metaPaint)
                    canvas.drawText(L10n.price(exp.amount, lang), width - 150f, yPos, subSumPaint)
                    yPos += 28f
                }
            }

            canvas.drawText("মোট বাজার ও খরচ: ${L10n.price(summary.totalExpenses, lang)}", 40f, yPos, subSumPaint)

            // Section 3: Final Balance Box
            yPos += 30f
            val sumBoxRect = RectF(36f, yPos, width - 36f, yPos + 80f)
            val sumBg = Paint().apply {
                color = if (summary.netBalance >= 0) Color.parseColor("#E8F5E9") else Color.parseColor("#FFEBEE")
                isAntiAlias = true
            }
            canvas.drawRoundRect(sumBoxRect, 12f, 12f, sumBg)

            val netTitlePaint = Paint().apply {
                color = if (summary.netBalance >= 0) Color.parseColor("#1B5E20") else Color.parseColor("#B71C1C")
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val netStatusText = if (summary.netBalance >= 0) {
                "উদ্বৃত্ত / হাতে আছে: (+) ${L10n.price(summary.netBalance, lang)}"
            } else {
                "ঘাটতি / অতিরিক্ত খরচ: (-) ${L10n.price(-summary.netBalance, lang)}"
            }
            canvas.drawText("হিসাব সারাংশ: মোট কালেকশন: ${L10n.price(summary.totalPaidCollection, lang)} | মোট খরচ: ${L10n.price(summary.totalExpenses, lang)}", 54f, yPos + 32f, textPaint)
            canvas.drawText(netStatusText, 54f, yPos + 62f, netTitlePaint)

            // Footer Branding
            val footerPaint = Paint().apply { color = Color.parseColor("#7A8F7D"); textSize = 12f; isAntiAlias = true }
            canvas.drawText("বাজারি (Bazari) — পিকনিক, বিয়ে ও ইভেন্টের নির্ভুল ডিজিটাল হিসাব মেমো।", 52f, totalHeight - 32f, footerPaint)

            val fileName = "bazari-event-${ev.title.replace("\\s+".toRegex(), "_")}-${getFormattedDate()}.png"
            val file = File(getExportDir(context), fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // =========================================================================
    // 9. EVENT INVOICE NATIVE PDF (A4 DOCUMENT)
    // =========================================================================
    fun exportEventInvoicePdf(
        context: Context,
        summary: EventInvoiceSummary,
        lang: AppLanguage
    ): File? {
        val pdfDocument = PdfDocument()
        return try {
            val pageWidth = 595 // A4 width pt
            val pageHeight = 842 // A4 height pt
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(Color.WHITE)

            // Header Banner
            val headerPaint = Paint().apply { color = Color.parseColor("#1B5E20"); style = Paint.Style.FILL }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 75f, headerPaint)

            val brandPaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("বাজারি (Bazari) — ইভেন্ট ও বাজেট মেমো", 30f, 42f, brandPaint)

            val taglinePaint = Paint().apply { color = Color.parseColor("#C8E6C9"); textSize = 11f; isAntiAlias = true }
            canvas.drawText("Event Expense & Contribution Invoice", 30f, 58f, taglinePaint)

            var rowY = 105f
            val ev = summary.event

            val docTitlePaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(ev.title, 30f, rowY, docTitlePaint)

            rowY += 20f
            val metaPaint = Paint().apply { color = Color.parseColor("#556B57"); textSize = 10f; isAntiAlias = true }
            val meta = "তারিখ: ${getDisplayDate(ev.eventDate, lang)}" +
                    (if (ev.organizerName.isNotBlank()) " | আয়োজক: ${ev.organizerName}" else "") +
                    (if (ev.location.isNotBlank()) " | স্থান: ${ev.location}" else "")
            canvas.drawText(meta, 30f, rowY, metaPaint)

            rowY += 16f
            val divPaint = Paint().apply { color = Color.parseColor("#DDE5DD"); strokeWidth = 1f }
            canvas.drawLine(30f, rowY, pageWidth - 30f, rowY, divPaint)

            // Section 1: Members table
            rowY += 24f
            val secPaint = Paint().apply {
                color = Color.parseColor("#2E7D32")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("১. সদস্য ও চাঁদা কালেকশন হিসাব", 30f, rowY, secPaint)

            rowY += 16f
            val itemPaint = Paint().apply { color = Color.parseColor("#1B2E1B"); textSize = 11f; isAntiAlias = true }
            val paidPaint = Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 10f; isAntiAlias = true }
            val duePaint = Paint().apply { color = Color.parseColor("#C62828"); textSize = 10f; isAntiAlias = true }

            if (summary.members.isEmpty()) {
                canvas.drawText("(কোন সদস্য তালিকাভুক্ত নেই)", 40f, rowY, metaPaint)
                rowY += 18f
            } else {
                summary.members.forEachIndexed { idx, m ->
                    if (rowY > pageHeight - 140f) return@forEachIndexed
                    val num = L10n.digits(idx + 1, lang)
                    canvas.drawText("$num. ${m.name}", 40f, rowY, itemPaint)
                    canvas.drawText("জমা: ${L10n.price(m.paidAmount, lang)}", 300f, rowY, itemPaint)
                    if (m.isPaid) {
                        canvas.drawText("✓ পরিশোধিত", pageWidth - 100f, rowY, paidPaint)
                    } else {
                        val due = (m.targetAmount - m.paidAmount).coerceAtLeast(0.0)
                        canvas.drawText("বাকি: ${L10n.price(due, lang)}", pageWidth - 100f, rowY, duePaint)
                    }
                    rowY += 18f
                }
            }

            val boldPaint = Paint().apply {
                color = Color.parseColor("#1B2E1B")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("মোট সংগৃহীত চাঁদা: ${L10n.price(summary.totalPaidCollection, lang)}", 40f, rowY, boldPaint)

            // Section 2: Expenses table
            rowY += 28f
            canvas.drawText("২. বাজার ও খরচ বিবরণী", 30f, rowY, secPaint)

            rowY += 16f
            if (summary.expenses.isEmpty()) {
                canvas.drawText("(কোন খরচ যুক্ত নেই)", 40f, rowY, metaPaint)
                rowY += 18f
            } else {
                summary.expenses.forEachIndexed { idx, exp ->
                    if (rowY > pageHeight - 140f) return@forEachIndexed
                    val num = L10n.digits(idx + 1, lang)
                    val t = if (exp.unitPrice > 0) "${exp.title} (${exp.quantity} ${exp.unit})" else exp.title
                    canvas.drawText("$num. $t", 40f, rowY, itemPaint)
                    canvas.drawText("পরিশোধ: ${exp.paidBy}", 300f, rowY, metaPaint)
                    canvas.drawText(L10n.price(exp.amount, lang), pageWidth - 90f, rowY, boldPaint)
                    rowY += 18f
                }
            }

            canvas.drawText("মোট খরচ: ${L10n.price(summary.totalExpenses, lang)}", 40f, rowY, boldPaint)

            // Summary Box
            val sumY = (rowY + 16f).coerceAtMost(pageHeight - 95f)
            val sumBg = Paint().apply {
                color = if (summary.netBalance >= 0) Color.parseColor("#E8F5E9") else Color.parseColor("#FFEBEE")
            }
            canvas.drawRoundRect(RectF(30f, sumY, pageWidth - 30f, sumY + 45f), 6f, 6f, sumBg)

            val netText = if (summary.netBalance >= 0) "উদ্বৃত্ত / হাতে আছে: (+) ${L10n.price(summary.netBalance, lang)}"
            else "ঘাটতি: (-) ${L10n.price(-summary.netBalance, lang)}"
            canvas.drawText("মোট কালেকশন: ${L10n.price(summary.totalPaidCollection, lang)} | মোট খরচ: ${L10n.price(summary.totalExpenses, lang)}", 45f, sumY + 18f, itemPaint)
            canvas.drawText(netText, 45f, sumY + 36f, boldPaint)

            // Footer
            val footerPaint = Paint().apply { color = Color.parseColor("#7A8F7D"); textSize = 9f; isAntiAlias = true }
            canvas.drawText("বাজারি (Bazari) — পিকনিক, বিয়ে ও ইভেন্ট বাজেট মেমো", 30f, pageHeight - 25f, footerPaint)

            pdfDocument.finishPage(page)

            val fileName = "bazari-event-${ev.title.replace("\\s+".toRegex(), "_")}-${getFormattedDate()}.pdf"
            val file = File(getExportDir(context), fileName)
            val out = FileOutputStream(file)
            pdfDocument.writeTo(out)
            out.flush()
            out.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }
}
