package com.example.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.model.ExcelSheet
import com.example.model.ExcelWorkbook
import com.example.model.PptPresentation
import com.example.model.PptSlide
import com.example.model.TextDocument
import com.example.model.WordDocument
import com.example.model.WordElement
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import java.util.zip.ZipFile

object OfficeEngine {

    // --- EXCEL & CSV ---

    fun parseExcelWorkbook(file: File): ExcelWorkbook {
        val fileName = file.name
        if (file.extension.lowercase() == "csv") {
            return parseCsvWorkbook(file)
        }

        return try {
            val zip = ZipFile(file)
            val sharedStrings = parseSharedStrings(zip)
            val sheets = mutableListOf<ExcelSheet>()

            // Find sheet files in xl/worksheets/
            val sheetEntries = zip.entries().asSequence()
                .filter { it.name.startsWith("xl/worksheets/sheet") && it.name.endsWith(".xml") }
                .sortedBy { it.name }
                .toList()

            for ((index, entry) in sheetEntries.withIndex()) {
                val xmlContent = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                val (headers, rows) = parseSheetXml(xmlContent, sharedStrings)
                sheets.add(ExcelSheet("Sheet ${index + 1}", headers, rows))
            }
            zip.close()

            if (sheets.isEmpty()) {
                // Fallback demo or empty
                ExcelWorkbook(fileName, listOf(ExcelSheet("Sheet 1", listOf("Item", "Category", "Quantity", "Price"), listOf(listOf("Sample Document", "General", "1", "$0.00")))))
            } else {
                ExcelWorkbook(fileName, sheets)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Try parsing as text/csv if corrupt or legacy
            parseCsvWorkbook(file)
        }
    }

    private fun parseSharedStrings(zip: ZipFile): List<String> {
        val strings = mutableListOf<String>()
        val entry = zip.getEntry("xl/sharedStrings.xml") ?: return strings
        val xmlContent = zip.getInputStream(entry).bufferedReader().use { it.readText() }
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        var eventType = parser.eventType
        var inText = false
        val currentStr = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "t") inText = true
                    if (parser.name == "si") currentStr.clear()
                }
                XmlPullParser.TEXT -> {
                    if (inText) currentStr.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") inText = false
                    if (parser.name == "si") strings.add(currentStr.toString())
                }
            }
            eventType = parser.next()
        }
        return strings
    }

    private fun parseSheetXml(xmlContent: String, sharedStrings: List<String>): Pair<List<String>, List<List<String>>> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        val allRows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        var inCell = false
        var cellType: String? = null
        var cellValue = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            inCell = true
                            cellType = parser.getAttributeValue(null, "t")
                            cellValue = StringBuilder()
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inCell) cellValue.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "c" -> {
                            inCell = false
                            val raw = cellValue.toString().trim()
                            val parsed = if (cellType == "s") {
                                val idx = raw.toIntOrNull()
                                if (idx != null && idx in sharedStrings.indices) sharedStrings[idx] else raw
                            } else raw
                            currentRow.add(parsed)
                        }
                        "row" -> {
                            if (currentRow.isNotEmpty() && currentRow.any { it.isNotBlank() }) {
                                allRows.add(currentRow)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        if (allRows.isEmpty()) {
            return Pair(listOf("Column 1", "Column 2"), emptyList())
        }
        val headers = allRows.first()
        val dataRows = if (allRows.size > 1) allRows.subList(1, allRows.size) else emptyList()
        return Pair(headers, dataRows)
    }

    private fun parseCsvWorkbook(file: File): ExcelWorkbook {
        val lines = file.readLines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return ExcelWorkbook(file.name, listOf(ExcelSheet("Sheet 1", listOf("Column 1"), emptyList())))
        }
        val parsedRows = lines.map { line ->
            // simple CSV split with quote support
            line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { it.trim(' ', '"') }
        }
        val headers = parsedRows.first()
        val dataRows = if (parsedRows.size > 1) parsedRows.subList(1, parsedRows.size) else emptyList()
        return ExcelWorkbook(file.name, listOf(ExcelSheet("Sheet 1", headers, dataRows)))
    }

    fun exportExcelToCsv(sheet: ExcelSheet, outputFile: File): Boolean {
        return try {
            outputFile.printWriter().use { writer ->
                writer.println(sheet.headers.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
                for (row in sheet.rows) {
                    writer.println(row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportExcelToPdf(sheet: ExcelSheet, outputFile: File): Boolean {
        val document = PdfDocument()
        try {
            val pageWidth = 842 // Landscape A4
            val pageHeight = 595
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            val titlePaint = Paint().apply {
                color = Color.rgb(79, 70, 229)
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val cellPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f
                isAntiAlias = true
            }
            val bgPaint = Paint().apply {
                color = Color.rgb(238, 242, 255)
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            canvas.drawText("DocMaster Export: ${sheet.name}", 40f, 40f, titlePaint)

            var startY = 70f
            val colCount = sheet.headers.size.coerceAtLeast(1)
            val colWidth = (pageWidth - 80f) / colCount
            val rowHeight = 24f

            // Draw header background
            canvas.drawRect(40f, startY, pageWidth - 40f, startY + rowHeight, bgPaint)
            for ((colIdx, header) in sheet.headers.withIndex()) {
                val x = 45f + (colIdx * colWidth)
                val truncated = if (header.length > 20) header.take(18) + "…" else header
                canvas.drawText(truncated, x, startY + 16f, headerPaint)
            }
            canvas.drawRect(40f, startY, pageWidth - 40f, startY + rowHeight, linePaint)
            startY += rowHeight

            // Draw up to 20 rows for page
            for (row in sheet.rows.take(20)) {
                for (colIdx in 0 until colCount) {
                    val cellText = if (colIdx in row.indices) row[colIdx] else ""
                    val x = 45f + (colIdx * colWidth)
                    val truncated = if (cellText.length > 20) cellText.take(18) + "…" else cellText
                    canvas.drawText(truncated, x, startY + 16f, cellPaint)
                }
                canvas.drawRect(40f, startY, pageWidth - 40f, startY + rowHeight, linePaint)
                startY += rowHeight
            }

            document.finishPage(page)
            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            document.close()
        }
    }

    // --- WORD (DOCX) ---

    fun parseWordDocument(file: File): WordDocument {
        val fileName = file.name
        val elements = mutableListOf<WordElement>()
        try {
            val zip = ZipFile(file)
            val entry = zip.getEntry("word/document.xml")
            if (entry != null) {
                val xmlContent = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(StringReader(xmlContent))

                var eventType = parser.eventType
                var inParagraph = false
                var inRun = false
                var isHeading = false
                var currentText = StringBuilder()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "p" -> {
                                    inParagraph = true
                                    isHeading = false
                                    currentText = StringBuilder()
                                }
                                "pStyle" -> {
                                    val styleVal = parser.getAttributeValue(null, "val") ?: ""
                                    if (styleVal.contains("Heading", ignoreCase = true) || styleVal.contains("Title", ignoreCase = true)) {
                                        isHeading = true
                                    }
                                }
                                "t" -> inRun = true
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inRun) currentText.append(parser.text)
                        }
                        XmlPullParser.END_TAG -> {
                            when (parser.name) {
                                "t" -> inRun = false
                                "p" -> {
                                    inParagraph = false
                                    val text = currentText.toString().trim()
                                    if (text.isNotEmpty()) {
                                        elements.add(WordElement.Paragraph(text, isHeading = isHeading))
                                    }
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
            zip.close()
        } catch (e: Exception) {
            e.printStackTrace()
            elements.add(WordElement.Paragraph("Document content preview", isHeading = true))
            elements.add(WordElement.Paragraph(file.readText().take(500)))
        }

        if (elements.isEmpty()) {
            elements.add(WordElement.Paragraph(file.nameWithoutExtension, isHeading = true))
            elements.add(WordElement.Paragraph("The document does not contain formatted paragraphs or is empty."))
        }
        return WordDocument(fileName, elements)
    }

    fun exportWordToPdf(doc: WordDocument, outputFile: File): Boolean {
        val pdf = PdfDocument()
        try {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 50f
            var pageNum = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            var page = pdf.startPage(pageInfo)
            var canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            val headingPaint = Paint().apply {
                color = Color.rgb(37, 99, 235)
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 12f
                isAntiAlias = true
            }

            var currentY = margin + 20f

            for (element in doc.elements) {
                if (element is WordElement.Paragraph) {
                    val paint = if (element.isHeading) headingPaint else bodyPaint
                    val lineHeight = if (element.isHeading) 28f else 18f

                    // Word-wrap long lines
                    val words = element.text.split(" ")
                    var line = StringBuilder()
                    for (word in words) {
                        val testLine = if (line.isEmpty()) word else "$line $word"
                        if (paint.measureText(testLine) > (pageWidth - (margin * 2))) {
                            if (currentY > pageHeight - margin) {
                                pdf.finishPage(page)
                                pageNum++
                                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                                page = pdf.startPage(pageInfo)
                                canvas = page.canvas
                                canvas.drawColor(Color.WHITE)
                                currentY = margin + 20f
                            }
                            canvas.drawText(line.toString(), margin, currentY, paint)
                            currentY += lineHeight
                            line = StringBuilder(word)
                        } else {
                            line = StringBuilder(testLine)
                        }
                    }
                    if (line.isNotEmpty()) {
                        if (currentY > pageHeight - margin) {
                            pdf.finishPage(page)
                            pageNum++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                            page = pdf.startPage(pageInfo)
                            canvas = page.canvas
                            canvas.drawColor(Color.WHITE)
                            currentY = margin + 20f
                        }
                        canvas.drawText(line.toString(), margin, currentY, paint)
                        currentY += lineHeight + 6f
                    }
                }
            }

            pdf.finishPage(page)
            FileOutputStream(outputFile).use { out ->
                pdf.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            pdf.close()
        }
    }

    // --- POWERPOINT (PPTX) ---

    fun parsePptPresentation(file: File): PptPresentation {
        val fileName = file.name
        val slides = mutableListOf<PptSlide>()

        try {
            val zip = ZipFile(file)
            val slideEntries = zip.entries().asSequence()
                .filter { it.name.startsWith("ppt/slides/slide") && it.name.endsWith(".xml") }
                .sortedBy { it.name }
                .toList()

            for ((index, entry) in slideEntries.withIndex()) {
                val xmlContent = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(StringReader(xmlContent))

                val lines = mutableListOf<String>()
                var currentLine = StringBuilder()
                var inText = false
                var eventType = parser.eventType

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "t") inText = true
                            if (parser.name == "p") currentLine = StringBuilder()
                        }
                        XmlPullParser.TEXT -> {
                            if (inText) currentLine.append(parser.text)
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "t") inText = false
                            if (parser.name == "p") {
                                val line = currentLine.toString().trim()
                                if (line.isNotEmpty()) lines.add(line)
                            }
                        }
                    }
                    eventType = parser.next()
                }

                val title = if (lines.isNotEmpty()) lines.first() else "Slide ${index + 1}"
                val bullets = if (lines.size > 1) lines.subList(1, lines.size) else emptyList()
                slides.add(PptSlide(index + 1, title, bullets))
            }
            zip.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (slides.isEmpty()) {
            slides.add(PptSlide(1, file.nameWithoutExtension, listOf("Presentation Overview", "Created with DocMaster")))
        }

        return PptPresentation(fileName, slides)
    }

    fun exportPptToPdf(ppt: PptPresentation, outputFile: File): Boolean {
        val doc = PdfDocument()
        try {
            val width = 842
            val height = 595
            val titlePaint = Paint().apply {
                color = Color.rgb(249, 115, 22)
                textSize = 28f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val bulletPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 16f
                isAntiAlias = true
            }
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
                isAntiAlias = true
            }

            for (slide in ppt.slides) {
                val pageInfo = PdfDocument.PageInfo.Builder(width, height, slide.slideNumber).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                // Top accent bar
                val barPaint = Paint().apply { color = Color.rgb(249, 115, 22) }
                canvas.drawRect(0f, 0f, width.toFloat(), 12f, barPaint)

                // Title
                canvas.drawText(slide.title, 60f, 80f, titlePaint)

                // Bullets
                var y = 140f
                for (line in slide.contentLines) {
                    canvas.drawCircle(70f, y - 6f, 4f, barPaint)
                    canvas.drawText(line, 90f, y, bulletPaint)
                    y += 36f
                }

                // Footer
                canvas.drawText("DocMaster Presentation Export • Slide ${slide.slideNumber} of ${ppt.slides.size}", 60f, height - 30f, footerPaint)
                doc.finishPage(page)
            }

            FileOutputStream(outputFile).use { out ->
                doc.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            doc.close()
        }
    }

    // --- TEXT (TXT) ---

    fun parseTextDocument(file: File): TextDocument {
        val text = file.readText()
        val lines = text.lines()
        val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        return TextDocument(
            fileName = file.name,
            text = text,
            lines = lines,
            lineCount = lines.size,
            wordCount = words,
            charCount = text.length
        )
    }
}
