/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.app.activity.lyric

import android.content.SharedPreferences
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.proify.android.extensions.json
import io.github.proify.lyricon.app.LyriconApp
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.compose.IconActions
import io.github.proify.lyricon.app.compose.custom.miuix.extra.WindowDialog
import io.github.proify.lyricon.app.compose.custom.miuix.preference.CheckboxPreference
import io.github.proify.lyricon.app.compose.preference.DoubleInputPreference
import io.github.proify.lyricon.app.compose.preference.IntInputPreference
import io.github.proify.lyricon.app.compose.preference.StringInputPreference
import io.github.proify.lyricon.app.compose.preference.rememberStringPreference
import io.github.proify.lyricon.app.util.toast
import io.github.proify.lyricon.lyric.ai.core.AiConfig
import io.github.proify.lyricon.lyric.ai.core.AiConfig.Companion.KEY_AI_CONFIG_API_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.window.WindowBottomSheet

/**
 * 统一 AI 基础配置：所有 AI 功能（翻译、解读）共用的连接配置。
 *
 * @author Tomakino
 * @since 2026
 */
@Composable
fun AiConfigPreference(preferences: SharedPreferences) {
    StringInputPreference(
        preferences = preferences,
        key = AiConfig.KEY_AI_CONFIG_BASE_URL,
        title = stringResource(R.string.item_translation_base_url),
        dialogSummary = stringResource(R.string.dialog_summary_translation_base_url),
        defaultValue = AiConfig.AI_TRANSLATION_HOST,
        startAction = { IconActions(painterResource(R.drawable.link_24px)) },
        maxLines = 1
    )

    AiConfigApiKeyPreference(preferences)
    AiConfigModelPreference(preferences)
    AiConfigAdvancedOptionsPreference(preferences)
}

@Composable
private fun AiConfigApiKeyPreference(preferences: SharedPreferences) {
    val apiKey = rememberStringPreference(preferences, KEY_AI_CONFIG_API_KEY, null)
    val summary =
        if (apiKey.value.isNullOrBlank()) {
            stringResource(R.string.item_translation_api_key_not_set)
        } else {
            stringResource(R.string.item_translation_api_key_set)
        }

    StringInputPreference(
        preferences = preferences,
        key = KEY_AI_CONFIG_API_KEY,
        title = stringResource(R.string.item_translation_api_key),
        summary = summary,
        dialogSummary = stringResource(R.string.dialog_summary_translation_api_key),
        startAction = { IconActions(painterResource(R.drawable.vpn_key_24px)) },
        maxLines = 1
    )
}

