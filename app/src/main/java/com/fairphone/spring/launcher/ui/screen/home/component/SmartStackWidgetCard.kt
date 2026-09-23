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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairphone.spring.launcher.R

/**
 * iOS-style Smart Stack component.
 * Allows vertical swiping between stacked widgets, displays iconic iOS vertical capsule dots,
 * and provides "Edit Stack" sheet to reorder, add, or remove widgets in the stack.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartStackWidgetCard(
    stack: FeedItem.Stack,
    notes: List<QuickNote>,
    onToggleNote: (QuickNote) -> Unit,
    onDeleteNote: (QuickNote) -> Unit,
    onAddNote: (String) -> Unit,
    onUpdateStack: (FeedItem.Stack) -> Unit,
    onDeleteStack: () -> Unit,
    onOpenAddWidgetToStack: (FeedItem.Stack) -> Unit,
    onEditWidgetInStack: (WidgetItem) -> Unit,
    isEditMode: Boolean = false,
    appWidgetHost: AppWidgetHost? = null,
    appWidgetManager: AppWidgetManager? = null,
    modifier: Modifier = Modifier
) {
    if (stack.items.isEmpty()) return

    var currentIndex by remember(stack.id, stack.items.size) { mutableIntStateOf(0) }
    val safeIndex = currentIndex.coerceIn(0, stack.items.size - 1)
    val currentItem = stack.items[safeIndex]

    var showEditStackSheet by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var slideDirection by remember { mutableIntStateOf(1) } // 1 = down/next, -1 = up/prev

    val isDark = isSystemInDarkTheme()
    val indicatorBg = if (isDark) Color(0xFF101216).copy(alpha = 0.70f) else Color(0xFFE0E2E8).copy(alpha = 0.75f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .draggable(
                state = rememberDraggableState { delta ->
                    dragAccumulator += delta
                    if (dragAccumulator < -40f) {
                        // Swiped UP -> Next widget in stack
                        if (safeIndex < stack.items.size - 1) {
                            slideDirection = 1
                            currentIndex = safeIndex + 1
                        } else {
                            // Cycle back to top (smart stack continuous loop)
                            slideDirection = 1
                            currentIndex = 0
                        }
                        dragAccumulator = 0f
                    } else if (dragAccumulator > 40f) {
                        // Swiped DOWN -> Previous widget in stack
                        if (safeIndex > 0) {
                            slideDirection = -1
                            currentIndex = safeIndex - 1
                        } else {
                            slideDirection = -1
                            currentIndex = stack.items.size - 1
                        }
                        dragAccumulator = 0f
                    }
                },
                orientation = Orientation.Vertical
            )
            .testTag("smart_stack_${stack.id}")
    ) {
        // Active Widget Card with smooth vertical stack transition
        AnimatedContent(
            targetState = currentItem,
            transitionSpec = {
                if (slideDirection >= 0) {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut()
                    )
                } else {
                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> height } + fadeOut()
                    )
                }
            },
            label = "stack_page_transition"
        ) { targetWidget ->
            WidgetCardContent(
                item = targetWidget,
                notes = notes,
                onToggleNote = onToggleNote,
                onDeleteNote = onDeleteNote,
                onAddNote = onAddNote,
                onEditWidget = { onEditWidgetInStack(targetWidget) },
                isEditMode = isEditMode,
                isInsideStack = true,
                appWidgetHost = appWidgetHost,
                appWidgetManager = appWidgetManager
            )
        }

        // Top-Right Chip and Delete badge ONLY visible in Edit Mode
        if (isEditMode) {
            // Delete Stack Button (top-left)
            IconButton(
                onClick = onDeleteStack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Stack",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Top-Right Chip: "🥞 Smart Stack (N)" + Edit Action
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable { showEditStackSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Smart Stack",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Stack (${stack.items.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Stack",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        // iOS Vertical Capsule Stack Indicator Dots (right edge)
        if (stack.items.size > 1) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = indicatorBg,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    stack.items.indices.forEach { index ->
                        val isSelected = index == safeIndex
                        val dotHeight by animateDpAsState(
                            targetValue = if (isSelected) 10.dp else 4.dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "stack_dot_height"
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                (if (isDark) Color.White else Color.Black).copy(alpha = 0.35f)
                            },
                            animationSpec = tween(durationMillis = 200),
                            label = "stack_dot_color"
                        )

                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = dotHeight)
                                .clip(CircleShape)
                                .background(dotColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        slideDirection = if (index > safeIndex) 1 else -1
                                        currentIndex = index
                                    }
                                )
                        )
                    }
                }
            }
        }
    }

    // iOS "Edit Stack" Bottom Sheet
    if (showEditStackSheet) {
        val sheetBg = if (isDark) Color(0xFF16171B) else Color(0xFFFFFFFF)
        val sheetTextColor = if (isDark) Color(0xFFEDEDED) else Color(0xFF141414)
        val cardBg = if (isDark) Color(0xFF22242B) else Color(0xFFF4F5F8)
        val dividerColor = if (isDark) Color(0xFF2C2F3A) else Color(0xFFE5E7EB)

        ModalBottomSheet(
            onDismissRequest = { showEditStackSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = sheetBg,
            contentColor = sheetTextColor,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            tonalElevation = 12.dp,
            modifier = Modifier.testTag("edit_stack_bottom_sheet")
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
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.widget_edit_stack),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = sheetTextColor
                            )
                        }
                        Text(
                            text = "Reorder or remove widgets in this Smart Stack",
                            style = MaterialTheme.typography.bodySmall,
                            color = sheetTextColor.copy(alpha = 0.65f)
                        )
                    }

                    IconButton(
                        onClick = { showEditStackSheet = false },
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

                // Add to Stack Button
                Button(
                    onClick = {
                        showEditStackSheet = false
                        onOpenAddWidgetToStack(stack)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Widget to this Stack",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of Widgets in Stack
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, dividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        stack.items.forEachIndexed { idx, widget ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = widget.type.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    val itemTitle = if (widget.type == WidgetType.APP_WIDGET && widget.appLabel.isNotBlank()) {
                                        widget.appLabel
                                    } else {
                                        stringResource(widget.type.titleResId)
                                    }
                                    val itemDesc = if (widget.type == WidgetType.APP_WIDGET && widget.appLabel.isNotBlank()) {
                                        "Installed App Widget"
                                    } else {
                                        widget.type.description
                                    }
                                    Column {
                                        Text(
                                            text = itemTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = sheetTextColor
                                        )
                                        Text(
                                            text = itemDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = sheetTextColor.copy(alpha = 0.6f),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Move Up
                                    IconButton(
                                        onClick = {
                                            if (idx > 0) {
                                                val mutable = stack.items.toMutableList()
                                                val item = mutable.removeAt(idx)
                                                mutable.add(idx - 1, item)
                                                onUpdateStack(stack.copy(items = mutable))
                                            }
                                        },
                                        enabled = idx > 0,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Move Up",
                                            tint = if (idx > 0) sheetTextColor else sheetTextColor.copy(alpha = 0.25f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Move Down
                                    IconButton(
                                        onClick = {
                                            if (idx < stack.items.size - 1) {
                                                val mutable = stack.items.toMutableList()
                                                val item = mutable.removeAt(idx)
                                                mutable.add(idx + 1, item)
                                                onUpdateStack(stack.copy(items = mutable))
                                            }
                                        },
                                        enabled = idx < stack.items.size - 1,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Move Down",
                                            tint = if (idx < stack.items.size - 1) sheetTextColor else sheetTextColor.copy(alpha = 0.25f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Delete from Stack
                                    IconButton(
                                        onClick = {
                                            if (widget.type == WidgetType.APP_WIDGET && widget.appWidgetId != -1) {
                                                try {
                                                    appWidgetHost?.deleteAppWidgetId(widget.appWidgetId)
                                                } catch (_: Exception) {}
                                            }
                                            if (stack.items.size > 1) {
                                                val mutable = stack.items.toMutableList()
                                                mutable.removeAt(idx)
                                                onUpdateStack(stack.copy(items = mutable))
                                            } else {
                                                // If only 1 item left, deleting removes entire stack
                                                showEditStackSheet = false
                                                onDeleteStack()
                                            }
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (idx < stack.items.size - 1) {
                                HorizontalDivider(color = dividerColor.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Delete Stack Button
                Button(
                    onClick = {
                        stack.items.forEach { item ->
                            if (item.type == WidgetType.APP_WIDGET && item.appWidgetId != -1) {
                                try {
                                    appWidgetHost?.deleteAppWidgetId(item.appWidgetId)
                                } catch (_: Exception) {}
                            }
                        }
                        showEditStackSheet = false
                        onDeleteStack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935).copy(alpha = 0.15f),
                        contentColor = Color(0xFFE53935)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Entire Stack",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Done Button
                Button(
                    onClick = { showEditStackSheet = false },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.widgets_done),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
