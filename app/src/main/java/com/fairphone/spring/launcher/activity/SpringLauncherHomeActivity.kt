/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.activity

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.fairphone.spring.launcher.data.prefs.AppPrefs
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.ui.navigation.HomeNavigation
import com.fairphone.spring.launcher.ui.screen.onboarding.FirstLaunchOnboardingScreen
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val ON_FINISH_DELAY = 400L
private const val SHOW_HOME_SCREEN_DELAY = 100L
private const val SHOW_ANIMATION_TIME = 1000L

class SpringLauncherHomeActivity : ComponentActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SpringLauncherHomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
            context.startActivity(intent)
        }

        private var instance: SpringLauncherHomeActivity? = null

        fun stop() {
            Log.d(Constants.LOG_TAG, "Stopping SpringLauncherHomeActivity")
            instance?.finish()
        }
    }

    private val isContentVisibleState = mutableStateOf(false)
    private val appPrefs: AppPrefs by inject()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        instance = this

        setContent {
            CompositionLocalProvider {
                SpringLauncherTheme {
                    val coroutineScope = rememberCoroutineScope()
                    var isFirstTimeUser by rememberSaveable { mutableStateOf<Boolean?>(null) }

                    LaunchedEffect(Unit) {
                        isFirstTimeUser = appPrefs.isFirstTimeUse()
                    }

                    if (isFirstTimeUser == true) {
                        FirstLaunchOnboardingScreen(
                            onOnboardingFinished = {
                                isFirstTimeUser = false
                            }
                        )
                    } else if (isFirstTimeUser == false) {
                        /**
                         * These two boolean flags control:
                         * - Triggering and synchronization of a Compose animation.
                         * - Dynamic switching of the UI background
                         *  (for entry / exit animation, a transparent background is needed).
                         */
                        var showEntryAnimation by rememberSaveable { mutableStateOf(true) }
                        var isContentVisible by rememberSaveable { isContentVisibleState }
                        LaunchedEffect(Unit) {
                            delay(SHOW_HOME_SCREEN_DELAY) // delay set to let the entry animation show properly
                            isContentVisibleState.value = true
                        }
                        LaunchedEffect(Unit) {
                            delay(SHOW_ANIMATION_TIME) // time within the entry animation can run
                            showEntryAnimation = false
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (showEntryAnimation || !isContentVisible)
                                        androidx.compose.ui.graphics.Color.Transparent
                                     else MaterialTheme.colorScheme.background
                                )
                        ) {
                            HomeNavigation(
                                showEntryAnimation = showEntryAnimation,
                                isContentVisible = isContentVisible,
                                startOnboarding = false
                            )
                        }
                    }
                }
            }
        }
    }

    override fun finish() {
        finishWithDelay()
    }

    private fun finishWithDelay() {
        isContentVisibleState.value = false
        lifecycleScope.launch {
            delay(ON_FINISH_DELAY) // delay set to let the exit animation show properly
            super.finish()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // ignore back button
        }
    }

    @SuppressLint("WrongConstant")
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(0) {
                // ignore back button
            }
        } else {
            onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }
        hideGestureBar()
    }

    override fun onPause() {
        super.onPause()
        onBackPressedCallback.remove()
        showGestureBar()
    }

    private fun hideGestureBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showGestureBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }
}
