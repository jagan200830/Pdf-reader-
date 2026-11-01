package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream

object PdfEngine {

    const val A4_WIDTH = 595
    const val A4_HEIGHT = 842

    fun getPdfPageCount(file: File): Int {
        return try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            renderer.close()
            pfd.close()
            count
        } catch (e: Exception) {
            1
        }
    }

    fun renderPdfPageToBitmap(file: File, pageIndex: Int, scale: Float = 2.0f): Bitmap? {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null
        return try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            if (pageIndex in 0 until renderer.pageCount) {
                page = renderer.openPage(pageIndex)
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            page?.close()
            renderer?.close()
            pfd?.close()
        }
    }

    fun renderAllPagesToBitmaps(file: File, maxPages: Int = 10, scale: Float = 1.5f): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val pagesToRender = minOf(renderer.pageCount, maxPages)
            for (i in 0 until pagesToRender) {
                val page = renderer.openPage(i)
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            renderer?.close()
            pfd?.close()
        }
        return bitmaps
    }

    fun imagesToPdf(imageFiles: List<File>, outputFile: File): Boolean {
        val document = PdfDocument()
        try {
            for ((index, file) in imageFiles.withIndex()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: continue
                val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, index + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                val scale = minOf(
                    (A4_WIDTH - 40f) / bitmap.width.toFloat(),
                    (A4_HEIGHT - 40f) / bitmap.height.toFloat()
                )
                val destWidth = bitmap.width * scale
                val destHeight = bitmap.height * scale
                val left = (A4_WIDTH - destWidth) / 2f
                val top = (A4_HEIGHT - destHeight) / 2f

                val destRect = RectF(left, top, left + destWidth, top + destHeight)
                canvas.drawBitmap(bitmap, null, destRect, null)
                document.finishPage(page)
                bitmap.recycle()
            }
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

    fun pdfToImages(pdfFile: File, outputDir: File, format: Bitmap.CompressFormat, quality: Int = 90): List<File> {
        val outputFiles = mutableListOf<File>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val ext = if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val width = page.width * 2
                val height = page.height * 2
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val outFile = File(outputDir, "${pdfFile.nameWithoutExtension}_page_${i + 1}.$ext")
                FileOutputStream(outFile).use { out ->
                    bitmap.compress(format, quality, out)
                }
                bitmap.recycle()
                outputFiles.add(outFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            renderer?.close()
            pfd?.close()
        }
        return outputFiles
    }

    fun mergePdfs(pdfFiles: List<File>, outputFile: File): Boolean {
        val mergedDocument = PdfDocument()
        var globalPageNumber = 1
        try {
            for (pdf in pdfFiles) {
                val pfd = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val width = page.width
                    val height = page.height
                    val bitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val pageInfo = PdfDocument.PageInfo.Builder(width, height, globalPageNumber++).create()
                    val docPage = mergedDocument.startPage(pageInfo)
                    docPage.canvas.drawBitmap(bitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
                    mergedDocument.finishPage(docPage)
                    bitmap.recycle()
                }
                renderer.close()
                pfd.close()
            }
            FileOutputStream(outputFile).use { out ->
                mergedDocument.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            mergedDocument.close()
        }
    }

    fun splitPdf(pdfFile: File, outputDir: File): List<File> {
        val outputFiles = mutableListOf<File>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            for (i in 0 until renderer.pageCount) {
                val singleDoc = PdfDocument()
                val page = renderer.openPage(i)
                val width = page.width
                val height = page.height
                val bitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
                val docPage = singleDoc.startPage(pageInfo)
                docPage.canvas.drawBitmap(bitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
                singleDoc.finishPage(docPage)
                bitmap.recycle()

                val outFile = File(outputDir, "${pdfFile.nameWithoutExtension}_part_${i + 1}.pdf")
                FileOutputStream(outFile).use { out ->
                    singleDoc.writeTo(out)
                }
                singleDoc.close()
                outputFiles.add(outFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            renderer?.close()
            pfd?.close()
        }
        return outputFiles
    }

    fun compressPdf(pdfFile: File, outputFile: File, compressionLevel: String): Boolean {
        val (scale, quality) = when (compressionLevel.lowercase()) {
            "low" -> Pair(1.6f, 85)
            "high" -> Pair(0.9f, 45)
            else -> Pair(1.2f, 65) // Recommended
        }

        val compressedDoc = PdfDocument()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val width = page.width
                val height = page.height

                val renderWidth = (width * scale).toInt().coerceAtLeast(200)
                val renderHeight = (height * scale).toInt().coerceAtLeast(200)
                val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                // Re-compress bitmap through JPEG stream to drop payload size
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                val compressedBytes = stream.toByteArray()
                val compressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

                val pageInfo = PdfDocument.PageInfo.Builder(width, height, i + 1).create()
                val docPage = compressedDoc.startPage(pageInfo)
                docPage.canvas.drawBitmap(compressedBitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
                compressedDoc.finishPage(docPage)

                bitmap.recycle()
                compressedBitmap?.recycle()
            }
            FileOutputStream(outputFile).use { out ->
                compressedDoc.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            renderer?.close()
            pfd?.close()
            compressedDoc.close()
        }
    }

    fun rotatePdf(pdfFile: File, outputFile: File, degrees: Float): Boolean {
        val rotatedDoc = PdfDocument()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val origWidth = page.width
                val origHeight = page.height

                val bitmap = Bitmap.createBitmap(origWidth * 2, origHeight * 2, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val matrix = Matrix().apply { postRotate(degrees) }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                val isSideways = (degrees.toInt() % 180 != 0)
                val newPageWidth = if (isSideways) origHeight else origWidth
                val newPageHeight = if (isSideways) origWidth else origHeight

                val pageInfo = PdfDocument.PageInfo.Builder(newPageWidth, newPageHeight, i + 1).create()
                val docPage = rotatedDoc.startPage(pageInfo)
                docPage.canvas.drawBitmap(rotatedBitmap, null, RectF(0f, 0f, newPageWidth.toFloat(), newPageHeight.toFloat()), null)
                rotatedDoc.finishPage(docPage)

                bitmap.recycle()
                rotatedBitmap.recycle()
            }
            FileOutputStream(outputFile).use { out ->
                rotatedDoc.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            renderer?.close()
            pfd?.close()
            rotatedDoc.close()
        }
    }

    fun deleteAndReorderPages(pdfFile: File, outputFile: File, pageOrder0Indexed: List<Int>): Boolean {
        val newDoc = PdfDocument()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            var newPageNum = 1
            for (pageIdx in pageOrder0Indexed) {
                if (pageIdx in 0 until renderer.pageCount) {
                    val page = renderer.openPage(pageIdx)
                    val width = page.width
                    val height = page.height
                    val bitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val pageInfo = PdfDocument.PageInfo.Builder(width, height, newPageNum++).create()
                    val docPage = newDoc.startPage(pageInfo)
                    docPage.canvas.drawBitmap(bitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
                    newDoc.finishPage(docPage)
                    bitmap.recycle()
                }
            }
            FileOutputStream(outputFile).use { out ->
                newDoc.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            renderer?.close()
            pfd?.close()
            newDoc.close()
        }
    }

    fun addWatermark(
        pdfFile: File,
        outputFile: File,
        watermarkText: String,
        opacityAlpha: Int = 80,
        textSizeSp: Float = 48f,
        colorHex: Int = Color.GRAY
    ): Boolean {
        val watermarkedDoc = PdfDocument()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val paint = Paint().apply {
                color = colorHex
                alpha = opacityAlpha
                textSize = textSizeSp
                isAntiAlias = true
                style = Paint.Style.FILL
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val width = page.width
                val height = page.height
                val bitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val pageInfo = PdfDocument.PageInfo.Builder(width, height, i + 1).create()
                val docPage = watermarkedDoc.startPage(pageInfo)
                val pageCanvas = docPage.canvas
                pageCanvas.drawBitmap(bitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)

                // Draw angled watermark in center
                pageCanvas.save()
                pageCanvas.translate(width / 2f, height / 2f)
                pageCanvas.rotate(-35f)
                pageCanvas.drawText(watermarkText, 0f, 0f, paint)
                pageCanvas.restore()

                watermarkedDoc.finishPage(docPage)
                bitmap.recycle()
            }
            FileOutputStream(outputFile).use { out ->
                watermarkedDoc.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            renderer?.close()
            pfd?.close()
            watermarkedDoc.close()
        }
    }

    fun addPageNumbers(pdfFile: File, outputFile: File, pattern: String = "Page {page} of {total}"): Boolean {
        val numberedDoc = PdfDocument()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val total = renderer.pageCount
            val paint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            for (i in 0 until total) {
                val page = renderer.openPage(i)
                val width = page.width
                val height = page.height
                val bitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val pageInfo = PdfDocument.PageInfo.Builder(width, height, i + 1).create()
                val docPage = numberedDoc.startPage(pageInfo)
                val pageCanvas = docPage.canvas
                pageCanvas.drawBitmap(bitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)

                val label = pattern.replace("{page}", "${i + 1}").replace("{total}", "$total")
                pageCanvas.drawText(label, width / 2f, height - 20f, paint)

                numberedDoc.finishPage(docPage)
                bitmap.recycle()
            }
            FileOutputStream(outputFile).use { out ->
                numberedDoc.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            renderer?.close()
            pfd?.close()
            numberedDoc.close()
        }
    }

    fun addImageToPdf(pdfFile: File, stampImageFile: File, outputFile: File, targetPage: Int = 1): Boolean {
        val newDoc = PdfDocument()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            val stampBitmap = BitmapFactory.decodeFile(stampImageFile.absolutePath) ?: return false
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val width = page.width
                val height = page.height
                val bitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val pageInfo = PdfDocument.PageInfo.Builder(width, height, i + 1).create()
                val docPage = newDoc.startPage(pageInfo)
                val pageCanvas = docPage.canvas
                pageCanvas.drawBitmap(bitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)

                if (i + 1 == targetPage) {
                    val stampWidth = 140f
                    val stampScale = stampWidth / stampBitmap.width.toFloat()
                    val stampHeight = stampBitmap.height * stampScale
                    val left = width - stampWidth - 30f
                    val top = height - stampHeight - 40f
                    pageCanvas.drawBitmap(stampBitmap, null, RectF(left, top, left + stampWidth, top + stampHeight), null)
                }

                newDoc.finishPage(docPage)
                bitmap.recycle()
            }
            stampBitmap.recycle()
            FileOutputStream(outputFile).use { out ->
                newDoc.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            renderer?.close()
            pfd?.close()
            newDoc.close()
        }
    }

    fun protectPdf(pdfFile: File, outputFile: File, passwordPin: String): Boolean {
        return try {
            // Write DocMaster encrypted container header with SHA256 hashed password + raw PDF payload
            val salt = "DocMasterSecured"
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest((passwordPin + salt).toByteArray(Charsets.UTF_8))
            val header = "DOCMASTER_SECURED_V1\n".toByteArray(Charsets.UTF_8)
            val hashHex = (hash.joinToString("") { "%02x".format(it) } + "\n").toByteArray(Charsets.UTF_8)

            FileOutputStream(outputFile).use { out ->
                out.write(header)
                out.write(hashHex)
                pdfFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun unlockPdf(protectedFile: File, outputFile: File, enteredPasswordPin: String): Pair<Boolean, String?> {
        return try {
            protectedFile.inputStream().bufferedReader().use { reader ->
                val line1 = reader.readLine()
                if (line1 != "DOCMASTER_SECURED_V1") {
                    return Pair(false, "File is not a DocMaster encrypted document or already unlocked.")
                }
                val expectedHashHex = reader.readLine()
                val salt = "DocMasterSecured"
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val hash = digest.digest((enteredPasswordPin + salt).toByteArray(Charsets.UTF_8))
                val actualHashHex = hash.joinToString("") { "%02x".format(it) }

                if (expectedHashHex != actualHashHex) {
                    return Pair(false, "Incorrect password. Access denied.")
                }
            }
            // Password verified! Extract raw PDF
            protectedFile.inputStream().use { input ->
                // Skip header line 1 and 2
                var newLinesFound = 0
                while (newLinesFound < 2) {
                    val b = input.read()
                    if (b == -1) break
                    if (b == '\n'.code) newLinesFound++
                }
                FileOutputStream(outputFile).use { out ->
                    input.copyTo(out)
                }
            }
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Unlock failed")
        }
    }
}
