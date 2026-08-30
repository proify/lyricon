/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 歌词翻译的 wire 模型（与 LLM 的 JSON 协议一一对应）。
 *
 * 序列化字段名受提示词协议约束，请勿改动：
 * 请求 `{"lyrics":[{"index":0,"src":"..."}]}`，响应 `{"translated":[{"index":0,"tran":"..."}]}`。
 */

@Serializable
data class TranslationRequestItem(
    val index: Int,
    @SerialName("src")
    val text: String
)

@Serializable
data class TranslationRequest(
    val lyrics: List<TranslationRequestItem>
)

@Serializable
data class TranslationItem(
    val index: Int,
    @SerialName("tran")
    val translation: String
)

@Serializable
data class TranslationResponse(
    val translated: List<TranslationItem>
)
