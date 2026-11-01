package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ProcessedOutputFile
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

data class SpreadsheetSheet(
    val name: String,
    val rows: List<List<String>>
)

data class PresentationSlide(
    val slideNumber: Int,
    val title: String,
    val bulletPoints: List<String>
)

data class DocumentParagraph(
    val text: String,
    val isHeading: Boolean = false
)

object OfficeProcessingEngine {

    private fun getOutputDir(context: Context): File {
        val dir = File(context.cacheDir, "docmaster_output")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun parseExcel(context: Context, uri: Uri): List<SpreadsheetSheet> = withContext(Dispatchers.IO) {
        val sheets = mutableListOf<SpreadsheetSheet>()
        val sharedStrings = mutableListOf<String>()

        try {
            // First pass: extract shared strings if present
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val zis = ZipInputStream(stream)
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "xl/sharedStrings.xml") {
                        val factory = XmlPullParserFactory.newInstance()
                        val parser = factory.newPullParser()
                        parser.setInput(zis, "UTF-8")
                        var eventType = parser.eventType
                        var inText = false
                        val currentText = StringBuilder()

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            when (eventType) {
                                XmlPullParser.START_TAG -> {
                                    if (parser.name == "t") {
                                        inText = true
                                        currentText.clear()
                                    }
                                }
                                XmlPullParser.TEXT -> {
                                    if (inText) currentText.append(parser.text)
                                }
                                XmlPullParser.END_TAG -> {
                                    if (parser.name == "t") {
                                        inText = false
                                        sharedStrings.add(currentText.toString())
                                    }
                                }
                            }
                            eventType = parser.next()
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            // Second pass: parse worksheet rows
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val zis = ZipInputStream(stream)
                var entry: ZipEntry? = zis.nextEntry
                var sheetCount = 1

                while (entry != null) {
                    if (entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml")) {
                        val sheetRows = mutableListOf<List<String>>()
                        val factory = XmlPullParserFactory.newInstance()
                        val parser = factory.newPullParser()
                        parser.setInput(zis, "UTF-8")

                        var eventType = parser.eventType
                        var currentRow = mutableListOf<String>()
                        var cellType: String? = null
                        var inValue = false
                        val cellValue = StringBuilder()

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            when (eventType) {
                                XmlPullParser.START_TAG -> {
                                    when (parser.name) {
                                        "row" -> currentRow = mutableListOf()
                                        "c" -> {
                                            cellType = parser.getAttributeValue(null, "t")
                                            cellValue.clear()
                                        }
                                        "v" -> inValue = true
                                    }
                                }
                                XmlPullParser.TEXT -> {
                                    if (inValue) cellValue.append(parser.text)
                                }
                                XmlPullParser.END_TAG -> {
                                    when (parser.name) {
                                        "v" -> inValue = false
                                        "c" -> {
                                            val raw = cellValue.toString().trim()
                                            val finalVal = if (cellType == "s") {
                                                val idx = raw.toIntOrNull() ?: -1
                                                if (idx in 0 until sharedStrings.size) sharedStrings[idx] else raw
                                            } else {
                                                raw
                                            }
                                            currentRow.add(finalVal)
                                        }
                                        "row" -> {
                                            if (currentRow.isNotEmpty()) {
                                                sheetRows.add(currentRow.toList())
                                            }
                                        }
                                    }
                                }
                            }
                            eventType = parser.next()
                        }

                        if (sheetRows.isNotEmpty()) {
                            sheets.add(SpreadsheetSheet("Sheet $sheetCount", sheetRows))
                            sheetCount++
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            // fallback: check if CSV or text
        }

        if (sheets.isEmpty()) {
            // Attempt to parse as plain CSV or tab-separated file
            val csvRows = parseCsv(context, uri)
            if (csvRows.isNotEmpty()) {
                sheets.add(SpreadsheetSheet("Table Data", csvRows))
            }
        }
        sheets
    }

    private fun parseCsv(context: Context, uri: Uri): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).useLines { lines ->
                    lines.forEach { line ->
                        val parts = if (line.contains(",")) {
                            line.split(",").map { it.trim().trim('\"') }
                        } else if (line.contains("\t")) {
                            line.split("\t").map { it.trim() }
                        } else {
                            listOf(line.trim())
                        }
                        rows.add(parts)
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return rows
    }

    suspend fun exportExcelToCsv(
        context: Context,
        sheet: SpreadsheetSheet,
        outputName: String = "exported_sheet.csv"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        FileOutputStream(outputFile).use { out ->
            sheet.rows.forEach { row ->
                val line = row.joinToString(",") { cell ->
                    if (cell.contains(",") || cell.contains("\"")) "\"${cell.replace("\"", "\"\"")}\""
                    else cell
                } + "\n"
                out.write(line.toByteArray(Charsets.UTF_8))
            }
        }

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "text/csv"
        )
    }

