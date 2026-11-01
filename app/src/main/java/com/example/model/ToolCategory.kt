package com.example.model

enum class ToolCategory(val title: String, val subtitle: String) {
    ALL("All Tools", "Complete document & image utility suite"),
    PDF("PDF Tools", "Merge, split, compress, watermark & convert"),
    IMAGE("Image Tools", "Convert, resize, compress, crop & merge"),
    WORD("Word Tools", "View DOC/DOCX & convert to PDF"),
    EXCEL("Excel Tools", "Spreadsheet viewer, filter, sort & export"),
    POWERPOINT("PowerPoint Tools", "Slide presentation viewer & export"),
    VIEWER("Document Viewer", "Universal in-app multi-format viewer"),
    CONVERSION("File Conversion", "Cross-format document & image converter"),
    COMPRESSION("Compression", "Smart file size reduction engine")
}
