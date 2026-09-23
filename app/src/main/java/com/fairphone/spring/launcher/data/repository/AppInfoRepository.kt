/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.repository

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.UserManager
import android.util.Log
import android.util.LruCache
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.protos.LauncherProfileApp
import com.fairphone.spring.launcher.util.isManagedProfile

interface AppInfoRepository {
    fun getAllInstalledApps(context: Context, forceRefresh: Boolean = false): List<AppInfo>
    fun getAppInfo(context: Context, packageName: String): AppInfo?
    fun getAppInfosByPackageNames(context: Context, packageNames: List<String>): List<AppInfo>
    fun getAppInfosByProfileApps(context: Context, profileApps: List<LauncherProfileApp>): List<AppInfo>
    fun clearCache()
}

class AppInfoRepositoryImpl : AppInfoRepository {

    // Fast memory cache for decoded Drawables (reduces RAM allocations and CPU decode spikes)
    private val iconCache = LruCache<String, Drawable>(160)

    // In-memory cache for installed apps to prevent continuous Binder IPC calls
    @Volatile
    private var cachedAllApps: List<AppInfo>? = null

    @Volatile
    private var cachedAppsByPackage: Map<String, AppInfo>? = null

    override fun clearCache() {
        cachedAllApps = null
        cachedAppsByPackage = null
        iconCache.evictAll()
    }

