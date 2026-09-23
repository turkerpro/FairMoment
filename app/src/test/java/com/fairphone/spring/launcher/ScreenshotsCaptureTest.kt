/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher

import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.AppLibraryCategorizer
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.protos.launcherProfile
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.ui.icons.mode.ModeIcon
import com.fairphone.spring.launcher.ui.screen.home.HomeScreen
import com.fairphone.spring.launcher.ui.screen.home.component.IosAppLibraryScreen
import com.fairphone.spring.launcher.ui.screen.home.component.TodayWidgetScreen
import com.fairphone.spring.launcher.ui.screen.onboarding.ChooseAtmosphereStepScreen
import com.fairphone.spring.launcher.ui.screen.onboarding.CurateAppsStepScreen
import com.fairphone.spring.launcher.ui.screen.onboarding.IntroConceptScreen
import com.fairphone.spring.launcher.ui.screen.onboarding.NameAndIconStepScreen
import com.fairphone.spring.launcher.ui.screen.onboarding.OnboardingSequenceStep
import com.fairphone.spring.launcher.ui.screen.onboarding.OnboardingStepPreviewLayout
import com.fairphone.spring.launcher.ui.screen.onboarding.SummaryReadyStepScreen
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class ScreenshotsCaptureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dummyIcon = ColorDrawable(android.graphics.Color.parseColor("#4CAF50"))
    private val blueIcon = ColorDrawable(android.graphics.Color.parseColor("#2196F3"))
    private val redIcon = ColorDrawable(android.graphics.Color.parseColor("#E91E63"))
    private val orangeIcon = ColorDrawable(android.graphics.Color.parseColor("#FF9800"))

    private val sampleApps = listOf(
        AppInfo(name = "Camera", packageName = "com.google.android.GoogleCamera", mainActivityClassName = "CameraActivity", icon = redIcon),
        AppInfo(name = "Chrome", packageName = "com.android.chrome", mainActivityClassName = "Main", icon = blueIcon),
        AppInfo(name = "Maps", packageName = "com.google.android.apps.maps", mainActivityClassName = "MapsActivity", icon = dummyIcon),
        AppInfo(name = "Messages", packageName = "com.google.android.apps.messaging", mainActivityClassName = "ConversationListActivity", icon = blueIcon),
        AppInfo(name = "Spotify", packageName = "com.spotify.music", mainActivityClassName = "MainActivity", icon = dummyIcon),
        AppInfo(name = "Notes", packageName = "com.google.android.keep", mainActivityClassName = "KeepActivity", icon = orangeIcon),
        AppInfo(name = "Photos", packageName = "com.google.android.apps.photos", mainActivityClassName = "PhotosActivity", icon = redIcon),
        AppInfo(name = "Settings", packageName = "com.android.settings", mainActivityClassName = "Settings", icon = dummyIcon)
    )

    private val selectedSampleApps = sampleApps.take(4)

    private val focusProfile = launcherProfile {
        id = "focus"
        name = "Focus"
        icon = "DeepFocus"
        bgColor1 = 0xB2C3D1D0
        bgColor2 = 0xB2FFBA63
    }

    @Before
    fun setUp() {
        File("../screenshots").mkdirs()
        File("screenshots").mkdirs()
    }

    private fun syncFile(name: String) {
        val src = File("../screenshots/$name")
        val altSrc = File("screenshots/$name")
        val rootTarget = File("/screenshots/$name")
        try {
            if (src.exists()) {
                Files.copy(src.toPath(), altSrc.toPath(), StandardCopyOption.REPLACE_EXISTING)
                Files.copy(src.toPath(), rootTarget.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } else if (altSrc.exists()) {
                Files.copy(altSrc.toPath(), src.toPath(), StandardCopyOption.REPLACE_EXISTING)
                Files.copy(altSrc.toPath(), rootTarget.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (_: Exception) {}
    }

    @Test
    fun capture_screenshot_1_intro() {
        composeTestRule.setContent {
            SpringLauncherTheme {
                OnboardingStepPreviewLayout(step = OnboardingSequenceStep.INTRO) {
                    IntroConceptScreen(onStart = {})
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "../screenshots/screenshot_1.png")
        syncFile("screenshot_1.png")
    }

    @Test
    fun capture_screenshot_2_name_and_icon() {
        composeTestRule.setContent {
            SpringLauncherTheme {
                OnboardingStepPreviewLayout(step = OnboardingSequenceStep.NAME_ICON) {
                    NameAndIconStepScreen(
                        name = "Focus",
                        icon = ModeIcon.DeepFocus,
                        onNameChange = {},
                        onCycleIcon = {},
                        onPresetSelect = { _, _ -> },
                        onContinue = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "../screenshots/screenshot_2.png")
        syncFile("screenshot_2.png")
    }

    @Test
    fun capture_screenshot_3_curate_apps() {
        composeTestRule.setContent {
            SpringLauncherTheme {
                OnboardingStepPreviewLayout(step = OnboardingSequenceStep.APPS) {
                    CurateAppsStepScreen(
                        filteredApps = sampleApps,
                        selectedApps = selectedSampleApps,
                        searchQuery = "",
                        isLoading = false,
                        errorNotice = null,
                        onSearchQueryChange = {},
                        onToggleApp = {},
                        onRemoveApp = {},
                        onContinue = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "../screenshots/screenshot_3.png")
        syncFile("screenshot_3.png")
    }

    @Test
    fun capture_screenshot_4_atmosphere() {
        composeTestRule.setContent {
            SpringLauncherTheme {
                OnboardingStepPreviewLayout(step = OnboardingSequenceStep.STYLE) {
                    ChooseAtmosphereStepScreen(
                        selectedColors = LauncherColors.Recharge,
                        onColorsSelected = {},
                        onContinue = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "../screenshots/screenshot_4.png")
        syncFile("screenshot_4.png")
    }

    @Test
    fun capture_screenshot_5_ready() {
        composeTestRule.setContent {
            SpringLauncherTheme {
                OnboardingStepPreviewLayout(step = OnboardingSequenceStep.SUMMARY) {
                    SummaryReadyStepScreen(
                        name = "Focus",
                        icon = ModeIcon.DeepFocus,
                        selectedApps = selectedSampleApps,
                        colors = LauncherColors.Recharge,
                        isSaving = false,
                        onFinish = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "../screenshots/screenshot_5.png")
        syncFile("screenshot_5.png")
    }

    @Test
    fun capture_screenshot_6_home() {
        composeTestRule.setContent {
            SpringLauncherTheme {
                HomeScreen(
                    isContentVisible = true,
                    date = "Wed, 23 Sep",
                    time = "05:42",
                    appUsageMode = UsageMode.DEFAULT,
                    activeProfile = focusProfile,
                    appList = selectedSampleApps,
                    categories = AppLibraryCategorizer.categorize(sampleApps, selectedSampleApps),
                    filteredApps = sampleApps,
                    searchQuery = "",
                    onAppClick = {},
                    onModeSwitcherButtonClick = {},
                    onTooltipClick = {},
                    onTimeClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "../screenshots/screenshot_6.png")
        syncFile("screenshot_6.png")
    }

    @Test
    fun capture_screenshot_7_app_library() {
        val categories = AppLibraryCategorizer.categorize(sampleApps, selectedSampleApps)
        composeTestRule.setContent {
            SpringLauncherTheme {
                IosAppLibraryScreen(
                    categories = categories,
                    filteredApps = sampleApps,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onAppClick = {},
                    onNavigateBackToHome = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "../screenshots/screenshot_7.png")
        syncFile("screenshot_7.png")
    }

    @Test
    fun capture_screenshot_8_today_widgets() {
        composeTestRule.setContent {
            SpringLauncherTheme {
                TodayWidgetScreen(
                    date = "Wednesday, 23 September",
                    onNavigateBackToHome = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "../screenshots/screenshot_8.png")
        syncFile("screenshot_8.png")
    }
}
