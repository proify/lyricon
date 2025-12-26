package io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.sheet

import android.graphics.drawable.Drawable
import java.util.WeakHashMap

object AppCache {

    private const val MAX_CACHE_SIZE = 20 * 1024 * 1024

    private val iconCache = WeakHashMap<String, Drawable>()
    private val labelCache = WeakHashMap<String, String>()

    @Synchronized
    fun getCachedIcon(packageName: String): Drawable? {
        return iconCache[packageName]
    }

    @Synchronized
    fun cacheIcon(packageName: String, icon: Drawable) {
        iconCache.put(packageName, icon)
    }

    @Synchronized
    fun getCachedLabel(packageName: String): String? {
        return labelCache[packageName]
    }

    @Synchronized
    fun cacheLabel(packageName: String, label: String) {
        labelCache.put(packageName, label)
    }

}