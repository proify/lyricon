package io.github.proify.android.extensions

import android.content.Context
import android.content.SharedPreferences

fun Context.getSharedPreferences(name: String, worldReadable: Boolean): SharedPreferences {
    if (!worldReadable) {
        return getSharedPreferences(name, Context.MODE_PRIVATE)
    }
    return try {
        @Suppress("DEPRECATION")
        getSharedPreferences(name, Context.MODE_WORLD_READABLE)
    } catch (e: Exception) {
        getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}