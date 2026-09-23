/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.model.protos

import kotlinx.serialization.Serializable

@Serializable
enum class ContactType(val value: Int) {
    CONTACT_TYPE_EVERYONE(0),
    CONTACT_TYPE_NONE(1),
    CONTACT_TYPE_ALL_CONTACTS(2),
    CONTACT_TYPE_STARRED(3),
    CONTACT_TYPE_CUSTOM(4),
    UNRECOGNIZED(-1);

    companion object {
        fun forNumber(value: Int): ContactType = entries.find { it.value == value } ?: UNRECOGNIZED
    }
}

@Serializable
enum class UiMode(val value: Int) {
    UI_MODE_LIGHT(0),
    UI_MODE_DARK(1),
    UI_MODE_SYSTEM(2);

    companion object {
        fun forNumber(value: Int): UiMode = entries.find { it.value == value } ?: UI_MODE_SYSTEM
    }
}

@Serializable
enum class SoundSetting(val value: Int) {
    SOUND_SETTING_LOUD(0),
    SOUND_SETTING_VIBRATE(1),
    SOUND_SETTING_SILENT(2),
    SOUND_SETTING_FOLLOW_DEVICE_SETTINGS(3),
    UNRECOGNIZED(-1);

    companion object {
        fun forNumber(value: Int): SoundSetting = entries.find { it.value == value } ?: UNRECOGNIZED
    }
}

@Serializable
data class LauncherProfileApp(
    var packageName: String = "",
    var isWorkApp: Boolean = false
) {
    companion object {
        fun newBuilder(): Builder = Builder()
    }

    class Builder(
        var packageName: String = "",
        var isWorkApp: Boolean = false
    ) {
        fun setPackageName(name: String) = apply { this.packageName = name }
        fun setIsWorkApp(isWork: Boolean) = apply { this.isWorkApp = isWork }
        fun build() = LauncherProfileApp(packageName, isWorkApp)
    }

    fun toBuilder() = Builder(packageName, isWorkApp)
}

fun launcherProfileApp(block: LauncherProfileApp.Builder.() -> Unit): LauncherProfileApp {
    return LauncherProfileApp.Builder().apply(block).build()
}

