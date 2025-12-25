package io.github.proify.lyricon.app.ui.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.proify.lyricon.app.Application
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.AppToolBarListContainer
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.IconActions
import io.github.proify.lyricon.app.util.LocaleHelper
import top.yukonga.miuix.kmp.extra.SuperDropdown
import java.util.Locale

class SettingsActivity : BaseActivity() {

    companion object {
        private var changedLanguage = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBackPressHandler()
        setContent { Content() }
    }

    /**
     * 配置返回按钮处理逻辑
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (changedLanguage) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    @Composable
    private fun Content() {
        AppToolBarListContainer(
            title = stringResource(id = R.string.activity_settings),
            canBack = true
        ) { scope ->
            scope.item("settings") {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                ) {
                    LanguageSelector()
                }
            }
        }
    }

    @Composable
    private fun LanguageSelector() {
        val languages = LocaleHelper.getLanguages()
        val currentLanguage = LocaleHelper.getLanguage(this)

        val languageOptions = remember(languages) {
            buildLanguageOptions(languages)
        }

        var selectedIndex by remember(currentLanguage) {
            mutableIntStateOf(languages.indexOf(currentLanguage).coerceAtLeast(0))
        }

        SuperDropdown(
            leftAction = {
                IconActions(painterResource(R.drawable.ic_language))
            },
            title = stringResource(id = R.string.item_app_language),
            items = languageOptions.map { it.second },
            selectedIndex = selectedIndex,
            onSelectedIndexChange = { newIndex ->
                if (newIndex != selectedIndex) {
                    selectedIndex = newIndex
                    handleLanguageChange(languages[newIndex])
                }
            }
        )
    }

    private fun buildLanguageOptions(languages: List<String>): List<Pair<String, String>> {
        return languages.map { languageCode ->
            languageCode to getLanguageDisplayName(languageCode)
        }
    }

    private fun getLanguageDisplayName(languageCode: String): String {

        return when (languageCode) {
            LocaleHelper.DEFAULT_LANGUAGE -> Application.unwrapContext.getString(R.string.language_system_default)
            else -> runCatching {
                val locale = Locale.forLanguageTag(languageCode)
                locale.getDisplayName(LocaleHelper.DEFAULT_LOCALE).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                }
            }.getOrElse { languageCode }
        }
    }

    private fun handleLanguageChange(newLanguage: String) {
        LocaleHelper.setLanguage(this, newLanguage)
        changedLanguage = true
        recreate()
    }

    @Preview(showBackground = true)
    @Composable
    private fun ContentPreview() {
        Content()
    }
}