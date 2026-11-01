package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Archive
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.ColorExcel
import com.example.ui.theme.ColorImage
import com.example.ui.theme.ColorPdf
import com.example.ui.theme.ColorPpt
import com.example.ui.theme.ColorWord
import com.example.ui.theme.ColorZip
import com.example.ui.theme.IndigoPrimary

enum class ToolId {
    // PDF Tools
    IMAGE_TO_PDF,
    PDF_TO_JPG,
    PDF_TO_PNG,
    MERGE_PDF,
    SPLIT_PDF,
    COMPRESS_PDF,
    ROTATE_PDF,
    DELETE_REORDER_PAGES,
    EXTRACT_PAGES,
    ADD_PAGE_NUMBERS,
    ADD_WATERMARK,
    PROTECT_PDF,
    UNLOCK_PDF,
    PDF_TO_WORD,
    PDF_TO_EXCEL,
    PDF_TO_PPT,
    ADD_IMAGE_TO_PDF,

    // Image Tools
    JPG_TO_PNG,
    PNG_TO_JPG,
    RESIZE_IMAGE,
    COMPRESS_IMAGE,
    ROTATE_IMAGE,
    CROP_IMAGE,
    MERGE_IMAGES_TO_PDF,

    // Office & Viewers
    VIEW_EXCEL,
    VIEW_WORD,
    VIEW_PPT,
    VIEW_DOCUMENT,
    EXCEL_TO_CSV,
    EXCEL_TO_PDF,
    WORD_TO_PDF,
    PPT_TO_PDF,
    BATCH_ZIP
}