@Serializable
data class LauncherProfile(
    var id: String = "",
    var name: String = "",
    var icon: String = "",
    var bgColor1: Long = 0L,
    var bgColor2: Long = 0L,
    var visibleApps: List<String> = emptyList(),
    var allowedContacts: ContactType = ContactType.CONTACT_TYPE_EVERYONE,
    var customContacts: List<String> = emptyList(),
    var repeatCallEnabled: Boolean = false,
    var appNotifications: List<String> = emptyList(),
    var wallpaperId: Int = 0,
    var uiMode: UiMode = UiMode.UI_MODE_SYSTEM,
    var blueLightFilterEnabled: Boolean = false,
    var soundSetting: SoundSetting = SoundSetting.SOUND_SETTING_FOLLOW_DEVICE_SETTINGS,
    var batterySaverEnabled: Boolean = false,
    var reduceBrightnessEnabled: Boolean = false,
    var ecoChargeEnabled: Boolean = false,
    var alwaysOnDisplayEnabled: Boolean = false,
    var airplaneModeEnabled: Boolean = false,
    var zenRuleId: String = "",
    var launcherProfileApps: List<LauncherProfileApp> = emptyList(),
    var grayScaleEnabled: Boolean = false
) {
    val visibleAppsList: List<String> get() = visibleApps
    val visibleAppsCount: Int get() = visibleApps.size
    val customContactsList: List<String> get() = customContacts
    val customContactsCount: Int get() = customContacts.size
    val appNotificationsList: List<String> get() = appNotifications
    val appNotificationsCount: Int get() = appNotifications.size
    val launcherProfileAppsList: List<LauncherProfileApp> get() = launcherProfileApps

    companion object {
        fun newBuilder(): Builder = Builder()
        fun getDefaultInstance() = LauncherProfile()
    }

    fun toBuilder(): Builder = Builder(
        id = id,
        name = name,
        icon = icon,
        bgColor1 = bgColor1,
        bgColor2 = bgColor2,
        visibleApps = visibleApps.toMutableList(),
        allowedContacts = allowedContacts,
        customContacts = customContacts.toMutableList(),
        repeatCallEnabled = repeatCallEnabled,
        appNotifications = appNotifications.toMutableList(),
        wallpaperId = wallpaperId,
        uiMode = uiMode,
        blueLightFilterEnabled = blueLightFilterEnabled,
        soundSetting = soundSetting,
        batterySaverEnabled = batterySaverEnabled,
        reduceBrightnessEnabled = reduceBrightnessEnabled,
        ecoChargeEnabled = ecoChargeEnabled,
        alwaysOnDisplayEnabled = alwaysOnDisplayEnabled,
        airplaneModeEnabled = airplaneModeEnabled,
        zenRuleId = zenRuleId,
        launcherProfileApps = launcherProfileApps.toMutableList(),
        grayScaleEnabled = grayScaleEnabled
    )

    class Builder(
        var id: String = "",
        var name: String = "",
        var icon: String = "",
        var bgColor1: Long = 0L,
        var bgColor2: Long = 0L,
        var visibleApps: MutableList<String> = mutableListOf(),
        var allowedContacts: ContactType = ContactType.CONTACT_TYPE_EVERYONE,
        var customContacts: MutableList<String> = mutableListOf(),
        var repeatCallEnabled: Boolean = false,
        var appNotifications: MutableList<String> = mutableListOf(),
        var wallpaperId: Int = 0,
        var uiMode: UiMode = UiMode.UI_MODE_SYSTEM,
        var blueLightFilterEnabled: Boolean = false,
        var soundSetting: SoundSetting = SoundSetting.SOUND_SETTING_FOLLOW_DEVICE_SETTINGS,
        var batterySaverEnabled: Boolean = false,
        var reduceBrightnessEnabled: Boolean = false,
        var ecoChargeEnabled: Boolean = false,
        var alwaysOnDisplayEnabled: Boolean = false,
        var airplaneModeEnabled: Boolean = false,
        var zenRuleId: String = "",
        var launcherProfileApps: MutableList<LauncherProfileApp> = mutableListOf(),
        var grayScaleEnabled: Boolean = false
    ) {
        val launcherProfileAppsList: List<LauncherProfileApp> get() = launcherProfileApps
        val visibleAppsList: List<String> get() = visibleApps
        val visibleAppsCount: Int get() = visibleApps.size
        val customContactsList: List<String> get() = customContacts
        val customContactsCount: Int get() = customContacts.size
        val appNotificationsList: List<String> get() = appNotifications
        val appNotificationsCount: Int get() = appNotifications.size

        fun setId(id: String) = apply { this.id = id }
        fun setName(name: String) = apply { this.name = name }
        fun setIcon(icon: String) = apply { this.icon = icon }
        fun setBgColor1(c: Long) = apply { this.bgColor1 = c }
        fun setBgColor2(c: Long) = apply { this.bgColor2 = c }
        fun addAllVisibleApps(apps: Iterable<String>) = apply { this.visibleApps.addAll(apps) }
        fun clearVisibleApps() = apply { this.visibleApps.clear() }
        fun setAllowedContacts(contacts: ContactType) = apply { this.allowedContacts = contacts }
        fun addAllCustomContacts(contacts: Iterable<String>) = apply { this.customContacts.addAll(contacts) }
        fun clearCustomContacts() = apply { this.customContacts.clear() }
        fun setRepeatCallEnabled(enabled: Boolean) = apply { this.repeatCallEnabled = enabled }
        fun addAllAppNotifications(notifications: Iterable<String>) = apply { this.appNotifications.addAll(notifications) }
        fun clearAppNotifications() = apply { this.appNotifications.clear() }
        fun setWallpaperId(id: Int) = apply { this.wallpaperId = id }
        fun setUiMode(mode: UiMode) = apply { this.uiMode = mode }
        fun setBlueLightFilterEnabled(enabled: Boolean) = apply { this.blueLightFilterEnabled = enabled }
        fun setSoundSetting(setting: SoundSetting) = apply { this.soundSetting = setting }
        fun setBatterySaverEnabled(enabled: Boolean) = apply { this.batterySaverEnabled = enabled }
        fun setReduceBrightnessEnabled(enabled: Boolean) = apply { this.reduceBrightnessEnabled = enabled }
        fun setEcoChargeEnabled(enabled: Boolean) = apply { this.ecoChargeEnabled = enabled }
        fun setAlwaysOnDisplayEnabled(enabled: Boolean) = apply { this.alwaysOnDisplayEnabled = enabled }
        fun setAirplaneModeEnabled(enabled: Boolean) = apply { this.airplaneModeEnabled = enabled }
        fun setZenRuleId(id: String) = apply { this.zenRuleId = id }
        fun addAllLauncherProfileApps(apps: Iterable<LauncherProfileApp>) = apply { this.launcherProfileApps.addAll(apps) }
        fun clearLauncherProfileApps() = apply { this.launcherProfileApps.clear() }
        fun setGrayScaleEnabled(enabled: Boolean) = apply { this.grayScaleEnabled = enabled }

        fun build() = LauncherProfile(
            id = id,
            name = name,
            icon = icon,
            bgColor1 = bgColor1,
            bgColor2 = bgColor2,
            visibleApps = visibleApps.toList(),
            allowedContacts = allowedContacts,
            customContacts = customContacts.toList(),
            repeatCallEnabled = repeatCallEnabled,
            appNotifications = appNotifications.toList(),
            wallpaperId = wallpaperId,
            uiMode = uiMode,
            blueLightFilterEnabled = blueLightFilterEnabled,
            soundSetting = soundSetting,
            batterySaverEnabled = batterySaverEnabled,
            reduceBrightnessEnabled = reduceBrightnessEnabled,
            ecoChargeEnabled = ecoChargeEnabled,
            alwaysOnDisplayEnabled = alwaysOnDisplayEnabled,
            airplaneModeEnabled = airplaneModeEnabled,
            zenRuleId = zenRuleId,
            launcherProfileApps = launcherProfileApps.toList(),
            grayScaleEnabled = grayScaleEnabled
        )
    }
}

