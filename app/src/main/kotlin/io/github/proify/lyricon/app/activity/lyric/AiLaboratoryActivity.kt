/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.app.activity.lyric

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.compose.AppToolBarListContainer
import io.github.proify.lyricon.app.compose.IconActions
import io.github.proify.lyricon.app.util.LyricPrefs
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * AI 实验室 (AI Laboratory)
 *
 * 聚合 Lyricon 所有 AI 能力：
 * - AI 歌词翻译（整首歌级联翻译流水线配置）
 * - AI 歌词解读（点击状态栏歌词弹出的控制窗口内触发）
 *
 * 所有 AI 功能集中在此处管理，便于用户一处配置、多端生效。
 *
 * @author Tomakino
 * @since 2026
 */
class AiLaboratoryActivity : AbstractLyricActivity() {

    private val preferences by lazy { LyricPrefs.basicStylePrefs }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Content() }
    }

    @Composable
    private fun Content() {
        AppToolBarListContainer(
            title = stringResource(R.string.activity_ai_laboratory),
            canBack = true
        ) {
            item(key = "intro") {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.section_ai_laboratory),
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.item_ai_laboratory_summary),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item(key = "ai_explain") {
                SmallTitle(
                    text = stringResource(R.string.section_ai_explain),
                    insideMargin = PaddingValues(
                        start = 26.dp,
                        top = 16.dp,
                        end = 26.dp,
                        bottom = 10.dp
                    )
                )
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconActions(painterResource(R.drawable.psychology_24px))
                            Text(
                                text = stringResource(R.string.item_ai_explain),
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.item_ai_explain_summary),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item(key = "ai_translation") {
                SmallTitle(
                    text = stringResource(R.string.section_translation),
                    insideMargin = PaddingValues(
                        start = 26.dp,
                        top = 16.dp,
                        end = 26.dp,
                        bottom = 10.dp
                    )
                )
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                ) {
                    AiTranslationPreference(preferences)
                }
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
