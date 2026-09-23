/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairphone.spring.launcher.R

/**
 * Bottom Sheet to customize / edit specific settings of any individual widget.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigSheet(
    item: WidgetItem,
    onSaveConfig: (WidgetItem) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val sheetBg = if (isDark) Color(0xFF16171B) else Color(0xFFFFFFFF)
    val sheetTextColor = if (isDark) Color(0xFFEDEDED) else Color(0xFF141414)
    val cardBg = if (isDark) Color(0xFF22242B) else Color(0xFFF4F5F8)
    val dividerColor = if (isDark) Color(0xFF2C2F3A) else Color(0xFFE5E7EB)

    // Configurable state
    var selectedCity by remember { mutableStateOf(item.weatherCity) }
    var selectedUnit by remember { mutableStateOf(item.weatherUnit) }
    var quoteCategory by remember { mutableStateOf(item.quoteCategory) }
    var batteryShowStorage by remember { mutableStateOf(item.batteryShowStorage) }
    var fitnessGoal by remember { mutableIntStateOf(item.fitnessGoal) }
    val activeShortcuts = remember { mutableStateListOf<String>().apply { addAll(item.selectedShortcuts) } }

    val cities = listOf("Amsterdam", "Istanbul", "London", "New York", "Tokyo", "Berlin", "Paris", "Sydney")
    val allShortcuts = listOf(
        Pair("search", "Search"),
        Pair("camera", "Camera"),
        Pair("calc", "Calculator"),
        Pair("alarm", "Alarm"),
        Pair("flashlight", "Torch"),
        Pair("settings", "Settings"),
        Pair("maps", "Maps"),
        Pair("browser", "Browser")
    )
    val quoteCategories = listOf("Sustainability", "Digital Minimalism", "Mindfulness", "Daily Motivation")
    val fitnessGoals = listOf(5000, 8000, 10000, 12000)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg,
        contentColor = sheetTextColor,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        tonalElevation = 12.dp,
        modifier = Modifier.testTag("widget_config_sheet")
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
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = item.type.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Edit ${stringResource(item.type.titleResId)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = sheetTextColor
                        )
                        Text(
                            text = "Customize parameters and display preferences",
                            style = MaterialTheme.typography.bodySmall,
                            color = sheetTextColor.copy(alpha = 0.65f)
                        )
                    }
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

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (item.type) {
                    WidgetType.WEATHER -> {
                        // City Selection
                        Text(
                            text = "City / Location",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = sheetTextColor
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                cities.chunked(3).forEach { rowCities ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        rowCities.forEach { city ->
                                            val isSelected = selectedCity == city
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedCity = city },
                                                label = { Text(city) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Unit Selection
                        Text(
                            text = "Temperature Unit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = sheetTextColor
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("°C", "°F").forEach { unit ->
                                val isSelected = selectedUnit == unit
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedUnit = unit },
                                    label = { Text(if (unit == "°C") "Celsius (°C)" else "Fahrenheit (°F)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    WidgetType.SHORTCUTS -> {
                        Text(
                            text = "Choose 4 Quick Action Shortcuts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = sheetTextColor
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dividerColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                allShortcuts.forEachIndexed { idx, (id, label) ->
                                    val isChecked = activeShortcuts.contains(id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = label, color = sheetTextColor, style = MaterialTheme.typography.bodyLarge)
                                        Switch(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    if (activeShortcuts.size < 4) activeShortcuts.add(id)
                                                } else {
                                                    if (activeShortcuts.size > 1) activeShortcuts.remove(id)
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    }
                                    if (idx < allShortcuts.size - 1) {
                                        HorizontalDivider(color = dividerColor.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }

                    WidgetType.QUOTES -> {
                        Text(
                            text = "Inspiration Category",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = sheetTextColor
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            quoteCategories.forEach { cat ->
                                val isSelected = quoteCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { quoteCategory = cat },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    WidgetType.FITNESS -> {
                        Text(
                            text = "Daily Step Goal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = sheetTextColor
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fitnessGoals.forEach { goal ->
                                val isSelected = fitnessGoal == goal
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { fitnessGoal = goal },
                                    label = { Text("$goal steps") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    WidgetType.BATTERY -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dividerColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Show RAM & Storage Details",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = sheetTextColor
                                    )
                                    Text(
                                        text = "Display memory and disk usage metrics",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = sheetTextColor.copy(alpha = 0.65f)
                                    )
                                }
                                Switch(
                                    checked = batteryShowStorage,
                                    onCheckedChange = { batteryShowStorage = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }

                    else -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dividerColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "This widget automatically synchronizes live data with your system.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = sheetTextColor.copy(alpha = 0.7f),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Save Changes Button
            Button(
                onClick = {
                    val updated = item.copy(
                        weatherCity = selectedCity,
                        weatherUnit = selectedUnit,
                        quoteCategory = quoteCategory,
                        batteryShowStorage = batteryShowStorage,
                        fitnessGoal = fitnessGoal,
                        selectedShortcuts = activeShortcuts.toList()
                    )
                    onSaveConfig(updated)
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