    override fun getAllInstalledApps(context: Context, forceRefresh: Boolean): List<AppInfo> {
        if (!forceRefresh) {
            cachedAllApps?.let { return it }
        }

        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        val density = context.resources.displayMetrics.densityDpi
        val apps = try {
            // Get all user profiles associated with the current user
            userManager.userProfiles.flatMap { profile -> // Iterate through each profile (UserHandle)
                // Get the list of launchable activities for the specific profile
                val activities = launcherApps.getActivityList(null, profile)
                    .sortedBy { it.label.toString().lowercase() }
                // Check if the profile is a work profile
                val isWorkProfile = profile.isManagedProfile(context)
                activities.mapNotNull { activityInfo ->
                    try {
                        val iconKey = "${activityInfo.componentName.flattenToString()}_$density"
                        val cachedIcon = iconCache.get(iconKey) ?: (activityInfo.getIcon(density) ?: context.packageManager.defaultActivityIcon).also {
                            iconCache.put(iconKey, it)
                        }

                        AppInfo(
                            name = activityInfo.label.toString(),
                            mainActivityClassName = activityInfo.componentName.className,
                            packageName = activityInfo.componentName.packageName,
                            icon = cachedIcon,
                            userUuid = activityInfo.user.hashCode(),
                            isWorkApp = isWorkProfile
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load info for ${activityInfo.componentName.flattenToString()}", e)
                        null
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException accessing LauncherApps. Check permissions or device policy.", e)
            emptyList() // Return empty on permission errors
        } catch (e: Exception) {
            Log.e(TAG, "Error loading apps using LauncherApps", e)
            emptyList() // Return empty list on other errors
        }

        cachedAllApps = apps
        cachedAppsByPackage = apps.associateBy { it.packageName }
        return apps
    }

    override fun getAppInfo(context: Context, packageName: String): AppInfo? {
        cachedAppsByPackage?.get(packageName)?.let { return it }
        return getInstalledAppsLauncherApps(context, listOf(packageName)).firstOrNull()
    }

    override fun getAppInfosByPackageNames(context: Context, packageNames: List<String>): List<AppInfo> {
        val cachedMap = cachedAppsByPackage
        if (cachedMap != null) {
            val fromCache = packageNames.mapNotNull { cachedMap[it] }
            if (fromCache.size == packageNames.size) {
                return fromCache
            }
        }
        return getInstalledAppsLauncherApps(context, packageNames)
    }

    private fun getInstalledAppsLauncherApps(
        context: Context,
        packageNames: List<String>
    ): List<AppInfo>  {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val density = context.resources.displayMetrics.densityDpi

        return try {
            // Get all user profiles associated with the current user
            userManager.userProfiles.flatMap { profile -> // Iterate through each profile (UserHandle)
                val isWorkProfile = profile.isManagedProfile(context)
                // Get the list of launchable activities for the specific profile
                val activities = packageNames.flatMap { packageName ->
                    launcherApps.getActivityList(packageName, profile)
                }.sortedBy { it.label.toString().lowercase() }

                activities.mapNotNull { activityInfo ->
                    try {
                        val iconKey = "${activityInfo.componentName.flattenToString()}_$density"
                        val cachedIcon = iconCache.get(iconKey) ?: (activityInfo.getIcon(density) ?: context.packageManager.defaultActivityIcon).also {
                            iconCache.put(iconKey, it)
                        }

                        AppInfo(
                            name = activityInfo.label.toString(),
                            mainActivityClassName = activityInfo.componentName.className,
                            packageName = activityInfo.componentName.packageName,
                            icon = cachedIcon,
                            userUuid = activityInfo.user.hashCode(),
                            isWorkApp = isWorkProfile
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load info for ${activityInfo.componentName.flattenToString()}", e)
                        null
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException accessing LauncherApps. Check permissions or device policy.", e)
            emptyList() // Return empty on permission errors
        } catch (e: Exception) {
            Log.e(TAG, "Error loading apps using LauncherApps", e)
            emptyList() // Return empty list on other errors
        }
    }

    /**
     * Retrieves a list of [AppInfo] objects based on a list of [LauncherProfileApp] objects.
     * @param context The application context.
     * @param profileApps A list of [LauncherProfileApp] objects specifying the apps to retrieve.
     * @return A list of [AppInfo] objects corresponding to the provided [LauncherProfileApp] objects.
     */
    override fun getAppInfosByProfileApps(
        context: Context,
        profileApps: List<LauncherProfileApp>
    ): List<AppInfo> {
        val cachedMap = cachedAppsByPackage
        if (!cachedMap.isNullOrEmpty()) {
            val results = profileApps.mapNotNull { profileApp ->
                cachedMap[profileApp.packageName]?.let { app ->
                    // Verify work app status matches
                    if (app.isWorkApp == profileApp.isWorkApp) app else null
                }
            }
            if (results.isNotEmpty() || profileApps.isEmpty()) {
                return results
            }
        }

        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val density = context.resources.displayMetrics.densityDpi

        return try {
            // Get all user profiles associated with the current user
            userManager.userProfiles.flatMap { profile -> // Iterate through each profile (UserHandle)
                // Get the list of launchable activities for the specific profile
                val isWorkProfile = profile.isManagedProfile(context)
                val activities = profileApps
                    // Keeps only those LauncherProfileApp objects whose isWorkApp property
                    // matches whether the current profile (UserHandle) being processed is a work profile
                    .filter { it.isWorkApp == isWorkProfile }
                    .flatMap { launcherProfileApp ->
                        launcherApps.getActivityList(launcherProfileApp.packageName, profile)
                    }.sortedBy { it.label.toString().lowercase() }

                activities.mapNotNull { activityInfo ->
                    try {
                        val iconKey = "${activityInfo.componentName.flattenToString()}_$density"
                        val cachedIcon = iconCache.get(iconKey) ?: (activityInfo.getIcon(density) ?: context.packageManager.defaultActivityIcon).also {
                            iconCache.put(iconKey, it)
                        }

                        AppInfo(
                            name = activityInfo.label.toString(),
                            mainActivityClassName = activityInfo.componentName.className,
                            packageName = activityInfo.componentName.packageName,
                            icon = cachedIcon,
                            userUuid = activityInfo.user.hashCode(),
                            isWorkApp = isWorkProfile
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load info for ${activityInfo.componentName.flattenToString()}", e)
                        null
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException accessing LauncherApps. Check permissions or device policy.", e)
            emptyList() // Return empty on permission errors
        } catch (e: Exception) {
            Log.e(TAG, "Error loading apps using LauncherApps", e)
            emptyList() // Return empty list on other errors
        }
    }


    companion object {
        const val TAG = "AppInfoRepository"
    }
}
