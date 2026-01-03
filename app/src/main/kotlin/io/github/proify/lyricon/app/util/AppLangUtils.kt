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

package io.github.proify.lyricon.app.util

import android.content.Context
import android.content.ContextWrapper
import io.github.proify.lyricon.app.GeneratedLangs
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.app.util.Utils.getDefaultSharedPreferences
import io.github.proify.lyricon.common.util.safe
import java.util.Locale

object AppLangUtils {
    private const val KEY_LANGUAGE = "language"
    const val DEFAULT_LANGUAGE = "system"
    val DEFAULT_LOCALE = Locale.getDefault()

    fun wrap(context: Context) = wrap(context, getCurrentLanguage(context))

    private fun wrap(context: Context, language: String): Context {
        val locale = if (language == DEFAULT_LANGUAGE) {
            DEFAULT_LOCALE
        } else Locale.forLanguageTag(language)

        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        val newContext = context.createConfigurationContext(config)
        return ContextWrapper(newContext)
    }

    fun getCurrentLanguage(context: Context): String =
        getDefaultSharedPreferences(context).safe().getString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
            ?: DEFAULT_LANGUAGE

    fun setLanguage(context: Context, language: String) {
        getDefaultSharedPreferences(context).commitEdit { putString(KEY_LANGUAGE, language) }
    }

    fun getLanguages() = GeneratedLangs.LANGUAGES

}