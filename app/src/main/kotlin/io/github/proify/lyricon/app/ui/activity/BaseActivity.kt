package io.github.proify.lyricon.app.ui.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

open class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    @Deprecated("Deprecated")
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences? {
        return try {
            @Suppress("DEPRECATION")
            super.getSharedPreferences(name, MODE_WORLD_READABLE)
        } catch (_: Throwable) {
            super.getSharedPreferences(name, mode)
        }
    }

}