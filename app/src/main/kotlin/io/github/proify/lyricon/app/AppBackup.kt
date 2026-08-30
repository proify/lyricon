/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.app

import android.content.SharedPreferences
import android.util.Log
import io.github.proify.android.extensions.deflate
import io.github.proify.android.extensions.getPrivateSharedPreferences
import io.github.proify.android.extensions.inflate
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.app.util.LyricPrefs.getLyricStylePrefNames
import io.github.proify.lyricon.app.util.editCommit
import io.github.proify.lyricon.lyric.ai.core.AiConfig.Companion.KEY_AI_CONFIG_API_KEY
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

object AppBackup {

    private const val TAG = "AppBackup"

    private val BLACKLIST_KEYS = listOf(
        KEY_AI_CONFIG_API_KEY
    )

    fun export(outputStream: OutputStream): Boolean {
        val map = collectAllPrefs()

        return runCatching {
            val root = JSONObject()
            map.forEach { (name, entries) ->
                root.put(name, entriesToJsonObject(entries))
            }

            val bytes = root.toString()
                .toByteArray(Charsets.UTF_8)
                .deflate()

            outputStream.use { it.write(bytes) }
            true
        }.onFailure {
            Log.e(TAG, "Export failed", it)
        }.getOrDefault(false)
    }

    fun restore(input: InputStream): Boolean {
        return runCatching {
            val raw = input.use { it.readBytes() }
            val jsonText = raw
                .inflate()
                .toString(Charsets.UTF_8)

            val root = JSONObject(jsonText)

            applyJsonToPrefs(root)
            true
        }.onFailure {
            Log.e(TAG, "Restore failed", it)
        }.getOrDefault(false)
    }

    private fun collectAllPrefs(): Map<String, Map<String, *>> {
        val prefNames = getLyricStylePrefNames()

        val result = mutableMapOf<String, Map<String, *>>()

        val context = LyriconApp.get()
        prefNames.forEach { name ->
            val prefs = when {
                LyricPrefs.isLyricStylePrefName(name) -> LyricPrefs.getSharedPreferences(name)
                else -> context.getPrivateSharedPreferences(name)
            }
            val entries = prefs.all
            result[name] = entries
        }

        return result
    }

    private fun entriesToJsonObject(entries: Map<String, *>): JSONObject {
        val jo = JSONObject()
        entries.forEach { (k, v) ->
            if (k in BLACKLIST_KEYS) return@forEach

            when (v) {
                is Set<*> -> jo.put(k, JSONArray(v))
                else -> jo.put(k, v)
            }
        }
        return jo
    }

    private fun applyJsonToPrefs(root: JSONObject) {
        val names = root.keys()
        val context = LyriconApp.get()

        while (names.hasNext()) {
            val prefsName = names.next().takeIf { it.isNotBlank() } ?: continue
            val prefsJson = root.optJSONObject(prefsName) ?: continue
            val prefs = when {
                LyricPrefs.isLyricStylePrefName(prefsName) -> LyricPrefs.getSharedPreferences(
                    prefsName
                )

                else -> context.getPrivateSharedPreferences(prefsName)
            }
            writeJsonToPrefs(prefs, prefsJson)
        }
    }

    private fun writeJsonToPrefs(prefs: SharedPreferences, json: JSONObject) {
        prefs.editCommit {
            //clear()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key in BLACKLIST_KEYS) continue

                when (val v = json.opt(key)) {
                    is Boolean -> putBoolean(key, v)
                    is String -> putString(key, v)
                    is Number -> putNumber(key, v)
                    is JSONArray -> putStringSet(key, jsonArrayToSet(v))
                }
            }
        }
    }

    private fun jsonArrayToSet(array: JSONArray): Set<String> {
        val s = HashSet<String>(array.length())
        for (i in 0 until array.length()) {
            s.add(array.optString(i))
        }
        return s
    }

    private fun SharedPreferences.Editor.putNumber(key: String, number: Number) {
        when (number) {
            is Int -> putInt(key, number)
            is Long -> putLong(key, number)
            is Float -> putFloat(key, number)
            is Double -> putFloat(key, number.toFloat())
            else -> {
                val lv = number.toLong()
                if (lv in Int.MIN_VALUE..Int.MAX_VALUE) putInt(key, lv.toInt()) else putLong(
                    key,
                    lv
                )
            }
        }
    }

}