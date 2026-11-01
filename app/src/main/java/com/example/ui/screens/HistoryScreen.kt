package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.theme.ColorExcel
import com.example.ui.theme.ColorPdf
import com.example.ui.theme.ColorWord
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.DocMasterViewModel
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

    val filteredList = historyList.filter { entity ->
        val matchesType = when (selectedFilter) {
            "PDF" -> entity.fileType == "PDF"
            "Images" -> entity.fileType in listOf("JPG", "PNG", "WEBP")
            "Office" -> entity.fileType in listOf("XLSX", "CSV", "DOCX", "PPTX")
            else -> true
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            entity.fileName.contains(searchQuery, ignoreCase = true) ||
            entity.toolType.contains(searchQuery, ignoreCase = true)
        }
        matchesType && matchesSearch
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("This will remove all recent conversion and processing records from your history. The files will remain in your cache until cleaned.") },
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
                text = "Recent Processed Files",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (historyList.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search processed files...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "PDF", "Images", "Office").forEach { type ->
                FilterChip(
                    selected = selectedFilter == type,
                    onClick = { selectedFilter = type },
                    label = { Text(type) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // List or Empty state
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No history records found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Files processed using DocMaster tools will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { entity ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
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
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (entity.fileType) {
                                            "PDF" -> ColorPdf.copy(alpha = 0.15f)
                                            "JPG", "PNG", "WEBP" -> VioletSecondary.copy(alpha = 0.15f)
                                            "XLSX", "CSV" -> ColorExcel.copy(alpha = 0.15f)
                                            "DOCX" -> ColorWord.copy(alpha = 0.15f)
                                            else -> IndigoPrimary.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (entity.fileType) {
                                        "PDF" -> Icons.Default.PictureAsPdf
                                        "JPG", "PNG", "WEBP" -> Icons.Default.Image
                                        "XLSX", "CSV" -> Icons.Default.TableChart
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = when (entity.fileType) {
                                        "PDF" -> ColorPdf
                                        "JPG", "PNG", "WEBP" -> VioletSecondary
                                        "XLSX", "CSV" -> ColorExcel
                                        "DOCX" -> ColorWord
                                        else -> IndigoPrimary
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entity.fileName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${entity.toolType} • ${FileUtils.formatFileSize(entity.processedSizeBytes)}",
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

                            IconButton(
                                onClick = {
                                    val f = File(entity.filePath)
                                    if (f.exists()) FileUtils.shareFile(context, f, entity.mimeType)
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = IndigoPrimary)
                            }

                            IconButton(
                                onClick = { viewModel.deleteHistoryItem(entity.id) }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
