package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ColorExcel
import com.example.ui.theme.ColorPdf
import com.example.ui.theme.ColorPpt
import com.example.ui.theme.ColorWord
import com.example.ui.theme.CosmicNeonCyan
import com.example.ui.theme.CosmicNeonMagenta
import com.example.ui.theme.CosmicNeonPink
import com.example.ui.theme.CosmicNeonPurple
import com.example.ui.theme.CosmicSunsetAmber
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPrimary
import com.example.ui.theme.CosmicTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroUploadSection(
    onBrowseFiles: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color(0x35180C3D),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x55C084FC),
                    Color(0x25EC4899),
                    Color(0x1038BDF8)
                )
            )
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Atmospheric Glow Art inside the card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(26.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cosmic_hero_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x66080417),
                                    Color(0xAA0F0726),
                                    Color(0xF0070314)
                                )
                            )
                        ),
                    alpha = 0.32f
                )

                // Gradient scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x40070314),
                                    Color(0x900B0620),
                                    Color(0xF5080416)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sci-fi Pill Badge ("Shape the Future")
                Surface(
                    shape = CircleShape,
                    color = Color(0x3D160938),
                    modifier = Modifier.border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(CosmicNeonPurple, CosmicNeonMagenta)
                        ),
                        shape = CircleShape
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CosmicNeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Built for the Next Generation",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFFF3E8FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Headline directly matching uploaded artwork
                Text(
                    text = "Built for the\nNext Generation",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                        lineHeight = 36.sp,
                        fontSize = 30.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = CosmicTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Powerful on-device processing, AI-grade conversions, and instant previews for all your files.",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    textAlign = TextAlign.Center,
                    color = CosmicTextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Large Drag & Drop / Upload Area (Frosted Glass Container)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x45231252),
                                    Color(0x35150A36)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x77C084FC),
                                    Color(0x66EC4899),
                                    Color(0x4438BDF8)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onBrowseFiles() }
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                        .testTag("hero_dropzone"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Glowing Upload Orb
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CosmicNeonPurple, CosmicNeonMagenta)
                                    )
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = Color(0x66FFFFFF),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload files",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Drop files here or click to browse",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = CosmicTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "PDF, DOCX, XLSX, PPTX, JPG, PNG, WEBP, TXT",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = CosmicTextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Futuristic Pill Action Button ("Explore / Browse")
                        Button(
                            onClick = onBrowseFiles,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            shape = CircleShape,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(CosmicNeonPurple, CosmicNeonMagenta)
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0x88FFFFFF),
                                    shape = CircleShape
                                )
                                .testTag("hero_browse_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileOpen,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Browse Files",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Supported Format Pills with glowing dots
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CosmicFormatBadge("PDF", ColorPdf)
                    Spacer(modifier = Modifier.width(6.dp))
                    CosmicFormatBadge("Word", ColorWord)
                    Spacer(modifier = Modifier.width(6.dp))
                    CosmicFormatBadge("Excel", ColorExcel)
                    Spacer(modifier = Modifier.width(6.dp))
                    CosmicFormatBadge("PowerPoint", ColorPpt)
                    Spacer(modifier = Modifier.width(6.dp))
                    CosmicFormatBadge("Images", CosmicNeonPurple)
                    Spacer(modifier = Modifier.width(6.dp))
                    CosmicFormatBadge("TXT / Code", CosmicNeonCyan)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frosted Floating Stats Card ("33+ Tools • 100% On-Device")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x30180B38),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.8.dp,
                            color = Color(0x33C084FC),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CosmicNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "33+ Tools",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CosmicTextPrimary
                            )
                        }

                        Text(
                            text = "•",
                            color = Color(0x55FFFFFF)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ColorExcel,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% On-Device",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CosmicTextPrimary
                            )
                        }

                        Text(
                            text = "•",
                            color = Color(0x55FFFFFF)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CosmicNeonPink,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zero Uploads",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CosmicTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CosmicFormatBadge(label: String, dotColor: Color) {
    Surface(
        shape = CircleShape,
        color = Color(0x281F104D),
        modifier = Modifier.border(
            width = 0.8.dp,
            color = Color(0x35E9D5FF),
            shape = CircleShape
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = CosmicTextPrimary
            )
        }
    }
}
