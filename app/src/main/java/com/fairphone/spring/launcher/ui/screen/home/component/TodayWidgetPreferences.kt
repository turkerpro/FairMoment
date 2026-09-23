/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TodayWidgetPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("today_widget_feed_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FEED_JSON = "key_widget_feed_json_v2"
        private const val KEY_NOTES_JSON = "key_quick_notes_json"

        @Volatile
        private var memoryCachedFeed: List<FeedItem>? = null

        @Volatile
        private var memoryCachedNotes: List<QuickNote>? = null
    }

    fun loadFeed(): List<FeedItem> {
        memoryCachedFeed?.let { return it }

        val jsonStr = prefs.getString(KEY_FEED_JSON, null)
        if (jsonStr.isNullOrBlank()) {
            val defaultFeed = getDefaultFeed()
            saveFeed(defaultFeed)
            memoryCachedFeed = defaultFeed
            return defaultFeed
        }

        val loaded = try {
            val jsonArr = JSONArray(jsonStr)
            val list = mutableListOf<FeedItem>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                FeedItem.fromJson(obj)?.let { list.add(it) }
            }
            if (list.isEmpty()) {
                val defaultFeed = getDefaultFeed()
                saveFeed(defaultFeed)
                defaultFeed
            } else {
                list
            }
        } catch (_: Exception) {
            val defaultFeed = getDefaultFeed()
            saveFeed(defaultFeed)
            defaultFeed
        }
        memoryCachedFeed = loaded
        return loaded
    }

    fun saveFeed(feed: List<FeedItem>) {
        memoryCachedFeed = feed
        try {
            val jsonArr = JSONArray()
            feed.forEach { jsonArr.put(it.toJson()) }
            prefs.edit().putString(KEY_FEED_JSON, jsonArr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadNotes(): List<QuickNote> {
        memoryCachedNotes?.let { return it }

        val jsonStr = prefs.getString(KEY_NOTES_JSON, null)
        if (jsonStr.isNullOrBlank()) {
            val defaultNotes = listOf(
                QuickNote(1, "Pick up groceries 🥦", false),
                QuickNote(2, "Moments design sync", true),
                QuickNote(3, "Call mom tonight ❤️", false)
            )
            memoryCachedNotes = defaultNotes
            return defaultNotes
        }

        val loaded = try {
            val jsonArr = JSONArray(jsonStr)
            val list = mutableListOf<QuickNote>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                list.add(
                    QuickNote(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        text = obj.optString("text", ""),
                        isDone = obj.optBoolean("isDone", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            listOf(
                QuickNote(1, "Pick up groceries 🥦", false),
                QuickNote(2, "Moments design sync", true),
                QuickNote(3, "Call mom tonight ❤️", false)
            )
        }
        memoryCachedNotes = loaded
        return loaded
    }

    fun saveNotes(notes: List<QuickNote>) {
        memoryCachedNotes = notes
        try {
            val jsonArr = JSONArray()
            notes.forEach { note ->
                val obj = JSONObject()
                obj.put("id", note.id)
                obj.put("text", note.text)
                obj.put("isDone", note.isDone)
                jsonArr.put(obj)
            }
            prefs.edit().putString(KEY_NOTES_JSON, jsonArr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun getDefaultFeed(): List<FeedItem> {
        // Initial setup has an iOS-style Smart Stack on top by default,
        // showcasing swipeable stacked widgets (Weather, Battery, Now Playing)!
        val primaryStack = FeedItem.Stack(
            id = "default_smart_stack",
            name = "Smart Stack",
            items = listOf(
                WidgetItem(id = "stack_w_weather", type = WidgetType.WEATHER, weatherCity = "Amsterdam"),
                WidgetItem(id = "stack_w_battery", type = WidgetType.BATTERY),
                WidgetItem(id = "stack_w_music", type = WidgetType.MUSIC)
            )
        )

        return listOf(
            primaryStack,
            FeedItem.Single(WidgetItem(id = "w_calendar", type = WidgetType.CALENDAR)),
            FeedItem.Single(WidgetItem(id = "w_notes", type = WidgetType.NOTES)),
            FeedItem.Single(WidgetItem(id = "w_shortcuts", type = WidgetType.SHORTCUTS)),
            FeedItem.Single(WidgetItem(id = "w_fitness", type = WidgetType.FITNESS)),
            FeedItem.Single(WidgetItem(id = "w_screentime", type = WidgetType.SCREEN_TIME)),
            FeedItem.Single(WidgetItem(id = "w_quotes", type = WidgetType.QUOTES))
        )
    }
}
