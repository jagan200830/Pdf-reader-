package com.example.model

import java.io.File

data class ProcessingResult(
    val success: Boolean,
    val title: String,
    val message: String,
    val originalSizeBytes: Long,
    val processedSizeBytes: Long,
    val outputFiles: List<File> = emptyList(),
    val error: String? = null
) {
    val compressionRatioPercentage: Int
        get() {
            if (originalSizeBytes <= 0 || processedSizeBytes <= 0) return 0
            if (processedSizeBytes >= originalSizeBytes) return 0
            val saved = originalSizeBytes - processedSizeBytes
            return ((saved.toDouble() / originalSizeBytes.toDouble()) * 100).toInt()
        }
}
