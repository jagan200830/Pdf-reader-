package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ProcessedOutputFile
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageProcessingEngine {

    private fun getOutputDir(context: Context): File {
        val dir = File(context.cacheDir, "docmaster_output")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun loadBitmap(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun convertFormat(
        context: Context,
        uri: Uri,
        targetFormat: Bitmap.CompressFormat,
        extension: String,
        outputName: String? = null
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val originalBitmap = loadBitmap(context, uri) ?: throw IllegalStateException("Cannot decode image")
        val finalName = outputName ?: "converted_image_${System.currentTimeMillis()}.$extension"
        val outputFile = File(getOutputDir(context), finalName)

        val workingBitmap = if (targetFormat == Bitmap.CompressFormat.JPEG && originalBitmap.hasAlpha()) {
            val nonAlpha = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(nonAlpha)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(originalBitmap, 0f, 0f, null)
            nonAlpha
        } else {
            originalBitmap
        }

        FileOutputStream(outputFile).use { out ->
            workingBitmap.compress(targetFormat, 95, out)
        }

        val thumb = Bitmap.createScaledBitmap(
            workingBitmap,
            300,
            (300f / workingBitmap.width * workingBitmap.height).toInt().coerceAtLeast(10),
            true
        )

        if (workingBitmap != originalBitmap) workingBitmap.recycle()
        originalBitmap.recycle()

        val mime = when (extension) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = mime,
            previewBitmap = thumb
        )
    }

    suspend fun resizeImage(
        context: Context,
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int,
        outputName: String = "resized_image.png"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val originalBitmap = loadBitmap(context, uri) ?: throw IllegalStateException("Cannot decode image")
        val outputFile = File(getOutputDir(context), outputName)

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
        FileOutputStream(outputFile).use { out ->
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }

        val thumb = Bitmap.createScaledBitmap(resizedBitmap, 300, (300f / targetWidth * targetHeight).toInt().coerceAtLeast(10), true)
        originalBitmap.recycle()
        resizedBitmap.recycle()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "image/png",
            previewBitmap = thumb
        )
    }

    suspend fun compressImage(
        context: Context,
        uri: Uri,
        qualityPercentage: Int, // 10 to 100
        outputName: String = "compressed_image.jpg"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val originalBitmap = loadBitmap(context, uri) ?: throw IllegalStateException("Cannot decode image")
        val outputFile = File(getOutputDir(context), outputName)

        // Convert alpha to white background if saving as JPEG
        val workingBitmap = if (originalBitmap.hasAlpha()) {
            val nonAlpha = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(nonAlpha)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(originalBitmap, 0f, 0f, null)
            nonAlpha
        } else {
            originalBitmap
        }

        FileOutputStream(outputFile).use { out ->
            workingBitmap.compress(Bitmap.CompressFormat.JPEG, qualityPercentage.coerceIn(5, 100), out)
        }

        val thumb = Bitmap.createScaledBitmap(
            workingBitmap,
            300,
            (300f / workingBitmap.width * workingBitmap.height).toInt().coerceAtLeast(10),
            true
        )
        if (workingBitmap != originalBitmap) workingBitmap.recycle()
        originalBitmap.recycle()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "image/jpeg",
            previewBitmap = thumb
        )
    }

    suspend fun rotateImage(
        context: Context,
        uri: Uri,
        angleDegrees: Float,
        outputName: String = "rotated_image.png"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val originalBitmap = loadBitmap(context, uri) ?: throw IllegalStateException("Cannot decode image")
        val outputFile = File(getOutputDir(context), outputName)

        val matrix = Matrix().apply { postRotate(angleDegrees) }
        val rotated = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

        FileOutputStream(outputFile).use { out ->
            rotated.compress(Bitmap.CompressFormat.PNG, 95, out)
        }

        val thumb = Bitmap.createScaledBitmap(rotated, 300, (300f / rotated.width * rotated.height).toInt().coerceAtLeast(10), true)
        originalBitmap.recycle()
        rotated.recycle()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "image/png",
            previewBitmap = thumb
        )
    }

    suspend fun cropImage(
        context: Context,
        uri: Uri,
        cropRectFraction: Rect, // left, top, right, bottom in 0..1000 scale
        outputName: String = "cropped_image.png"
    ): ProcessedOutputFile = withContext(Dispatchers.IO) {
        val originalBitmap = loadBitmap(context, uri) ?: throw IllegalStateException("Cannot decode image")
        val outputFile = File(getOutputDir(context), outputName)

        val x = (cropRectFraction.left / 1000f * originalBitmap.width).toInt().coerceIn(0, originalBitmap.width - 10)
        val y = (cropRectFraction.top / 1000f * originalBitmap.height).toInt().coerceIn(0, originalBitmap.height - 10)
        val w = ((cropRectFraction.right - cropRectFraction.left) / 1000f * originalBitmap.width).toInt().coerceIn(10, originalBitmap.width - x)
        val h = ((cropRectFraction.bottom - cropRectFraction.top) / 1000f * originalBitmap.height).toInt().coerceIn(10, originalBitmap.height - y)

        val cropped = Bitmap.createBitmap(originalBitmap, x, y, w, h)
        FileOutputStream(outputFile).use { out ->
            cropped.compress(Bitmap.CompressFormat.PNG, 95, out)
        }

        val thumb = Bitmap.createScaledBitmap(cropped, 300, (300f / w * h).toInt().coerceAtLeast(10), true)
        originalBitmap.recycle()
        cropped.recycle()

        val outUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
        ProcessedOutputFile(
            name = outputFile.name,
            uri = outUri,
            filePath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
            mimeType = "image/png",
            previewBitmap = thumb
        )
    }
}
