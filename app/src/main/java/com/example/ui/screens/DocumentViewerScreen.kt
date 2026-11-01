package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import com.example.ui.components.ImageViewerComponent
import com.example.ui.components.PdfViewerComponent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Transform
import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FileUtils
import com.example.engine.OfficeEngine
import com.example.engine.PdfEngine
import com.example.model.DocumentItem
import com.example.model.ExcelWorkbook
import com.example.model.PptPresentation
import com.example.model.TextDocument
import com.example.model.WordDocument
import com.example.model.WordElement
import com.example.ui.theme.ColorExcel
import com.example.ui.theme.ColorPdf
import com.example.ui.theme.ColorPpt
import com.example.ui.theme.ColorWord
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.DocMasterViewModel
import java.io.File

@Composable
fun DocumentViewerScreen(
    documentItem: DocumentItem?,
    viewModel: DocMasterViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val item = documentItem ?: viewModel.viewingDoc.collectAsState().value
    val excelDoc by viewModel.viewingExcel.collectAsState()
    val wordDoc by viewModel.viewingWord.collectAsState()
    val pptDoc by viewModel.viewingPpt.collectAsState()
    val textDoc by viewModel.viewingText.collectAsState()

    var isFullscreen by remember { mutableStateOf(false) }

    if (item == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No document loaded for preview.")
        }
        return
    }

    val file = item.tempFilePath?.let { File(it) }
    val ext = file?.extension?.lowercase() ?: "pdf"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("document_viewer_screen")
    ) {
        // Top Toolbar
        if (!isFullscreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("viewer_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${ext.uppercase()} • ${FileUtils.formatFileSize(item.sizeBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (file != null && file.exists()) {
                    IconButton(
                        onClick = { FileUtils.shareFile(context, file, item.mimeType) },
                        modifier = Modifier.testTag("viewer_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = IndigoPrimary)
                    }
                }

                IconButton(
                    onClick = { isFullscreen = !isFullscreen },
                    modifier = Modifier.testTag("viewer_fullscreen_button")
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Toggle Fullscreen"
                    )
                }
            }
        }

        // Body Content based on document extension
        Box(modifier = Modifier.fillMaxSize()) {
            when (ext) {
                "pdf" -> {
                    if (file != null && file.exists()) {
                        PdfViewerComponent(
                            file = file,
                            isFullscreen = isFullscreen,
                            onToggleFullscreen = { isFullscreen = !isFullscreen }
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("PDF file not available.")
                        }
                    }
                }
                "jpg", "jpeg", "png", "webp" -> {
                    if (file != null && file.exists()) {
                        ImageViewerComponent(
                            file = file,
                            isFullscreen = isFullscreen,
                            onToggleFullscreen = { isFullscreen = !isFullscreen }
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Image file not available.")
                        }
                    }
                }
                "xlsx", "xls", "csv" -> {
                    if (excelDoc != null) {
                        ExcelDocumentView(workbook = excelDoc!!, onExportPdf = {
                            val out = File(context.cacheDir, "${item.nameWithoutExtension()}.pdf")
                            excelDoc!!.sheets.firstOrNull()?.let { sheet ->
                                if (OfficeEngine.exportExcelToPdf(sheet, out)) {
                                    FileUtils.shareFile(context, out, "application/pdf")
                                }
                            }
                        })
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading spreadsheet rows...")
                        }
                    }
                }
                "docx", "doc" -> {
                    if (wordDoc != null) {
                        WordDocumentView(doc = wordDoc!!, onExportPdf = {
                            val out = File(context.cacheDir, "${item.nameWithoutExtension()}.pdf")
                            if (OfficeEngine.exportWordToPdf(wordDoc!!, out)) {
                                FileUtils.shareFile(context, out, "application/pdf")
                            }
                        })
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading Word document...")
                        }
                    }
                }
                "pptx", "ppt" -> {
                    if (pptDoc != null) {
                        PptDocumentView(ppt = pptDoc!!, isFullscreen = isFullscreen, onExportPdf = {
                            val out = File(context.cacheDir, "${item.nameWithoutExtension()}.pdf")
                            if (OfficeEngine.exportPptToPdf(pptDoc!!, out)) {
                                FileUtils.shareFile(context, out, "application/pdf")
                            }
                        })
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading presentation slides...")
                        }
                    }
                }
                else -> {
                    // Plain text / Code viewer
                    if (textDoc != null) {
                        TextDocumentView(textDoc = textDoc!!)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading text...")
                        }
                    }
                }
            }

            // Floating 'Download to Device' button using MediaStore API
            if (!isFullscreen && file != null && file.exists()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val uri = FileUtils.saveFileToDownloads(context, file, item.mimeType)
                        if (uri != null) {
                            Toast.makeText(context, "Saved to Downloads/DocMaster: ${file.name}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    text = { Text("Download to Device", fontWeight = FontWeight.Bold) },
                    containerColor = IndigoPrimary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("viewer_download_fab")
                )
            }
        }
    }
}

