package com.example.model

import android.graphics.Bitmap
import android.net.Uri

data class DocumentItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val uri: Uri,
    val sizeBytes: Long,
    val mimeType: String,
    val pageCount: Int = 1,
    val thumbnailBitmap: Bitmap? = null,
    val tempFilePath: String? = null
)
