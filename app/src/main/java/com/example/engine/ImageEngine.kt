package com.example.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import java.io.File
import java.io.FileOutputStream

object ImageEngine {

    fun convertFormat(inputFile: File, outputFile: File, targetFormat: Bitmap.CompressFormat, quality: Int = 92): Boolean {
        return try {
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return false
            FileOutputStream(outputFile).use { out ->
                if (targetFormat == Bitmap.CompressFormat.JPEG) {
                    // JPEG has no transparency, ensure white background
                    val cleanBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(cleanBitmap)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    cleanBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    cleanBitmap.recycle()
                } else {
                    bitmap.compress(targetFormat, quality, out)
                }
            }
            bitmap.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resizeImage(inputFile: File, outputFile: File, targetWidth: Int, targetHeight: Int, quality: Int = 90): Boolean {
        return try {
            val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return false
            val scaled = Bitmap.createScaledBitmap(original, targetWidth.coerceAtLeast(10), targetHeight.coerceAtLeast(10), true)
            val format = if (outputFile.extension.lowercase() == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            FileOutputStream(outputFile).use { out ->
                scaled.compress(format, quality, out)
            }
            if (scaled != original) scaled.recycle()
            original.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun compressImage(inputFile: File, outputFile: File, qualityPercent: Int): Boolean {
        return try {
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return false
            val format = if (inputFile.extension.lowercase() == "png") {
                // If compressing PNG, use WebP or JPEG for massive reduction, or JPEG if requested
                Bitmap.CompressFormat.JPEG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            FileOutputStream(outputFile).use { out ->
                if (format == Bitmap.CompressFormat.JPEG && bitmap.hasAlpha()) {
                    val whiteBgBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(whiteBgBitmap)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    whiteBgBitmap.compress(Bitmap.CompressFormat.JPEG, qualityPercent.coerceIn(5, 100), out)
                    whiteBgBitmap.recycle()
                } else {
                    bitmap.compress(format, qualityPercent.coerceIn(5, 100), out)
                }
            }
            bitmap.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun rotateImage(inputFile: File, outputFile: File, degrees: Float, flipHorizontal: Boolean = false): Boolean {
        return try {
            val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return false
            val matrix = Matrix().apply {
                if (flipHorizontal) postScale(-1f, 1f, original.width / 2f, original.height / 2f)
                postRotate(degrees)
            }
            val transformed = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
            val format = if (outputFile.extension.lowercase() == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            FileOutputStream(outputFile).use { out ->
                transformed.compress(format, 92, out)
            }
            transformed.recycle()
            original.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun cropImage(inputFile: File, outputFile: File, aspectX: Int, aspectY: Int): Boolean {
        return try {
            val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return false
            val origWidth = original.width
            val origHeight = original.height

            var cropWidth = origWidth
            var cropHeight = origHeight

            if (aspectX > 0 && aspectY > 0) {
                val targetRatio = aspectX.toFloat() / aspectY.toFloat()
                val currentRatio = origWidth.toFloat() / origHeight.toFloat()

                if (currentRatio > targetRatio) {
                    cropWidth = (origHeight * targetRatio).toInt()
                } else {
                    cropHeight = (origWidth / targetRatio).toInt()
                }
            }

            val startX = (origWidth - cropWidth) / 2
            val startY = (origHeight - cropHeight) / 2

            val cropped = Bitmap.createBitmap(original, startX, startY, cropWidth, cropHeight)
            val format = if (outputFile.extension.lowercase() == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            FileOutputStream(outputFile).use { out ->
                cropped.compress(format, 92, out)
            }
            cropped.recycle()
            original.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
