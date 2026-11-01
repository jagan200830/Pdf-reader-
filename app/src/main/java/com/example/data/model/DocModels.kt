package com.example.data.model

import android.graphics.Bitmap
import android.net.Uri

data class DocItem(
    val id: String,
    val name: String,
    val uri: Uri,
    val sizeBytes: Long,
    val mimeType: String,
    val pageCount: Int = 1,
    val thumbnailBitmap: Bitmap? = null
)

enum class ToolCategory(val displayName: String) {
    ALL("All Tools"),
    PDF("PDF Tools"),
    IMAGE("Image Tools"),
    WORD("Word Tools"),
    EXCEL("Excel Tools"),
    PPT("PowerPoint Tools"),
    CONVERSION("File Conversion"),
    COMPRESSION("File Compression"),
    VIEWER("Document Viewer")
}

enum class DocToolId {
    // Image Tools
    IMAGE_TO_PDF,
    PDF_TO_JPG,
    PDF_TO_PNG,
    IMAGES_TO_PDF_MERGE,
    JPG_TO_PNG,
    PNG_TO_JPG,
    RESIZE_IMAGE,
    COMPRESS_IMAGE,
    ROTATE_IMAGE,
    CROP_IMAGE,

    // PDF Tools
    MERGE_PDF,
    SPLIT_PDF,
    COMPRESS_PDF,
    ROTATE_PDF,
    DELETE_PDF_PAGES,
    REORDER_PDF_PAGES,
    EXTRACT_PDF_PAGES,
    ADD_PAGE_NUMBERS,
    ADD_WATERMARK,
    PROTECT_PDF,
    UNLOCK_PDF,
    PDF_TO_WORD,
    PDF_TO_EXCEL,
    PDF_TO_PPT,
    ADD_IMAGE_TO_PDF,

    // Excel Tools
    EXCEL_VIEWER,
    EXCEL_TO_PDF,
    EXCEL_TO_CSV,

    // PowerPoint Tools
    PPT_VIEWER,
    PPT_TO_PDF,

    // Word Tools
    WORD_VIEWER,
    WORD_TO_PDF,

    // General Viewer
    DOCUMENT_VIEWER
}

data class DocTool(
    val id: DocToolId,
    val category: ToolCategory,
    val title: String,
    val description: String,
    val acceptedMimeTypes: List<String>,
    val allowsMultiple: Boolean = false,
    val isViewerDirect: Boolean = false
)

data class ProcessedOutputFile(
    val name: String,
    val uri: Uri,
    val filePath: String,
    val sizeBytes: Long,
    val mimeType: String,
    val previewBitmap: Bitmap? = null
)

data class ProcessingResult(
    val isSuccess: Boolean,
    val toolTitle: String,
    val outputFiles: List<ProcessedOutputFile> = emptyList(),
    val originalSizeBytes: Long = 0,
    val processedSizeBytes: Long = 0,
    val compressionPercentage: Int = 0,
    val message: String = ""
)

enum class CompressionLevel(val label: String, val factor: Float, val description: String) {
    LOW("Low Compression", 0.85f, "Highest visual quality, smaller size reduction"),
    RECOMMENDED("Recommended", 0.55f, "Optimal balance of clarity and file size"),
    HIGH("High Compression", 0.30f, "Maximum compression, smallest file size")
}
