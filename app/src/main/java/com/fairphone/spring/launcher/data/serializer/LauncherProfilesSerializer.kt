/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.serializer

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.fairphone.spring.launcher.data.model.protos.LauncherProfiles
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object LauncherProfilesSerializer : Serializer<LauncherProfiles> {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    override val defaultValue: LauncherProfiles
        get() = LauncherProfiles.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LauncherProfiles {
        return try {
            val bytes = input.readBytes()
            if (bytes.isEmpty()) {
                LauncherProfiles.getDefaultInstance()
            } else {
                json.decodeFromString(LauncherProfiles.serializer(), bytes.decodeToString())
            }
        } catch (e: Exception) {
            Log.e("LauncherProfilesSerializer", "Error deserializing profiles", e)
            throw CorruptionException("Cannot read profiles.", e)
        }
    }

    override suspend fun writeTo(
        t: LauncherProfiles,
        output: OutputStream
    ) {
        val string = json.encodeToString(LauncherProfiles.serializer(), t)
        output.write(string.encodeToByteArray())
    }
}