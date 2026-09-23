/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Minimalist, cute, and tightly clustered pager indicator shaped like "o O o".
 * Positioned closely together for a sweet, refined look,
 * with contrast protection to remain clearly visible over both dark and pure white wallpapers.
 */
@Composable
fun LauncherPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    isLightWallpaper: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(28.dp)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { pageIndex ->
            val isSelected = pageIndex == currentPage

            // "o O o" sizing: 5.5dp for inactive "o", 8.5dp for active "O"
            val targetSize = if (isSelected) 8.5.dp else 5.5.dp
            val dotSize by animateDpAsState(
                targetValue = targetSize,
                animationSpec = tween(durationMillis = 240),
                label = "dot_size"
            )

            // Dynamic adaptive fill color
            val targetFillColor = if (isLightWallpaper) {
                // Dark dots on white/light background
                if (isSelected) Color(0xFF141414).copy(alpha = 0.90f)
                else Color(0xFF282828).copy(alpha = 0.40f)
            } else {
                // White dots on dark background
                if (isSelected) Color.White.copy(alpha = 0.95f)
                else Color.White.copy(alpha = 0.45f)
            }
            val dotColor by animateColorAsState(
                targetValue = targetFillColor,
                animationSpec = tween(durationMillis = 240),
                label = "dot_color"
            )

            // High contrast border/halo to guarantee legibility on any photo background
            val targetBorderColor = if (isLightWallpaper) {
                Color.White.copy(alpha = if (isSelected) 0.70f else 0.40f)
            } else {
                Color.Black.copy(alpha = if (isSelected) 0.50f else 0.35f)
            }
            val borderColor by animateColorAsState(
                targetValue = targetBorderColor,
                animationSpec = tween(durationMillis = 240),
                label = "dot_border_color"
            )

            // Sweet, closely-spaced touch target (10.dp width) keeping dots close & charming
            Box(
                modifier = Modifier
                    .size(width = 10.dp, height = 28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onPageSelected(pageIndex) }
                    )
                    .testTag("pager_indicator_dot_$pageIndex"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(
                            width = 0.8.dp,
                            color = borderColor,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
