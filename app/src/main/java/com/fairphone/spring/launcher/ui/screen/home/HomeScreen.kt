/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.AppLibraryCategory
import com.fairphone.spring.launcher.data.model.Mock_Profile
import com.fairphone.spring.launcher.data.model.colors
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.prefs.LauncherWallpaperType
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.WorkAppBadge
import com.fairphone.spring.launcher.ui.screen.home.component.CurrentModeButton
import com.fairphone.spring.launcher.ui.screen.home.component.IosAppLibraryScreen
import com.fairphone.spring.launcher.ui.screen.home.component.LauncherCustomizerSheet
import com.fairphone.spring.launcher.ui.screen.home.component.LauncherPagerIndicator
import com.fairphone.spring.launcher.ui.screen.home.component.LauncherWallpaperBackground
import com.fairphone.spring.launcher.ui.screen.home.component.TodayWidgetScreen
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.LauncherFont
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

const val CLOCK_TIME_FORMAT = "HH:mm"
const val CLOCK_DATE_FORMAT = "EEE, dd LLL"

private val DATE_FORMATTER = DateTimeFormatter.ofPattern(CLOCK_DATE_FORMAT)
private val TIME_FORMATTER = DateTimeFormatter.ofPattern(CLOCK_TIME_FORMAT)

private const val CONTENT_FADE_IN_DURATION = 420

