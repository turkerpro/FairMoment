/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.prefs.LauncherWallpaperType
import com.fairphone.spring.launcher.ui.component.AnimatedBackground

@Composable
fun LauncherWallpaperBackground(
    wallpaperType: LauncherWallpaperType,
    blurRadius: Float = 0f,
    dimAlpha: Float = 0f,
    customImageUri: String? = null,
    customColorPrimary: Long = 0xFF4F46E5L,
    customColorSecondary: Long = 0xFFEC4899L,
    customColorStyle: String = "linear",
    profileColors: LauncherColors = LauncherColors.Default,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Base Wallpaper Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (blurRadius > 0.5f) {
                        Modifier.blur(blurRadius.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            Crossfade(
                targetState = wallpaperType,
                animationSpec = tween(400),
                label = "wallpaper_fade"
            ) { type ->
                when (type) {
                    LauncherWallpaperType.FAIRPHONE_DYNAMIC -> {
                        AnimatedBackground(
                            colors = profileColors,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LauncherWallpaperType.OLED_DARK -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF080808))
                        )
                    }
                    LauncherWallpaperType.DEEP_NEBULA -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0F0C29),
                                            Color(0xFF1E1742),
                                            Color(0xFF281E48),
                                            Color(0xFF0A071A)
                                        )
                                    )
                                )
                        )
                    }
                    LauncherWallpaperType.NORDIC_TWILIGHT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0D1B2A),
                                            Color(0xFF1B263B),
                                            Color(0xFF1D3557),
                                            Color(0xFF102030)
                                        )
                                    )
                                )
                        )
                    }
                    LauncherWallpaperType.SUNSET_DUNES -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF2C1320),
                                            Color(0xFF5D2436),
                                            Color(0xFF9E4338),
                                            Color(0xFFCE6E3B)
                                        )
                                    )
                                )
                        )
                    }
                    LauncherWallpaperType.EMERALD_FOREST -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF061A14),
                                            Color(0xFF0D2E24),
                                            Color(0xFF134537),
                                            Color(0xFF1D5A47)
                                        )
                                    )
                                )
                        )
                    }
                    LauncherWallpaperType.MINIMAL_WHITE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF),
                                            Color(0xFFF6F6F9),
                                            Color(0xFFEFEFF4)
                                        )
                                    )
                                )
                        )
                    }
                    LauncherWallpaperType.CUSTOM_SPECTRUM -> {
                        when (customColorStyle) {
                            "radial" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(customColorSecondary),
                                                    Color(customColorPrimary),
                                                    Color(customColorPrimary).copy(alpha = 0.7f),
                                                    Color(0xFF090A10)
                                                ),
                                                radius = 1200f
                                            )
                                        )
                                )
                            }
                            "diagonal" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(customColorPrimary),
                                                    Color(customColorSecondary),
                                                    Color(0xFF0D0E17)
                                                ),
                                                start = androidx.compose.ui.geometry.Offset.Zero,
                                                end = androidx.compose.ui.geometry.Offset.Infinite
                                            )
                                        )
                                )
                            }
                            "dynamic" -> {
                                AnimatedBackground(
                                    colors = LauncherColors(
                                        rightColor = customColorPrimary,
                                        leftColor = customColorSecondary
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> { // "linear"
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(customColorPrimary),
                                                    Color(customColorSecondary),
                                                    Color(0xFF0C0D15)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                    LauncherWallpaperType.CUSTOM_IMAGE -> {
                        if (!customImageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = Uri.parse(customImageUri),
                                contentDescription = "Custom Wallpaper",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Fallback to Dynamic if URI is empty
                            AnimatedBackground(
                                colors = profileColors,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Dim / Contrast Overlay
        if (dimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAlpha))
            )
        }

        // Foreground content (Launcher UI)
        content()
    }
}
