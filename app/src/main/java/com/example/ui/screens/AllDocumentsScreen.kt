package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.History
import com.example.data.local.ProcessedFileEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FileUtils
import com.example.model.DocTool
import com.example.model.DocumentItem
import com.example.model.ToolId
import com.example.ui.theme.ColorExcel
import com.example.ui.theme.ColorPdf
import com.example.ui.theme.ColorPpt
import com.example.ui.theme.ColorWord
import com.example.ui.theme.CosmicNeonCyan
import com.example.ui.theme.CosmicNeonMagenta
import com.example.ui.theme.CosmicNeonPurple
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPrimary
import com.example.ui.theme.CosmicTextSecondary
import com.example.ui.theme.IndigoPrimary
import com.example.viewmodel.DocMasterViewModel
import java.io.File

@Composable
fun AllDocumentsScreen(
    viewModel: DocMasterViewModel,
    onBack: () -> Unit,
    onViewDocument: (DocumentItem) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val allDocs by viewModel.allDeviceDocuments.collectAsState()
    val isLoading by viewModel.isLoadingDocuments.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Launcher for selecting any file from device storage
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                viewModel.importDocumentAndOpen(uri)
            }
            Toast.makeText(context, "Opened ${uris.size} document(s)", Toast.LENGTH_SHORT).show()
        }
    }

    // Recent document names set for fast filter lookup
    val recentFileNames = remember(recentFiles) { recentFiles.map { it.fileName }.toSet() }

    // Filter documents by format, recent access, and search query
    val filteredDocs = allDocs.filter { doc ->
        val ext = doc.name.substringAfterLast(".", "").lowercase()
        val matchesCategory = when (selectedFilter) {
            "Recent" -> recentFileNames.contains(doc.name)
            "PDF" -> ext == "pdf"
            "PPTX" -> ext in listOf("pptx", "ppt")
            "Word" -> ext in listOf("docx", "doc")
            "Excel" -> ext in listOf("xlsx", "xls", "csv")
            "Text" -> ext in listOf("txt", "md", "json", "log", "xml")
            else -> true
        }

        val cleanQuery = searchQuery.trim()
        val matchesSearch = if (cleanQuery.isBlank()) {
            true
        } else {
            doc.name.contains(cleanQuery, ignoreCase = true) ||
            ext.contains(cleanQuery, ignoreCase = true)
        }

        matchesCategory && matchesSearch
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("all_documents_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("all_docs_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CosmicTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "All Documents",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = CosmicTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color(0x3538BDF8),
                                border = BorderStroke(0.8.dp, Color(0x5538BDF8))
                            ) {
                                Text(
                                    text = "${allDocs.size} Files",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicNeonCyan,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "View PDFs, PPTXs, Word & Excel without converting",
                            style = MaterialTheme.typography.bodySmall,
                            color = CosmicTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.loadAllDocuments() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x20FFFFFF))
                            .testTag("refresh_device_docs_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = CosmicNeonPurple
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh files",
                                tint = CosmicNeonPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Quick Import Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x35190C3D)),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x45A855F7),
                                Color(0x45EC4899),
                                Color(0x3538BDF8)
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CosmicNeonPurple, CosmicNeonMagenta)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Open Any Document",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CosmicTextPrimary
                            )
                            Text(
                                text = "Browse storage for any PDF, presentation, or office sheet",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                        "application/vnd.ms-powerpoint",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "application/msword",
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel",
                                        "text/*",
                                        "*/*"
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("browse_device_storage_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Browse", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Recently Accessed Section
            if (recentFiles.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = CosmicNeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recently Accessed",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CosmicTextPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${recentFiles.size} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = CosmicTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (item in recentFiles.take(8)) {
                                val ext = item.fileType.lowercase()
                                val chipColor = when (ext) {
                                    "pdf" -> ColorPdf
                                    "pptx", "ppt" -> ColorPpt
                                    "docx", "doc" -> ColorWord
                                    "xlsx", "xls", "csv" -> ColorExcel
                                    else -> CosmicNeonCyan
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0x35190C3D),
                                    border = BorderStroke(1.dp, chipColor.copy(alpha = 0.35f)),
                                    modifier = Modifier.clickable {
                                        val file = File(item.filePath)
                                        if (file.exists()) {
                                            onViewDocument(
                                                DocumentItem(
                                                    name = item.fileName,
                                                    uri = Uri.fromFile(file),
                                                    sizeBytes = item.processedSizeBytes,
                                                    mimeType = item.mimeType,
                                                    tempFilePath = item.filePath
                                                )
                                            )
                                        } else {
                                            Toast.makeText(context, "Opening ${item.fileName}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(chipColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (ext) {
                                                    "pdf" -> Icons.Default.PictureAsPdf
                                                    "csv", "xlsx", "xls" -> Icons.Default.TableChart
                                                    else -> Icons.Default.Description
                                                },
                                                contentDescription = null,
                                                tint = chipColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.fileName,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = CosmicTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = CosmicTextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("all_docs_search_input"),
                        placeholder = {
                            Text(
                                "Search documents by name or format (e.g. pptx, pdf)...",
                                color = CosmicTextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CosmicNeonPurple)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = CosmicTextSecondary
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x351F104D),
                            unfocusedContainerColor = Color(0x281B0E3D),
                            focusedBorderColor = CosmicNeonPurple,
                            unfocusedBorderColor = Color(0x35E9D5FF),
                            focusedTextColor = CosmicTextPrimary,
                            unfocusedTextColor = CosmicTextPrimary,
                            cursorColor = CosmicNeonPurple
                        ),
                        singleLine = true
                    )
                }
            }

            // Format Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "All" to Color.White,
                        "Recent" to CosmicNeonCyan,
                        "PDF" to ColorPdf,
                        "PPTX" to ColorPpt,
                        "Word" to ColorWord,
                        "Excel" to ColorExcel,
                        "Text" to CosmicNeonPurple
                    )
                    for ((filterName, accent) in filters) {
                        val isSelected = selectedFilter == filterName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filterName },
                            label = { Text(filterName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.25f),
                                selectedLabelColor = if (filterName == "All") Color.White else accent,
                                containerColor = Color(0x18FFFFFF),
                                labelColor = CosmicTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) accent else Color(0x22FFFFFF),
                                selectedBorderColor = accent,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.2.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Document Count Indicator
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DOCUMENTS (${filteredDocs.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = CosmicTextMuted
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Tap to open full-screen",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicNeonCyan
                    )
                }
            }

            // Document Cards List
            if (filteredDocs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x25A855F7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = CosmicNeonPurple,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No documents matching \"$searchQuery\"" else "No documents found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CosmicTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Use Browse button to open any document from storage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredDocs, key = { it.id }) { doc ->
                    DeviceDocumentCard(
                        doc = doc,
                        onOpen = {
                            onViewDocument(doc)
                        },
                        onProcessWithTools = {
                            // Pick appropriate tool
                            val ext = doc.name.substringAfterLast(".", "").lowercase()
                            val tool = when (ext) {
                                "pdf" -> DocTool.ALL_TOOLS.find { it.id == ToolId.COMPRESS_PDF }
                                "docx", "doc" -> DocTool.ALL_TOOLS.find { it.id == ToolId.WORD_TO_PDF }
                                "xlsx", "xls", "csv" -> DocTool.ALL_TOOLS.find { it.id == ToolId.EXCEL_TO_PDF }
                                "pptx", "ppt" -> DocTool.ALL_TOOLS.find { it.id == ToolId.PPT_TO_PDF }
                                else -> DocTool.ALL_TOOLS.find { it.id == ToolId.VIEW_DOCUMENT }
                            }
                            if (tool != null) {
                                viewModel.addFiles(listOf(doc.uri))
                                viewModel.openTool(tool)
                            } else {
                                onViewDocument(doc)
                            }
                        },
                        onShare = {
                            val file = doc.tempFilePath?.let { File(it) }
                            if (file != null && file.exists()) {
                                FileUtils.shareFile(context, file, doc.mimeType)
                            } else {
                                Toast.makeText(context, "Ready to view in full-screen", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        // Floating Action Button to pick any document
        ExtendedFloatingActionButton(
            onClick = {
                filePickerLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-excel",
                        "text/*",
                        "*/*"
                    )
                )
            },
            icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
            text = { Text("Open Document", fontWeight = FontWeight.Bold, color = Color.White) },
            containerColor = CosmicNeonPurple,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_open_document")
        )
    }
}

@Composable
fun DeviceDocumentCard(
    doc: DocumentItem,
    onOpen: () -> Unit,
    onProcessWithTools: () -> Unit,
    onShare: () -> Unit
) {
    val ext = doc.name.substringAfterLast(".", "").lowercase()
    val (typeColor, formatLabel, iconVector) = when (ext) {
        "pdf" -> Triple(ColorPdf, "PDF Document", Icons.Default.PictureAsPdf)
        "pptx", "ppt" -> Triple(ColorPpt, "PowerPoint Slide Deck", Icons.Default.Description)
        "docx", "doc" -> Triple(ColorWord, "Word Document", Icons.Default.Description)
        "xlsx", "xls", "csv" -> Triple(ColorExcel, "Excel Spreadsheet", Icons.Default.TableChart)
        else -> Triple(CosmicNeonPurple, "Text Document", Icons.Default.Description)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onOpen() }
            .testTag("device_doc_card_${doc.name}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x35190C3D)),
        border = BorderStroke(1.dp, Color(0x25E9D5FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Format Icon Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(typeColor.copy(alpha = 0.18f))
                        .border(1.dp, typeColor.copy(alpha = 0.35f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = CosmicTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = typeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = formatLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = typeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (doc.sizeBytes > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${FileUtils.formatFileSize(doc.sizeBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        if (doc.pageCount > 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${doc.pageCount} ${if (ext in listOf("pptx", "ppt")) "slides" else "pages"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = typeColor.copy(alpha = 0.22f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "View Fullscreen",
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onProcessWithTools,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicNeonPurple),
                    border = BorderStroke(1.dp, Color(0x35A855F7)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Transform,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tools", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = CosmicTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
