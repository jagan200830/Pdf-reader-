package com.example.model

import android.graphics.Bitmap
import android.net.Uri

data class DocumentItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val uri: Uri = Uri.EMPTY,
    val sizeBytes: Long = 0L,
    val mimeType: String = "*/*",
    val pageCount: Int = 1,
    val thumbnailBitmap: Bitmap? = null,
    val tempFilePath: String? = null
) {
    fun nameWithoutExtension(): String = name.substringBeforeLast(".")
}
