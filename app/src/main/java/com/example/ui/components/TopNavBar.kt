package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicCardBorder
import com.example.ui.theme.CosmicNeonCyan
import com.example.ui.theme.CosmicNeonMagenta
import com.example.ui.theme.CosmicNeonPurple
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPrimary
import com.example.ui.theme.CosmicTextSecondary
import com.example.viewmodel.ScreenState

@Composable
fun TopNavBar(
    currentScreen: ScreenState,
    isDarkMode: Boolean,
    onNavigate: (ScreenState) -> Unit,
    onToggleTheme: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color(0xEE09041A),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.8.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x10C084FC),
                            Color(0x35C084FC),
                            Color(0x35EC4899),
                            Color(0x1038BDF8)
                        )
                    ),
                    shape = RectangleShape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("brand_logo_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(CosmicNeonPurple, CosmicNeonCyan)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_wolf_logo),
                            contentDescription = "DocMaster Wolf Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DocMaster",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 21.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = CosmicTextPrimary
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Sci-fi Pill Badge
                        Surface(
                            shape = CircleShape,
                            color = Color(0x28A855F7),
                            modifier = Modifier.border(
                                width = 0.8.dp,
                                color = Color(0x55C084FC),
                                shape = CircleShape
                            )
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = CosmicNeonCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action: All Documents (PDF, PPTX, Word, Excel on device)
                IconButton(
                    onClick = { onNavigate(ScreenState.ALL_DOCUMENTS) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (currentScreen == ScreenState.ALL_DOCUMENTS) Color(0x3538BDF8) else Color(0x18FFFFFF))
                        .border(
                            width = 0.8.dp,
                            color = if (currentScreen == ScreenState.ALL_DOCUMENTS) CosmicNeonCyan else Color(0x22FFFFFF),
                            shape = CircleShape
                        )
                        .testTag("all_docs_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "All Documents",
                        tint = if (currentScreen == ScreenState.ALL_DOCUMENTS) CosmicNeonCyan else CosmicTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action: Recent Files / History
                IconButton(
                    onClick = { onNavigate(ScreenState.HISTORY) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (currentScreen == ScreenState.HISTORY) Color(0x35A855F7) else Color(0x18FFFFFF))
                        .border(
                            width = 0.8.dp,
                            color = if (currentScreen == ScreenState.HISTORY) CosmicNeonPurple else Color(0x22FFFFFF),
                            shape = CircleShape
                        )
                        .testTag("history_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Recent Files",
                        tint = if (currentScreen == ScreenState.HISTORY) CosmicNeonPurple else CosmicTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action: About
                IconButton(
                    onClick = { onNavigate(ScreenState.ABOUT) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (currentScreen == ScreenState.ABOUT) Color(0x35A855F7) else Color(0x18FFFFFF))
                        .border(
                            width = 0.8.dp,
                            color = if (currentScreen == ScreenState.ABOUT) CosmicNeonPurple else Color(0x22FFFFFF),
                            shape = CircleShape
                        )
                        .testTag("about_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About DocMaster",
                        tint = if (currentScreen == ScreenState.ABOUT) CosmicNeonPurple else CosmicTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action: Dark/Light Mode Toggle
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .border(
                            width = 0.8.dp,
                            color = Color(0x22FFFFFF),
                            shape = CircleShape
                        )
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (isDarkMode) Color(0xFFFBBF24) else CosmicTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
