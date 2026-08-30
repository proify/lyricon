/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.lyric.ai.core

import android.content.SharedPreferences
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 支持的 AI 服务提供商（OpenAI 兼容接口）。
 *
 * 每个提供商天然携带一组默认的连接参数（模型 / Base URL），
 * 用户可在「AI 实验室」中覆盖。仅被 [AiConfig] 使用，故与配置同置。
 */
enum class AiProvider(val provider: String, val model: String, val url: String) {
    OPENAI(
        "openai",
        "gpt-4o-mini",
        "https://api.openai.com/v1"
    ),
}

/**
 * 统一 AI 连接配置：所有 AI 功能（歌词翻译、音乐解读）共用的 OpenAI 兼容服务连接信息。
 *
 * 只包含"如何连上服务"的参数（provider / baseUrl / apiKey / model / 采样参数），
 * 不含任何功能语义——目标语言、风格要求等业务参数由各功能自行管理。
 */
@Serializable
@Parcelize
data class AiConfig(
    val provider: String? = null,
    val apiKey: String? = null,
    val model: String? = null,
    val baseUrl: String? = null,
    val temperature: Float = DEFAULT_TEMPERATURE,
    val topP: Float = DEFAULT_TOP_P,
    val maxTokens: Int = DEFAULT_MAX_TOKENS,
    val presencePenalty: Float = DEFAULT_PRESENCE_PENALTY,
    val frequencyPenalty: Float = DEFAULT_FREQUENCY_PENALTY
) : Parcelable {

    /** 连接配置是否可用（能否发起请求）。 */
    @IgnoredOnParcel
    val isUsable by lazy {
        !provider.isNullOrBlank()
                && !apiKey.isNullOrBlank()
                && !model.isNullOrBlank()
                && !baseUrl.isNullOrBlank()
    }

    override fun toString(): String {
        return "AiConfig(baseUrl=$baseUrl, provider=$provider, apiKey=${
            apiKey.orEmpty().take(6)
        }..., model=$model temperature=$temperature topP=$topP maxTokens=$maxTokens, isUsable=$isUsable)"
    }

    companion object {
        const val DEFAULT_TEMPERATURE = 0.7f
        const val DEFAULT_TOP_P = 1.0f
        const val DEFAULT_MAX_TOKENS = 0
        const val DEFAULT_PRESENCE_PENALTY = 0.3f
        const val DEFAULT_FREQUENCY_PENALTY = 0.3f

        // ===== 连接配置 pref keys（所有 AI 功能共用）=====
        const val KEY_AI_CONFIG_PROVIDER = "ai_config_provider"
        const val KEY_AI_CONFIG_API_KEY = "ai_config_api_key"
        const val KEY_AI_CONFIG_BASE_URL = "ai_config_base_url"
        const val KEY_AI_CONFIG_MODEL = "ai_config_model"
        const val KEY_AI_CONFIG_TEMPERATURE = "ai_config_temperature"
        const val KEY_AI_CONFIG_TOP_P = "ai_config_top_p"
        const val KEY_AI_CONFIG_MAX_TOKENS = "ai_config_max_tokens"
        const val KEY_AI_CONFIG_PRESENCE_PENALTY = "ai_config_presence_penalty"
        const val KEY_AI_CONFIG_FREQUENCY_PENALTY = "ai_config_frequency_penalty"

        // ===== 连接默认值 =====
        val AI_TRANSLATION_PROVIDER: String = AiProvider.OPENAI.provider

        val AI_TRANSLATION_HOST: String by lazy {
            AiProvider.entries.find {
                it.provider == AI_TRANSLATION_PROVIDER
            }?.url.orEmpty()
        }

        val AI_TRANSLATION_MODEL: String = AiProvider.OPENAI.model
        const val AI_TRANSLATION_TEMPERATURE = DEFAULT_TEMPERATURE
        const val AI_TRANSLATION_TOP_P = DEFAULT_TOP_P
        const val AI_TRANSLATION_MAX_TOKENS = DEFAULT_MAX_TOKENS
        const val AI_TRANSLATION_PRESENCE_PENALTY = DEFAULT_PRESENCE_PENALTY
        const val AI_TRANSLATION_FREQUENCY_PENALTY = DEFAULT_FREQUENCY_PENALTY

        /**
         * 从 [SharedPreferences] 读取统一的 AI 连接配置。
         *
         * 所有 AI 功能共用这一读取入口，保证"一份配置、多端生效"。
         */
        fun fromPreferences(preferences: SharedPreferences): AiConfig {
            val providerName =
                preferences.getString(KEY_AI_CONFIG_PROVIDER, AI_TRANSLATION_PROVIDER)
            val provider = AiProvider.entries.firstOrNull {
                it.name.equals(providerName, ignoreCase = true)
            }
            // 服务商名不在枚举（自定义/私有 OpenAI 兼容服务）时保留原始名称，
            // 确保连接仍可用；枚举命中则用规范名。
            val resolvedProvider = provider?.name ?: providerName

            return AiConfig(
                provider = resolvedProvider,
                apiKey = preferences.getString(KEY_AI_CONFIG_API_KEY, null),
                model = preferences.getString(KEY_AI_CONFIG_MODEL, provider?.model),
                baseUrl = preferences.getString(KEY_AI_CONFIG_BASE_URL, provider?.url),
                temperature = preferences.floatValue(
                    KEY_AI_CONFIG_TEMPERATURE,
                    AI_TRANSLATION_TEMPERATURE
                ),
                topP = preferences.floatValue(
                    KEY_AI_CONFIG_TOP_P,
                    AI_TRANSLATION_TOP_P
                ),
                maxTokens = preferences.intValue(
                    KEY_AI_CONFIG_MAX_TOKENS,
                    AI_TRANSLATION_MAX_TOKENS
                ),
                presencePenalty = preferences.floatValue(
                    KEY_AI_CONFIG_PRESENCE_PENALTY,
                    AI_TRANSLATION_PRESENCE_PENALTY
                ),
                frequencyPenalty = preferences.floatValue(
                    KEY_AI_CONFIG_FREQUENCY_PENALTY,
                    AI_TRANSLATION_FREQUENCY_PENALTY
                )
            )
        }

        /** 将统一的 AI 连接配置写回 [SharedPreferences]。 */
        fun writeTo(editor: SharedPreferences.Editor, configs: AiConfig) {
            editor.putString(KEY_AI_CONFIG_PROVIDER, configs.provider)
            editor.putString(KEY_AI_CONFIG_MODEL, configs.model)
            editor.putString(KEY_AI_CONFIG_BASE_URL, configs.baseUrl)
            editor.putFloat(KEY_AI_CONFIG_TEMPERATURE, configs.temperature)
            editor.putFloat(KEY_AI_CONFIG_TOP_P, configs.topP)
            editor.putInt(KEY_AI_CONFIG_MAX_TOKENS, configs.maxTokens)
            editor.putFloat(KEY_AI_CONFIG_PRESENCE_PENALTY, configs.presencePenalty)
            editor.putFloat(KEY_AI_CONFIG_FREQUENCY_PENALTY, configs.frequencyPenalty)
        }

        // 同一 key 可能被不同写入方以 String/数值两种类型写入，读取时统一容忍处理。
        private fun SharedPreferences.floatValue(key: String, defaultValue: Float): Float {
            return when (val value = all[key]) {
                is Float -> value
                is String -> value.toFloatOrNull() ?: defaultValue
                is Int -> value.toFloat()
                is Long -> value.toFloat()
                is Double -> value.toFloat()
                else -> defaultValue
            }
        }

        private fun SharedPreferences.intValue(key: String, defaultValue: Int): Int {
            return when (val value = all[key]) {
                is Int -> value
                is String -> value.toIntOrNull() ?: defaultValue
                is Long -> value.toInt()
                is Float -> value.toInt()
                is Double -> value.toInt()
                else -> defaultValue
            }
        }
    }
}
