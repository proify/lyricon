package io.github.proify.lyricon.app

import android.content.Context
import android.os.Handler
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import io.github.proify.lyricon.app.util.AppLangUtils

class Application : ModuleApplication() {

    override fun attachBaseContext(base: Context) {
        unwrapContext = base
        super.attachBaseContext(AppLangUtils.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MAIN_HANDLER = Handler(mainLooper)
    }

    companion object {
        lateinit var MAIN_HANDLER: Handler
        lateinit var unwrapContext: Context
        lateinit var instance: Application
    }

}