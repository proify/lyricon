/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.proify.lyricon.app.util

import android.content.Context
import android.content.ContextWrapper
import androidx.core.os.LocaleListCompat
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
        } else try {
            Locale.forLanguageTag(language)
        } catch (e: Exception) {
            DEFAULT_LOCALE
        }

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
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
        getDefaultSharedPreferences(context).commitEdit { putString(KEY_LANGUAGE, language) }
    }

    fun getLanguages() = GeneratedLangs.LANGUAGES

}