@Composable
fun HomeScreen(
    isContentVisible: Boolean,
    onModeSwitcherButtonClick: () -> Unit,
    onTimeClick: () -> Unit,
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val dateTime by viewModel.dateTime.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val (date, time) = remember(dateTime) {
        dateTime.format(DATE_FORMATTER) to dateTime.format(TIME_FORMATTER)
    }

    screenState ?: return

    HomeScreen(
        isContentVisible = isContentVisible,
        date = date,
        time = time,
        appUsageMode = screenState!!.appUsageMode,
        activeProfile = screenState!!.activeProfile,
        appList = screenState!!.visibleApps,
        categories = screenState!!.categories,
        filteredApps = screenState!!.filteredApps,
        searchQuery = screenState!!.searchQuery,
        clockFont = screenState!!.clockFont,
        menuFont = screenState!!.menuFont,
        wallpaperType = screenState!!.wallpaperType,
        blurRadius = screenState!!.blurRadius,
        dimAlpha = screenState!!.dimAlpha,
        customImageUri = screenState!!.customImageUri,
        customColorPrimary = screenState!!.customColorPrimary,
        customColorSecondary = screenState!!.customColorSecondary,
        customColorStyle = screenState!!.customColorStyle,
        onClockFontChange = viewModel::setClockFont,
        onMenuFontChange = viewModel::setMenuFont,
        onWallpaperTypeChange = viewModel::setWallpaperType,
        onBlurRadiusChange = viewModel::setBlurRadius,
        onDimAlphaChange = viewModel::setDimAlpha,
        onCustomImageSelected = viewModel::setCustomImageUri,
        onCustomColorPrimaryChange = viewModel::setCustomColorPrimary,
        onCustomColorSecondaryChange = viewModel::setCustomColorSecondary,
        onCustomColorStyleChange = viewModel::setCustomColorStyle,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAppClick = { appInfo ->
            viewModel.finishOnBoarding()
            viewModel.onAppClick(context, appInfo)
        },
        onModeSwitcherButtonClick = {
            viewModel.finishOnBoarding()
            onModeSwitcherButtonClick()
        },
        onTooltipClick = {
            viewModel.finishOnBoarding()
        },
        onTimeClick = onTimeClick,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isContentVisible: Boolean,
    date: String,
    time: String,
    appUsageMode: UsageMode,
    activeProfile: LauncherProfile,
    appList: List<AppInfo>,
    categories: List<AppLibraryCategory> = emptyList(),
    filteredApps: List<AppInfo> = emptyList(),
    searchQuery: String = "",
    clockFont: LauncherFont = LauncherFont.BRICOLAGE,
    menuFont: LauncherFont = LauncherFont.DM_SANS,
    wallpaperType: LauncherWallpaperType = LauncherWallpaperType.FAIRPHONE_DYNAMIC,
    blurRadius: Float = 0f,
    dimAlpha: Float = 0f,
    customImageUri: String? = null,
    customColorPrimary: Long = 0xFF4F46E5L,
    customColorSecondary: Long = 0xFFEC4899L,
    customColorStyle: String = "linear",
    onClockFontChange: (LauncherFont) -> Unit = {},
    onMenuFontChange: (LauncherFont) -> Unit = {},
    onWallpaperTypeChange: (LauncherWallpaperType) -> Unit = {},
    onBlurRadiusChange: (Float) -> Unit = {},
    onDimAlphaChange: (Float) -> Unit = {},
    onCustomImageSelected: (String?) -> Unit = {},
    onCustomColorPrimaryChange: (Long) -> Unit = {},
    onCustomColorSecondaryChange: (Long) -> Unit = {},
    onCustomColorStyleChange: (String) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onAppClick: (AppInfo) -> Unit,
    onModeSwitcherButtonClick: () -> Unit,
    onTooltipClick: () -> Unit,
    onTimeClick: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    // 3 Pages: 0 = Today Widgets (Left), 1 = Home Screen (Center), 2 = iOS App Library (Right)
    val pagerState = rememberPagerState(initialPage = 1) { 3 }
    var showLauncherCustomizer by remember { mutableStateOf(false) }

    // Contrast calculation: when wallpaper is bright white and dim is subtle, invert text and dots
    val isLightWallpaper = (wallpaperType == LauncherWallpaperType.MINIMAL_WHITE) && (dimAlpha < 0.35f)
    val clockTextColor = if (isLightWallpaper) Color(0xFF141414) else MaterialTheme.colorScheme.onBackground
    val dateTextColor = if (isLightWallpaper) Color(0xFF333333) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
    val appItemTextColor = if (isLightWallpaper) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.onBackground

    // If on Widgets (page 0) or App Library (page 2), pressing Back returns to Home Screen (page 1)
    BackHandler(enabled = pagerState.currentPage != 1) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(1)
        }
    }

    val fadeInAnimation = remember {
        fadeIn(animationSpec = tween(CONTENT_FADE_IN_DURATION))
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = isContentVisible,
        enter = fadeInAnimation,
        exit = ExitTransition.None
    ) {
        LauncherWallpaperBackground(
            wallpaperType = wallpaperType,
            blurRadius = blurRadius,
            dimAlpha = dimAlpha,
            customImageUri = customImageUri,
            customColorPrimary = customColorPrimary,
            customColorSecondary = customColorSecondary,
            customColorStyle = customColorStyle,
            profileColors = activeProfile.colors(),
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    // Page 0: Left Side - Today Widgets Screen
                    TodayWidgetScreen(
                        date = date,
                        onNavigateBackToHome = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                }
                1 -> {
                    // Page 1: Center - Home Screen with Long-Press Customization & Contrast Awareness
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(bottom = 16.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        showLauncherCustomizer = true
                                    }
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 48.dp) // space for bottom pagination dots
                        ) {
                            // Header Area: Clock, Date, Mode Button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 36.dp)
                            ) {
                                // Time with Customizable Font & Long-Press Support
                                Text(
                                    text = time,
                                    style = TextStyle(
                                        fontSize = 46.sp,
                                        lineHeight = 46.sp,
                                        fontFamily = clockFont.getFontFamily(),
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = clockTextColor,
                                    modifier = Modifier
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = { onTimeClick() },
                                                onLongPress = { showLauncherCustomizer = true }
                                            )
                                        }
                                        .testTag("home_clock_text")
                                )

                                // Date with Customizable Font & Long-Press / Tap Support
                                Text(
                                    text = date,
                                    style = TextStyle(
                                        fontSize = 22.sp,
                                        lineHeight = 24.sp,
                                        fontFamily = clockFont.getFontFamily(),
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = dateTextColor,
                                    modifier = Modifier
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = { showLauncherCustomizer = true },
                                                onLongPress = { showLauncherCustomizer = true }
                                            )
                                        }
                                        .testTag("home_date_text")
                                )

                                CurrentModeButton(
                                    modifier = Modifier.padding(top = 10.dp),
                                    activeProfile = activeProfile,
                                    appUsageMode = appUsageMode,
                                    onModeSwitcherButtonClick = onModeSwitcherButtonClick,
                                    onTooltipClick = onTooltipClick
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // App List with Customizable Menu Font and Contrast Protection
                            AppList(
                                appList = appList,
                                onAppClick = onAppClick,
                                menuFontFamily = menuFont.getFontFamily(),
                                textColor = appItemTextColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }

                        // Bottom Pagination Dots: "o O o" style, transparent, with adaptive contrast
                        LauncherPagerIndicator(
                            pageCount = 3,
                            currentPage = pagerState.currentPage,
                            onPageSelected = { targetPage ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            },
                            isLightWallpaper = isLightWallpaper,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .testTag("home_pagination_dots")
                        )
                    }
                }
                2 -> {
                    // Page 2: Right Side - iOS App Library Screen
                    IosAppLibraryScreen(
                        categories = categories,
                        filteredApps = filteredApps,
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onAppClick = onAppClick,
                        onNavigateBackToHome = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                }
            }
        }
    }
}

    // Launcher Customizer Sheet (Fonts, Wallpaper Presets, Blur Slider & Dim Slider)
    if (showLauncherCustomizer) {
        LauncherCustomizerSheet(
            selectedClockFont = clockFont,
            selectedMenuFont = menuFont,
            selectedWallpaperType = wallpaperType,
            blurRadius = blurRadius,
            dimAlpha = dimAlpha,
            customImageUri = customImageUri,
            customColorPrimary = customColorPrimary,
            customColorSecondary = customColorSecondary,
            customColorStyle = customColorStyle,
            onClockFontChange = onClockFontChange,
            onMenuFontChange = onMenuFontChange,
            onWallpaperTypeChange = onWallpaperTypeChange,
            onBlurRadiusChange = onBlurRadiusChange,
            onDimAlphaChange = onDimAlphaChange,
            onCustomImageSelected = onCustomImageSelected,
            onCustomColorPrimaryChange = onCustomColorPrimaryChange,
            onCustomColorSecondaryChange = onCustomColorSecondaryChange,
            onCustomColorStyleChange = onCustomColorStyleChange,
            onDismiss = { showLauncherCustomizer = false },
            activeMomentName = activeProfile.name,
            currentTime = time,
            currentDate = date
        )
    }
}

