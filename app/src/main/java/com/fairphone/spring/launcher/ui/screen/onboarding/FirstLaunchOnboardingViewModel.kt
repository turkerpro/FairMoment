/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.protos.launcherProfileApp
import com.fairphone.spring.launcher.data.prefs.AppPrefs
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.data.repository.AppInfoRepository
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import com.fairphone.spring.launcher.ui.icons.mode.ModeIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingSequenceStep {
    INTRO,
    NAME_ICON,
    APPS,
    STYLE,
    SUMMARY
}

data class FirstLaunchOnboardingUiState(
    val currentStep: OnboardingSequenceStep = OnboardingSequenceStep.INTRO,
    val momentName: String = "Essentials",
    val momentIcon: ModeIcon = ModeIcon.Spring,
    val installedApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val selectedApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedColors: LauncherColors = LauncherColors.Default,
    val isLoadingApps: Boolean = false,
    val isSaving: Boolean = false,
    val errorNotice: String? = null
)

class FirstLaunchOnboardingViewModel(
    private val appInfoRepository: AppInfoRepository,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase,
    private val appPrefs: AppPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(FirstLaunchOnboardingUiState())
    val uiState: StateFlow<FirstLaunchOnboardingUiState> = _uiState.asStateFlow()

    private var hasLoadedInitialData = false

    fun loadInitialData(context: Context) {
        if (hasLoadedInitialData) return
        hasLoadedInitialData = true

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }

            val apps = appInfoRepository.getAllInstalledApps(context)
            var initialName = "Essentials"
            var initialIcon = ModeIcon.Spring
            var initialColors = LauncherColors.Default
            var initialSelectedApps = emptyList<AppInfo>()

            try {
                val profile = getActiveProfileUseCase.execute(Unit).first()
                if (profile != null) {
                    if (profile.name.isNotBlank()) initialName = profile.name
                    initialIcon = ModeIcon.fromString(profile.icon)
                    initialColors = LauncherColors.All.firstOrNull { it.rightColor == profile.bgColor2 } ?: LauncherColors.Default

                    val profileApps = appInfoRepository.getAppInfosByProfileApps(context, profile.launcherProfileAppsList)
                    if (profileApps.isNotEmpty()) {
                        initialSelectedApps = profileApps.take(4)
                    }
                }
            } catch (_: Exception) {}

            // If no apps pre-selected, smartly pick the first 2-3 common launchable apps
            if (initialSelectedApps.isEmpty() && apps.isNotEmpty()) {
                val defaultPackages = listOf("dialer", "phone", "messaging", "mms", "chrome", "browser", "camera")
                val foundDefaults = apps.filter { app ->
                    val pkg = app.packageName.lowercase()
                    defaultPackages.any { pkg.contains(it) }
                }.take(3)

                initialSelectedApps = if (foundDefaults.isNotEmpty()) foundDefaults else apps.take(3)
            }

            _uiState.update { current ->
                current.copy(
                    momentName = initialName,
                    momentIcon = initialIcon,
                    selectedColors = initialColors,
                    installedApps = apps,
                    filteredApps = apps,
                    selectedApps = initialSelectedApps,
                    isLoadingApps = false
                )
            }
        }
    }

    fun goToStep(step: OnboardingSequenceStep) {
        _uiState.update { it.copy(currentStep = step, errorNotice = null) }
    }

    fun nextStep() {
        val current = _uiState.value.currentStep
        val next = when (current) {
            OnboardingSequenceStep.INTRO -> OnboardingSequenceStep.NAME_ICON
            OnboardingSequenceStep.NAME_ICON -> OnboardingSequenceStep.APPS
            OnboardingSequenceStep.APPS -> {
                if (_uiState.value.selectedApps.isEmpty()) {
                    _uiState.update { it.copy(errorNotice = "at_least_one") }
                    return
                }
                OnboardingSequenceStep.STYLE
            }
            OnboardingSequenceStep.STYLE -> OnboardingSequenceStep.SUMMARY
            OnboardingSequenceStep.SUMMARY -> OnboardingSequenceStep.SUMMARY
        }
        _uiState.update { it.copy(currentStep = next, errorNotice = null) }
    }

    fun previousStep() {
        val current = _uiState.value.currentStep
        val prev = when (current) {
            OnboardingSequenceStep.INTRO -> OnboardingSequenceStep.INTRO
            OnboardingSequenceStep.NAME_ICON -> OnboardingSequenceStep.INTRO
            OnboardingSequenceStep.APPS -> OnboardingSequenceStep.NAME_ICON
            OnboardingSequenceStep.STYLE -> OnboardingSequenceStep.APPS
            OnboardingSequenceStep.SUMMARY -> OnboardingSequenceStep.STYLE
        }
        _uiState.update { it.copy(currentStep = prev, errorNotice = null) }
    }

    fun setMomentName(name: String) {
        if (name.length <= 15) {
            _uiState.update { it.copy(momentName = name) }
        }
    }

    fun cycleIcon() {
        _uiState.update { current ->
            current.copy(momentIcon = ModeIcon.nextIcon(current.momentIcon.name))
        }
    }

    fun setMomentIcon(icon: ModeIcon) {
        _uiState.update { it.copy(momentIcon = icon) }
    }

    fun applyPreset(name: String, icon: ModeIcon) {
        _uiState.update { it.copy(momentName = name, momentIcon = icon) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = if (query.isBlank()) {
                current.installedApps
            } else {
                current.installedApps.filter { it.name.contains(query, ignoreCase = true) }
            }
            current.copy(searchQuery = query, filteredApps = filtered)
        }
    }

    fun toggleAppSelection(app: AppInfo) {
        _uiState.update { current ->
            val exists = current.selectedApps.any { it.packageName == app.packageName }
            if (exists) {
                current.copy(
                    selectedApps = current.selectedApps.filterNot { it.packageName == app.packageName },
                    errorNotice = null
                )
            } else if (current.selectedApps.size < 4) {
                current.copy(
                    selectedApps = current.selectedApps + app,
                    errorNotice = null
                )
            } else {
                current.copy(errorNotice = "max_apps_reached")
            }
        }
    }

    fun removeSelectedApp(app: AppInfo) {
        _uiState.update { current ->
            current.copy(
                selectedApps = current.selectedApps.filterNot { it.packageName == app.packageName },
                errorNotice = null
            )
        }
    }

    fun setSelectedColors(colors: LauncherColors) {
        _uiState.update { it.copy(selectedColors = colors) }
    }

    fun completeFirstLaunch(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val profile = getActiveProfileUseCase.execute(Unit).first()
                val profileApps = _uiState.value.selectedApps.map {
                    launcherProfileApp {
                        packageName = it.packageName
                        isWorkApp = it.isWorkApp
                    }
                }
                val updatedProfile = profile.toBuilder()
                    .setName(_uiState.value.momentName.trim().ifEmpty { "Essentials" })
                    .setIcon(_uiState.value.momentIcon.name)
                    .setBgColor1(_uiState.value.selectedColors.leftColor)
                    .setBgColor2(_uiState.value.selectedColors.rightColor)
                    .clearLauncherProfileApps()
                    .addAllLauncherProfileApps(profileApps)
                    .build()

                updateLauncherProfileUseCase.execute(updatedProfile)
                appPrefs.setFirstTimeUse(false)
                appPrefs.setUsageMode(UsageMode.DEFAULT)
                onComplete()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorNotice = e.message) }
            }
        }
    }
}
