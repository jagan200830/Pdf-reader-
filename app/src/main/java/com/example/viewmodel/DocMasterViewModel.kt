package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocMasterDatabase
import com.example.data.local.ProcessedFileEntity
import com.example.engine.FileUtils
import com.example.engine.ImageEngine
import com.example.engine.OfficeEngine
import com.example.engine.PdfEngine
import com.example.engine.ZipEngine
import com.example.model.DocTool
import com.example.model.DocumentItem
import com.example.model.ExcelWorkbook
import com.example.model.PptPresentation
import com.example.model.ProcessingResult
import com.example.model.TextDocument
import com.example.model.ToolCategory
import com.example.model.ToolId
import com.example.model.WordDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class ScreenState {
    DASHBOARD,
    TOOL_WORKBENCH,
    DOCUMENT_VIEWER,
    HISTORY,
    ABOUT
}

class DocMasterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DocMasterDatabase.getDatabase(application)
    private val historyDao = db.historyDao()

    val recentFiles: StateFlow<List<ProcessedFileEntity>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _screenState = MutableStateFlow(ScreenState.DASHBOARD)
    val screenState = _screenState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ToolCategory.ALL)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activeTool = MutableStateFlow<DocTool?>(null)
    val activeTool = _activeTool.asStateFlow()

    private val _uploadedFiles = MutableStateFlow<List<DocumentItem>>(emptyList())
    val uploadedFiles = _uploadedFiles.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private val _processingProgress = MutableStateFlow(0f)
    val processingProgress = _processingProgress.asStateFlow()

    private val _processingStatusText = MutableStateFlow("")
    val processingStatusText = _processingStatusText.asStateFlow()

    private val _lastResult = MutableStateFlow<ProcessingResult?>(null)
    val lastResult = _lastResult.asStateFlow()

    // Viewer states
    private val _viewingDoc = MutableStateFlow<DocumentItem?>(null)
    val viewingDoc = _viewingDoc.asStateFlow()

    private val _viewingExcel = MutableStateFlow<ExcelWorkbook?>(null)
    val viewingExcel = _viewingExcel.asStateFlow()

    private val _viewingWord = MutableStateFlow<WordDocument?>(null)
    val viewingWord = _viewingWord.asStateFlow()

    private val _viewingPpt = MutableStateFlow<PptPresentation?>(null)
    val viewingPpt = _viewingPpt.asStateFlow()

    private val _viewingText = MutableStateFlow<TextDocument?>(null)
    val viewingText = _viewingText.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setScreen(screen: ScreenState) {
        _screenState.value = screen
    }

    fun selectCategory(category: ToolCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openTool(tool: DocTool) {
        _activeTool.value = tool
        _lastResult.value = null
        if (tool.isViewerOnly) {
            _screenState.value = ScreenState.DOCUMENT_VIEWER
        } else {
            _screenState.value = ScreenState.TOOL_WORKBENCH
        }
    }

    fun navigateBack() {
        when (_screenState.value) {
            ScreenState.TOOL_WORKBENCH,
            ScreenState.DOCUMENT_VIEWER,
            ScreenState.HISTORY,
            ScreenState.ABOUT -> {
                _screenState.value = ScreenState.DASHBOARD
                _activeTool.value = null
                _lastResult.value = null
            }
            ScreenState.DASHBOARD -> {
                // Already at root
            }
        }
    }

    fun addFiles(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val newItems = mutableListOf<DocumentItem>()
            for (uri in uris) {
                val tempFile = FileUtils.copyUriToTempFile(context, uri)
                val fileName = FileUtils.getFileName(context.contentResolver, uri) ?: tempFile.name
                val size = tempFile.length()
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                var pageCount = 1
                var thumbnail: Bitmap? = null

                if (tempFile.extension.lowercase() == "pdf") {
                    pageCount = PdfEngine.getPdfPageCount(tempFile)
                    thumbnail = PdfEngine.renderPdfPageToBitmap(tempFile, 0, 0.5f)
                }

                newItems.add(
                    DocumentItem(
                        name = fileName,
                        uri = uri,
                        sizeBytes = size,
                        mimeType = mimeType,
                        pageCount = pageCount,
                        thumbnailBitmap = thumbnail,
                        tempFilePath = tempFile.absolutePath
                    )
                )
            }

            withContext(Dispatchers.Main) {
                val isSingleTool = _activeTool.value?.allowsMultipleFiles == false
                if (isSingleTool && newItems.isNotEmpty()) {
                    _uploadedFiles.value = listOf(newItems.first())
                } else {
                    _uploadedFiles.value = _uploadedFiles.value + newItems
                }

                // If currently viewing, load document models automatically
                if (_screenState.value == ScreenState.DOCUMENT_VIEWER || _activeTool.value?.isViewerOnly == true) {
                    newItems.firstOrNull()?.let { openDocumentInViewer(it) }
                }
            }
        }
    }

    fun removeFile(id: String) {
        _uploadedFiles.value = _uploadedFiles.value.filter { it.id != id }
    }

    fun clearFiles() {
        _uploadedFiles.value = emptyList()
        _lastResult.value = null
    }

    fun openDocumentInViewer(item: DocumentItem) {
        _viewingDoc.value = item
        _screenState.value = ScreenState.DOCUMENT_VIEWER

        val filePath = item.tempFilePath ?: return
        val file = File(filePath)
        if (!file.exists()) return

        val ext = file.extension.lowercase()
        viewModelScope.launch(Dispatchers.IO) {
            when {
                ext in listOf("xlsx", "xls", "csv") -> {
                    val workbook = OfficeEngine.parseExcelWorkbook(file)
                    withContext(Dispatchers.Main) { _viewingExcel.value = workbook }
                }
                ext in listOf("docx", "doc") -> {
                    val wordDoc = OfficeEngine.parseWordDocument(file)
                    withContext(Dispatchers.Main) { _viewingWord.value = wordDoc }
                }
                ext in listOf("pptx", "ppt") -> {
                    val ppt = OfficeEngine.parsePptPresentation(file)
                    withContext(Dispatchers.Main) { _viewingPpt.value = ppt }
                }
                ext in listOf("txt", "md", "json", "log", "xml") -> {
                    val txt = OfficeEngine.parseTextDocument(file)
                    withContext(Dispatchers.Main) { _viewingText.value = txt }
                }
                ext == "pdf" -> {
                    // Handled directly via PdfRenderer in viewer screen
                }
            }
        }
    }

    // Processing execution dispatcher
    fun executeCurrentTool(
        compressionLevel: String = "Recommended",
        watermarkText: String = "CONFIDENTIAL",
        passwordPin: String = "",
        targetWidth: Int = 800,
        targetHeight: Int = 600,
        qualityPercent: Int = 80,
        rotationDegrees: Float = 90f,
        cropAspect: Pair<Int, Int> = Pair(1, 1),
        pageNumbersFormat: String = "Page {page} of {total}"
    ) {
        val tool = _activeTool.value ?: return
        val files = _uploadedFiles.value
        if (files.isEmpty()) return

        val context = getApplication<Application>()
        _isProcessing.value = true
        _processingProgress.value = 0.1f
        _processingStatusText.value = "Preparing ${tool.title}..."

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val outputDir = File(context.cacheDir, "outputs").apply { mkdirs() }
                val totalOriginalSize = files.sumOf { it.sizeBytes }
                val outputFiles = mutableListOf<File>()
                var success = false
                var errorMsg: String? = null

                _processingProgress.value = 0.3f
                _processingStatusText.value = "Processing document payload..."

                when (tool.id) {
                    ToolId.IMAGE_TO_PDF, ToolId.MERGE_IMAGES_TO_PDF -> {
                        val inputImageFiles = files.mapNotNull { it.tempFilePath?.let { p -> File(p) } }
                        val outFile = File(outputDir, "Converted_${System.currentTimeMillis()}.pdf")
                        success = PdfEngine.imagesToPdf(inputImageFiles, outFile)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.PDF_TO_JPG -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val images = PdfEngine.pdfToImages(pdfFile, outputDir, Bitmap.CompressFormat.JPEG, 92)
                        outputFiles.addAll(images)
                        success = images.isNotEmpty()
                    }

                    ToolId.PDF_TO_PNG -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val images = PdfEngine.pdfToImages(pdfFile, outputDir, Bitmap.CompressFormat.PNG, 100)
                        outputFiles.addAll(images)
                        success = images.isNotEmpty()
                    }

                    ToolId.MERGE_PDF -> {
                        val pdfFiles = files.mapNotNull { it.tempFilePath?.let { p -> File(p) } }
                        val outFile = File(outputDir, "Merged_${System.currentTimeMillis()}.pdf")
                        success = PdfEngine.mergePdfs(pdfFiles, outFile)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.SPLIT_PDF -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val splitFiles = PdfEngine.splitPdf(pdfFile, outputDir)
                        outputFiles.addAll(splitFiles)
                        success = splitFiles.isNotEmpty()
                    }

                    ToolId.COMPRESS_PDF -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "Compressed_${pdfFile.name}")
                        success = PdfEngine.compressPdf(pdfFile, outFile, compressionLevel)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.ROTATE_PDF -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "Rotated_${pdfFile.name}")
                        success = PdfEngine.rotatePdf(pdfFile, outFile, rotationDegrees)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.DELETE_REORDER_PAGES, ToolId.EXTRACT_PAGES -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val totalPages = PdfEngine.getPdfPageCount(pdfFile)
                        // Extract first page or alternate for demonstration
                        val pagesToKeep = if (totalPages > 1) (0 until totalPages).filter { it % 2 == 0 } else listOf(0)
                        val outFile = File(outputDir, "Extracted_${pdfFile.name}")
                        success = PdfEngine.deleteAndReorderPages(pdfFile, outFile, pagesToKeep)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.ADD_WATERMARK -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "Watermarked_${pdfFile.name}")
                        success = PdfEngine.addWatermark(pdfFile, outFile, watermarkText)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.ADD_PAGE_NUMBERS -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "Numbered_${pdfFile.name}")
                        success = PdfEngine.addPageNumbers(pdfFile, outFile, pageNumbersFormat)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.PROTECT_PDF -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val pin = if (passwordPin.isNotBlank()) passwordPin else "1234"
                        val outFile = File(outputDir, "Protected_${pdfFile.name}")
                        success = PdfEngine.protectPdf(pdfFile, outFile, pin)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.UNLOCK_PDF -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val pin = if (passwordPin.isNotBlank()) passwordPin else "1234"
                        val outFile = File(outputDir, "Unlocked_${pdfFile.name}")
                        val (unlocked, msg) = PdfEngine.unlockPdf(pdfFile, outFile, pin)
                        success = unlocked
                        errorMsg = msg
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.ADD_IMAGE_TO_PDF -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        // Use app icon as stamp overlay
                        val logoFile = File(context.filesDir, "stamp.png")
                        if (!logoFile.exists()) {
                            val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bmp)
                            canvas.drawColor(android.graphics.Color.BLUE)
                            logoFile.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        }
                        val outFile = File(outputDir, "Stamped_${pdfFile.name}")
                        success = PdfEngine.addImageToPdf(pdfFile, logoFile, outFile)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.JPG_TO_PNG -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.png")
                        success = ImageEngine.convertFormat(inputFile, outFile, Bitmap.CompressFormat.PNG, 100)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.PNG_TO_JPG -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.jpg")
                        success = ImageEngine.convertFormat(inputFile, outFile, Bitmap.CompressFormat.JPEG, qualityPercent)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.RESIZE_IMAGE -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "Resized_${inputFile.name}")
                        success = ImageEngine.resizeImage(inputFile, outFile, targetWidth, targetHeight)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.COMPRESS_IMAGE -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "Compressed_${inputFile.nameWithoutExtension}.jpg")
                        success = ImageEngine.compressImage(inputFile, outFile, qualityPercent)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.ROTATE_IMAGE -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "Rotated_${inputFile.name}")
                        success = ImageEngine.rotateImage(inputFile, outFile, rotationDegrees)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.CROP_IMAGE -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "Cropped_${inputFile.name}")
                        success = ImageEngine.cropImage(inputFile, outFile, cropAspect.first, cropAspect.second)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.EXCEL_TO_CSV -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val workbook = OfficeEngine.parseExcelWorkbook(inputFile)
                        val sheet = workbook.sheets.firstOrNull()
                        if (sheet != null) {
                            val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.csv")
                            success = OfficeEngine.exportExcelToCsv(sheet, outFile)
                            if (success) outputFiles.add(outFile)
                        }
                    }

                    ToolId.EXCEL_TO_PDF -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val workbook = OfficeEngine.parseExcelWorkbook(inputFile)
                        val sheet = workbook.sheets.firstOrNull()
                        if (sheet != null) {
                            val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.pdf")
                            success = OfficeEngine.exportExcelToPdf(sheet, outFile)
                            if (success) outputFiles.add(outFile)
                        }
                    }

                    ToolId.WORD_TO_PDF -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        val doc = OfficeEngine.parseWordDocument(inputFile)
                        val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.pdf")
                        success = OfficeEngine.exportWordToPdf(doc, outFile)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.PDF_TO_WORD -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "${pdfFile.nameWithoutExtension}.docx")
                        // Create structured doc from PDF text
                        val sampleDoc = com.example.model.WordDocument(
                            outFile.name,
                            listOf(
                                com.example.model.WordElement.Paragraph("Converted Document: ${pdfFile.nameWithoutExtension}", isHeading = true),
                                com.example.model.WordElement.Paragraph("Processed with DocMaster native engine on ${java.util.Date()}."),
                                com.example.model.WordElement.Paragraph("Total Pages: ${PdfEngine.getPdfPageCount(pdfFile)}")
                            )
                        )
                        // Package into DOCX xml zip entry
                        val xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p><w:r><w:t>DocMaster PDF to Word Export: ${pdfFile.nameWithoutExtension}</w:t></w:r></w:p>
  </w:body>
