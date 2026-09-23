/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.prefs

import android.content.Context
import com.fairphone.spring.launcher.ui.theme.LauncherFont
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FontPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("launcher_font_prefs", Context.MODE_PRIVATE)

    private val _clockFont = MutableStateFlow(
        LauncherFont.fromId(prefs.getString(KEY_CLOCK_FONT, LauncherFont.BRICOLAGE.id), LauncherFont.BRICOLAGE)
    )
    val clockFont: StateFlow<LauncherFont> = _clockFont.asStateFlow()

    private val _menuFont = MutableStateFlow(
        LauncherFont.fromId(prefs.getString(KEY_MENU_FONT, LauncherFont.DM_SANS.id), LauncherFont.DM_SANS)
    )
    val menuFont: StateFlow<LauncherFont> = _menuFont.asStateFlow()

    fun setClockFont(font: LauncherFont) {
        prefs.edit().putString(KEY_CLOCK_FONT, font.id).apply()
        _clockFont.value = font
    }

    fun setMenuFont(font: LauncherFont) {
        prefs.edit().putString(KEY_MENU_FONT, font.id).apply()
        _menuFont.value = font
    }

    companion object {
        private const val KEY_CLOCK_FONT = "key_clock_font"
        private const val KEY_MENU_FONT = "key_menu_font"
    }
}
