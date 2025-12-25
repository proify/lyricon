package io.github.proify.lyricon.app

import android.content.Context
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import io.github.proify.lyricon.app.util.LocaleHelper

class Application : ModuleApplication() {

    override fun attachBaseContext(base: Context) {
        unwrapContext = base
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var unwrapContext: Context
        lateinit var instance: Application
    }

}