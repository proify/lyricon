package io.github.proify.lyricon.app.util

import android.content.Context
import android.content.ContextWrapper
import androidx.core.content.edit
import io.github.proify.lyricon.app.GeneratedLangs
import io.github.proify.lyricon.app.getDefaultSharedPreferences
import io.github.proify.lyricon.common.util.safe
import java.util.Locale

object LocaleHelper {
    private const val KEY_LANGUAGE = "language"
    const val DEFAULT_LANGUAGE = "default"
    val DEFAULT_LOCALE = Locale.getDefault()

    fun wrap(context: Context) = wrap(context, getLanguage(context))

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

    fun getLanguage(context: Context): String =
        getDefaultSharedPreferences(context).safe().getString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
            ?: DEFAULT_LANGUAGE

    fun setLanguage(context: Context, language: String) {
        getDefaultSharedPreferences(context).edit { putString(KEY_LANGUAGE, language) }
    }

    fun getLanguages() = GeneratedLangs.LANGUAGES

}