/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fairphone.spring.launcher.R

data class AppWidgetDisplayItem(
    val providerInfo: AppWidgetProviderInfo,
    val appName: String,
    val widgetLabel: String,
    val iconBitmap: ImageBitmap?
)

/**
 * Global cache for installed app widgets and converted icon bitmaps
 * to minimize RAM allocations and avoid UI thread jank.
 */
object AppWidgetCache {
    private val iconBitmapCache = LruCache<String, ImageBitmap>(80)
    @Volatile
    private var cachedWidgetList: List<AppWidgetDisplayItem>? = null

    fun getWidgets(context: Context, forceRefresh: Boolean = false): List<AppWidgetDisplayItem> {
        if (!forceRefresh) {
            cachedWidgetList?.let { return it }
        }
        val appWidgetManager = try {
            AppWidgetManager.getInstance(context)
        } catch (e: Exception) {
            return emptyList()
        }
        val pm = context.packageManager
        val providers = try {
            appWidgetManager.installedProviders
        } catch (e: Exception) {
            emptyList()
        }

        val appLabelCache = mutableMapOf<String, String>()

        val list = providers.map { provider ->
            val pkg = provider.provider.packageName
            val appName = appLabelCache.getOrPut(pkg) {
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    pkg
                }
            }
            val label = try {
                provider.loadLabel(pm)
            } catch (e: Exception) {
                provider.provider.shortClassName ?: "Widget"
            }

            val cachedBmp = iconBitmapCache.get(pkg) ?: run {
                try {
                    val drawable = pm.getApplicationIcon(pkg)
                    val bmp = drawable.toBitmap(96, 96).asImageBitmap()
                    iconBitmapCache.put(pkg, bmp)
                    bmp
                } catch (e: Exception) {
                    null
                }
            }

            AppWidgetDisplayItem(
                providerInfo = provider,
                appName = appName,
                widgetLabel = label,
                iconBitmap = cachedBmp
            )
        }.sortedBy { it.appName.lowercase() }

        cachedWidgetList = list
        return list
    }

    fun clear() {
        cachedWidgetList = null
        iconBitmapCache.evictAll()
    }
}