    suspend fun exportExcelToPdf(
        context: Context,
        sheet: SpreadsheetSheet,
        outputName: String = "spreadsheet_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val document = PdfDocument()

        val pageWidth = 842 // A4 Landscape
        val pageHeight = 595
        val maxCols = sheet.rows.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
        val colWidth = (pageWidth - 60f) / maxCols.coerceAtMost(8)

        val headerPaint = Paint().apply {
            color = Color.rgb(79, 70, 229) // Indigo
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var pageNum = 1
        var rowIndex = 0
        val rowsPerPage = 22

        while (rowIndex < sheet.rows.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Title
            canvas.drawText("${sheet.name} - Page $pageNum", 30f, 40f, titlePaint)
            var currentY = 75f

            val endRow = (rowIndex + rowsPerPage).coerceAtMost(sheet.rows.size)
            for (r in rowIndex until endRow) {
                val rowData = sheet.rows[r]
                val isHeaderRow = (r == 0)
                val paint = if (isHeaderRow) headerPaint else textPaint

                var currentX = 30f
                for (c in 0 until maxCols.coerceAtMost(8)) {
                    val text = if (c < rowData.size) rowData[c] else ""
                    val truncated = if (text.length > 22) text.take(19) + "..." else text
                    canvas.drawText(truncated, currentX, currentY, paint)
                    currentX += colWidth
                }
                currentY += 20f
                canvas.drawLine(30f, currentY - 6f, pageWidth - 30f, currentY - 6f, linePaint)
            }

            document.finishPage(page)
            rowIndex = endRow
            pageNum++
        }

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf"
        )
    }

    suspend fun parseWordDocx(context: Context, uri: Uri): List<DocumentParagraph> = withContext(Dispatchers.IO) {
        val paragraphs = mutableListOf<DocumentParagraph>()
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val zis = ZipInputStream(stream)
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val factory = XmlPullParserFactory.newInstance()
                        val parser = factory.newPullParser()
                        parser.setInput(zis, "UTF-8")

                        var eventType = parser.eventType
                        var inParagraph = false
                        var inText = false
                        var isHeading = false
                        val currentText = StringBuilder()

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            when (eventType) {
                                XmlPullParser.START_TAG -> {
                                    when (parser.name) {
                                        "w:p" -> {
                                            inParagraph = true
                                            isHeading = false
                                            currentText.clear()
                                        }
                                        "w:pStyle" -> {
                                            val style = parser.getAttributeValue(null, "w:val")
                                            if (style != null && style.contains("Heading", ignoreCase = true)) {
                                                isHeading = true
                                            }
                                        }
                                        "w:t" -> inText = true
                                    }
                                }
                                XmlPullParser.TEXT -> {
                                    if (inText) currentText.append(parser.text)
                                }
                                XmlPullParser.END_TAG -> {
                                    when (parser.name) {
                                        "w:t" -> inText = false
                                        "w:p" -> {
                                            inParagraph = false
                                            val txt = currentText.toString().trim()
                                            if (txt.isNotEmpty()) {
                                                paragraphs.add(DocumentParagraph(txt, isHeading))
                                            }
                                        }
                                    }
                                }
                            }
                            eventType = parser.next()
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            // fallback plain text reading
        }

