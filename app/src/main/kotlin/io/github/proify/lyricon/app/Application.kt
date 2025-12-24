package io.github.proify.lyricon.app

import android.content.SharedPreferences
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication

class Application : ModuleApplication() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: Application
    }

    @Suppress("DEPRECATION")
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return try {
            @Suppress("DEPRECATION")
            super.getSharedPreferences(name, MODE_WORLD_READABLE)
        } catch (_: Throwable) {
            super.getSharedPreferences(name, mode)
        }
    }

}