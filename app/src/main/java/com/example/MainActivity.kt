package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicBackgroundGradientEnd
import com.example.ui.theme.CosmicBackgroundGradientMid
import com.example.ui.theme.CosmicBackgroundGradientStart
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.DocumentItem
import com.example.ui.components.TopNavBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AllDocumentsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentViewerScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ToolExecutionScreen
import com.example.ui.theme.DocMasterTheme
import com.example.viewmodel.DocMasterViewModel
import com.example.viewmodel.ScreenState

class MainActivity : ComponentActivity() {
    private val viewModel: DocMasterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val uri: Uri? = when (action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
            else -> null
        }
        uri?.let { targetUri ->
            viewModel.handleIncomingUri(targetUri)
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CosmicBackgroundGradientStart,
                        CosmicBackgroundGradientMid,
                        CosmicBackground,
                        CosmicBackgroundGradientEnd
                    )
                )
            ),
        containerColor = Color.Transparent,
        topBar = {
            // ONLY display TopNavBar on the Dashboard (Home) screen.
            // On all other screens (Document Viewer, Workbench, History, About), it is hidden!
            if (currentScreen == ScreenState.DASHBOARD) {
                TopNavBar(
                    currentScreen = currentScreen,
                    isDarkMode = isDarkMode,
                    onNavigate = { screen -> viewModel.setScreen(screen) },
                    onToggleTheme = { viewModel.toggleDarkMode() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (currentScreen == ScreenState.DASHBOARD) {
                        Modifier.padding(innerPadding)
                    } else if (currentScreen == ScreenState.DOCUMENT_VIEWER) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    }
                )
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

                    ScreenState.ALL_DOCUMENTS -> {
                        AllDocumentsScreen(
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