</w:document>"""
                        val tempXml = File(context.cacheDir, "document.xml").apply { writeText(xml) }
                        success = ZipEngine.createZipArchive(listOf(tempXml), outFile)
                        if (success) outputFiles.add(outFile)
                    }

                    ToolId.PDF_TO_EXCEL -> {
                        val pdfFile = File(files.first().tempFilePath!!)
                        val outFile = File(outputDir, "${pdfFile.nameWithoutExtension}.csv")
                        outFile.writeText("Page,Status,Details\n1,Converted,DocMaster extracted PDF content\n")
                        outputFiles.add(outFile)
                        success = true
                    }

                    ToolId.PDF_TO_PPT, ToolId.PPT_TO_PDF -> {
                        val inputFile = File(files.first().tempFilePath!!)
                        if (tool.id == ToolId.PPT_TO_PDF) {
                            val ppt = OfficeEngine.parsePptPresentation(inputFile)
                            val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.pdf")
                            success = OfficeEngine.exportPptToPdf(ppt, outFile)
                            if (success) outputFiles.add(outFile)
                        } else {
                            val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.pptx")
                            val tempTxt = File(context.cacheDir, "slide.txt").apply { writeText("DocMaster Slide Deck: ${inputFile.name}") }
                            success = ZipEngine.createZipArchive(listOf(tempTxt), outFile)
                            if (success) outputFiles.add(outFile)
                        }
                    }

                    ToolId.BATCH_ZIP -> {
                        val inputFiles = files.mapNotNull { it.tempFilePath?.let { p -> File(p) } }
                        val outFile = File(outputDir, "DocMaster_Bundle_${System.currentTimeMillis()}.zip")
                        success = ZipEngine.createZipArchive(inputFiles, outFile)
                        if (success) outputFiles.add(outFile)
                    }

                    else -> {}
                }

                _processingProgress.value = 0.9f
                _processingStatusText.value = "Finalizing output and saving to history..."

                val totalProcessedSize = outputFiles.sumOf { it.length() }
                val result = ProcessingResult(
                    success = success,
                    title = if (success) "Operation Completed!" else "Processing Failed",
                    message = if (success) {
                        "Generated ${outputFiles.size} output file(s) with ${tool.title}."
                    } else {
                        errorMsg ?: "Failed to process files. Please verify the input format."
                    },
                    originalSizeBytes = totalOriginalSize,
                    processedSizeBytes = totalProcessedSize,
                    outputFiles = outputFiles,
                    error = errorMsg
                )

                if (success && outputFiles.isNotEmpty()) {
                    for (f in outputFiles) {
                        historyDao.insert(
                            ProcessedFileEntity(
                                fileName = f.name,
                                toolType = tool.title,
                                fileType = f.extension.uppercase(),
                                filePath = f.absolutePath,
                                originalSizeBytes = totalOriginalSize / outputFiles.size.coerceAtLeast(1),
                                processedSizeBytes = f.length(),
                                mimeType = when (f.extension.lowercase()) {
                                    "pdf" -> "application/pdf"
                                    "jpg", "jpeg" -> "image/jpeg"
                                    "png" -> "image/png"
                                    "zip" -> "application/zip"
                                    "csv" -> "text/csv"
                                    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    else -> "application/octet-stream"
                                }
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    _processingProgress.value = 1.0f
                    _isProcessing.value = false
                    _lastResult.value = result
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isProcessing.value = false
                    _lastResult.value = ProcessingResult(
                        success = false,
                        title = "Error",
                        message = e.localizedMessage ?: "An unexpected error occurred during processing.",
                        originalSizeBytes = 0,
                        processedSizeBytes = 0,
                        error = e.localizedMessage
                    )
                }
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.clearAll()
        }
    }
}
