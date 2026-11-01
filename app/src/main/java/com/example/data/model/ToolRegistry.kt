package com.example.data.model

object ToolRegistry {
    val allTools: List<DocTool> = listOf(
        // PDF Tools
        DocTool(
            id = DocToolId.MERGE_PDF,
            category = ToolCategory.PDF,
            title = "Merge PDF",
            description = "Combine multiple PDF documents into one seamless file",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = true
        ),
        DocTool(
            id = DocToolId.SPLIT_PDF,
            category = ToolCategory.PDF,
            title = "Split PDF",
            description = "Extract individual pages or page ranges into separate PDFs",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.COMPRESS_PDF,
            category = ToolCategory.PDF,
            title = "Compress PDF",
            description = "Reduce PDF file size with adjustable quality presets",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.ROTATE_PDF,
            category = ToolCategory.PDF,
            title = "Rotate PDF",
            description = "Rotate PDF pages 90°, 180°, or 270° clockwise",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.DELETE_PDF_PAGES,
            category = ToolCategory.PDF,
            title = "Delete PDF Pages",
            description = "Remove unwanted pages from your PDF document",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.REORDER_PDF_PAGES,
            category = ToolCategory.PDF,
            title = "Reorder PDF Pages",
            description = "Rearrange and reorder pages in any sequence",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.EXTRACT_PDF_PAGES,
            category = ToolCategory.PDF,
            title = "Extract Pages",
            description = "Extract selected pages into a new compact PDF",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.ADD_PAGE_NUMBERS,
            category = ToolCategory.PDF,
            title = "Add Page Numbers",
            description = "Insert clear page numbers at the bottom or top of your PDF",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.ADD_WATERMARK,
            category = ToolCategory.PDF,
            title = "Add Watermark",
            description = "Overlay custom text watermark with angle and opacity control",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.PROTECT_PDF,
            category = ToolCategory.PDF,
            title = "Protect PDF",
            description = "Encrypt your document with password security",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.UNLOCK_PDF,
            category = ToolCategory.PDF,
            title = "Unlock PDF",
            description = "Decrypt and remove password restrictions from your file",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.ADD_IMAGE_TO_PDF,
            category = ToolCategory.PDF,
            title = "Add Image to PDF",
            description = "Stamp signatures, photos, or logos into existing PDF pages",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),

        // Conversion Tools
        DocTool(
            id = DocToolId.PDF_TO_JPG,
            category = ToolCategory.CONVERSION,
            title = "PDF to JPG",
            description = "Convert each PDF page into high-resolution JPG images",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.PDF_TO_PNG,
            category = ToolCategory.CONVERSION,
            title = "PDF to PNG",
            description = "Convert PDF pages into lossless PNG graphics",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.PDF_TO_WORD,
            category = ToolCategory.CONVERSION,
            title = "PDF to Word",
            description = "Convert PDF documents to editable Microsoft Word (.docx)",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.PDF_TO_EXCEL,
            category = ToolCategory.CONVERSION,
            title = "PDF to Excel",
            description = "Extract tabular data from PDF into spreadsheet format",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.PDF_TO_PPT,
            category = ToolCategory.CONVERSION,
            title = "PDF to PowerPoint",
            description = "Generate a PowerPoint (.pptx) deck from your PDF pages",
            acceptedMimeTypes = listOf("application/pdf"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.WORD_TO_PDF,
            category = ToolCategory.CONVERSION,
            title = "Word to PDF",
            description = "Convert Microsoft Word (.docx) documents to PDF",
            acceptedMimeTypes = listOf(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword"
            ),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.EXCEL_TO_PDF,
            category = ToolCategory.CONVERSION,
            title = "Excel to PDF",
            description = "Render spreadsheet tables and sheets directly to PDF",
            acceptedMimeTypes = listOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
            ),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.EXCEL_TO_CSV,
            category = ToolCategory.CONVERSION,
            title = "Excel to CSV",
            description = "Export spreadsheet data to comma-separated values (CSV)",
            acceptedMimeTypes = listOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
            ),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.PPT_TO_PDF,
            category = ToolCategory.CONVERSION,
            title = "PowerPoint to PDF",
            description = "Convert presentation slide decks (.pptx) into printable PDF",
            acceptedMimeTypes = listOf(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-powerpoint"
            ),
            allowsMultiple = false
        ),

