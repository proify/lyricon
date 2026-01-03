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

package io.github.proify.lyricon.central

object Constants {

    internal const val ACTION_REGISTER_PROVIDER: String =
        "io.github.proify.lyricon.lyric.bridge.REGISTER_PROVIDER"

    internal const val ACTION_REGISTER_SUBSCRIBER: String =
        "io.github.proify.lyricon.lyric.bridge.REGISTER_SUBSCRIBER"

    internal const val ACTION_CENTRAL_BOOT_COMPLETED: String =
        "io.github.proify.lyricon.lyric.bridge.CENTRAL_BOOT_COMPLETED"

    internal const val EXTRA_BUNDLE: String = "bundle"
    internal const val EXTRA_BINDER: String = "binder"
}