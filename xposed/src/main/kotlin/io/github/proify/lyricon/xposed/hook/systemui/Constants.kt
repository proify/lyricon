package io.github.proify.lyricon.xposed.hook.systemui

import android.content.Context

object Constants {

    var statusBarLayoutId: Int = 0
    var clockId: Int = 0

    fun initResourceIds(appContext: Context) {
        val resources = appContext.resources
        statusBarLayoutId =
            resources.getIdentifier("status_bar", "layout", appContext.packageName)
        clockId =
            resources.getIdentifier("clock", "id", appContext.packageName)
    }
}