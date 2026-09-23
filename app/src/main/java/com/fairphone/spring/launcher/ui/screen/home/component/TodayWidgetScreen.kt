/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairphone.spring.launcher.R
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayWidgetScreen(
    date: String,
    onNavigateBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val widgetPrefs = remember { TodayWidgetPreferences(context) }

    // AppWidget system integration for installed applications
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = remember { AppWidgetHost(context.applicationContext, 1024) }

    DisposableEffect(appWidgetHost) {
        try {
            appWidgetHost.startListening()
        } catch (e: Exception) {
            // ignore
        }
        onDispose {
            try {
                appWidgetHost.stopListening()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // Persistent widget feed state
    var feedItems by remember { mutableStateOf(widgetPrefs.loadFeed()) }
    val notes = remember { mutableStateListOf<QuickNote>().apply { addAll(widgetPrefs.loadNotes()) } }

    // Modals & Sheets
    var showGallerySheet by remember { mutableStateOf(false) }
    var targetStackForAdd by remember { mutableStateOf<FeedItem.Stack?>(null) }
    var showManageFeedSheet by remember { mutableStateOf(false) }
    var editingWidgetItem by remember { mutableStateOf<WidgetItem?>(null) }
    var isEditMode by remember { mutableStateOf(false) }

    // Pending AppWidget states during binding/configuration
    var pendingAppWidgetId by remember { mutableIntStateOf(-1) }
    var pendingProviderInfo by remember { mutableStateOf<AppWidgetProviderInfo?>(null) }
    var pendingAddToStack by remember { mutableStateOf(false) }
    var pendingPickAppWidgetId by remember { mutableIntStateOf(-1) }

    val isDark = isSystemInDarkTheme()
    val headerTextColor = if (isDark) Color(0xFFF0F1F5) else Color(0xFF141519)
    val subTextColor = if (isDark) Color(0xFFA2A7B5) else Color(0xFF5E6370)
    val buttonBg = if (isDark) Color(0xFF242732).copy(alpha = 0.8f) else Color(0xFFE8EBF2).copy(alpha = 0.8f)

    fun persistFeed(updated: List<FeedItem>) {
        feedItems = updated
        widgetPrefs.saveFeed(updated)
    }

    fun persistNotes() {
        widgetPrefs.saveNotes(notes)
    }

    fun deleteWidgetItem(item: WidgetItem) {
        if (item.type == WidgetType.APP_WIDGET && item.appWidgetId >= 0) {
            try {
                appWidgetHost.deleteAppWidgetId(item.appWidgetId)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun addConfiguredAppWidget(id: Int, info: AppWidgetProviderInfo, addToStack: Boolean) {
        val pm = context.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(info.provider.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            info.loadLabel(pm)
        }
        val newWidget = WidgetItem(
            type = WidgetType.APP_WIDGET,
            appWidgetId = id,
            providerPackage = info.provider.packageName,
            providerClass = info.provider.className,
            appLabel = "$appName • ${info.loadLabel(pm)}",
            minHeightDp = info.minHeight.coerceIn(120, 320)
        )

        if (addToStack && targetStackForAdd != null) {
            val idx = feedItems.indexOfFirst { it.id == targetStackForAdd?.id }
            if (idx >= 0) {
                val stack = feedItems[idx] as FeedItem.Stack
                val mutable = feedItems.toMutableList()
                mutable[idx] = stack.copy(items = stack.items + newWidget)
                persistFeed(mutable)
            }
        } else if (addToStack && feedItems.any { it is FeedItem.Stack }) {
            val firstStack = feedItems.first { it is FeedItem.Stack } as FeedItem.Stack
            val idx = feedItems.indexOfFirst { it.id == firstStack.id }
            val mutable = feedItems.toMutableList()
            mutable[idx] = firstStack.copy(items = firstStack.items + newWidget)
            persistFeed(mutable)
        } else {
            persistFeed(feedItems + FeedItem.Single(newWidget))
        }
    }

    val configureAppWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val returnedId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId) ?: pendingAppWidgetId
            pendingProviderInfo?.let { addConfiguredAppWidget(returnedId, it, pendingAddToStack) }
        } else {
            if (pendingAppWidgetId != -1) {
                try {
                    appWidgetHost.deleteAppWidgetId(pendingAppWidgetId)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        pendingAppWidgetId = -1
        pendingProviderInfo = null
    }

    val bindAppWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val info = pendingProviderInfo
            val id = pendingAppWidgetId
            if (info != null && id != -1) {
                if (info.configure != null) {
                    val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                        component = info.configure
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    }
                    configureAppWidgetLauncher.launch(configIntent)
                } else {
                    addConfiguredAppWidget(id, info, pendingAddToStack)
                    pendingAppWidgetId = -1
                    pendingProviderInfo = null
                }
            }
        } else {
            if (pendingAppWidgetId != -1) {
                try {
                    appWidgetHost.deleteAppWidgetId(pendingAppWidgetId)
                } catch (e: Exception) {
                    // ignore
                }
            }
            pendingAppWidgetId = -1
            pendingProviderInfo = null
        }
    }

    val pickAppWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pickedId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (pickedId != -1) {
                val pickedInfo = appWidgetManager.getAppWidgetInfo(pickedId)
                if (pickedInfo != null) {
                    if (pickedInfo.configure != null) {
                        pendingAppWidgetId = pickedId
                        pendingProviderInfo = pickedInfo
                        pendingAddToStack = false
                        val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                            component = pickedInfo.configure
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pickedId)
                        }
                        configureAppWidgetLauncher.launch(configIntent)
                    } else {
                        addConfiguredAppWidget(pickedId, pickedInfo, false)
                    }
                }
            }
        } else {
            if (pendingPickAppWidgetId != -1) {
                try {
                    appWidgetHost.deleteAppWidgetId(pendingPickAppWidgetId)
                } catch (e: Exception) {
                    // ignore
                }
                pendingPickAppWidgetId = -1
            }
        }
    }

    fun requestAddAppWidget(info: AppWidgetProviderInfo, addToStack: Boolean) {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val canBind = try {
            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)
        } catch (e: Exception) {
            false
        }

        if (canBind) {
            if (info.configure != null) {
                pendingAppWidgetId = appWidgetId
                pendingProviderInfo = info
                pendingAddToStack = addToStack
                val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = info.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                configureAppWidgetLauncher.launch(configIntent)
            } else {
                addConfiguredAppWidget(appWidgetId, info, addToStack)
            }
        } else {
            pendingAppWidgetId = appWidgetId
            pendingProviderInfo = info
            pendingAddToStack = addToStack
            val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
            }
            bindAppWidgetLauncher.launch(bindIntent)
        }
    }

    fun launchSystemPicker() {
        val newId = appWidgetHost.allocateAppWidgetId()
        pendingPickAppWidgetId = newId
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
        }
        pickAppWidgetLauncher.launch(pickIntent)
    }

    // Handle Android system back button
    BackHandler {
        when {
            showGallerySheet -> showGallerySheet = false
            showManageFeedSheet -> showManageFeedSheet = false
            editingWidgetItem != null -> editingWidgetItem = null
            isEditMode -> isEditMode = false
            else -> onNavigateBackToHome()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isEditMode) "Edit Widgets" else stringResource(R.string.widgets_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = headerTextColor
                )
                Text(
                    text = if (isEditMode) "Tap pencil to edit or - to remove" else date,
                    style = MaterialTheme.typography.labelLarge,
                    color = subTextColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // "+" Add Widget Button (iOS style)
                IconButton(
                    onClick = {
                        targetStackForAdd = null
                        showGallerySheet = true
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .testTag("widgets_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.widget_add_title),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isEditMode) {
                    // "Done" Button to exit edit mode
                    Button(
                        onClick = { isEditMode = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("widgets_done_button")
                    ) {
                        Text(
                            text = stringResource(R.string.widgets_done),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    // Customize / Reorder / Enter Edit Mode Button
                    IconButton(
                        onClick = { isEditMode = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(buttonBg)
                            .testTag("widgets_customize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = stringResource(R.string.widgets_customize),
                            tint = headerTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Back to Home
                    IconButton(
                        onClick = onNavigateBackToHome,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(buttonBg)
                            .testTag("widgets_back_to_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.bt_navigate_back),
                            tint = headerTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Widgets Scrollable List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(
                items = feedItems,
                key = { _, item -> item.id }
            ) { index, feedItem ->
                when (feedItem) {
                    is FeedItem.Stack -> {
                        SmartStackWidgetCard(
                            stack = feedItem,
                            notes = notes,
                            onToggleNote = { note ->
                                val noteIndex = notes.indexOfFirst { it.id == note.id }
                                if (noteIndex >= 0) {
                                    notes[noteIndex] = note.copy(isDone = !note.isDone)
                                    persistNotes()
                                }
                            },
                            onDeleteNote = { note ->
                                notes.removeAll { it.id == note.id }
                                persistNotes()
                            },
                            onAddNote = { text ->
                                notes.add(QuickNote(System.currentTimeMillis(), text, false))
                                persistNotes()
                            },
                            onUpdateStack = { updatedStack ->
                                val mutable = feedItems.toMutableList()
                                mutable[index] = updatedStack
                                persistFeed(mutable)
                            },
                            onDeleteStack = {
                                feedItem.items.forEach { deleteWidgetItem(it) }
                                val mutable = feedItems.toMutableList()
                                mutable.removeAt(index)
                                persistFeed(mutable)
                            },
                            onOpenAddWidgetToStack = { stack ->
                                targetStackForAdd = stack
                                showGallerySheet = true
                            },
                            onEditWidgetInStack = { widgetItem ->
                                editingWidgetItem = widgetItem
                            },
                            isEditMode = isEditMode,
                            appWidgetHost = appWidgetHost,
                            appWidgetManager = appWidgetManager
                        )
                    }

                    is FeedItem.Single -> {
                        WidgetCardContent(
                            item = feedItem.item,
                            notes = notes,
                            onToggleNote = { note ->
                                val noteIndex = notes.indexOfFirst { it.id == note.id }
                                if (noteIndex >= 0) {
                                    notes[noteIndex] = note.copy(isDone = !note.isDone)
                                    persistNotes()
                                }
                            },
                            onDeleteNote = { note ->
                                notes.removeAll { it.id == note.id }
                                persistNotes()
                            },
                            onAddNote = { text ->
                                notes.add(QuickNote(System.currentTimeMillis(), text, false))
                                persistNotes()
                            },
                            onEditWidget = {
                                editingWidgetItem = feedItem.item
                            },
                            onDeleteWidget = {
                                deleteWidgetItem(feedItem.item)
                                val mutable = feedItems.toMutableList()
                                mutable.removeAt(index)
                                persistFeed(mutable)
                            },
                            isEditMode = isEditMode,
                            appWidgetHost = appWidgetHost,
                            appWidgetManager = appWidgetManager
                        )
                    }
                }
            }

            // Bottom Action Cards (Add Widget & Manage / Done)
            item(key = "footer_actions") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            targetStackForAdd = null
                            showGallerySheet = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBg,
                            contentColor = headerTextColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.widget_add_title),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    if (isEditMode) {
                        Button(
                            onClick = { showManageFeedSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonBg,
                                contentColor = headerTextColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Arrange All",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    } else {
                        Button(
                            onClick = { isEditMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonBg,
                                contentColor = headerTextColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.widgets_customize),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // SHEET 1: WIDGET GALLERY (iOS Style "+ Add Widget")
    // -------------------------------------------------------------
    if (showGallerySheet) {
        val existingStacks = feedItems.filterIsInstance<FeedItem.Stack>()
        WidgetGallerySheet(
            targetStackForDirectAdd = targetStackForAdd,
            hasExistingStacks = existingStacks.isNotEmpty(),
            onAddWidgetToFeed = { type ->
                val newWidget = WidgetItem(type = type)
                persistFeed(feedItems + FeedItem.Single(newWidget))
            },
            onAddWidgetToStack = { type ->
                val newWidget = WidgetItem(type = type)
                if (targetStackForAdd != null) {
                    val idx = feedItems.indexOfFirst { it.id == targetStackForAdd?.id }
                    if (idx >= 0) {
                        val stack = feedItems[idx] as FeedItem.Stack
                        val mutable = feedItems.toMutableList()
                        mutable[idx] = stack.copy(items = stack.items + newWidget)
                        persistFeed(mutable)
                    }
                } else if (existingStacks.isNotEmpty()) {
                    // Add to first existing stack
                    val firstStack = existingStacks.first()
                    val idx = feedItems.indexOfFirst { it.id == firstStack.id }
                    if (idx >= 0) {
                        val mutable = feedItems.toMutableList()
                        mutable[idx] = firstStack.copy(items = firstStack.items + newWidget)
                        persistFeed(mutable)
                    }
                } else {
                    // Create new stack
                    val newStack = FeedItem.Stack(
                        id = UUID.randomUUID().toString(),
                        name = "Smart Stack",
                        items = listOf(newWidget)
                    )
                    persistFeed(feedItems + newStack)
                }
            },
            onCreateNewStackWithWidget = { type ->
                val newWidget = WidgetItem(type = type)
                val newStack = FeedItem.Stack(
                    id = UUID.randomUUID().toString(),
                    name = "Smart Stack",
                    items = listOf(newWidget)
                )
                persistFeed(feedItems + newStack)
            },
            onSelectAppWidget = { providerInfo, addToStack ->
                requestAddAppWidget(providerInfo, addToStack)
            },
            onLaunchSystemWidgetPicker = {
                launchSystemPicker()
            },
            onDismiss = {
                showGallerySheet = false
                targetStackForAdd = null
            }
        )
    }

    // -------------------------------------------------------------
    // SHEET 2: WIDGET CONFIGURATION / PARAMETER CUSTOMIZATION
    // -------------------------------------------------------------
    editingWidgetItem?.let { itemToEdit ->
        WidgetConfigSheet(
            item = itemToEdit,
            onSaveConfig = { updatedItem ->
                // Look for item in singles or inside any stack
                val updatedFeed = feedItems.map { feedItem ->
                    when (feedItem) {
                        is FeedItem.Single -> {
                            if (feedItem.item.id == updatedItem.id) FeedItem.Single(updatedItem) else feedItem
                        }
                        is FeedItem.Stack -> {
                            val updatedStackItems = feedItem.items.map {
                                if (it.id == updatedItem.id) updatedItem else it
                            }
                            feedItem.copy(items = updatedStackItems)
                        }
                    }
                }
                persistFeed(updatedFeed)
                editingWidgetItem = null
            },
            onDismiss = { editingWidgetItem = null }
        )
    }

    // -------------------------------------------------------------
    // SHEET 3: MANAGE FEED & STACKS (Reorder, Combine, Delete, Reset)
    // -------------------------------------------------------------
    if (showManageFeedSheet) {
        val sheetBg = if (isDark) Color(0xFF16171B) else Color(0xFFFFFFFF)
        val sheetTextColor = if (isDark) Color(0xFFEDEDED) else Color(0xFF141414)
        val cardBg = if (isDark) Color(0xFF22242B) else Color(0xFFF4F5F8)
        val dividerColor = if (isDark) Color(0xFF2C2F3A) else Color(0xFFE5E7EB)

        ModalBottomSheet(
            onDismissRequest = { showManageFeedSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = sheetBg,
            contentColor = sheetTextColor,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            tonalElevation = 12.dp,
            modifier = Modifier.testTag("manage_feed_bottom_sheet")
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
                    Column {
                        Text(
                            text = "Arrange & Customize Widgets",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = sheetTextColor
                        )
                        Text(
                            text = "Reorder items, create smart stacks, or customize",
                            style = MaterialTheme.typography.bodySmall,
                            color = sheetTextColor.copy(alpha = 0.65f)
                        )
                    }

                    IconButton(
                        onClick = { showManageFeedSheet = false },
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

                // Action Bar: Create Smart Stack & Add Widget
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Combine first two eligible single items into a new Smart Stack
                            val singles = feedItems.filterIsInstance<FeedItem.Single>()
                            if (singles.size >= 2) {
                                val item1 = singles[0].item
                                val item2 = singles[1].item
                                val remaining = feedItems.filter { it.id != singles[0].id && it.id != singles[1].id }
                                val newStack = FeedItem.Stack(
                                    name = "Smart Stack",
                                    items = listOf(item1, item2)
                                )
                                persistFeed(listOf(newStack) + remaining)
                            } else {
                                // Add an empty stack with Weather and Battery
                                val newStack = FeedItem.Stack(
                                    name = "Smart Stack",
                                    items = listOf(
                                        WidgetItem(type = WidgetType.WEATHER),
                                        WidgetItem(type = WidgetType.BATTERY)
                                    )
                                )
                                persistFeed(listOf(newStack) + feedItems)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Stack", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    OutlinedButton(
                        onClick = {
                            showManageFeedSheet = false
                            targetStackForAdd = null
                            showGallerySheet = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Widget", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Reorder List
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, dividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        feedItems.forEachIndexed { idx, item ->
                            val title = when (item) {
                                is FeedItem.Single -> stringResource(item.item.type.titleResId)
                                is FeedItem.Stack -> "🥞 ${item.name} (${item.items.size} widgets)"
                            }
                            val icon = when (item) {
                                is FeedItem.Single -> item.item.type.icon
                                is FeedItem.Stack -> Icons.Default.Layers
                            }

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
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = sheetTextColor
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Edit settings if single
                                    if (item is FeedItem.Single) {
                                        IconButton(
                                            onClick = {
                                                showManageFeedSheet = false
                                                editingWidgetItem = item.item
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Move Up
                                    IconButton(
                                        onClick = {
                                            if (idx > 0) {
                                                val mutable = feedItems.toMutableList()
                                                val target = mutable.removeAt(idx)
                                                mutable.add(idx - 1, target)
                                                persistFeed(mutable)
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
                                            if (idx < feedItems.size - 1) {
                                                val mutable = feedItems.toMutableList()
                                                val target = mutable.removeAt(idx)
                                                mutable.add(idx + 1, target)
                                                persistFeed(mutable)
                                            }
                                        },
                                        enabled = idx < feedItems.size - 1,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Move Down",
                                            tint = if (idx < feedItems.size - 1) sheetTextColor else sheetTextColor.copy(alpha = 0.25f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Remove
                                    IconButton(
                                        onClick = {
                                            val mutable = feedItems.toMutableList()
                                            mutable.removeAt(idx)
                                            persistFeed(mutable)
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

                            if (idx < feedItems.size - 1) {
                                HorizontalDivider(color = dividerColor.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset to Default Feed Button
                OutlinedButton(
                    onClick = {
                        persistFeed(widgetPrefs.loadFeed())
                        showManageFeedSheet = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Layout to Default")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Done Button
                Button(
                    onClick = { showManageFeedSheet = false },
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
