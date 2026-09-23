/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.model

import com.fairphone.spring.launcher.R

data class AppLibraryCategory(
    val id: String,
    val titleResId: Int,
    val defaultTitle: String,
    val apps: List<AppInfo>
)

object AppLibraryCategorizer {

    fun categorize(
        installedApps: List<AppInfo>,
        essentialApps: List<AppInfo> = emptyList()
    ): List<AppLibraryCategory> {
        if (installedApps.isEmpty()) return emptyList()

        val suggestions = if (essentialApps.isNotEmpty()) {
            essentialApps.take(8)
        } else {
            installedApps.take(4)
        }

        val socialKeywords = listOf(
            "phone", "dialer", "contact", "message", "sms", "mms", "talk", "chat",
            "whatsapp", "telegram", "signal", "discord", "instagram", "facebook",
            "twitter", "reddit", "snapchat", "linkedin", "mail", "email", "gmail",
            "outlook", "slack", "teams", "zoom", "skype", "viber", "wechat", "line"
        )

        val productivityKeywords = listOf(
            "calendar", "clock", "alarm", "deskclock", "calculator", "calc", "note",
            "notes", "keep", "drive", "doc", "docs", "sheet", "sheets", "slide",
            "slides", "office", "word", "excel", "powerpoint", "pdf", "bank",
            "wallet", "finance", "pay", "money", "notion", "todo", "task", "trello",
            "evernote", "reminder"
        )

        val utilitiesKeywords = listOf(
            "setting", "config", "file", "document", "download", "storage", "browser",
            "chrome", "firefox", "opera", "edge", "weather", "map", "maps",
            "navigation", "gps", "compass", "tool", "security", "antivirus", "cleaner",
            "terminal", "assistant", "system"
        )

        val entertainmentKeywords = listOf(
            "music", "audio", "sound", "spotify", "youtube", "netflix", "prime",
            "disney", "video", "player", "tv", "podcast", "game", "games", "play.games",
            "twitch", "tiktok", "steam", "arcade"
        )

        val creativityKeywords = listOf(
            "camera", "photo", "gallery", "image", "canvas", "draw", "paint",
            "edit", "art", "studio", "design", "snapseed", "lightroom", "vsco"
        )

        fun matches(app: AppInfo, keywords: List<String>): Boolean {
            val pkg = app.packageName.lowercase()
            val name = app.name.lowercase()
            return keywords.any { kw -> pkg.contains(kw) || name.contains(kw) }
        }

        val socialApps = mutableListOf<AppInfo>()
        val productivityApps = mutableListOf<AppInfo>()
        val utilitiesApps = mutableListOf<AppInfo>()
        val entertainmentApps = mutableListOf<AppInfo>()
        val creativityApps = mutableListOf<AppInfo>()
        val otherApps = mutableListOf<AppInfo>()

        for (app in installedApps) {
            when {
                matches(app, creativityKeywords) -> creativityApps.add(app)
                matches(app, socialKeywords) -> socialApps.add(app)
                matches(app, productivityKeywords) -> productivityApps.add(app)
                matches(app, entertainmentKeywords) -> entertainmentApps.add(app)
                matches(app, utilitiesKeywords) -> utilitiesApps.add(app)
                else -> otherApps.add(app)
            }
        }

        val categories = mutableListOf<AppLibraryCategory>()

        if (suggestions.isNotEmpty()) {
            categories.add(
                AppLibraryCategory(
                    id = "suggestions",
                    titleResId = R.string.category_suggestions,
                    defaultTitle = "Suggestions",
                    apps = suggestions
                )
            )
        }

        if (socialApps.isNotEmpty()) {
            categories.add(
                AppLibraryCategory(
                    id = "social",
                    titleResId = R.string.category_social,
                    defaultTitle = "Social",
                    apps = socialApps
                )
            )
        }

        if (utilitiesApps.isNotEmpty()) {
            categories.add(
                AppLibraryCategory(
                    id = "utilities",
                    titleResId = R.string.category_utilities,
                    defaultTitle = "Utilities",
                    apps = utilitiesApps
                )
            )
        }

        if (productivityApps.isNotEmpty()) {
            categories.add(
                AppLibraryCategory(
                    id = "productivity",
                    titleResId = R.string.category_productivity,
                    defaultTitle = "Productivity & Finance",
                    apps = productivityApps
                )
            )
        }

        if (creativityApps.isNotEmpty()) {
            categories.add(
                AppLibraryCategory(
                    id = "creativity",
                    titleResId = R.string.category_creativity,
                    defaultTitle = "Creativity",
                    apps = creativityApps
                )
            )
        }

        if (entertainmentApps.isNotEmpty()) {
            categories.add(
                AppLibraryCategory(
                    id = "entertainment",
                    titleResId = R.string.category_entertainment,
                    defaultTitle = "Entertainment",
                    apps = entertainmentApps
                )
            )
        }

        if (otherApps.isNotEmpty()) {
            categories.add(
                AppLibraryCategory(
                    id = "other",
                    titleResId = R.string.category_other,
                    defaultTitle = "Other",
                    apps = otherApps
                )
            )
        }

        return categories
    }
}