        if (paragraphs.isEmpty()) {
            val plainText = readPlainText(context, uri)
            plainText.lines().filter { it.isNotBlank() }.forEach {
                paragraphs.add(DocumentParagraph(it.trim()))
            }
        }
        paragraphs
    }

    suspend fun exportWordToPdf(
        context: Context,
        paragraphs: List<DocumentParagraph>,
        outputName: String = "word_converted.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val document = PdfDocument()

        val pageWidth = 595 // A4 Portrait
        val pageHeight = 842

        val titlePaint = Paint().apply {
            color = Color.rgb(79, 70, 229)
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 11f
            isAntiAlias = true
        }

        var pageNum = 1
        var paragraphIdx = 0

        while (paragraphIdx < paragraphs.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            var currentY = 60f

            while (paragraphIdx < paragraphs.size && currentY < pageHeight - 60f) {
                val p = paragraphs[paragraphIdx]
                val paint = if (p.isHeading) titlePaint else bodyPaint
                val lineHeight = if (p.isHeading) 24f else 16f

                // Simple wrap
                val words = p.text.split(" ")
                val lineBuilder = StringBuilder()
                for (word in words) {
                    val testLine = if (lineBuilder.isEmpty()) word else "$lineBuilder $word"
                    if (paint.measureText(testLine) > (pageWidth - 80f)) {
                        canvas.drawText(lineBuilder.toString(), 40f, currentY, paint)
                        currentY += lineHeight
                        lineBuilder.clear()
                        lineBuilder.append(word)
                        if (currentY >= pageHeight - 60f) break
                    } else {
                        lineBuilder.clear()
                        lineBuilder.append(testLine)
                    }
                }
                if (lineBuilder.isNotEmpty() && currentY < pageHeight - 60f) {
                    canvas.drawText(lineBuilder.toString(), 40f, currentY, paint)
                    currentY += lineHeight + 8f
                }
                paragraphIdx++
            }

            document.finishPage(page)
            pageNum++
        }

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf"
        )
    }

    suspend fun parsePowerPoint(context: Context, uri: Uri): List<PresentationSlide> = withContext(Dispatchers.IO) {
        val slides = mutableListOf<PresentationSlide>()
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val zis = ZipInputStream(stream)
                var entry: ZipEntry? = zis.nextEntry
                var slideNum = 1

                while (entry != null) {
                    if (entry.name.startsWith("ppt/slides/slide") && entry.name.endsWith(".xml")) {
                        val factory = XmlPullParserFactory.newInstance()
                        val parser = factory.newPullParser()
                        parser.setInput(zis, "UTF-8")

                        var eventType = parser.eventType
                        var inText = false
                        val currentText = StringBuilder()
                        val textBlocks = mutableListOf<String>()

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            when (eventType) {
                                XmlPullParser.START_TAG -> {
                                    if (parser.name == "a:t") {
                                        inText = true
                                        currentText.clear()
                                    }
                                }
                                XmlPullParser.TEXT -> {
                                    if (inText) currentText.append(parser.text)
                                }
                                XmlPullParser.END_TAG -> {
                                    if (parser.name == "a:t") {
                                        inText = false
                                        val str = currentText.toString().trim()
                                        if (str.isNotEmpty()) textBlocks.add(str)
                                    }
                                }
                            }
                            eventType = parser.next()
                        }

                        val title = textBlocks.firstOrNull() ?: "Slide $slideNum"
                        val bullets = if (textBlocks.size > 1) textBlocks.drop(1) else listOf("Slide presentation content")
                        slides.add(PresentationSlide(slideNum, title, bullets))
                        slideNum++
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        if (slides.isEmpty()) {
            slides.add(PresentationSlide(1, "Presentation Overview", listOf("Slide contents ready for display")))
        }
        slides
    }

    suspend fun exportPptToPdf(
        context: Context,
        slides: List<PresentationSlide>,
        outputName: String = "presentation_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val document = PdfDocument()

        val pageWidth = 960 // 16:9 Presentation slide aspect
        val pageHeight = 540

        val bgPaint = Paint().apply { color = Color.rgb(15, 23, 42) } // Slate dark card slide
        val cardPaint = Paint().apply { color = Color.rgb(30, 41, 59) }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val accentBarPaint = Paint().apply { color = Color.rgb(99, 102, 241) }
        val bulletPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            textSize = 16f
            isAntiAlias = true
        }
        val dotPaint = Paint().apply {
            color = Color.rgb(139, 92, 246)
            isAntiAlias = true
        }
        val footerPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 12f
            isAntiAlias = true
        }

        slides.forEachIndexed { idx, slide ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, idx + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Slide background
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            // Inner card
            canvas.drawRoundRect(40f, 40f, pageWidth - 40f, pageHeight - 40f, 16f, 16f, cardPaint)

            // Accent bar
            canvas.drawRect(60f, 75f, 66f, 115f, accentBarPaint)

            // Title
            canvas.drawText(slide.title, 80f, 105f, titlePaint)

            // Bullets
            var bulletY = 170f
            slide.bulletPoints.take(8).forEach { bullet ->
                canvas.drawCircle(80f, bulletY - 5f, 4f, dotPaint)
                canvas.drawText(bullet.take(80), 96f, bulletY, bulletPaint)
                bulletY += 34f
            }

            // Footer
            canvas.drawText("DocMaster Presentation Deck • Slide ${idx + 1} of ${slides.size}", 80f, pageHeight - 65f, footerPaint)

            document.finishPage(page)
        }

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf"
        )
    }

    suspend fun readPlainText(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun createDocxFromText(
        context: Context,
        title: String,
        paragraphs: List<String>,
        outputName: String = "converted_document.docx"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)

        val xmlContent = buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
            append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n")
            append("<w:body>\n")
            append("<w:p><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t>${escapeXml(title)}</w:t></w:r></w:p>\n")
            paragraphs.forEach { p ->
                append("<w:p><w:r><w:t>${escapeXml(p)}</w:t></w:r></w:p>\n")
            }
            append("</w:body></w:document>")
        }

        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

        val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

        FileOutputStream(outputFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                // [Content_Types].xml
                zos.putNextEntry(ZipEntry("[Content_Types].xml"))
                zos.write(contentTypesXml.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // _rels/.rels
                zos.putNextEntry(ZipEntry("_rels/.rels"))
                zos.write(relsXml.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // word/document.xml
                zos.putNextEntry(ZipEntry("word/document.xml"))
                zos.write(xmlContent.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