/**
 * iOS-style Widget Gallery / Add Widget Sheet.
 * Supports both Built-in Launcher Widgets and 3rd-party Installed App Widgets!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetGallerySheet(
    targetStackForDirectAdd: FeedItem.Stack? = null,
    hasExistingStacks: Boolean = false,
    onAddWidgetToFeed: (WidgetType) -> Unit,
    onAddWidgetToStack: (WidgetType) -> Unit,
    onCreateNewStackWithWidget: (WidgetType) -> Unit,
    onSelectAppWidget: (AppWidgetProviderInfo, addToStack: Boolean) -> Unit,
    onLaunchSystemWidgetPicker: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val isDark = isSystemInDarkTheme()
    val sheetBg = if (isDark) Color(0xFF16171B) else Color(0xFFFFFFFF)
    val sheetTextColor = if (isDark) Color(0xFFEDEDED) else Color(0xFF141414)
    val cardBg = if (isDark) Color(0xFF22242B) else Color(0xFFF4F5F8)
    val dividerColor = if (isDark) Color(0xFF2C2F3A) else Color(0xFFE5E7EB)

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Launcher Widgets, 1 = Installed App Widgets
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Daily", "System", "Media", "Lifestyle")

    val builtInWidgets = remember {
        listOf(
            Triple(WidgetType.WEATHER, "Daily", "View real-time temperature, sky condition, and highs/lows."),
            Triple(WidgetType.CALENDAR, "Daily", "Keep track of scheduled calendar events and meetings."),
            Triple(WidgetType.BATTERY, "System", "Battery percentage with system RAM & storage health."),
            Triple(WidgetType.NOTES, "Daily", "Interactive checklist for your quick thoughts and tasks."),
            Triple(WidgetType.MUSIC, "Media", "Mini music controller with playback and track info."),
            Triple(WidgetType.SCREEN_TIME, "Lifestyle", "Daily screen usage, phone pickups, and moments focus."),
            Triple(WidgetType.SHORTCUTS, "System", "Fast one-tap access to Camera, Search, Calc, and Alarm."),
            Triple(WidgetType.FITNESS, "Lifestyle", "Daily activity step progress, active minutes, and calories."),
            Triple(WidgetType.WORLD_CLOCK, "Daily", "Global clocks across multiple world time zones."),
            Triple(WidgetType.QUOTES, "Lifestyle", "Inspiring quotes focusing on sustainability & mindfulness.")
        )
    }

    // Query installed app widgets asynchronously off the main thread with cache
    val installedAppWidgets by produceState<List<AppWidgetDisplayItem>>(initialValue = emptyList(), context) {
        value = withContext(Dispatchers.IO) {
            AppWidgetCache.getWidgets(context)
        }
    }

    val filteredBuiltIn = builtInWidgets.filter { (type, category, desc) ->
        val matchesCategory = selectedCategory == "All" || category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                type.name.contains(searchQuery, ignoreCase = true) ||
                desc.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val filteredAppWidgets = installedAppWidgets.filter { item ->
        searchQuery.isBlank() ||
                item.appName.contains(searchQuery, ignoreCase = true) ||
                item.widgetLabel.contains(searchQuery, ignoreCase = true) ||
                item.providerInfo.provider.packageName.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg,
        contentColor = sheetTextColor,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        tonalElevation = 12.dp,
        modifier = Modifier.testTag("widget_gallery_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.widget_gallery_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = sheetTextColor
                    )
                    Text(
                        text = if (targetStackForDirectAdd != null) {
                            "Select widget to add to ${targetStackForDirectAdd.name}"
                        } else {
                            stringResource(R.string.widget_gallery_subtitle)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = sheetTextColor.copy(alpha = 0.65f)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(cardBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = sheetTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Tab Row: Launcher Widgets vs Installed Apps
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_launcher_widgets),
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.tab_installed_app_widgets),
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                            )
                            if (installedAppWidgets.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "${installedAppWidgets.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (selectedTabIndex == 0) "Search widgets..." else stringResource(R.string.search_app_widgets_hint)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = sheetTextColor.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = dividerColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // TAB 0: BUILT-IN LAUNCHER WIDGETS
            if (selectedTabIndex == 0) {
                // Category Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredBuiltIn) { (type, category, desc) ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dividerColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = type.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = stringResource(type.titleResId),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = sheetTextColor
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = dividerColor.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = category,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = sheetTextColor.copy(alpha = 0.7f),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = sheetTextColor.copy(alpha = 0.7f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (targetStackForDirectAdd != null) {
                                        Button(
                                            onClick = {
                                                onAddWidgetToStack(type)
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add to ${targetStackForDirectAdd.name}")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                onAddWidgetToFeed(type)
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add Widget")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (hasExistingStacks) {
                                                    onAddWidgetToStack(type)
                                                } else {
                                                    onCreateNewStackWithWidget(type)
                                                }
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (hasExistingStacks) "Add to Stack" else "New Stack")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: INSTALLED APP WIDGETS
            if (selectedTabIndex == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Found ${filteredAppWidgets.size} app widgets",
                        style = MaterialTheme.typography.labelMedium,
                        color = sheetTextColor.copy(alpha = 0.65f)
                    )

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onLaunchSystemWidgetPicker()
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.open_system_widget_picker), style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (filteredAppWidgets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = sheetTextColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_app_widgets_found),
                                style = MaterialTheme.typography.bodyMedium,
                                color = sheetTextColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredAppWidgets, key = { "${it.providerInfo.provider.flattenToString()}_${it.widgetLabel}" }) { appWidget ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, dividerColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // App Icon
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(dividerColor.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (appWidget.iconBitmap != null) {
                                                Image(
                                                    bitmap = appWidget.iconBitmap,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Widgets,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = appWidget.appName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = sheetTextColor
                                            )
                                            Text(
                                                text = appWidget.widgetLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = sheetTextColor.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Dimension chip (e.g. 4x2 or min dimensions)
                                        val minW = appWidget.providerInfo.minWidth
                                        val minH = appWidget.providerInfo.minHeight
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = dividerColor.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                text = "${(minW + 30) / 70}x${(minH + 30) / 70}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                                                color = sheetTextColor.copy(alpha = 0.8f),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action buttons: Add to Feed or Add to Stack
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onSelectAppWidget(appWidget.providerInfo, false)
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add to Feed")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                onSelectAppWidget(appWidget.providerInfo, true)
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add to Stack")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
