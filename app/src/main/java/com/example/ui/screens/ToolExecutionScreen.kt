package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocTool
import com.example.model.DocumentItem
import com.example.model.ToolId
import com.example.ui.components.FileChip
import com.example.ui.components.ResultComparisonCard
import com.example.ui.theme.IndigoPrimary
import com.example.viewmodel.DocMasterViewModel
import java.io.File

@Composable
fun ToolExecutionScreen(
    tool: DocTool,
    viewModel: DocMasterViewModel,
    onBack: () -> Unit,
    onPreviewFile: (File) -> Unit
) {
    BackHandler { onBack() }

    val uploadedFiles by viewModel.uploadedFiles.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val processingProgress by viewModel.processingProgress.collectAsState()
    val statusText by viewModel.processingStatusText.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()

    // Parameters state
    var compressionLevel by remember { mutableStateOf("Recommended") }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var passwordPin by remember { mutableStateOf("") }
    var targetWidth by remember { mutableIntStateOf(1080) }
    var targetHeight by remember { mutableIntStateOf(1080) }
    var qualityPercent by remember { mutableFloatStateOf(75f) }
    var rotationDegrees by remember { mutableFloatStateOf(90f) }
    var cropAspect by remember { mutableStateOf(Pair(1, 1)) }
    var pageNumbersFormat by remember { mutableStateOf("Page {page} of {total}") }

    // Launcher for selecting files
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addFiles(uris)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tool_execution_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tool Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("tool_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to dashboard",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tool.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = null,
                        tint = tool.accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tool.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Selected Files section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (tool.allowsMultipleFiles) "Selected Files (${uploadedFiles.size})" else "Selected File",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (uploadedFiles.isNotEmpty()) {
                            Text(
                                text = "Clear",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clickable { viewModel.clearFiles() }
                                    .padding(4.dp),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uploadedFiles.isEmpty()) {
                        // Empty upload prompt
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, IndigoPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable {
                                    launcher.launch(tool.acceptedMimeTypes.toTypedArray())
                                }
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Select ${tool.acceptedMimeTypes.joinToString(", ")} to begin",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { launcher.launch(tool.acceptedMimeTypes.toTypedArray()) },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Choose File", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // List of files
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (item in uploadedFiles) {
                                FileChip(item = item, onRemove = { viewModel.removeFile(item.id) })
                            }
                        }

                        if (tool.allowsMultipleFiles) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { launcher.launch(tool.acceptedMimeTypes.toTypedArray()) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add More Files")
                            }
                        }
                    }
                }
            }
        }

        // Tool-specific parameter settings
        if (uploadedFiles.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tool Options & Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        when (tool.id) {
                            ToolId.COMPRESS_PDF -> {
                                Text("Compression Level:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Low", "Recommended", "High").forEach { level ->
                                        FilterChip(
                                            selected = compressionLevel == level,
                                            onClick = { compressionLevel = level },
                                            label = { Text(level) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = IndigoPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            ToolId.ADD_WATERMARK -> {
                                OutlinedTextField(
                                    value = watermarkText,
                                    onValueChange = { watermarkText = it },
                                    label = { Text("Watermark Text") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            ToolId.PROTECT_PDF, ToolId.UNLOCK_PDF -> {
                                OutlinedTextField(
                                    value = passwordPin,
                                    onValueChange = { passwordPin = it },
                                    label = { Text(if (tool.id == ToolId.PROTECT_PDF) "Set Password PIN" else "Enter Password to Unlock") },
                                    placeholder = { Text("e.g. 1234") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            ToolId.COMPRESS_IMAGE -> {
                                Text("Compression Quality: ${qualityPercent.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Slider(
                                    value = qualityPercent,
                                    onValueChange = { qualityPercent = it },
                                    valueRange = 10f..95f,
                                    steps = 16
                                )
                            }

                            ToolId.ROTATE_IMAGE, ToolId.ROTATE_PDF -> {
                                Text("Rotation Angle:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(90f, 180f, 270f).forEach { deg ->
                                        FilterChip(
                                            selected = rotationDegrees == deg,
                                            onClick = { rotationDegrees = deg },
                                            label = { Text("${deg.toInt()}°") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = IndigoPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            ToolId.CROP_IMAGE -> {
                                Text("Aspect Ratio:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val ratios = listOf(
                                        Pair(1, 1) to "1:1 Square",
                                        Pair(4, 3) to "4:3 Classic",
                                        Pair(16, 9) to "16:9 Wide",
                                        Pair(0, 0) to "Free"
                                    )
                                    ratios.forEach { (ratio, label) ->
                                        FilterChip(
                                            selected = cropAspect == ratio,
                                            onClick = { cropAspect = ratio },
                                            label = { Text(label) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = IndigoPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            ToolId.RESIZE_IMAGE -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = targetWidth.toString(),
                                        onValueChange = { targetWidth = it.toIntOrNull() ?: targetWidth },
                                        label = { Text("Width (px)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = targetHeight.toString(),
                                        onValueChange = { targetHeight = it.toIntOrNull() ?: targetHeight },
                                        label = { Text("Height (px)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            ToolId.ADD_PAGE_NUMBERS -> {
                                Text("Numbering Format:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                listOf("Page {page} of {total}", "{page} / {total}", "{page}").forEach { pattern ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { pageNumbersFormat = pattern }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(selected = pageNumbersFormat == pattern, onClick = { pageNumbersFormat = pattern })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(pattern.replace("{page}", "1").replace("{total}", "5"))
                                    }
                                }
                            }

                            else -> {
                                Text(
                                    "Ready to process with default optimized settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Primary Execute Button
            item {
                Button(
                    onClick = {
                        viewModel.executeCurrentTool(
                            compressionLevel = compressionLevel,
                            watermarkText = watermarkText,
                            passwordPin = passwordPin,
                            targetWidth = targetWidth,
                            targetHeight = targetHeight,
                            qualityPercent = qualityPercent.toInt(),
                            rotationDegrees = rotationDegrees,
                            cropAspect = cropAspect,
                            pageNumbersFormat = pageNumbersFormat
                        )
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("execute_tool_button")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Processing...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tool.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Processing Progress bar
        if (isProcessing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${(processingProgress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = IndigoPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { processingProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = IndigoPrimary
                        )
                    }
                }
            }
        }

        // Result Card
        if (lastResult != null) {
            item {
                ResultComparisonCard(
                    result = lastResult!!,
                    onViewDocument = { file -> onPreviewFile(file) },
                    onZipBundle = {
                        viewModel.openTool(DocTool.ALL_TOOLS.first { it.id == ToolId.BATCH_ZIP })
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