fun launcherProfile(block: LauncherProfile.Builder.() -> Unit): LauncherProfile {
    return LauncherProfile.Builder().apply(block).build()
}

fun LauncherProfile.copy(block: LauncherProfile.Builder.() -> Unit): LauncherProfile {
    return toBuilder().apply(block).build()
}

@Serializable
data class LauncherProfiles(
    var active: String = "",
    var edited: String = "",
    var profiles: List<LauncherProfile> = emptyList()
) {
    val profilesList: List<LauncherProfile> get() = profiles

    companion object {
        fun newBuilder(): Builder = Builder()
        fun getDefaultInstance() = LauncherProfiles()
    }

    fun toBuilder(): Builder = Builder(
        active = active,
        edited = edited,
        profiles = profiles.toMutableList()
    )

    class Builder(
        var active: String = "",
        var edited: String = "",
        var profiles: MutableList<LauncherProfile> = mutableListOf()
    ) {
        val profilesList: List<LauncherProfile> get() = profiles

        fun setActive(active: String) = apply { this.active = active }
        fun setEdited(edited: String) = apply { this.edited = edited }
        fun addAllProfiles(profiles: Iterable<LauncherProfile>) = apply { this.profiles.addAll(profiles) }
        fun clearProfiles() = apply { this.profiles.clear() }
        fun addProfile(profile: LauncherProfile) = apply { this.profiles.add(profile) }

        fun build() = LauncherProfiles(
            active = active,
            edited = edited,
            profiles = profiles.toList()
        )
    }
}

fun launcherProfiles(block: LauncherProfiles.Builder.() -> Unit): LauncherProfiles {
    return LauncherProfiles.Builder().apply(block).build()
}
