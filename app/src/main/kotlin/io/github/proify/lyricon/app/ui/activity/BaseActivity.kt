package io.github.proify.lyricon.app.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import io.github.proify.lyricon.app.util.AppLangUtils

open class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLangUtils.wrap(base))
    }
}