@Composable
fun AppList(
    appList: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    menuFontFamily: FontFamily = FairphoneTypography.AppButtonDefault.fontFamily ?: FontFamily.Default,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        appList.forEach { app ->
            LauncherAppButton(
                appName = app.name,
                onAppClick = { onAppClick(app) },
                isWorkApp = app.isWorkApp,
                fontFamily = menuFontFamily,
                textColor = textColor
            )
        }
    }
}

@Composable
fun LauncherAppButton(
    appName: String,
    onAppClick: () -> Unit,
    isWorkApp: Boolean = false,
    fontFamily: FontFamily = FairphoneTypography.AppButtonDefault.fontFamily ?: FontFamily.Default,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onAppClick
            )
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = appName,
            style = FairphoneTypography.AppButtonDefault.copy(
                fontFamily = fontFamily
            ),
            color = textColor,
        )

        if (isWorkApp) {
            WorkAppBadge(modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
@FP6Preview
@FP6PreviewDark
fun HomeScreen_Preview() {
    SpringLauncherTheme {
        HomeScreen(
            isContentVisible = true,
            date = "Wednesday, 23 Sep",
            time = "10:30",
            appUsageMode = UsageMode.DEFAULT,
            activeProfile = Mock_Profile,
            appList = emptyList(),
            onAppClick = {},
            onModeSwitcherButtonClick = {},
            onTooltipClick = {},
            onTimeClick = {},
        )
    }
}
