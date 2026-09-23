/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.app.AutomaticZenRule
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.notification.Condition
import android.service.notification.ZenDeviceEffects
import android.service.notification.ZenPolicy
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.fairphone.spring.launcher.activity.LauncherSettingsActivity
import com.fairphone.spring.launcher.data.model.CreateLauncherProfile
import com.fairphone.spring.launcher.data.model.protos.ContactType
import com.fairphone.spring.launcher.data.model.protos.UiMode

class ZenNotificationManager(private val context: Context) {

    companion object {
        val ZEN_RULE_CONDITION_ID = "com.fairphone.moments".toUri()
    }

    /**
     * Enables Do Not Disturb mode for the given rule.
     */
    fun enableDnd(zenRuleId: String, name: String) {
        if (!context.isDoNotDisturbAccessGranted() || zenRuleId.isBlank()) return

        try {
            setAutomaticZenRuleState(zenRuleId, name, Condition.STATE_TRUE)
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Failed to enable DND: ${e.message}")
        }
    }

    /**
     * Disables Do Not Disturb mode for the given rule.
     */
    fun disableDnd(zenRuleId: String, name: String) {
        if (!context.isDoNotDisturbAccessGranted() || zenRuleId.isBlank()) return

        try {
            setAutomaticZenRuleState(zenRuleId, name, Condition.STATE_FALSE)
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Failed to disable DND: ${e.message}")
        }
    }

    /**
     * Disables Do Not Disturb mode for all automatic zen rules.
     */
    fun disableAllDnd() {
        if (!context.isDoNotDisturbAccessGranted()) return

        try {
            context.notificationManager().automaticZenRules.forEach { ruleId, rule ->
                disableDnd(ruleId, rule.name)
            }
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Failed to disable all DND: ${e.message}")
        }
    }

    /**
     * Creates a new automatic zen rule using the given parameters and adds it to the notification manager.
     */
    fun createAutomaticZenRule(profile: CreateLauncherProfile): String {
        if (!context.isDoNotDisturbAccessGranted()) return ""

        return try {
            val zenRule = createZenRule(
                name = profile.name,
                allowedContacts = profile.allowedContacts,
                uiMode = profile.uiMode,
                repeatCallEnabled = profile.repeatCallEnabled,
            )
            context.notificationManager().addAutomaticZenRule(zenRule) ?: ""
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Failed to create zen rule: ${e.message}")
            ""
        }
    }

    /**
     * Updates an existing automatic zen rule using the given parameters.
     */
    fun updateAutomaticZenRule(
        zenRuleId: String,
        name: String,
        allowedContacts: ContactType,
        uiMode: UiMode,
        repeatCallEnabled: Boolean,
    ): Result<AutomaticZenRule?> {
        if (!context.isDoNotDisturbAccessGranted() || zenRuleId.isBlank()) {
            return Result.success(null)
        }

        return try {
            val existingRule = context.notificationManager().getAutomaticZenRule(zenRuleId)
            if (existingRule == null) {
                return Result.success(null)
            }

            // Disable DND first
            disableDnd(zenRuleId, name)

            // Update rule
            val updatedZenRule = createZenRule(
                name = name,
                allowedContacts = allowedContacts,
                uiMode = uiMode,
                repeatCallEnabled = repeatCallEnabled,
            )
            val result = context.notificationManager().updateAutomaticZenRule(zenRuleId, updatedZenRule)

            if (result) {
                Result.success(context.notificationManager().getAutomaticZenRule(zenRuleId))
            } else {
                Result.failure(Exception("Failed to update automatic zen rule"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Removes an existing automatic zen rule.
     */
    fun removeAutomaticZenRule(zenRuleId: String): Result<Unit> {
        if (!context.isDoNotDisturbAccessGranted() || zenRuleId.isBlank()) {
            return Result.success(Unit)
        }

        return try {
            val result = context.notificationManager().removeAutomaticZenRule(zenRuleId)
            if (result) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove automatic zen rule"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sets the state of the given automatic zen rule.
     */
    private fun setAutomaticZenRuleState(
        zenRuleId: String,
        name: String,
        state: Int
    ) {
        context.notificationManager()
            .setAutomaticZenRuleState(
                zenRuleId,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    Condition(
                        ZEN_RULE_CONDITION_ID,
                        name,
                        state,
                        Condition.SOURCE_USER_ACTION
                    )
                } else {
                    Condition(
                        ZEN_RULE_CONDITION_ID,
                        name,
                        state
                    )
                }
            )
    }

    /**
     * Creates an automatic zen rule using the given parameters.
     */
    private fun createZenRule(
        name: String,
        allowedContacts: ContactType,
        uiMode: UiMode,
        repeatCallEnabled: Boolean,
    ): AutomaticZenRule {
        val configActivity = getConfigurationActivity(context)
        val zenPolicy = createZenPolicy(allowedContacts, repeatCallEnabled)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return AutomaticZenRule(
                name,
                null,
                configActivity,
                ZEN_RULE_CONDITION_ID,
                zenPolicy,
                NotificationManager.INTERRUPTION_FILTER_PRIORITY,
                true
            )
        }

        val zenDeviceEffects = createZenDeviceEffects(uiMode)

        return AutomaticZenRule.Builder(name, ZEN_RULE_CONDITION_ID)
            .setConfigurationActivity(configActivity)
            .setDeviceEffects(zenDeviceEffects)
            .setZenPolicy(zenPolicy)
            .build()
    }

    private fun getConfigurationActivity(context: Context): ComponentName {
        return ComponentName(context, LauncherSettingsActivity::class.java)
    }

    private fun createZenPolicy(
        allowedContacts: ContactType,
        allowRepeatCallers: Boolean,
    ): ZenPolicy {
        val peopleType = when (allowedContacts) {
            ContactType.CONTACT_TYPE_EVERYONE -> ZenPolicy.PEOPLE_TYPE_ANYONE
            ContactType.CONTACT_TYPE_NONE -> ZenPolicy.PEOPLE_TYPE_NONE
            ContactType.CONTACT_TYPE_ALL_CONTACTS -> ZenPolicy.PEOPLE_TYPE_CONTACTS
            ContactType.CONTACT_TYPE_STARRED -> ZenPolicy.PEOPLE_TYPE_STARRED
            ContactType.CONTACT_TYPE_CUSTOM -> ZenPolicy.PEOPLE_TYPE_UNSET
            ContactType.UNRECOGNIZED -> ZenPolicy.PEOPLE_TYPE_NONE
        }

        val builder = ZenPolicy.Builder()
            .allowCalls(peopleType)
            .allowMessages(peopleType)
            .allowMedia(true)
            .allowRepeatCallers(allowRepeatCallers)
            .hideAllVisualEffects()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.allowConversations(peopleType)
        }

        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun createZenDeviceEffects(uiMode: UiMode): ZenDeviceEffects {
        return ZenDeviceEffects.Builder()
            .setShouldUseNightMode(uiMode == UiMode.UI_MODE_DARK)
            .build()
    }

}