/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.AppLibraryCategorizer
import com.fairphone.spring.launcher.data.model.AppLibraryCategory
import com.fairphone.spring.launcher.data.model.LAUNCHER_MAX_APP_COUNT
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.prefs.AppPrefs
import com.fairphone.spring.launcher.data.prefs.FontPreferences
import com.fairphone.spring.launcher.data.prefs.LauncherWallpaperType
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.data.prefs.WallpaperPreferences
import com.fairphone.spring.launcher.data.repository.AppInfoRepository
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.InitializeSpringLauncherUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetApplicationUsageModeUseCase
import com.fairphone.spring.launcher.ui.theme.LauncherFont
import com.fairphone.spring.launcher.util.launchApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class HomeScreenViewModel(
    private val context: Context,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val setApplicationUsageModeUseCase: SetApplicationUsageModeUseCase,
    private val appPrefs: AppPrefs,
    private val appInfoRepository: AppInfoRepository,
    private val initializeSpringLauncherUseCase: InitializeSpringLauncherUseCase,
) : ViewModel() {

    private val _dateTime: MutableStateFlow<LocalDateTime> = MutableStateFlow(LocalDateTime.now())
    val dateTime: StateFlow<LocalDateTime> = _dateTime.asStateFlow()

    private val _allInstalledApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allInstalledApps: StateFlow<List<AppInfo>> = _allInstalledApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val fontPreferences = FontPreferences(context)
    val clockFont: StateFlow<LauncherFont> = fontPreferences.clockFont
    val menuFont: StateFlow<LauncherFont> = fontPreferences.menuFont

    private val wallpaperPreferences = WallpaperPreferences(context)
    val wallpaperType: StateFlow<LauncherWallpaperType> = wallpaperPreferences.wallpaperType
    val blurRadius: StateFlow<Float> = wallpaperPreferences.blurRadius
    val dimAlpha: StateFlow<Float> = wallpaperPreferences.dimAlpha
    val customImageUri: StateFlow<String?> = wallpaperPreferences.customImageUri

    // Cache categories to avoid heavy re-computation during search and wallpaper adjustments
    private var lastCategorizedInstalledApps: List<AppInfo>? = null
    private var lastCategorizedVisibleApps: List<AppInfo>? = null
    private var cachedCategories: List<AppLibraryCategory> = emptyList()

    val screenState: StateFlow<HomeScreenState?> =
        combine(
            getActiveProfileUseCase.execute(Unit),
            _allInstalledApps,
            _searchQuery,
            fontPreferences.clockFont,
            fontPreferences.menuFont,
            wallpaperPreferences.wallpaperUpdates
        ) { args: Array<Any?> ->
            @Suppress("UNCHECKED_CAST")
            val profile = args[0] as LauncherProfile
            @Suppress("UNCHECKED_CAST")
            val installedApps = args[1] as List<AppInfo>
            val query = args[2] as String
            val clockF = args[3] as LauncherFont
            val menuF = args[4] as LauncherFont

            // Per-Focus/Profile wallpaper settings - distinct for each focus mode
            wallpaperPreferences.setActiveProfileId(profile.id)
            val wallType = wallpaperPreferences.getWallpaperType(profile.id, profile.name, profile.icon)
            val blurR = wallpaperPreferences.getBlurRadius(profile.id)
            val dimA = wallpaperPreferences.getDimAlpha(profile.id)
            val customUri = wallpaperPreferences.getCustomImageUri(profile.id)

            // Resolve visible apps directly from in-memory installedApps map when available
            val visibleApps = if (installedApps.isNotEmpty()) {
                val map = installedApps.associateBy { it.packageName }
                val resolved = profile.launcherProfileAppsList.mapNotNull { profileApp ->
                    map[profileApp.packageName]?.let { app ->
                        if (app.isWorkApp == profileApp.isWorkApp) app else null
                    }
                }
                if (resolved.isNotEmpty() || profile.launcherProfileAppsList.isEmpty()) {
                    resolved.take(LAUNCHER_MAX_APP_COUNT)
                } else {
                    appInfoRepository
                        .getAppInfosByProfileApps(context, profile.launcherProfileAppsList)
                        .take(LAUNCHER_MAX_APP_COUNT)
                }
            } else {
                appInfoRepository
                    .getAppInfosByProfileApps(context, profile.launcherProfileAppsList)
                    .take(LAUNCHER_MAX_APP_COUNT)
            }

            // Only recompute category groupings when installed or visible apps change
            val categories = if (
                lastCategorizedInstalledApps === installedApps &&
                lastCategorizedVisibleApps === visibleApps &&
                cachedCategories.isNotEmpty()
            ) {
                cachedCategories
            } else {
                val computed = AppLibraryCategorizer.categorize(installedApps, visibleApps)
                lastCategorizedInstalledApps = installedApps
                lastCategorizedVisibleApps = visibleApps
                cachedCategories = computed
                computed
            }

            val filteredApps = if (query.isBlank()) {
                installedApps
            } else {
                val trimmedQuery = query.trim()
                installedApps.filter { app ->
                    app.name.contains(trimmedQuery, ignoreCase = true) ||
                    app.packageName.contains(trimmedQuery, ignoreCase = true)
                }
            }

            HomeScreenState(
                activeProfile = profile,
                visibleApps = visibleApps,
                allInstalledApps = installedApps,
                categories = categories,
                filteredApps = filteredApps,
                searchQuery = query,
                appUsageMode = appPrefs.usageMode(),
                clockFont = clockF,
                menuFont = menuF,
                wallpaperType = wallType,
                blurRadius = blurR,
                dimAlpha = dimA,
                customImageUri = customUri
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    fun setClockFont(font: LauncherFont) {
        fontPreferences.setClockFont(font)
    }

    fun setMenuFont(font: LauncherFont) {
        fontPreferences.setMenuFont(font)
    }

    fun setWallpaperType(type: LauncherWallpaperType) {
        val currentProfileId = screenState.value?.activeProfile?.id ?: ""
        wallpaperPreferences.setWallpaperType(currentProfileId, type)
    }

    fun setBlurRadius(radius: Float) {
        val currentProfileId = screenState.value?.activeProfile?.id ?: ""
        wallpaperPreferences.setBlurRadius(currentProfileId, radius)
    }

    fun setDimAlpha(dim: Float) {
        val currentProfileId = screenState.value?.activeProfile?.id ?: ""
        wallpaperPreferences.setDimAlpha(currentProfileId, dim)
    }

    fun setCustomImageUri(uri: String?) {
        val currentProfileId = screenState.value?.activeProfile?.id ?: ""
        wallpaperPreferences.setCustomImageUri(currentProfileId, uri)
    }

    init {
        refreshTime()
        initializeSpringLauncher()
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appInfoRepository.getAllInstalledApps(context)
            _allInstalledApps.value = apps
        }
    }

    private fun refreshTime() = viewModelScope.launch(Dispatchers.Default) {
        while (isActive) {
            val now = LocalDateTime.now()
            if (now.minute != _dateTime.value.minute || now.dayOfYear != _dateTime.value.dayOfYear) {
                _dateTime.value = now
            }
            // Sleep until start of next minute to avoid 1-second busy wakeups
            val delayMillis = ((60 - now.second) * 1000L - (now.nano / 1_000_000L)).coerceIn(1000L, 60000L)
            delay(delayMillis)
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onAppClick(context: Context, appInfo: AppInfo) {
        viewModelScope.launch {
            context.launchApp(appInfo)
        }
    }

    fun initializeSpringLauncher() {
        viewModelScope.launch {
            initializeSpringLauncherUseCase.execute(Unit)
        }
    }

    fun finishOnBoarding() {
        viewModelScope.launch {
            if (screenState.value?.appUsageMode == UsageMode.ON_BOARDING_COMPLETE) {
                setApplicationUsageModeUseCase.execute(UsageMode.DEFAULT)
            }
        }
    }
}

data class HomeScreenState(
    val activeProfile: LauncherProfile,
    val visibleApps: List<AppInfo> = emptyList(),
    val allInstalledApps: List<AppInfo> = emptyList(),
    val categories: List<AppLibraryCategory> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val appUsageMode: UsageMode = UsageMode.ON_BOARDING,
    val clockFont: LauncherFont = LauncherFont.BRICOLAGE,
    val menuFont: LauncherFont = LauncherFont.DM_SANS,
    val wallpaperType: LauncherWallpaperType = LauncherWallpaperType.FAIRPHONE_DYNAMIC,
    val blurRadius: Float = 0f,
    val dimAlpha: Float = 0f,
    val customImageUri: String? = null,
)
