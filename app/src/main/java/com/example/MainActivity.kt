package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DocumentItem
import com.example.ui.components.TopNavBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentViewerScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ToolExecutionScreen
import com.example.ui.theme.DocMasterTheme
import com.example.viewmodel.DocMasterViewModel
import com.example.viewmodel.ScreenState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DocMasterViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            DocMasterTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DocMasterApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DocMasterApp(viewModel: DocMasterViewModel) {
    val currentScreen by viewModel.screenState.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val viewingDoc by viewModel.viewingDoc.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopNavBar(
                currentScreen = currentScreen,
                isDarkMode = isDarkMode,
                onNavigate = { screen -> viewModel.setScreen(screen) },
                onToggleTheme = { viewModel.toggleDarkMode() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    ScreenState.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onOpenTool = { tool -> viewModel.openTool(tool) },
                            onViewDocument = { docItem -> viewModel.openDocumentInViewer(docItem) }
                        )
                    }

                    ScreenState.TOOL_WORKBENCH -> {
                        if (activeTool != null) {
                            ToolExecutionScreen(
                                tool = activeTool!!,
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() },
                                onPreviewFile = { file ->
                                    viewModel.openDocumentInViewer(
                                        DocumentItem(
                                            name = file.name,
                                            uri = Uri.fromFile(file),
                                            sizeBytes = file.length(),
                                            mimeType = when (file.extension.lowercase()) {
                                                "pdf" -> "application/pdf"
                                                "jpg", "jpeg" -> "image/jpeg"
                                                "png" -> "image/png"
                                                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                                "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                                "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                                                else -> "text/plain"
                                            },
                                            tempFilePath = file.absolutePath
                                        )
                                    )
                                }
                            )
                        } else {
                            DashboardScreen(
                                viewModel = viewModel,
                                onOpenTool = { tool -> viewModel.openTool(tool) },
                                onViewDocument = { docItem -> viewModel.openDocumentInViewer(docItem) }
                            )
                        }
                    }

                    ScreenState.DOCUMENT_VIEWER -> {
                        DocumentViewerScreen(
                            documentItem = viewingDoc,
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    ScreenState.HISTORY -> {
                        HistoryScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() },
                            onViewDocument = { docItem -> viewModel.openDocumentInViewer(docItem) }
                        )
                    }

                    ScreenState.ABOUT -> {
                        AboutScreen(
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
