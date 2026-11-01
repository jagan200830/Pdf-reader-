package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import com.example.data.model.CompressionLevel
import com.example.data.model.ProcessedOutputFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfProcessingEngine {

    private fun getOutputDir(context: Context): File {
        val dir = File(context.cacheDir, "docmaster_output")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun copyUriToTempFile(context: Context, uri: Uri, tempName: String): File {
        val tempFile = File(context.cacheDir, tempName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    suspend fun getPdfPageCount(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                renderer = PdfRenderer(pfd)
                return@withContext renderer.pageCount
            }
            0
        } catch (e: Exception) {
            0
        } finally {
            renderer?.close()
            pfd?.close()
        }
    }

    suspend fun renderPdfPage(
        context: Context,
        uri: Uri,
        pageIndex: Int,
        targetWidth: Int = 1000
    ): Bitmap? = withContext(Dispatchers.IO) {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null
        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext null
            renderer = PdfRenderer(pfd)
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null
            page = renderer.openPage(pageIndex)

            val width = targetWidth
            val height = (targetWidth.toFloat() / page.width * page.height).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (e: Exception) {
            null
        } finally {
            page?.close()
            renderer?.close()
            pfd?.close()
        }
    }

    suspend fun mergePdfs(
        context: Context,
        uris: List<Uri>,
        outputName: String = "merged_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val document = PdfDocument()
        var globalPageIndex = 1
        var firstPageBitmap: Bitmap? = null

        for (uri in uris) {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: continue
            val renderer = PdfRenderer(pfd)
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val width = page.width
                val height = page.height
                val pageInfo = PdfDocument.PageInfo.Builder(width, height, globalPageIndex).create()
                val docPage = document.startPage(pageInfo)

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                docPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(docPage)

                if (firstPageBitmap == null) {
                    firstPageBitmap = Bitmap.createScaledBitmap(bitmap, 300, (300f / width * height).toInt(), true)
                }
                bitmap.recycle()
                page.close()
                globalPageIndex++
            }
            renderer.close()
            pfd.close()
        }

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        val outputUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outputUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf",
            previewBitmap = firstPageBitmap
        )
    }

    suspend fun splitPdf(
        context: Context,
        uri: Uri,
        baseName: String = "page"
    ): List<ProcessedOutputFile> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ProcessedOutputFile>()
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext results
        val renderer = PdfRenderer(pfd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, 1).create()
            val docPage = doc.startPage(pageInfo)

            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            docPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
            doc.finishPage(docPage)

            val outFile = File(getOutputDir(context), "${baseName}_page_${i + 1}.pdf")
            FileOutputStream(outFile).use { doc.writeTo(it) }
            doc.close()

            val thumb = Bitmap.createScaledBitmap(bitmap, 300, (300f / page.width * page.height).toInt(), true)
            bitmap.recycle()
            page.close()

            val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
            results.add(
                ProcessedOutputFile(
                    name = outFile.name,
                    uri = outUri,
                    filePath = outFile.absolutePath,
                    sizeBytes = outFile.length(),
                    mimeType = "application/pdf",
                    previewBitmap = thumb
                )
            )
        }
        renderer.close()
        pfd.close()
        results
    }

    suspend fun compressPdf(
        context: Context,
        uri: Uri,
        level: CompressionLevel,
        outputName: String = "compressed_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalStateException("Cannot open PDF")
        val renderer = PdfRenderer(pfd)
        val document = PdfDocument()
        var firstThumb: Bitmap? = null

        val scaleFactor = when (level) {
            CompressionLevel.LOW -> 0.90f
            CompressionLevel.RECOMMENDED -> 0.70f
            CompressionLevel.HIGH -> 0.50f
        }
        val jpegQuality = when (level) {
            CompressionLevel.LOW -> 85
            CompressionLevel.RECOMMENDED -> 60
            CompressionLevel.HIGH -> 40
        }

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val targetW = (page.width * scaleFactor).toInt().coerceAtLeast(400)
            val targetH = (page.height * scaleFactor).toInt().coerceAtLeast(500)

            val rawBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val rawCanvas = Canvas(rawBitmap)
            rawCanvas.drawColor(Color.WHITE)
            page.render(rawBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            // Compress to JPEG byte array
            val byteStream = java.io.ByteArrayOutputStream()
            rawBitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, byteStream)
            val compressedBytes = byteStream.toByteArray()
            val compressedBitmap = android.graphics.BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
            val docPage = document.startPage(pageInfo)
            val destRect = Rect(0, 0, page.width, page.height)
            docPage.canvas.drawBitmap(compressedBitmap, null, destRect, null)
            document.finishPage(docPage)

            if (firstThumb == null) {
                firstThumb = Bitmap.createScaledBitmap(compressedBitmap, 300, (300f / page.width * page.height).toInt(), true)
            }

            rawBitmap.recycle()
            compressedBitmap.recycle()
            page.close()
        }
        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf",
            previewBitmap = firstThumb
        )
    }

    suspend fun rotatePdf(
        context: Context,
        uri: Uri,
        angleDegrees: Float,
        outputName: String = "rotated_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalStateException("Cannot open PDF")
        val renderer = PdfRenderer(pfd)
        val document = PdfDocument()
        var firstThumb: Bitmap? = null

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val isSideways = (angleDegrees % 180f != 0f)
            val newW = if (isSideways) page.height else page.width
            val newH = if (isSideways) page.width else page.height

            val rawBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val rawCanvas = Canvas(rawBitmap)
            rawCanvas.drawColor(Color.WHITE)
            page.render(rawBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val matrix = Matrix().apply {
                postRotate(angleDegrees)
                if (angleDegrees == 90f) postTranslate(page.height.toFloat(), 0f)
                else if (angleDegrees == 180f) postTranslate(page.width.toFloat(), page.height.toFloat())
                else if (angleDegrees == 270f) postTranslate(0f, page.width.toFloat())
            }

            val rotatedBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, page.width, page.height, matrix, true)
            rawBitmap.recycle()

            val pageInfo = PdfDocument.PageInfo.Builder(newW, newH, i + 1).create()
            val docPage = document.startPage(pageInfo)
            docPage.canvas.drawBitmap(rotatedBitmap, 0f, 0f, null)
            document.finishPage(docPage)

            if (firstThumb == null) {
                firstThumb = Bitmap.createScaledBitmap(rotatedBitmap, 300, (300f / newW * newH).toInt(), true)
            }
            rotatedBitmap.recycle()
            page.close()
        }
        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf",
            previewBitmap = firstThumb
        )
    }

    suspend fun reorderOrDeletePages(
        context: Context,
        uri: Uri,
        includedPageIndices: List<Int>,
        outputName: String = "modified_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalStateException("Cannot open PDF")
        val renderer = PdfRenderer(pfd)
        val document = PdfDocument()
        var firstThumb: Bitmap? = null

        includedPageIndices.forEachIndexed { newIndex, pageIdx ->
            if (pageIdx in 0 until renderer.pageCount) {
                val page = renderer.openPage(pageIdx)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, newIndex + 1).create()
                val docPage = document.startPage(pageInfo)
                docPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(docPage)

                if (firstThumb == null) {
                    firstThumb = Bitmap.createScaledBitmap(bitmap, 300, (300f / page.width * page.height).toInt(), true)
                }
                bitmap.recycle()
                page.close()
            }
        }
        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf",
            previewBitmap = firstThumb
        )
    }

    suspend fun addWatermark(
        context: Context,
        uri: Uri,
        text: String,
        opacity: Float = 0.35f,
        angle: Float = -45f,
        outputName: String = "watermarked_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalStateException("Cannot open PDF")
        val renderer = PdfRenderer(pfd)
        val document = PdfDocument()
        var firstThumb: Bitmap? = null

        val watermarkPaint = Paint().apply {
            color = Color.DKGRAY
            alpha = (opacity * 255).toInt().coerceIn(20, 255)
            textSize = 54f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            // Draw watermark on top
            val cx = page.width / 2f
            val cy = page.height / 2f
            canvas.save()
            canvas.rotate(angle, cx, cy)
            canvas.drawText(text, cx, cy, watermarkPaint)
            canvas.restore()

            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
            val docPage = document.startPage(pageInfo)
            docPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(docPage)

            if (firstThumb == null) {
                firstThumb = Bitmap.createScaledBitmap(bitmap, 300, (300f / page.width * page.height).toInt(), true)
            }
            bitmap.recycle()
            page.close()
        }
        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf",
            previewBitmap = firstThumb
        )
    }

    suspend fun addPageNumbers(
        context: Context,
        uri: Uri,
        format: String = "Page %d of %d",
        outputName: String = "numbered_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalStateException("Cannot open PDF")
        val renderer = PdfRenderer(pfd)
        val document = PdfDocument()
        val total = renderer.pageCount
        var firstThumb: Bitmap? = null

        val numPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        for (i in 0 until total) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val numberText = String.format(format, i + 1, total)
            canvas.drawText(numberText, page.width / 2f, page.height - 24f, numPaint)

            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
            val docPage = document.startPage(pageInfo)
            docPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(docPage)

            if (firstThumb == null) {
                firstThumb = Bitmap.createScaledBitmap(bitmap, 300, (300f / page.width * page.height).toInt(), true)
            }
            bitmap.recycle()
            page.close()
        }
        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf",
            previewBitmap = firstThumb
        )
    }

    suspend fun pdfToImages(
        context: Context,
        uri: Uri,
        format: Bitmap.CompressFormat,
        extension: String,
        baseName: String = "pdf_page"
    ): List<ProcessedOutputFile> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ProcessedOutputFile>()
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext results
        val renderer = PdfRenderer(pfd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val width = 1200
            val height = (1200f / page.width * page.height).toInt().coerceAtLeast(100)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            val outFile = File(getOutputDir(context), "${baseName}_${i + 1}.$extension")
            FileOutputStream(outFile).use { out ->
                bitmap.compress(format, 95, out)
            }

            val thumb = Bitmap.createScaledBitmap(bitmap, 300, (300f / width * height).toInt(), true)
            bitmap.recycle()
            page.close()

            val mime = if (extension == "png") "image/png" else "image/jpeg"
            val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
            results.add(
                ProcessedOutputFile(
                    name = outFile.name,
                    uri = outUri,
                    filePath = outFile.absolutePath,
                    sizeBytes = outFile.length(),
                    mimeType = mime,
                    previewBitmap = thumb
                )
            )
        }
        renderer.close()
        pfd.close()
        results
    }

    suspend fun convertImagesToPdf(
        context: Context,
        imageUris: List<Uri>,
        outputName: String = "images_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val document = PdfDocument()
        var firstThumb: Bitmap? = null

        imageUris.forEachIndexed { index, imgUri ->
            context.contentResolver.openInputStream(imgUri)?.use { stream ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    val pageW = bitmap.width.coerceAtMost(2400)
                    val pageH = bitmap.height.coerceAtMost(3200)
                    val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, index + 1).create()
                    val page = document.startPage(pageInfo)

                    val dest = Rect(0, 0, pageW, pageH)
                    page.canvas.drawBitmap(bitmap, null, dest, null)
                    document.finishPage(page)

                    if (firstThumb == null) {
                        firstThumb = Bitmap.createScaledBitmap(bitmap, 300, (300f / bitmap.width * bitmap.height).toInt(), true)
                    }
                    bitmap.recycle()
                }
            }
        }

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf",
            previewBitmap = firstThumb
        )
    }

    suspend fun stampImageOntoPdf(
        context: Context,
        pdfUri: Uri,
        imageUri: Uri,
        outputName: String = "stamped_docmaster.pdf"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val outputFile = File(getOutputDir(context), outputName)
        val stampBitmap = context.contentResolver.openInputStream(imageUri)?.use {
            android.graphics.BitmapFactory.decodeStream(it)
        } ?: throw IllegalStateException("Cannot load stamp image")

        val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: throw IllegalStateException("Cannot open PDF")
        val renderer = PdfRenderer(pfd)
        val document = PdfDocument()
        var firstThumb: Bitmap? = null

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            // Stamp bottom-right corner
            val stampW = (page.width * 0.25f).toInt()
            val stampH = (stampW.toFloat() / stampBitmap.width * stampBitmap.height).toInt()
            val stampRect = Rect(
                page.width - stampW - 30,
                page.height - stampH - 30,
                page.width - 30,
                page.height - 30
            )
            canvas.drawBitmap(stampBitmap, null, stampRect, null)

            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
            val docPage = document.startPage(pageInfo)
            docPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(docPage)

            if (firstThumb == null) {
                firstThumb = Bitmap.createScaledBitmap(bitmap, 300, (300f / page.width * page.height).toInt(), true)
            }
            bitmap.recycle()
            page.close()
        }
        renderer.close()
        pfd.close()
        stampBitmap.recycle()

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "application/pdf",
            previewBitmap = firstThumb
        )
    }
}
