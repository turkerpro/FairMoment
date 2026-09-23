/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.prefs.LauncherWallpaperType
import com.fairphone.spring.launcher.ui.theme.LauncherFont
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherCustomizerSheet(
    selectedClockFont: LauncherFont,
    selectedMenuFont: LauncherFont,
    selectedWallpaperType: LauncherWallpaperType,
    blurRadius: Float,
    dimAlpha: Float,
    customImageUri: String?,
    onClockFontChange: (LauncherFont) -> Unit,
    onMenuFontChange: (LauncherFont) -> Unit,
    onWallpaperTypeChange: (LauncherWallpaperType) -> Unit,
    onBlurRadiusChange: (Float) -> Unit,
    onDimAlphaChange: (Float) -> Unit,
    onCustomImageSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    currentTime: String = "12:45",
    currentDate: String = "Wed, 23 Sep"
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var fontTargetSegment by remember { mutableIntStateOf(0) } // 0 = Clock & Date, 1 = Menu & Apps

    val isDark = isSystemInDarkTheme()
    // Solid, opaque container backgrounds to completely prevent background bleeding and overlap
    val sheetBg = if (isDark) Color(0xFF16171B) else Color(0xFFFFFFFF)
    val cardBg = if (isDark) Color(0xFF22242B) else Color(0xFFF4F5F8)
    val segmentBarBg = if (isDark) Color(0xFF23252E) else Color(0xFFE9ECF1)
    val sheetTextColor = if (isDark) Color(0xFFEDEDED) else Color(0xFF141414)
    val subtitleTextColor = if (isDark) Color(0xFFA5A9B4) else Color(0xFF636773)

    // Modern Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onCustomImageSelected(uri.toString())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg,
        contentColor = sheetTextColor,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        tonalElevation = 12.dp,
        modifier = Modifier.testTag("launcher_customizer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Row: Title & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.launcher_customize_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = sheetTextColor
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(cardBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.widgets_done),
                        tint = sheetTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Primary Tabs: Fonts vs Wallpaper
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                containerColor = segmentBarBg
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FontDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.tab_typography),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.tab_wallpaper),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                if (selectedTabIndex == 0) {
                    // ==========================================
                    // TAB 1: TYPOGRAPHY (Clean, spacious, uncrowded)
                    // ==========================================

                    // Segmented Selector: Clock vs Menu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(segmentBarBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = { fontTargetSegment = 0 },
                            shape = RoundedCornerShape(10.dp),
                            color = if (fontTargetSegment == 0) (if (isDark) Color(0xFF2C2F3A) else Color.White) else Color.Transparent,
                            shadowElevation = if (fontTargetSegment == 0) 2.dp else 0.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.font_settings_clock_title),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (fontTargetSegment == 0) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (fontTargetSegment == 0) MaterialTheme.colorScheme.primary else subtitleTextColor
                                )
                            }
                        }

                        Surface(
                            onClick = { fontTargetSegment = 1 },
                            shape = RoundedCornerShape(10.dp),
                            color = if (fontTargetSegment == 1) (if (isDark) Color(0xFF2C2F3A) else Color.White) else Color.Transparent,
                            shadowElevation = if (fontTargetSegment == 1) 2.dp else 0.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.font_settings_menu_title),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (fontTargetSegment == 1) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (fontTargetSegment == 1) MaterialTheme.colorScheme.primary else subtitleTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sleek, compact live preview banner (not oversized, leaving ample breathing room)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF2D303B) else Color(0xFFE2E4E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (fontTargetSegment == 0) "Preview" else "Apps Preview",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = subtitleTextColor
                            )

                            if (fontTargetSegment == 0) {
                                Text(
                                    text = "$currentTime  •  $currentDate",
                                    style = TextStyle(
                                        fontFamily = selectedClockFont.getFontFamily(),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = sheetTextColor
                                    )
                                )
                            } else {
                                Text(
                                    text = "Phone  •  Messages  •  Camera",
                                    style = TextStyle(
                                        fontFamily = selectedMenuFont.getFontFamily(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = sheetTextColor
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clean Vertical List of Font Options with solid opaque cards
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LauncherFont.entries.forEach { font ->
                            val isSelected = if (fontTargetSegment == 0) {
                                font == selectedClockFont
                            } else {
                                font == selectedMenuFont
                            }

                            FontChoiceRow(
                                font = font,
                                isSelected = isSelected,
                                isDark = isDark,
                                cardBg = cardBg,
                                sheetTextColor = sheetTextColor,
                                subtitleTextColor = subtitleTextColor,
                                onClick = {
                                    if (fontTargetSegment == 0) {
                                        onClockFontChange(font)
                                    } else {
                                        onMenuFontChange(font)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // TAB 2: WALLPAPER & BLUR (Duvar Kağıdı)
                    // ==========================================

                    Text(
                        text = stringResource(R.string.wallpaper_style_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = sheetTextColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gallery Pick Option Button
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cardBg,
                            contentColor = sheetTextColor
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF2E323D) else Color(0xFFE2E4E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (customImageUri != null) "Change Photo (${if (selectedWallpaperType == LauncherWallpaperType.CUSTOM_IMAGE) "Active" else "Select"})" else stringResource(R.string.wallpaper_gallery_pick),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid of Wallpapers
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val wallpaperList = LauncherWallpaperType.entries.filter { it != LauncherWallpaperType.CUSTOM_IMAGE }
                        wallpaperList.chunked(2).forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowItems.forEach { type ->
                                    val isSelected = selectedWallpaperType == type
                                    WallpaperCardItem(
                                        type = type,
                                        isSelected = isSelected,
                                        cardBg = cardBg,
                                        isDark = isDark,
                                        onClick = { onWallpaperTypeChange(type) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Blur Slider Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF2E323D) else Color(0xFFE2E4E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.wallpaper_blur_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = sheetTextColor
                                )
                                Text(
                                    text = "${blurRadius.roundToInt()} dp",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Slider(
                                value = blurRadius,
                                onValueChange = onBlurRadiusChange,
                                valueRange = 0f..25f,
                                steps = 24,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Adjust frosted blur effect for launcher wallpaper.",
                                style = MaterialTheme.typography.bodySmall,
                                color = subtitleTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dim Contrast Slider Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF2E323D) else Color(0xFFE2E4E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.wallpaper_dim_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = sheetTextColor
                                )
                                Text(
                                    text = "${(dimAlpha * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Slider(
                                value = dimAlpha,
                                onValueChange = onDimAlphaChange,
                                valueRange = 0f..0.60f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Adjust dark scrim for optimal text contrast on light wallpapers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = subtitleTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Done Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.widgets_done),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun FontChoiceRow(
    font: LauncherFont,
    isSelected: Boolean,
    isDark: Boolean,
    cardBg: Color,
    sheetTextColor: Color,
    subtitleTextColor: Color,
    onClick: () -> Unit
) {
    val selectedBg = if (isDark) Color(0xFF133631) else Color(0xFFE1F5F2)
    val selectedBorder = MaterialTheme.colorScheme.primary
    val unselectedBorder = if (isDark) Color(0xFF2C303B) else Color(0xFFE4E6EB)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) selectedBg else cardBg,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) selectedBorder else unselectedBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = font.title,
                    style = TextStyle(
                        fontFamily = font.getFontFamily(),
                        fontSize = 17.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else sheetTextColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = font.sampleText,
                    style = TextStyle(
                        fontFamily = font.getFontFamily(),
                        fontSize = 13.sp
                    ),
                    color = subtitleTextColor
                )
            }

            // Radio/Check indicator
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else subtitleTextColor.copy(alpha = 0.5f)
                ),
                modifier = Modifier.size(22.dp)
            ) {
                if (isSelected) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperCardItem(
    type: LauncherWallpaperType,
    isSelected: Boolean,
    cardBg: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unselectedBorder = if (isDark) Color(0xFF2C303B) else Color(0xFFE4E6EB)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, unselectedBorder)
        },
        modifier = modifier.clip(RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Preview Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        when (type) {
                            LauncherWallpaperType.FAIRPHONE_DYNAMIC -> Modifier.background(
                                Brush.linearGradient(listOf(Color(0xFF2C1320), Color(0xFF6E2845), Color(0xFFE28859)))
                            )
                            LauncherWallpaperType.OLED_DARK -> Modifier.background(Color(0xFF101010))
                            LauncherWallpaperType.DEEP_NEBULA -> Modifier.background(
                                Brush.verticalGradient(listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E)))
                            )
                            LauncherWallpaperType.NORDIC_TWILIGHT -> Modifier.background(
                                Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1D3557), Color(0xFF102030)))
                            )
                            LauncherWallpaperType.SUNSET_DUNES -> Modifier.background(
                                Brush.verticalGradient(listOf(Color(0xFF5D2436), Color(0xFFCE6E3B), Color(0xFFF7BD62)))
                            )
                            LauncherWallpaperType.EMERALD_FOREST -> Modifier.background(
                                Brush.verticalGradient(listOf(Color(0xFF061A14), Color(0xFF134537), Color(0xFF1D5A47)))
                            )
                            LauncherWallpaperType.MINIMAL_WHITE -> Modifier.background(
                                Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F5)))
                            )
                            LauncherWallpaperType.CUSTOM_IMAGE -> Modifier.background(Color(0xFF202020))
                        }
                    )
                    .then(
                        if (type == LauncherWallpaperType.MINIMAL_WHITE) {
                            Modifier.border(0.5.dp, Color.LightGray, RoundedCornerShape(10.dp))
                        } else Modifier
                    )
            ) {
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = type.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
