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
    CUSTOM_SPECTRUM("custom_spectrum", "Renk Skalası", false, "Kişisel degrade & renk skalası"),
    CUSTOM_IMAGE("custom_image", "Gallery Photo", false, "User chosen custom image");

    companion object {
        fun fromId(id: String?, default: LauncherWallpaperType = FAIRPHONE_DYNAMIC): LauncherWallpaperType {
            return entries.find { it.id == id } ?: default
        }

        fun defaultWallpaperForProfile(
            profileId: String,
            profileName: String = "",
            profileIcon: String = ""
        ): LauncherWallpaperType {
            val key = "$profileId $profileName $profileIcon".lowercase()
            return when {
                key.contains("work") || key.contains("ofis") || key.contains("study") || key.contains("çalışma") ->
                    NORDIC_TWILIGHT
                key.contains("relax") || key.contains("evening") || key.contains("sunset") || key.contains("dinlenme") || key.contains("akşam") ->
                    SUNSET_DUNES
                key.contains("focus") || key.contains("deep") || key.contains("zen") || key.contains("odak") ->
                    DEEP_NEBULA
                key.contains("nature") || key.contains("health") || key.contains("fitness") || key.contains("doğa") || key.contains("spor") ->
                    EMERALD_FOREST
                key.contains("sleep") || key.contains("night") || key.contains("uyku") || key.contains("gece") ->
                    OLED_DARK
                key.contains("minimal") || key.contains("light") || key.contains("read") || key.contains("kitap") ->
                    MINIMAL_WHITE
                key.contains("default") || key.contains("varsayılan") || key.contains("spring") || key.contains("essentials") ->
                    FAIRPHONE_DYNAMIC
                else -> {
                    val distinctOptions = listOf(
                        DEEP_NEBULA,
                        SUNSET_DUNES,
                        NORDIC_TWILIGHT,
                        EMERALD_FOREST,
                        OLED_DARK,
                        FAIRPHONE_DYNAMIC,
                        MINIMAL_WHITE
                    )
                    val hash = Math.abs(profileId.hashCode())
                    distinctOptions[hash % distinctOptions.size]
                }
            }
        }
    }
}

class WallpaperPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("launcher_wallpaper_prefs", Context.MODE_PRIVATE)

    // Observable counter to notify reactive state flows whenever a per-profile wallpaper changes
    private val _wallpaperUpdates = MutableStateFlow(0)
    val wallpaperUpdates: StateFlow<Int> = _wallpaperUpdates.asStateFlow()

    // Active profile ID tracked by preferences
    private var activeProfileId: String = ""

    // Backward-compatibility flows
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

    fun setActiveProfileId(profileId: String) {
        activeProfileId = profileId
    }

    private fun keyFor(baseKey: String, profileId: String): String {
        return if (profileId.isBlank()) baseKey else "${baseKey}_$profileId"
    }

    // Per-profile getters
    fun getWallpaperType(
        profileId: String,
        profileName: String = "",
        profileIcon: String = ""
    ): LauncherWallpaperType {
        val specificKey = keyFor(KEY_WALLPAPER_TYPE, profileId)
        val saved = prefs.getString(specificKey, null)
        return if (saved != null) {
            LauncherWallpaperType.fromId(saved)
        } else {
            // Assign distinct wallpaper per focus profile
            LauncherWallpaperType.defaultWallpaperForProfile(profileId, profileName, profileIcon)
        }
    }

    fun setWallpaperType(profileId: String, type: LauncherWallpaperType) {
        val specificKey = keyFor(KEY_WALLPAPER_TYPE, profileId)
        prefs.edit().putString(specificKey, type.id).apply()
        _wallpaperType.value = type
        _wallpaperUpdates.value++
    }

    fun getBlurRadius(profileId: String): Float {
        val specificKey = keyFor(KEY_BLUR_RADIUS, profileId)
        return prefs.getFloat(specificKey, 0f)
    }

    fun setBlurRadius(profileId: String, radius: Float) {
        val clamped = radius.coerceIn(0f, 30f)
        val specificKey = keyFor(KEY_BLUR_RADIUS, profileId)
        prefs.edit().putFloat(specificKey, clamped).apply()
        _blurRadius.value = clamped
        _wallpaperUpdates.value++
    }

    fun getDimAlpha(profileId: String): Float {
        val specificKey = keyFor(KEY_DIM_ALPHA, profileId)
        return prefs.getFloat(specificKey, 0f)
    }

    fun setDimAlpha(profileId: String, dim: Float) {
        val clamped = dim.coerceIn(0f, 0.75f)
        val specificKey = keyFor(KEY_DIM_ALPHA, profileId)
        prefs.edit().putFloat(specificKey, clamped).apply()
        _dimAlpha.value = clamped
        _wallpaperUpdates.value++
    }

    fun getCustomImageUri(profileId: String): String? {
        val specificKey = keyFor(KEY_CUSTOM_URI, profileId)
        return prefs.getString(specificKey, null)
    }

    fun setCustomImageUri(profileId: String, uri: String?) {
        val specificKey = keyFor(KEY_CUSTOM_URI, profileId)
        prefs.edit().putString(specificKey, uri).apply()
        _customImageUri.value = uri
        if (uri != null) {
            setWallpaperType(profileId, LauncherWallpaperType.CUSTOM_IMAGE)
        } else {
            _wallpaperUpdates.value++
        }
    }

    // Global / fallback setters that use the current active profile if set
    fun setWallpaperType(type: LauncherWallpaperType) {
        setWallpaperType(activeProfileId, type)
    }

    fun setBlurRadius(radius: Float) {
        setBlurRadius(activeProfileId, radius)
    }

    fun setDimAlpha(dim: Float) {
        setDimAlpha(activeProfileId, dim)
    }

    fun setCustomImageUri(uri: String?) {
        setCustomImageUri(activeProfileId, uri)
    }

    // Per-profile custom color spectrum methods
    fun getCustomColorPrimary(profileId: String): Long {
        val specificKey = keyFor(KEY_CUSTOM_COLOR_PRIMARY, profileId)
        return prefs.getLong(specificKey, 0xFF4F46E5L) // Elegant Modern Indigo
    }

    fun setCustomColorPrimary(profileId: String, color: Long) {
        val specificKey = keyFor(KEY_CUSTOM_COLOR_PRIMARY, profileId)
        prefs.edit().putLong(specificKey, color).apply()
        _wallpaperUpdates.value++
    }

    fun getCustomColorSecondary(profileId: String): Long {
        val specificKey = keyFor(KEY_CUSTOM_COLOR_SECONDARY, profileId)
        return prefs.getLong(specificKey, 0xFFEC4899L) // Modern Rose Pink
    }

    fun setCustomColorSecondary(profileId: String, color: Long) {
        val specificKey = keyFor(KEY_CUSTOM_COLOR_SECONDARY, profileId)
        prefs.edit().putLong(specificKey, color).apply()
        _wallpaperUpdates.value++
    }

    fun getCustomColorStyle(profileId: String): String {
        val specificKey = keyFor(KEY_CUSTOM_COLOR_STYLE, profileId)
        return prefs.getString(specificKey, "linear") ?: "linear"
    }

    fun setCustomColorStyle(profileId: String, style: String) {
        val specificKey = keyFor(KEY_CUSTOM_COLOR_STYLE, profileId)
        prefs.edit().putString(specificKey, style).apply()
        _wallpaperUpdates.value++
    }

    fun setCustomColorPrimary(color: Long) {
        setCustomColorPrimary(activeProfileId, color)
    }

    fun setCustomColorSecondary(color: Long) {
        setCustomColorSecondary(activeProfileId, color)
    }

    fun setCustomColorStyle(style: String) {
        setCustomColorStyle(activeProfileId, style)
    }

    companion object {
        private const val KEY_WALLPAPER_TYPE = "key_wallpaper_type"
        private const val KEY_BLUR_RADIUS = "key_blur_radius"
        private const val KEY_DIM_ALPHA = "key_dim_alpha"
        private const val KEY_CUSTOM_URI = "key_custom_uri"
        private const val KEY_CUSTOM_COLOR_PRIMARY = "key_custom_color_primary"
        private const val KEY_CUSTOM_COLOR_SECONDARY = "key_custom_color_secondary"
        private const val KEY_CUSTOM_COLOR_STYLE = "key_custom_color_style"
    }
}
