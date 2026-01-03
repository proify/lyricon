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