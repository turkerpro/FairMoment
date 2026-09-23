/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import android.app.SearchManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairphone.spring.launcher.R

/**
 * Universal renderer for any WidgetItem.
 * Displays clean M3 styling, solid/translucent contrast-protected surfaces,
 * and optional direct customize action.
 */
@Composable
fun WidgetCardContent(
    item: WidgetItem,
    notes: List<QuickNote> = emptyList(),
    onToggleNote: (QuickNote) -> Unit = {},
    onDeleteNote: (QuickNote) -> Unit = {},
    onAddNote: (String) -> Unit = {},
    onEditWidget: (() -> Unit)? = null,
    onDeleteWidget: (() -> Unit)? = null,
    isEditMode: Boolean = false,
    isInsideStack: Boolean = false,
    appWidgetHost: AppWidgetHost? = null,
    appWidgetManager: AppWidgetManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Container surface colors
    val cardBg = if (isDark) Color(0xFF1E2028).copy(alpha = 0.90f) else Color(0xFFFFFFFF).copy(alpha = 0.92f)
    val cardBorder = if (isDark) Color(0xFF2E323E) else Color(0xFFE2E5EC)
    val textColor = if (isDark) Color(0xFFF0F1F5) else Color(0xFF141519)
    val subTextColor = if (isDark) Color(0xFFA2A7B5) else Color(0xFF5E6370)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        shadowElevation = if (isInsideStack) 0.dp else 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Widget specific body
            when (item.type) {
                WidgetType.WEATHER -> WeatherContent(item, context, textColor, subTextColor)
                WidgetType.CALENDAR -> CalendarContent(context, textColor, subTextColor)
                WidgetType.BATTERY -> BatteryContent(item, context, textColor, subTextColor)
                WidgetType.NOTES -> NotesContent(notes, onToggleNote, onDeleteNote, onAddNote, textColor, subTextColor)
                WidgetType.MUSIC -> MusicContent(textColor, subTextColor)
                WidgetType.SCREEN_TIME -> ScreenTimeContent(textColor, subTextColor)
                WidgetType.SHORTCUTS -> ShortcutsContent(item, context, textColor, subTextColor)
                WidgetType.FITNESS -> FitnessContent(item, textColor, subTextColor)
                WidgetType.WORLD_CLOCK -> WorldClockContent(item, textColor, subTextColor)
                WidgetType.QUOTES -> QuotesContent(item, textColor, subTextColor)
                WidgetType.APP_WIDGET -> {
                    if (appWidgetHost != null && appWidgetManager != null) {
                        AndroidAppWidgetHostContainer(
                            appWidgetId = item.appWidgetId,
                            appWidgetHost = appWidgetHost,
                            appWidgetManager = appWidgetManager,
                            appLabel = item.appLabel,
                            minHeightDp = item.minHeightDp
                        )
                    }
                }
            }

            // Edit & Delete buttons ONLY visible when in Edit Mode (Düzenle modu açıkken)
            if (isEditMode && !isInsideStack) {
                // Delete button (top-left red badge, iOS style)
                if (onDeleteWidget != null) {
                    IconButton(
                        onClick = onDeleteWidget,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Widget",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Edit pencil button (top-right)
                if (onEditWidget != null) {
                    IconButton(
                        onClick = onEditWidget,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Widget",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. WEATHER WIDGET
// -------------------------------------------------------------
@Composable
private fun WeatherContent(
    item: WidgetItem,
    context: Context,
    textColor: Color,
    subTextColor: Color
) {
    // City weather data dictionary for interactive realism
    val cityTempMap = remember {
        mapOf(
            "Amsterdam" to Pair("19°C", "H: 22° L: 14°"),
            "Istanbul" to Pair("24°C", "H: 27° L: 18°"),
            "London" to Pair("17°C", "H: 20° L: 12°"),
            "New York" to Pair("22°C", "H: 25° L: 16°"),
            "Tokyo" to Pair("26°C", "H: 29° L: 21°"),
            "Berlin" to Pair("18°C", "H: 21° L: 13°"),
            "Paris" to Pair("21°C", "H: 24° L: 15°"),
            "Sydney" to Pair("20°C", "H: 23° L: 14°")
        )
    }

    val (tempDisplay, highLow) = cityTempMap[item.weatherCity] ?: Pair("21°C", "H: 24° L: 15°")
    val convertedTemp = if (item.weatherUnit == "°F") {
        val cVal = tempDisplay.replace("°C", "").toIntOrNull() ?: 20
        "${(cVal * 9 / 5) + 32}°F"
    } else {
        tempDisplay
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://www.google.com/search?q=weather+${item.weatherCity}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF2E6FF2).copy(alpha = 0.12f),
                        Color(0xFF00B0FF).copy(alpha = 0.05f)
                    )
                )
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Partly Sunny",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${item.weatherCity}  •  $highLow",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }

            Text(
                text = convertedTemp,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

// -------------------------------------------------------------
// 2. CALENDAR WIDGET
// -------------------------------------------------------------
@Composable
private fun CalendarContent(
    context: Context,
    textColor: Color,
    subTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALENDAR)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.widget_calendar_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "15:00",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Product Sprint Planning",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )
        Text(
            text = "15:00 - 16:30 • Google Meet",
            style = MaterialTheme.typography.bodySmall,
            color = subTextColor
        )
    }
}

// -------------------------------------------------------------
// 3. BATTERY & DEVICE WIDGET
// -------------------------------------------------------------
@Composable
private fun BatteryContent(
    item: WidgetItem,
    context: Context,
    textColor: Color,
    subTextColor: Color
) {
    val batteryManager = remember {
        context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    }
    val batteryLevel = remember {
        batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 82
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.widget_battery_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor
                )
            }

            Text(
                text = "$batteryLevel%",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { batteryLevel / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        if (item.batteryShowStorage) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RAM: 4.8 GB / 8 GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
                Text(
                    text = "Storage: 64% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. QUICK NOTES WIDGET
// -------------------------------------------------------------
@Composable
private fun NotesContent(
    notes: List<QuickNote>,
    onToggleNote: (QuickNote) -> Unit,
    onDeleteNote: (QuickNote) -> Unit,
    onAddNote: (String) -> Unit,
    textColor: Color,
    subTextColor: Color
) {
    var isAddingNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.widget_notes_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )

            if (!isAddingNote) {
                IconButton(
                    onClick = { isAddingNote = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Note",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (notes.isEmpty() && !isAddingNote) {
            Text(
                text = "No notes yet. Tap + to add one!",
                style = MaterialTheme.typography.bodyMedium,
                color = subTextColor
            )
        }

        notes.take(4).forEach { note ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = note.isDone,
                    onCheckedChange = { onToggleNote(note) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = note.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (note.isDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (note.isDone) subTextColor.copy(alpha = 0.5f) else textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onDeleteNote(note) },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = subTextColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = isAddingNote) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    singleLine = true,
                    placeholder = { Text("Write a quick note...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            isAddingNote = false
                            noteText = ""
                        },
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (noteText.isNotBlank()) {
                                onAddNote(noteText.trim())
                                noteText = ""
                                isAddingNote = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. NOW PLAYING / MUSIC WIDGET
// -------------------------------------------------------------
@Composable
private fun MusicContent(
    textColor: Color,
    subTextColor: Color
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    val trackList = remember {
        listOf(
            Pair("Fairphone Moments", "Ambient Focus Session"),
            Pair("Midnight Horizon", "Lo-Fi Beats"),
            Pair("Electric Pine", "Nordic Acoustic Chill")
        )
    }

    val (currentSong, currentArtist) = trackList[currentTrackIndex % trackList.size]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Album Art Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF5E2563), Color(0xFFE28859))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = currentSong,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentArtist,
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor,
                        maxLines = 1
                    )
                }
            }

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { currentTrackIndex++ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Track",
                        tint = subTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simulated Progress Bar
        LinearProgressIndicator(
            progress = { 0.42f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// -------------------------------------------------------------
// 6. SCREEN TIME WIDGET
// -------------------------------------------------------------
@Composable
private fun ScreenTimeContent(
    textColor: Color,
    subTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.widget_screentime_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "28 pickups • Moments Active",
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor
            )
        }

        Text(
            text = "1h 45m",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

// -------------------------------------------------------------
// 7. SHORTCUTS WIDGET
// -------------------------------------------------------------
@Composable
private fun ShortcutsContent(
    item: WidgetItem,
    context: Context,
    textColor: Color,
    subTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Text(
            text = stringResource(R.string.widget_shortcuts_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            item.selectedShortcuts.take(4).forEach { actionId ->
                when (actionId) {
                    "search" -> ShortcutItem(Icons.Default.Search, "Search", textColor) {
                        try {
                            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    "camera" -> ShortcutItem(Icons.Default.CameraAlt, "Camera", textColor) {
                        try {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    "calc" -> ShortcutItem(Icons.Default.Calculate, "Calc", textColor) {
                        try {
                            val intent = Intent().apply {
                                action = Intent.ACTION_MAIN
                                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    "alarm" -> ShortcutItem(Icons.Default.Alarm, "Alarm", textColor) {
                        try {
                            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    "flashlight" -> ShortcutItem(Icons.Default.FlashlightOn, "Torch", textColor) {}
                    "settings" -> ShortcutItem(Icons.Default.Settings, "Settings", textColor) {
                        try {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        } catch (_: Exception) {}
                    }
                    "maps" -> ShortcutItem(Icons.Default.Map, "Maps", textColor) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("geo:0,0?q=Coffee")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    "browser" -> ShortcutItem(Icons.Default.Language, "Web", textColor) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("https://google.com")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    else -> ShortcutItem(Icons.Default.Search, "Search", textColor) {}
                }
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    icon: ImageVector,
    label: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

// -------------------------------------------------------------
// 8. ACTIVITY & FITNESS WIDGET
// -------------------------------------------------------------
@Composable
private fun FitnessContent(
    item: WidgetItem,
    textColor: Color,
    subTextColor: Color
) {
    val currentSteps = 6840
    val progress = (currentSteps.toFloat() / item.fitnessGoal).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    tint = Color(0xFFFF5722),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.widget_fitness_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor
                )
            }

            Text(
                text = "$currentSteps / ${item.fitnessGoal} steps",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFFF5722),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🔥 340 kcal",
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor
            )
            Text(
                text = "📍 4.8 km",
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor
            )
            Text(
                text = "⏱️ 42 active mins",
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor
            )
        }
    }
}

// -------------------------------------------------------------
// 9. WORLD CLOCK WIDGET
// -------------------------------------------------------------
@Composable
private fun WorldClockContent(
    item: WidgetItem,
    textColor: Color,
    subTextColor: Color
) {
    val clocks = remember {
        listOf(
            Triple("London", "11:00", "Today, -1HR"),
            Triple("New York", "06:00", "Today, -6HRS"),
            Triple("Tokyo", "20:00", "Today, +7HRS")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.widget_world_clock_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            clocks.forEach { (city, time, diff) ->
                Column {
                    Text(
                        text = city,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Text(
                        text = diff,
                        style = MaterialTheme.typography.labelSmall,
                        color = subTextColor
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 10. DAILY INSPIRATION / QUOTES WIDGET
// -------------------------------------------------------------
@Composable
private fun QuotesContent(
    item: WidgetItem,
    textColor: Color,
    subTextColor: Color
) {
    var quoteIndex by remember { mutableIntStateOf(0) }
    val quotes = remember {
        listOf(
            Pair("The most sustainable phone is the one you already own.", "Fairphone"),
            Pair("Simplicity is the ultimate sophistication.", "Leonardo da Vinci"),
            Pair("Less distraction, more real connection.", "Digital Mindfulness"),
            Pair("Design for repairability and longevity.", "Circular Future")
        )
    }

    val (currentQuote, author) = quotes[quoteIndex % quotes.size]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.quoteCategory,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { quoteIndex++ },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Next Quote",
                    tint = subTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "“$currentQuote”",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "— $author",
            style = MaterialTheme.typography.labelSmall,
            color = subTextColor
        )
    }
}