@Composable
private fun AiConfigAdvancedOptionsPreference(preferences: SharedPreferences) {
    var showSheet by remember { mutableStateOf(false) }

    ArrowPreference(
        title = stringResource(R.string.item_translation_advanced_options),
        summary = stringResource(R.string.item_translation_advanced_options_summary),
        startAction = { IconActions(painterResource(R.drawable.more_horiz_24px)) },
        holdDownState = showSheet,
        onClick = { showSheet = true }
    )

    if (showSheet) {
        OverlayBottomSheet(
            show = showSheet,
            title = stringResource(R.string.item_translation_advanced_options),
            onDismissRequest = { showSheet = false },
            backgroundColor = MiuixTheme.colorScheme.surface,
            insideMargin = DpSize(0.dp, 0.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .overScrollVertical()
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
                            .fillMaxWidth(),
                    ) {
                        DoubleInputPreference(
                            preferences = preferences,
                            key = AiConfig.KEY_AI_CONFIG_TEMPERATURE,
                            title = stringResource(R.string.item_translation_temperature),
                            dialogSummary = stringResource(R.string.dialog_summary_translation_temperature),
                            defaultValue = AiConfig.AI_TRANSLATION_TEMPERATURE.toDouble(),
                            range = 0.0..2.0,
                            startAction = { IconActions(painterResource(R.drawable.device_thermostat_24px)) },
                        )

                        DoubleInputPreference(
                            preferences = preferences,
                            key = AiConfig.KEY_AI_CONFIG_TOP_P,
                            title = stringResource(R.string.item_translation_top_p),
                            dialogSummary = stringResource(R.string.dialog_summary_translation_top_p),
                            defaultValue = AiConfig.AI_TRANSLATION_TOP_P.toDouble(),
                            range = 0.0..1.0,
                            startAction = { IconActions(painterResource(R.drawable.discover_tune_24px)) },
                        )

                        IntInputPreference(
                            preferences = preferences,
                            key = AiConfig.KEY_AI_CONFIG_MAX_TOKENS,
                            title = stringResource(R.string.item_translation_max_tokens),
                            dialogSummary = stringResource(R.string.dialog_summary_translation_max_tokens),
                            defaultValue = AiConfig.AI_TRANSLATION_MAX_TOKENS,
                            range = 0..200000,
                            summary = {
                                if (it == 0) {
                                    stringResource(R.string.item_translation_max_tokens_default)
                                } else {
                                    null
                                }
                            },
                            startAction = { IconActions(painterResource(R.drawable.token_24px)) },
                        )

                        DoubleInputPreference(
                            preferences = preferences,
                            key = AiConfig.KEY_AI_CONFIG_PRESENCE_PENALTY,
                            title = stringResource(R.string.item_translation_presence_penalty),
                            dialogSummary = stringResource(R.string.dialog_summary_translation_presence_penalty),
                            defaultValue = AiConfig.AI_TRANSLATION_PRESENCE_PENALTY.toDouble(),
                            range = -2.0..2.0,
                            startAction = { IconActions(painterResource(R.drawable.do_not_disturb_on_24px)) },
                        )

                        DoubleInputPreference(
                            preferences = preferences,
                            key = AiConfig.KEY_AI_CONFIG_FREQUENCY_PENALTY,
                            title = stringResource(R.string.item_translation_frequency_penalty),
                            dialogSummary = stringResource(R.string.dialog_summary_translation_frequency_penalty),
                            defaultValue = AiConfig.AI_TRANSLATION_FREQUENCY_PENALTY.toDouble(),
                            range = -2.0..2.0,
                            startAction = { IconActions(painterResource(R.drawable.lightbulb_2_24px)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiConfigModelPreference(preferences: SharedPreferences) {
    val preferenceKey = AiConfig.KEY_AI_CONFIG_MODEL
    val defaultModel = AiConfig.AI_TRANSLATION_MODEL
    var modelPreference by rememberStringPreference(preferences, preferenceKey, defaultModel)
    val currentModel = modelPreference ?: defaultModel
    var models by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val title = stringResource(R.string.item_translation_model)
    val apiKeyNotSetMessage = stringResource(R.string.item_translation_model_api_key_not_set)
    val noModelsMessage = stringResource(R.string.item_translation_model_empty)
    val unknownErrorMessage = stringResource(R.string.unknown)

    val showMsgDialog = remember { mutableStateOf(false) }
    var msgDialogTitle by remember { mutableStateOf("") }
    var msgDialogSummary by remember { mutableStateOf("") }

    @Composable
    fun MessageDialog(
        show: MutableState<Boolean>,
        title: String,
        summary: String,
    ) {
        WindowDialog(
            title = title,
            summary = summary,
            show = show.value,
            onDismissRequest = { show.value = false }
        ) {
            val dismiss = LocalDismissState.current
            TextButton(
                text = stringResource(R.string.ok),
                onClick = { dismiss?.invoke() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
    MessageDialog(showMsgDialog, msgDialogTitle, msgDialogSummary)

    StringInputPreference(
        preferences = preferences,
        key = preferenceKey,
        title = title,
        dialogSummary = stringResource(R.string.dialog_summary_translation_model),
        defaultValue = defaultModel,
        startAction = { IconActions(painterResource(R.drawable.psychology_24px)) },
        maxLines = 1,
        endActions = {
            IconButton(
                onClick = {
                    if (isLoading) return@IconButton

                    val apiKey = preferences.getString(KEY_AI_CONFIG_API_KEY, null)
                    if (apiKey.isNullOrBlank()) {
                        toast(apiKeyNotSetMessage)
                        return@IconButton
                    }

                    val baseUrl = preferences.getString(
                        AiConfig.KEY_AI_CONFIG_BASE_URL,
                        AiConfig.AI_TRANSLATION_HOST
                    ).orEmpty()

                    isLoading = true
                    coroutineScope.launch {
                        val result = fetchOpenAiModels(baseUrl, apiKey)
                        isLoading = false

                        result.onSuccess { fetchedModels ->
                            if (fetchedModels.isEmpty()) {
                                toast(noModelsMessage)
                            } else {
                                models = (fetchedModels + currentModel)
                                    .filter { it.isNotBlank() }
                                    .distinct()
                                showDialog = true
                            }
                        }.onFailure { error ->
                            val context = LyriconApp.get()

                            msgDialogTitle =
                                context.getString(R.string.title_translation_model_load_failed)
                            msgDialogSummary = error.message ?: unknownErrorMessage
                            showMsgDialog.value = true
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = MiuixIcons.Search,
                        contentDescription = null
                    )
                }
            }
        }
    )

    if (showDialog) {
        WindowBottomSheet(
            show = showDialog,
            title = stringResource(R.string.dialog_title_available_models),
            onDismissRequest = { showDialog = false },
            backgroundColor = MiuixTheme.colorScheme.surface,
            insideMargin = DpSize(0.dp, 0.dp),
        ) {

            val dismiss = LocalDismissState.current
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .overScrollVertical()
            ) {
                itemsIndexed(
                    items = models,
                    key = { _, it -> it }
                ) { _, model ->

                    Card(
                        modifier =
                            Modifier
                                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
                                .fillMaxWidth()
                    ) {
                        CheckboxPreference(
                            title = model,
                            checked = currentModel == model,
                            onCheckedChange = {
                                modelPreference = model
                                dismiss?.invoke()
                            }
                        )
                    }
                }
            }
        }
    }
}

private val modelsHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
}

private suspend fun fetchOpenAiModels(
    baseUrl: String,
    apiKey: String
): Result<List<String>> = withContext(Dispatchers.IO) {
    runCatching {
        val request = Request.Builder()
            .url(buildOpenAiModelsUrl(baseUrl))
            .header("Authorization", "Bearer $apiKey")
            .get()
            .build()

        modelsHttpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("HTTP ${response.code} ${body.take(160)}".trim())
            }

            json.decodeFromString<OpenAiModelsResponse>(body)
                .data
                .map { it.id }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
    }
}

private fun buildOpenAiModelsUrl(baseUrl: String): String {
    val trimmedUrl = baseUrl.trim().removeSuffix("/")
    val normalizedBaseUrl = when {
        trimmedUrl.endsWith("/models") -> return trimmedUrl
        trimmedUrl.endsWith("/chat/completions") -> trimmedUrl.removeSuffix("/chat/completions")
        trimmedUrl.isBlank() -> AiConfig.AI_TRANSLATION_HOST.removeSuffix("/")
        else -> trimmedUrl
    }

    return "$normalizedBaseUrl/models"
}

@Serializable
private data class OpenAiModelsResponse(
    val data: List<OpenAiModel> = emptyList()
)

@Serializable
private data class OpenAiModel(
    val id: String = ""
)
