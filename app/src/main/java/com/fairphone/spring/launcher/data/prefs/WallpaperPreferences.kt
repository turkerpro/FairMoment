/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LauncherWallpaperType(
    val id: String,
    val title: String,
    val isLight: Boolean,
    val description: String
) {
    FAIRPHONE_DYNAMIC("fp_dynamic", "Fairphone Dynamic", false, "Animated ambient gradient blobs"),
    OLED_DARK("oled_dark", "Pitch Black OLED", false, "Pure battery-saving deep black"),
    DEEP_NEBULA("deep_nebula", "Deep Nebula", false, "Cosmic violet and midnight navy"),
    NORDIC_TWILIGHT("nordic_twilight", "Nordic Twilight", false, "Cold misty mountain aurora"),
    SUNSET_DUNES("sunset_dunes", "Sunset Dunes", false, "Warm terracotta and golden amber"),
    EMERALD_FOREST("emerald_forest", "Emerald Forest", false, "Calm deep pine and woodland greens"),
    MINIMAL_WHITE("minimal_white", "Minimal White", true, "Crisp high-key minimalist theme"),
    CUSTOM_IMAGE("custom_image", "Gallery Photo", false, "User chosen custom image");

    companion object {
        fun fromId(id: String?, default: LauncherWallpaperType = FAIRPHONE_DYNAMIC): LauncherWallpaperType {
            return entries.find { it.id == id } ?: default
        }
    }
}

class WallpaperPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("launcher_wallpaper_prefs", Context.MODE_PRIVATE)

    private val _wallpaperType = MutableStateFlow(
        LauncherWallpaperType.fromId(prefs.getString(KEY_WALLPAPER_TYPE, LauncherWallpaperType.FAIRPHONE_DYNAMIC.id))
    )
    val wallpaperType: StateFlow<LauncherWallpaperType> = _wallpaperType.asStateFlow()

    private val _blurRadius = MutableStateFlow(
        prefs.getFloat(KEY_BLUR_RADIUS, 0f)
    )
    val blurRadius: StateFlow<Float> = _blurRadius.asStateFlow()

    private val _dimAlpha = MutableStateFlow(
        prefs.getFloat(KEY_DIM_ALPHA, 0f)
    )
    val dimAlpha: StateFlow<Float> = _dimAlpha.asStateFlow()

    private val _customImageUri = MutableStateFlow(
        prefs.getString(KEY_CUSTOM_URI, null)
    )
    val customImageUri: StateFlow<String?> = _customImageUri.asStateFlow()

    fun setWallpaperType(type: LauncherWallpaperType) {
        prefs.edit().putString(KEY_WALLPAPER_TYPE, type.id).apply()
        _wallpaperType.value = type
    }

    fun setBlurRadius(radius: Float) {
        val clamped = radius.coerceIn(0f, 30f)
        prefs.edit().putFloat(KEY_BLUR_RADIUS, clamped).apply()
        _blurRadius.value = clamped
    }

    fun setDimAlpha(dim: Float) {
        val clamped = dim.coerceIn(0f, 0.75f)
        prefs.edit().putFloat(KEY_DIM_ALPHA, clamped).apply()
        _dimAlpha.value = clamped
    }

    fun setCustomImageUri(uri: String?) {
        prefs.edit().putString(KEY_CUSTOM_URI, uri).apply()
        _customImageUri.value = uri
        if (uri != null) {
            setWallpaperType(LauncherWallpaperType.CUSTOM_IMAGE)
        }
    }

    companion object {
        private const val KEY_WALLPAPER_TYPE = "key_wallpaper_type"
        private const val KEY_BLUR_RADIUS = "key_blur_radius"
        private const val KEY_DIM_ALPHA = "key_dim_alpha"
        private const val KEY_CUSTOM_URI = "key_custom_uri"
    }
}
