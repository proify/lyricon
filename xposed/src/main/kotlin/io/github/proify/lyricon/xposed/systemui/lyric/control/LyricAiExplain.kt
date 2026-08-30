/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.control

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import io.github.proify.android.extensions.dp
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.xposed.logger.YLog
import io.github.proify.lyricon.xposed.systemui.aitrans.AiLyricExplainer
import io.github.proify.lyricon.xposed.systemui.lyric.LyricPrefs
import io.github.proify.lyricon.xposed.systemui.lyric.LyricViewController
import io.github.proify.lyricon.xposed.systemui.util.MediaTrackMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * AI 歌词解释（AI Lyric Explain）
 *
 * 由控制窗口右下角的 AI 按钮触发：
 * 1. 抽取当前歌词及其上下若干行作为上下文；
 * 2. 调用 [AiLyricExplainer]（复用 [LyricPrefs] 中的 AI 配置）；
 * 3. 用 PopupWindow 展示解读结果——SystemUI 进程没有 Activity 的 window token，
 *    Dialog.show() 会抛 BadTokenException，PopupWindow 锚定到状态栏视图即可正常显示。
 *
 * @author Tomakino
 * @since 2026
 */
object LyricAiExplain {

    private const val TAG = "LyricAiExplain"
    private const val MAX_CONTEXT_LINES = 6

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** 当前显示中的解读弹窗。 */
    private var explanationPopup: PopupWindow? = null

    /**
     * 启动一次"解释歌词"流程。
     *
     * @param anchor 弹窗锚点（状态栏歌词视图）
     * @param button 触发按钮（请求期间置为忙碌态）
     * @param song 当前歌曲；无歌词时直接忽略
     */
    fun launch(anchor: View, button: View, song: Song?) {
        val context = button.context
        val lyrics = extractContextLyrics(song)
        if (lyrics.isBlank()) {
            YLog.info(TAG, "AI explain skipped: no lyric context.")
            return
        }

        // 按钮为 ImageView，无法改文字；通过禁用+降低透明度表达"思考中"
        button.isEnabled = false
        button.alpha = BUSY_ALPHA

        scope.launch {
            val configs = LyricPrefs.getLyricStyle().basicStyle.aiTranslationConfigs
            if (configs == null || !configs.isUsable) {
                YLog.warning(TAG, "AI config unusable. Please configure AI in the app.")
                restoreButton(button)
                toast(context, "请在「AI 实验室」中配置好 API 后再试")
                return@launch
            }

            val result = AiLyricExplainer.explain(configs, mergeSystemMeta(song), lyrics)
            restoreButton(button)
            showExplanation(anchor, result)
        }
    }

    /** 关闭解读弹窗（控制窗口关闭时调用）。 */
    fun dismiss() {
        explanationPopup?.let { if (it.isShowing) it.dismiss() }
        explanationPopup = null
    }

    /**
     * 用系统媒体会话元数据覆盖 Song 的标题/歌手（仅用于 AI 请求的元数据，
     * 歌词行不参与合并）。系统元数据优先，缺失时保留 Song 原值。
     */
    private fun mergeSystemMeta(song: Song?): Song? {
        val song = song ?: return null
        val meta = MediaTrackMeta.resolve(LyricViewController.activePackage) ?: return song
        val title = meta.title ?: song.name
        val artist = meta.artist ?: song.artist
        if (title == song.name && artist == song.artist) return song
        return song.copy(name = title, artist = artist)
    }

    // -------------------------------------------------------------------------
    // 上下文抽取
    // -------------------------------------------------------------------------

    /**
     * 抽取当前歌词及其上下若干行，作为 AI 解读的上下文。
     */
    private fun extractContextLyrics(song: Song?): String {
        val song = song ?: return ""
        val lyrics = song.lyrics.orEmpty()
        if (lyrics.isEmpty()) return ""

        val position = LyricViewController.currentLogicPosition
        val index = lyrics.indexOfFirst { it.begin <= position && position < it.end }
            .let { if (it >= 0) it else lyrics.indexOfLast { it.begin <= position } }
        val center = index.coerceAtLeast(0)
        val start = (center - MAX_CONTEXT_LINES / 2).coerceAtLeast(0)
        val end = (start + MAX_CONTEXT_LINES).coerceAtMost(lyrics.size)

        return (start until end)
            .map { lyrics[it].text.orEmpty() }
            .filter { it.isNotBlank() }
            .joinToString("\n") { "- $it" }
            .also { YLog.info(TAG, "AI explain context: $it") }
    }

    // -------------------------------------------------------------------------
    // 解读弹窗
    // -------------------------------------------------------------------------

    private fun showExplanation(anchor: View, result: String?) {
        val context = anchor.context
        if (result == null) {
            toast(context, "AI 解释失败，请检查网络与配置")
            return
        }
        dismiss()

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 24.dp, 24.dp, 20.dp)
            background = ControlUi.roundedDrawable(
                color = Color.argb(244, 30, 30, 32),
                radius = 26.dp.toFloat()
            )
        }

        content.addView(
            TextView(context).apply {
                text = "歌词解读"
                textSize = 16f
                setTextColor(Color.WHITE)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setPadding(0, 0, 0, 12.dp)
            }
        )

        val resultView = TextView(context).apply {
            text = result
            textSize = 14f
            setTextColor(Color.argb(235, 255, 255, 255))
            setLineSpacing(0f, 1.25f)
            setPadding(0, 0, 0, 4.dp)
        }

        val metrics = context.resources.displayMetrics
        val maxHeight = (metrics.heightPixels * 0.55f).roundToInt()
        val width = (metrics.widthPixels * 0.86f).roundToInt().coerceIn(280.dp, 520.dp)

        val scroll = ScrollView(context).apply {
            isFillViewport = false
            addView(
                resultView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        if (maxHeight > 0) {
            scroll.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                maxHeight
            )
        }
        content.addView(scroll)

        val window = PopupWindow(content, width, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            isFocusable = true
            isOutsideTouchable = true
            elevation = 24.dp.toFloat()
            setBackgroundDrawable(null)
        }
        window.setOnDismissListener { explanationPopup = null }
        explanationPopup = window

        window.showAtLocation(anchor, Gravity.CENTER, 0, 0)
    }

    // -------------------------------------------------------------------------
    // 工具
    // -------------------------------------------------------------------------

    private fun restoreButton(button: View) {
        button.isEnabled = true
        button.alpha = 1f
    }

    private fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private const val BUSY_ALPHA = 0.5f
}
