/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.sheet

import android.graphics.drawable.Drawable
import java.util.WeakHashMap

object AppCache {
    private val iconCache = WeakHashMap<String, Drawable>()
    private val labelCache = WeakHashMap<String, String>()

    @Synchronized
    fun getCachedIcon(packageName: String): Drawable? = iconCache[packageName]

    @Synchronized
    fun cacheIcon(packageName: String, icon: Drawable) {
        iconCache.put(packageName, icon)
    }

    @Synchronized
    fun getCachedLabel(packageName: String): String? = labelCache[packageName]

    @Synchronized
    fun cacheLabel(packageName: String, label: String) {
        labelCache.put(packageName, label)
    }
}