/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private val widgetInfoCache = mutableMapOf<Int, AppWidgetProviderInfo>()

@Composable
fun AndroidAppWidgetHostContainer(
    appWidgetId: Int,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    appLabel: String = "",
    minHeightDp: Int = 180,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val appWidgetInfo: AppWidgetProviderInfo? = remember(appWidgetId) {
        widgetInfoCache[appWidgetId] ?: run {
            try {
                appWidgetManager.getAppWidgetInfo(appWidgetId)?.also {
                    widgetInfoCache[appWidgetId] = it
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    val cardBg = if (isDark) Color(0xFF1E2028).copy(alpha = 0.90f) else Color(0xFFFFFFFF).copy(alpha = 0.92f)
    val cardBorder = if (isDark) Color(0xFF2E323E) else Color(0xFFE2E5EC)
    val textColor = if (isDark) Color(0xFFF0F1F5) else Color(0xFF141519)
    val subTextColor = if (isDark) Color(0xFFA2A7B5) else Color(0xFF5E6370)

    val heightDp = remember(appWidgetInfo, minHeightDp) {
        if (appWidgetInfo != null && appWidgetInfo.minHeight > 0) {
            appWidgetInfo.minHeight.coerceIn(120, 320).dp
        } else {
            minHeightDp.coerceIn(120, 320).dp
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
    ) {
        if (appWidgetInfo == null) {
            // Widget uninstalled or invalid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = subTextColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (appLabel.isNotBlank()) appLabel else "Application Widget",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Text(
                    text = "Widget is no longer installed or configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }
        } else {
            // Render actual live Android AppWidgetHostView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 340.dp)
                    .height(heightDp)
                    .padding(4.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        try {
                            appWidgetHost.createView(ctx, appWidgetId, appWidgetInfo).apply {
                                setAppWidget(appWidgetId, appWidgetInfo)
                            }
                        } catch (e: Exception) {
                            android.widget.TextView(ctx).apply {
                                text = "Unable to load widget"
                            }
                        }
                    },
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}