private fun DocumentItem.nameWithoutExtension(): String {
    return name.substringBeforeLast(".")
}

@Composable
fun ExcelDocumentView(workbook: ExcelWorkbook, onExportPdf: () -> Unit) {
    var selectedSheetIdx by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val currentSheet = workbook.sheets.getOrNull(selectedSheetIdx) ?: workbook.sheets.first()

    val filteredRows = if (searchQuery.isBlank()) currentSheet.rows else {
        currentSheet.rows.filter { row ->
            row.any { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sheet Tabs
        if (workbook.sheets.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = selectedSheetIdx,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                workbook.sheets.forEachIndexed { index, sheet ->
                    Tab(
                        selected = selectedSheetIdx == index,
                        onClick = { selectedSheetIdx = index },
                        text = { Text(sheet.name, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Search & Export Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search cells...") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onExportPdf,
                colors = ButtonDefaults.buttonColors(containerColor = ColorExcel),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF", fontSize = 12.sp)
            }
        }

        // Spreadsheet Table Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            LazyColumn(
                modifier = Modifier.width(800.dp)
            ) {
                // Headers Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorExcel.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            .padding(vertical = 10.dp)
                    ) {
                        for (header in currentSheet.headers) {
                            Text(
                                text = header,
                                modifier = Modifier
                                    .width(160.dp)
                                    .padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Data Rows
                itemsIndexed(filteredRows) { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            .padding(vertical = 8.dp)
                    ) {
                        for (colIdx in currentSheet.headers.indices) {
                            val cellText = if (colIdx in row.indices) row[colIdx] else ""
                            Text(
                                text = cellText,
                                modifier = Modifier
                                    .width(160.dp)
                                    .padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordDocumentView(doc: WordDocument, onExportPdf: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Document View",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onExportPdf,
                colors = ButtonDefaults.buttonColors(containerColor = ColorWord),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF", fontSize = 12.sp)
            }
        }

        // Formatted Document Canvas
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(doc.elements) { element ->
                    when (element) {
                        is WordElement.Paragraph -> {
                            if (element.isHeading) {
                                Text(
                                    text = element.text,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ColorWord
                                    )
                                )
                            } else {
                                Text(
                                    text = element.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        is WordElement.Table -> {
                            // Render table
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PptDocumentView(ppt: PptPresentation, isFullscreen: Boolean, onExportPdf: () -> Unit) {
    var currentSlideIdx by remember { mutableIntStateOf(0) }
    val currentSlide = ppt.slides.getOrNull(currentSlideIdx) ?: ppt.slides.first()

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isFullscreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Slide ${currentSlide.slideNumber} of ${ppt.slides.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onExportPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPpt),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export PDF", fontSize = 12.sp)
                }
            }
        }

        // Slide Canvas card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top presentation accent banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(ColorPpt)
                    )

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = currentSlide.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = ColorPpt
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        currentSlide.contentLines.forEach { line ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ColorPpt)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Slide Navigation Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (currentSlideIdx > 0) currentSlideIdx-- },
                enabled = currentSlideIdx > 0
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
                Text("Previous")
            }

            Text(
                "${currentSlideIdx + 1} / ${ppt.slides.size}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedButton(
                onClick = { if (currentSlideIdx < ppt.slides.size - 1) currentSlideIdx++ },
                enabled = currentSlideIdx < ppt.slides.size - 1
            ) {
                Text("Next")
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
fun TextDocumentView(textDoc: TextDocument) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Stats bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Lines: ${textDoc.lineCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Words: ${textDoc.wordCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Characters: ${textDoc.charCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }

        // Code / Text Viewer with Line Numbers
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            itemsIndexed(textDoc.lines) { index, line ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${index + 1}".padStart(4, ' '),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.width(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = line,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