data class DocTool(
    val id: ToolId,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val accentColor: Color,
    val acceptedMimeTypes: List<String>,
    val allowsMultipleFiles: Boolean = false,
    val isViewerOnly: Boolean = false
) {
    companion object {
        val ALL_TOOLS: List<DocTool> = listOf(
            // PDF Suite
            DocTool(
                id = ToolId.IMAGE_TO_PDF,
                title = "Image to PDF",
                description = "Convert one or multiple JPG/PNG images into a PDF",
                category = ToolCategory.PDF,
                icon = Icons.Default.PictureAsPdf,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("image/jpeg", "image/png", "image/webp", "image/*"),
                allowsMultipleFiles = true
            ),
            DocTool(
                id = ToolId.PDF_TO_JPG,
                title = "PDF to JPG",
                description = "Extract pages of your PDF document into crisp JPG images",
                category = ToolCategory.PDF,
                icon = Icons.Default.Image,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.PDF_TO_PNG,
                title = "PDF to PNG",
                description = "Extract PDF pages into lossless transparent-capable PNG files",
                category = ToolCategory.PDF,
                icon = Icons.Default.Image,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.MERGE_PDF,
                title = "Merge PDF",
                description = "Combine multiple PDF documents into a single unified file",
                category = ToolCategory.PDF,
                icon = Icons.Default.MergeType,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf"),
                allowsMultipleFiles = true
            ),
            DocTool(
                id = ToolId.SPLIT_PDF,
                title = "Split PDF",
                description = "Split a PDF into individual single-page documents",
                category = ToolCategory.PDF,
                icon = Icons.Default.CallSplit,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.COMPRESS_PDF,
                title = "Compress PDF",
                description = "Reduce PDF file size with Low, Recommended, or High compression",
                category = ToolCategory.COMPRESSION,
                icon = Icons.Default.PhotoSizeSelectLarge,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.ROTATE_PDF,
                title = "Rotate PDF Pages",
                description = "Rotate PDF pages 90°, 180°, or 270° clockwise",
                category = ToolCategory.PDF,
                icon = Icons.Default.RotateRight,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.DELETE_REORDER_PAGES,
                title = "Delete & Reorder Pages",
                description = "Remove unwanted pages or re-sequence your PDF document",
                category = ToolCategory.PDF,
                icon = Icons.Default.Description,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.EXTRACT_PAGES,
                title = "Extract Pages",
                description = "Select and extract specific page numbers into a new document",
                category = ToolCategory.PDF,
                icon = Icons.Default.FindInPage,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.ADD_PAGE_NUMBERS,
                title = "Add Page Numbers",
                description = "Stamp clean page numbering in your preferred position",
                category = ToolCategory.PDF,
                icon = Icons.Default.FormatListNumbered,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.ADD_WATERMARK,
                title = "Add Watermark",
                description = "Overlay custom text watermark with rotation and opacity controls",
                category = ToolCategory.PDF,
                icon = Icons.Default.WaterDrop,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.PROTECT_PDF,
                title = "Protect PDF",
                description = "Encrypt your PDF document with secure user password",
                category = ToolCategory.PDF,
                icon = Icons.Default.Lock,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.UNLOCK_PDF,
                title = "Unlock PDF",
                description = "Remove password protection from authorized PDF documents",
                category = ToolCategory.PDF,
                icon = Icons.Default.LockOpen,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.PDF_TO_WORD,
                title = "PDF to Word (DOCX)",
                description = "Convert PDF text and structure into editable Word document",
                category = ToolCategory.CONVERSION,
                icon = Icons.Default.Description,
                accentColor = ColorWord,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.PDF_TO_EXCEL,
                title = "PDF to Excel / CSV",
                description = "Extract tabular PDF content into structured spreadsheet rows",
                category = ToolCategory.CONVERSION,
                icon = Icons.Default.TableChart,
                accentColor = ColorExcel,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.PDF_TO_PPT,
                title = "PDF to PowerPoint",
                description = "Transform PDF pages into a presentation slide deck",
                category = ToolCategory.CONVERSION,
                icon = Icons.Default.PresentToAll,
                accentColor = ColorPpt,
                acceptedMimeTypes = listOf("application/pdf")
            ),
            DocTool(
                id = ToolId.ADD_IMAGE_TO_PDF,
                title = "Add Image to PDF",
                description = "Insert signature or stamp image onto a PDF document",
                category = ToolCategory.PDF,
                icon = Icons.Default.Image,
                accentColor = ColorPdf,
                acceptedMimeTypes = listOf("application/pdf")
            ),

            // Image Suite
            DocTool(
                id = ToolId.JPG_TO_PNG,
                title = "JPG to PNG",
                description = "Convert JPG pictures into lossless PNG format",
                category = ToolCategory.IMAGE,
                icon = Icons.Default.Transform,
                accentColor = ColorImage,
                acceptedMimeTypes = listOf("image/jpeg")
            ),
            DocTool(
                id = ToolId.PNG_TO_JPG,
                title = "PNG to JPG",
                description = "Convert PNG images into optimized JPG files",
                category = ToolCategory.IMAGE,
                icon = Icons.Default.Transform,
                accentColor = ColorImage,
                acceptedMimeTypes = listOf("image/png")
            ),
            DocTool(
                id = ToolId.RESIZE_IMAGE,
                title = "Resize Image",
                description = "Adjust dimensions by percentage or custom width & height",
                category = ToolCategory.IMAGE,
                icon = Icons.Default.PhotoSizeSelectLarge,
                accentColor = ColorImage,
                acceptedMimeTypes = listOf("image/jpeg", "image/png", "image/webp", "image/*")
            ),
            DocTool(
                id = ToolId.COMPRESS_IMAGE,
                title = "Compress Image",
                description = "Shrink image file size with interactive quality slider",
                category = ToolCategory.COMPRESSION,
                icon = Icons.Default.PhotoSizeSelectLarge,
                accentColor = ColorImage,
                acceptedMimeTypes = listOf("image/jpeg", "image/png", "image/webp", "image/*")
            ),
            DocTool(
                id = ToolId.ROTATE_IMAGE,
                title = "Rotate Image",
                description = "Rotate photo 90°, 180°, 270°, or mirror horizontally",
                category = ToolCategory.IMAGE,
                icon = Icons.Default.RotateRight,
                accentColor = ColorImage,
                acceptedMimeTypes = listOf("image/jpeg", "image/png", "image/webp", "image/*")
            ),
            DocTool(
                id = ToolId.CROP_IMAGE,
                title = "Crop Image",
                description = "Crop to 1:1 Square, 4:3, 16:9 widescreen, or custom frame",
                category = ToolCategory.IMAGE,
                icon = Icons.Default.Crop,
                accentColor = ColorImage,
                acceptedMimeTypes = listOf("image/jpeg", "image/png", "image/webp", "image/*")
            ),
            DocTool(
                id = ToolId.MERGE_IMAGES_TO_PDF,
                title = "Merge Images to PDF",
                description = "Select multiple photos and bind them into a single PDF",
                category = ToolCategory.IMAGE,
                icon = Icons.Default.PictureAsPdf,
                accentColor = ColorImage,
                acceptedMimeTypes = listOf("image/*"),
                allowsMultipleFiles = true
            ),

            // Office Tools & Document Viewers
            DocTool(
                id = ToolId.VIEW_DOCUMENT,
                title = "Document Viewer",
                description = "In-app multi-format viewer for PDF, DOCX, XLSX, PPTX & TXT",
                category = ToolCategory.VIEWER,
                icon = Icons.Default.Visibility,
                accentColor = IndigoPrimary,
                acceptedMimeTypes = listOf(
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-powerpoint",
                    "text/plain",
                    "*/*"
                ),
                isViewerOnly = true
            ),
            DocTool(
                id = ToolId.VIEW_EXCEL,
                title = "Excel Spreadsheet Viewer",
                description = "Browse sheets, sort rows, filter columns, search cells & export",
                category = ToolCategory.EXCEL,
                icon = Icons.Default.TableChart,
                accentColor = ColorExcel,
                acceptedMimeTypes = listOf(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel",
                    "text/csv"
                ),
                isViewerOnly = true
            ),
            DocTool(
                id = ToolId.EXCEL_TO_CSV,
                title = "Excel to CSV",
                description = "Extract Excel workbook sheets into standardized CSV files",
                category = ToolCategory.EXCEL,
                icon = Icons.Default.Transform,
                accentColor = ColorExcel,
                acceptedMimeTypes = listOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel")
            ),
            DocTool(
                id = ToolId.EXCEL_TO_PDF,
                title = "Excel to PDF",
                description = "Render structured spreadsheet sheets into printable PDF",
                category = ToolCategory.EXCEL,
                icon = Icons.Default.PictureAsPdf,
                accentColor = ColorExcel,
                acceptedMimeTypes = listOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel")
            ),
            DocTool(
                id = ToolId.VIEW_WORD,
                title = "Word Document Viewer",
                description = "View DOCX formatting, paragraphs, tables, and bullet lists",
                category = ToolCategory.WORD,
                icon = Icons.Default.Description,
                accentColor = ColorWord,
                acceptedMimeTypes = listOf(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword"
                ),
                isViewerOnly = true
            ),
            DocTool(
                id = ToolId.WORD_TO_PDF,
                title = "Word to PDF",
                description = "Convert Microsoft Word DOCX documents into clean PDF files",
                category = ToolCategory.WORD,
                icon = Icons.Default.PictureAsPdf,
                accentColor = ColorWord,
                acceptedMimeTypes = listOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword")
            ),
            DocTool(
                id = ToolId.VIEW_PPT,
                title = "PowerPoint Presentation Viewer",
                description = "Interactive slide carousel, zoom, and fullscreen presentation",
                category = ToolCategory.POWERPOINT,
                icon = Icons.Default.PresentToAll,
                accentColor = ColorPpt,
                acceptedMimeTypes = listOf(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-powerpoint"
                ),
                isViewerOnly = true
            ),
            DocTool(
                id = ToolId.PPT_TO_PDF,
                title = "PowerPoint to PDF",
                description = "Export presentation slides into multi-page PDF document",
                category = ToolCategory.POWERPOINT,
                icon = Icons.Default.PictureAsPdf,
                accentColor = ColorPpt,
                acceptedMimeTypes = listOf(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-powerpoint"
                )
            ),
            DocTool(
                id = ToolId.BATCH_ZIP,
                title = "Batch ZIP Archiver",
                description = "Bundle multiple converted documents or images into a ZIP",
                category = ToolCategory.CONVERSION,
                icon = Icons.Default.Archive,
                accentColor = ColorZip,
                acceptedMimeTypes = listOf("*/*"),
                allowsMultipleFiles = true
            )
        )
    }
}
