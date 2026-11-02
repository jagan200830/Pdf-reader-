package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProcessedFileEntity
import com.example.engine.FileUtils
import com.example.model.DocTool
import com.example.model.DocumentItem
import com.example.model.ToolCategory
import com.example.ui.components.HeroUploadSection
import com.example.ui.components.ToolCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.FolderOpen
import com.example.ui.theme.ColorExcel
import com.example.ui.theme.ColorPdf
import com.example.ui.theme.ColorPpt
import com.example.ui.theme.ColorWord
import com.example.viewmodel.ScreenState
import com.example.ui.theme.CosmicNeonCyan
import com.example.ui.theme.CosmicNeonMagenta
import com.example.ui.theme.CosmicNeonPurple
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPrimary
import com.example.ui.theme.CosmicTextSecondary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.DocMasterViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DocMasterViewModel,
    onOpenTool: (DocTool) -> Unit,
    onViewDocument: (DocumentItem) -> Unit
) {
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val allDeviceDocs by viewModel.allDeviceDocuments.collectAsState()
    var isGridView by remember { mutableStateOf(false) }

    // File picker launcher for hero upload
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val firstUri = uris.first()
            val fileName = FileUtils.getFileName(context.contentResolver, firstUri) ?: ""
            if (fileName.endsWith(".pdf", ignoreCase = true) ||
                fileName.endsWith(".docx", ignoreCase = true) ||
                fileName.endsWith(".xlsx", ignoreCase = true) ||
                fileName.endsWith(".pptx", ignoreCase = true) ||
                fileName.endsWith(".txt", ignoreCase = true) ||
                fileName.endsWith(".doc", ignoreCase = true) ||
                fileName.endsWith(".xls", ignoreCase = true) ||
                fileName.endsWith(".ppt", ignoreCase = true) ||
                fileName.endsWith(".csv", ignoreCase = true)) {
                viewModel.importDocumentAndOpen(firstUri)
            } else {
                viewModel.addFiles(uris)
                DocTool.ALL_TOOLS.find { it.id == com.example.model.ToolId.IMAGE_TO_PDF }?.let {
                    onOpenTool(it)
                }
            }
        }
    }

    // Filter tools by category and search
    val filteredTools = DocTool.ALL_TOOLS.filter { tool ->
        val matchesCategory = when (selectedCategory) {
            ToolCategory.ALL -> true
            else -> tool.category == selectedCategory
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            tool.title.contains(searchQuery, ignoreCase = true) ||
            tool.description.contains(searchQuery, ignoreCase = true) ||
            tool.category.title.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero upload dropzone
        item {
            HeroUploadSection(
                onBrowseFiles = { launcher.launch(arrayOf("*/*")) }
            )
        }

        // All Documents Hub (Device Files Viewer)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.setScreen(ScreenState.ALL_DOCUMENTS) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x35190C3D)),
                border = BorderStroke(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0x5538BDF8), Color(0x55A855F7), Color(0x35EC4899))
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color(0x3038BDF8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = CosmicNeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "All Documents Viewer",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CosmicTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0x2838BDF8)
                                ) {
                                    Text(
                                        text = "${allDeviceDocs.size} Ready",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        color = CosmicNeonCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "View PDF, PPTX, Word & Excel without converting",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSecondary
                            )
                        }

                        TextButton(
                            onClick = { viewModel.setScreen(ScreenState.ALL_DOCUMENTS) }
                        ) {
                            Text("See All", color = CosmicNeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (allDeviceDocs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (doc in allDeviceDocs.take(6)) {
                                val ext = doc.name.substringAfterLast(".", "").lowercase()
                                val chipColor = when (ext) {
                                    "pdf" -> ColorPdf
                                    "pptx", "ppt" -> ColorPpt
                                    "docx", "doc" -> ColorWord
                                    "xlsx", "xls", "csv" -> ColorExcel
                                    else -> CosmicNeonPurple
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = chipColor.copy(alpha = 0.15f),
                                    border = BorderStroke(0.8.dp, chipColor.copy(alpha = 0.35f)),
                                    modifier = Modifier.clickable { onViewDocument(doc) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(chipColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = doc.name,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = CosmicTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 140.dp)
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
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tool_search_input"),
                    placeholder = { Text("Search tools (e.g. compress, merge, word, excel)...", color = CosmicTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CosmicNeonPurple)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = CosmicTextSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
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

        // Categories Tabs Scroll
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolCategory.values().forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = {
                            Text(
                                text = category.title,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CosmicNeonPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0x251C0F45),
                            labelColor = CosmicTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = Color(0x25E9D5FF),
                            selectedBorderColor = CosmicNeonPurple
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                    )
                }
            }
        }

        // Section Title with View Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedCategory.title} (${filteredTools.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CosmicTextPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                // Toggle layout button: List View vs Grid View
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x281C0E45),
                    border = BorderStroke(0.8.dp, Color(0x25E9D5FF))
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isGridView = false },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isGridView) CosmicNeonPurple else Color.Transparent)
                                .testTag("view_mode_list")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewList,
                                contentDescription = "List View",
                                tint = if (!isGridView) Color.White else CosmicTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { isGridView = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isGridView) CosmicNeonPurple else Color.Transparent)
                                .testTag("view_mode_grid")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Grid View",
                                tint = if (isGridView) Color.White else CosmicTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Tools List / Grid
        if (isGridView) {
            items(filteredTools.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (tool in pair) {
                        Box(modifier = Modifier.weight(1f)) {
                            ToolCard(
                                tool = tool,
                                isGridView = true,
                                onClick = { onOpenTool(tool) }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // Full-width List layout where titles are 100% visible and never cut off
            items(filteredTools) { tool ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    ToolCard(
                        tool = tool,
                        isGridView = false,
                        onClick = { onOpenTool(tool) }
                    )
                }
            }
        }

        // Recent Files Section
        if (recentFiles.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Processed Files",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { viewModel.clearAllHistory() },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            items(recentFiles.take(5)) { entity ->
                RecentFileItemRow(
                    entity = entity,
                    onShare = {
                        val file = File(entity.filePath)
                        if (file.exists()) FileUtils.shareFile(context, file, entity.mimeType)
                    },
                    onDelete = { viewModel.deleteHistoryItem(entity.id) },
                    onOpen = {
                        val file = File(entity.filePath)
                        if (file.exists()) {
                            onViewDocument(
                                DocumentItem(
                                    name = entity.fileName,
                                    uri = Uri.fromFile(file),
                                    sizeBytes = entity.processedSizeBytes,
                                    mimeType = entity.mimeType,
                                    tempFilePath = entity.filePath
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RecentFileItemRow(
    entity: ProcessedFileEntity,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onOpen() }
            .testTag("recent_file_${entity.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x35190C3D)
        ),
        border = BorderStroke(1.dp, Color(0x25E9D5FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (entity.fileType) {
                            "PDF" -> ColorPdf.copy(alpha = 0.2f)
                            "JPG", "PNG", "WEBP" -> CosmicNeonPurple.copy(alpha = 0.2f)
                            "CSV", "XLSX" -> ColorExcel.copy(alpha = 0.2f)
                            "DOCX" -> ColorWord.copy(alpha = 0.2f)
                            else -> CosmicNeonCyan.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (entity.fileType) {
                        "PDF" -> Icons.Default.PictureAsPdf
                        "JPG", "PNG", "WEBP" -> Icons.Default.Image
                        "CSV", "XLSX" -> Icons.Default.TableChart
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = when (entity.fileType) {
                        "PDF" -> ColorPdf
                        "JPG", "PNG", "WEBP" -> CosmicNeonPurple
                        "CSV", "XLSX" -> ColorExcel
                        "DOCX" -> ColorWord
                        else -> CosmicNeonCyan
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.fileName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = CosmicTextPrimary
                )
                Text(
                    text = "${entity.toolType} • ${FileUtils.formatFileSize(entity.processedSizeBytes)} • ${SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(entity.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CosmicTextSecondary,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Clear, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}
