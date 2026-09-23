/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import com.fairphone.spring.launcher.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class WidgetType(
    val titleResId: Int,
    val description: String,
    val icon: ImageVector
) {
    WEATHER(R.string.widget_weather_title, "Current weather, temperature & forecast", Icons.Default.WbSunny),
    CALENDAR(R.string.widget_calendar_title, "Upcoming meetings and calendar events", Icons.Default.CalendarMonth),
    BATTERY(R.string.widget_battery_title, "Device battery level, RAM and storage", Icons.Default.BatteryChargingFull),
    NOTES(R.string.widget_notes_title, "Interactive to-do checklist and quick thoughts", Icons.Default.NoteAlt),
    MUSIC(R.string.widget_music_title, "Media playback controls and track details", Icons.Default.MusicNote),
    SCREEN_TIME(R.string.widget_screentime_title, "Screen time and digital wellbeing stats", Icons.Default.HourglassTop),
    SHORTCUTS(R.string.widget_shortcuts_title, "One-tap essential app & action shortcuts", Icons.Default.TouchApp),
    FITNESS(R.string.widget_fitness_title, "Daily step goal progress and activity rings", Icons.Default.DirectionsRun),
    WORLD_CLOCK(R.string.widget_world_clock_title, "Global city clocks and time difference", Icons.Default.Public),
    QUOTES(R.string.widget_quotes_title, "Thoughtful quotes for focus & sustainability", Icons.Default.FormatQuote),
    APP_WIDGET(R.string.widget_installed_app_title, "Installed application widget", Icons.Default.Widgets);

    companion object {
        fun fromName(name: String, fallback: WidgetType = WEATHER): WidgetType {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: fallback
        }
    }
}

data class QuickNote(
    val id: Long,
    val text: String,
    val isDone: Boolean = false
)

data class WidgetItem(
    val id: String = UUID.randomUUID().toString(),
    val type: WidgetType,
    val weatherCity: String = "Amsterdam",
    val weatherUnit: String = "°C",
    val quoteCategory: String = "Sustainability",
    val selectedShortcuts: List<String> = listOf("search", "camera", "calc", "alarm"),
    val batteryShowStorage: Boolean = true,
    val fitnessGoal: Int = 8000,
    val worldClockCities: List<String> = listOf("London", "New York", "Tokyo"),
    val appWidgetId: Int = -1,
    val providerPackage: String = "",
    val providerClass: String = "",
    val appLabel: String = "",
    val minHeightDp: Int = 180
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("type", type.name)
        json.put("weatherCity", weatherCity)
        json.put("weatherUnit", weatherUnit)
        json.put("quoteCategory", quoteCategory)
        json.put("selectedShortcuts", JSONArray(selectedShortcuts))
        json.put("batteryShowStorage", batteryShowStorage)
        json.put("fitnessGoal", fitnessGoal)
        json.put("worldClockCities", JSONArray(worldClockCities))
        json.put("appWidgetId", appWidgetId)
        json.put("providerPackage", providerPackage)
        json.put("providerClass", providerClass)
        json.put("appLabel", appLabel)
        json.put("minHeightDp", minHeightDp)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): WidgetItem {
            val shortcuts = mutableListOf<String>()
            val shortcutsJson = json.optJSONArray("selectedShortcuts")
            if (shortcutsJson != null) {
                for (i in 0 until shortcutsJson.length()) {
                    shortcuts.add(shortcutsJson.getString(i))
                }
            } else {
                shortcuts.addAll(listOf("search", "camera", "calc", "alarm"))
            }

            val cities = mutableListOf<String>()
            val citiesJson = json.optJSONArray("worldClockCities")
            if (citiesJson != null) {
                for (i in 0 until citiesJson.length()) {
                    cities.add(citiesJson.getString(i))
                }
            } else {
                cities.addAll(listOf("London", "New York", "Tokyo"))
            }

            return WidgetItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                type = WidgetType.fromName(json.optString("type", "WEATHER")),
                weatherCity = json.optString("weatherCity", "Amsterdam"),
                weatherUnit = json.optString("weatherUnit", "°C"),
                quoteCategory = json.optString("quoteCategory", "Sustainability"),
                selectedShortcuts = shortcuts,
                batteryShowStorage = json.optBoolean("batteryShowStorage", true),
                fitnessGoal = json.optInt("fitnessGoal", 8000),
                worldClockCities = cities,
                appWidgetId = json.optInt("appWidgetId", -1),
                providerPackage = json.optString("providerPackage", ""),
                providerClass = json.optString("providerClass", ""),
                appLabel = json.optString("appLabel", ""),
                minHeightDp = json.optInt("minHeightDp", 180)
            )
        }
    }
}

sealed class FeedItem {
    abstract val id: String

    data class Single(val item: WidgetItem) : FeedItem() {
        override val id: String get() = item.id
    }

    data class Stack(
        override val id: String = UUID.randomUUID().toString(),
        val name: String = "Smart Stack",
        val items: List<WidgetItem>,
        val autoRotate: Boolean = false
    ) : FeedItem()

    fun toJson(): JSONObject {
        val json = JSONObject()
        when (this) {
            is Single -> {
                json.put("itemType", "SINGLE")
                json.put("item", item.toJson())
            }
            is Stack -> {
                json.put("itemType", "STACK")
                json.put("id", id)
                json.put("name", name)
                json.put("autoRotate", autoRotate)
                val itemsArr = JSONArray()
                items.forEach { itemsArr.put(it.toJson()) }
                json.put("items", itemsArr)
            }
        }
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): FeedItem? {
            val itemType = json.optString("itemType")
            return when (itemType) {
                "SINGLE" -> {
                    val itemJson = json.optJSONObject("item") ?: return null
                    Single(WidgetItem.fromJson(itemJson))
                }
                "STACK" -> {
                    val id = json.optString("id", UUID.randomUUID().toString())
                    val name = json.optString("name", "Smart Stack")
                    val autoRotate = json.optBoolean("autoRotate", false)
                    val itemsArr = json.optJSONArray("items") ?: JSONArray()
                    val list = mutableListOf<WidgetItem>()
                    for (i in 0 until itemsArr.length()) {
                        list.add(WidgetItem.fromJson(itemsArr.getJSONObject(i)))
                    }
                    if (list.isEmpty()) {
                        // fallback if empty
                        list.add(WidgetItem(type = WidgetType.WEATHER))
                    }
                    Stack(id = id, name = name, items = list, autoRotate = autoRotate)
                }
                else -> null
            }
        }
    }
}
