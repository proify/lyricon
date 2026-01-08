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
    const val DEFAULT_LANGUAGE: String = "system"
    val DEFAULT_LOCALE: Locale = Locale.getDefault()

    fun wrapContext(context: Context): Context =
        wrapContext(context, getCustomizeLang(context))

    fun setDefaultLocale(context: Context) {
        val language = getCustomizeLang(context)
        val locale = forLanguageTag(language)
        Locale.setDefault(locale ?: DEFAULT_LOCALE)
    }

    fun forLanguageTag(language: String): Locale? {
        return if (language == DEFAULT_LANGUAGE) {
            DEFAULT_LOCALE
        } else runCatching {
            Locale.forLanguageTag(language)
        }.getOrNull()
    }

    fun wrapContext(context: Context, language: String): Context {
        val locale = forLanguageTag(language) ?: return context

        val config = context.resources.configuration
        config.setLocale(locale)

        val newContext = context.createConfigurationContext(config)
        return Cold(newContext)
    }

    fun getCustomizeLang(context: Context): String =
        getDefaultSharedPreferences(context)
            .safe()
            .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
            ?: DEFAULT_LANGUAGE

    fun saveCustomizeLanguage(context: Context, language: String) {
        val locale = forLanguageTag(language)
        getDefaultSharedPreferences(context)
            .commitEdit {
                if (locale != null) {
                    putString(KEY_LANGUAGE, language)
                } else {
                    remove(KEY_LANGUAGE)
                }
            }
    }

    fun getLanguages(): List<String> = GeneratedLangs.LANGUAGES

    class Cold(base: Context) : ContextWrapper(base)
}