        // Image Tools
        DocTool(
            id = DocToolId.IMAGE_TO_PDF,
            category = ToolCategory.IMAGE,
            title = "Image to PDF",
            description = "Convert individual JPG, PNG, or WEBP photos to PDF",
            acceptedMimeTypes = listOf("image/*"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.IMAGES_TO_PDF_MERGE,
            category = ToolCategory.IMAGE,
            title = "Merge Images to PDF",
            description = "Batch merge multiple photos into a single organized PDF album",
            acceptedMimeTypes = listOf("image/*"),
            allowsMultiple = true
        ),
        DocTool(
            id = DocToolId.JPG_TO_PNG,
            category = ToolCategory.IMAGE,
            title = "JPG to PNG",
            description = "Convert JPEG images to high-fidelity transparent PNG",
            acceptedMimeTypes = listOf("image/jpeg", "image/jpg"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.PNG_TO_JPG,
            category = ToolCategory.IMAGE,
            title = "PNG to JPG",
            description = "Convert PNG images to standard JPEG format with solid background",
            acceptedMimeTypes = listOf("image/png"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.RESIZE_IMAGE,
            category = ToolCategory.IMAGE,
            title = "Resize Image",
            description = "Scale image dimensions with custom width, height, and aspect lock",
            acceptedMimeTypes = listOf("image/*"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.COMPRESS_IMAGE,
            category = ToolCategory.IMAGE,
            title = "Compress Image",
            description = "Reduce image file size with live quality slider and savings readout",
            acceptedMimeTypes = listOf("image/*"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.ROTATE_IMAGE,
            category = ToolCategory.IMAGE,
            title = "Rotate Image",
            description = "Rotate photo 90°, 180°, or 270° orientation",
            acceptedMimeTypes = listOf("image/*"),
            allowsMultiple = false
        ),
        DocTool(
            id = DocToolId.CROP_IMAGE,
            category = ToolCategory.IMAGE,
            title = "Crop Image",
            description = "Crop images with aspect ratios (1:1, 4:3, 16:9, or custom)",
            acceptedMimeTypes = listOf("image/*"),
            allowsMultiple = false
        ),

        // Document Viewers
        DocTool(
            id = DocToolId.DOCUMENT_VIEWER,
            category = ToolCategory.VIEWER,
            title = "Universal Viewer",
            description = "Preview PDF, Word, Excel, PowerPoint, and TXT files instantly",
            acceptedMimeTypes = listOf("*/*"),
            allowsMultiple = false,
            isViewerDirect = true
        ),
        DocTool(
            id = DocToolId.EXCEL_VIEWER,
            category = ToolCategory.EXCEL,
            title = "Excel Viewer & Editor",
            description = "Interactive spreadsheet viewer with multiple sheets, search, and sorting",
            acceptedMimeTypes = listOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel",
                "text/csv"
            ),
            allowsMultiple = false,
            isViewerDirect = true
        ),
        DocTool(
            id = DocToolId.PPT_VIEWER,
            category = ToolCategory.PPT,
            title = "PowerPoint Viewer",
            description = "Full presentation slide carousel with fullscreen viewing mode",
            acceptedMimeTypes = listOf(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-powerpoint"
            ),
            allowsMultiple = false,
            isViewerDirect = true
        ),
        DocTool(
            id = DocToolId.WORD_VIEWER,
            category = ToolCategory.WORD,
            title = "Word Document Reader",
            description = "Clean reader for Microsoft Word (.docx) and rich documents",
            acceptedMimeTypes = listOf(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword",
                "text/plain"
            ),
            allowsMultiple = false,
            isViewerDirect = true
        )
    )

    fun getTool(id: DocToolId): DocTool {
        return allTools.first { it.id == id }
    }
}
