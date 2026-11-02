package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.model.DocumentItem
import androidx.compose.material.icons.filled.FolderOpen
import com.example.ui.theme.ColorExcel
import com.example.ui.theme.ColorPdf
import com.example.ui.theme.ColorWord
import com.example.ui.theme.CosmicNeonCyan
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.DocMasterViewModel
import com.example.viewmodel.ScreenState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: DocMasterViewModel,
    onBack: () -> Unit,
    onViewDocument: (DocumentItem) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val historyList by viewModel.recentFiles.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    // Rename state
    var entityToRename by remember { mutableStateOf<ProcessedFileEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Filtering by both file name, file type/extension, and tool category
    val filteredList = historyList.filter { entity ->
        val matchesCategory = when (selectedFilter) {
            "PDF" -> entity.fileType.equals("PDF", ignoreCase = true)
            "Images" -> entity.fileType in listOf("JPG", "JPEG", "PNG", "WEBP")
            "Office" -> entity.fileType in listOf("XLSX", "XLS", "CSV", "DOCX", "DOC", "PPTX", "PPT")
            "Archives" -> entity.fileType.equals("ZIP", ignoreCase = true)
            else -> true
        }

        val cleanQuery = searchQuery.trim()
        val matchesSearch = if (cleanQuery.isBlank()) {
            true
        } else {
            val ext = entity.filePath.substringAfterLast(".", "")
            entity.fileName.contains(cleanQuery, ignoreCase = true) ||
            entity.fileType.contains(cleanQuery, ignoreCase = true) ||
            ext.contains(cleanQuery, ignoreCase = true) ||
            entity.toolType.contains(cleanQuery, ignoreCase = true)
        }

        matchesCategory && matchesSearch
    }

    // Clear All Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all recent conversion records from your history. Your files in cache will not be lost immediately.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog
    if (entityToRename != null) {
        val targetEntity = entityToRename!!
        val file = File(targetEntity.filePath)
        val ext = file.extension
        AlertDialog(
            onDismissRequest = { entityToRename = null },
            title = { Text("Rename File", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter new name for \"${targetEntity.fileName}\":",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        singleLine = true,
                        label = { Text("File Name") },
                        trailingIcon = {
                            if (ext.isNotEmpty()) {
                                Text(
                                    text = ".$ext",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = renameInput.trim()
                        if (trimmed.isNotBlank()) {
                            val renamed = viewModel.renameOutputFile(file, trimmed)
                            if (renamed != null) {
                                Toast.makeText(context, "Renamed to ${renamed.name}", Toast.LENGTH_SHORT).show()
                            }
                            entityToRename = null
                        } else {
                            Toast.makeText(context, "Filename cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entityToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "File History",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = { viewModel.setScreen(ScreenState.ALL_DOCUMENTS) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicNeonCyan),
                    border = BorderStroke(1.dp, Color(0x3538BDF8)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("All Files", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (historyList.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Interactive Search Bar (filters by file name or type)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0x351F104D),
                border = BorderStroke(1.dp, Color(0x35E9D5FF))
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Filter by name or type (e.g. report, pdf, xlsx)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFA78BFA).copy(alpha = 0.8f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFA855F7)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color(0xFFCBD5E1))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFA855F7)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true
                )
            }

            // Quick Category Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "PDF", "Images", "Office", "Archives").forEach { type ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { selectedFilter = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFA855F7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0x251C0F45),
                            labelColor = Color(0xFFCBD5E1)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == type,
                            borderColor = Color(0x25E9D5FF),
                            selectedBorderColor = Color(0xFFA855F7)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            }

            // Search Results Summary / Reset button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isBlank()) {
                        "${filteredList.size} item(s) found"
                    } else {
                        "${filteredList.size} match(es) for \"${searchQuery.trim()}\""
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (searchQuery.isNotBlank() || selectedFilter != "All") {
                    Text(
                        text = "Reset search",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = IndigoPrimary,
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            selectedFilter = "All"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // History List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotBlank()) Icons.Default.Search else Icons.Default.History,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No files match \"${searchQuery.trim()}\"" else "No converted files yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try searching for a different name, extension (e.g. pdf, jpg, xlsx), or tool." else "Processed documents and converted files will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { entity ->
                        val file = File(entity.filePath)
                        val fileExists = file.exists()

                        val iconVector = when (entity.fileType) {
                            "PDF" -> Icons.Default.PictureAsPdf
                            "JPG", "PNG", "WEBP", "JPEG" -> Icons.Default.Image
                            "XLSX", "CSV", "XLS" -> Icons.Default.TableChart
                            "ZIP" -> Icons.Default.Archive
                            else -> Icons.Default.Description
                        }

                        val accentColor = when (entity.fileType) {
                            "PDF" -> ColorPdf
                            "JPG", "PNG", "WEBP", "JPEG" -> VioletSecondary
                            "XLSX", "CSV", "XLS" -> ColorExcel
                            "DOCX", "DOC" -> ColorWord
                            "ZIP" -> IndigoPrimary
                            else -> IndigoPrimary
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (fileExists) {
                                        onViewDocument(
                                            DocumentItem(
                                                name = entity.fileName,
                                                uri = Uri.fromFile(file),
                                                sizeBytes = entity.processedSizeBytes,
                                                mimeType = entity.mimeType,
                                                tempFilePath = entity.filePath
                                            )
                                        )
                                    } else {
                                        Toast.makeText(context, "File is no longer cached", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x35190C3D)),
                            border = BorderStroke(1.dp, Color(0x25E9D5FF)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(accentColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entity.fileName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${entity.toolType} • ${entity.fileType} • ${FileUtils.formatFileSize(entity.processedSizeBytes)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(entity.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }

                                    // Quick Rename Button
                                    IconButton(
                                        onClick = {
                                            entityToRename = entity
                                            renameInput = entity.fileName.substringBeforeLast(".")
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Rename file",
                                            tint = IndigoPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete from History Button
                                    IconButton(
                                        onClick = { viewModel.deleteHistoryItem(entity.id) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons Row: Download to Device | Share | Preview
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (fileExists) {
                                                val uri = FileUtils.saveFileToDownloads(context, file, entity.mimeType)
                                                if (uri != null) {
                                                    Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "File is no longer cached", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (fileExists) {
                                                FileUtils.shareFile(context, file, entity.mimeType)
                                            } else {
                                                Toast.makeText(context, "File is no longer cached", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            if (fileExists) {
                                                onViewDocument(
                                                    DocumentItem(
                                                        name = entity.fileName,
                                                        uri = Uri.fromFile(file),
                                                        sizeBytes = entity.processedSizeBytes,
                                                        mimeType = entity.mimeType,
                                                        tempFilePath = entity.filePath
                                                    )
                                                )
                                            } else {
                                                Toast.makeText(context, "File is no longer cached", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating 'Download All' button using MediaStore API
        if (filteredList.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = {
                    val validFiles = filteredList.mapNotNull {
                        val f = File(it.filePath)
                        if (f.exists()) f else null
                    }
                    val count = FileUtils.saveMultipleFilesToDownloads(context, validFiles)
                    if (count > 0) {
                        Toast.makeText(context, "Saved $count file(s) to Downloads/DocMaster folder", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No files available to download", Toast.LENGTH_SHORT).show()
                    }
                },
                icon = { Icon(Icons.Default.Download, contentDescription = null) },
                text = { Text("Download All (${filteredList.size})", fontWeight = FontWeight.Bold) },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .testTag("history_download_all_fab")
            )
        }
    }
}
