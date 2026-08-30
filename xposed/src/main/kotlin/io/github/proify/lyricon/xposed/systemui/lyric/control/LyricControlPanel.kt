/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.control

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.SystemClock
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.setPadding
import io.github.proify.android.extensions.dp
import io.github.proify.android.extensions.sp
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.xposed.R
import io.github.proify.lyricon.xposed.systemui.lyric.control.LyricControlPanel.Companion.NO_SEEK_TARGET
import io.github.proify.lyricon.xposed.systemui.util.MediaTrackMeta
import kotlin.math.abs

/**
 * 状态栏歌词控制面板的内容视图（卡片本体）。
 *
 * 布局结构（全部代码构建，运行在 SystemUI 进程内）：
 *  [封面] [歌名/歌手 + 进度条 + 时间行]
 *  [占位] [上一首] [播放/暂停] [下一首] [AI]
 *
 * 面板持有全部子视图引用并封装更新方法（歌曲、进度、封面、背景取色），
 * 交互则通过 [ActionListener] 回抛给宿主（[LyricControlPopup]）。
 *
 * @author Tomakino
 * @since 2026
 */
@SuppressLint("ClickableViewAccessibility")
class LyricControlPanel(context: Context) : FrameLayout(context) {

    /** 面板交互回调。 */
    interface ActionListener {
        fun onPrevious()
        fun onTogglePlay()
        fun onNext()
        fun onSeekTo(position: Long)
        fun onAiExplain(button: View)
    }

    var actionListener: ActionListener? = null

    // ---- 视图（在 init 中创建并组装） ----
    private val card: LinearLayout
    private val coverView: ImageView

    /**
     * 标题视图：跑马灯专用（[MarqueeTitleView]）。
     * 弹窗是非焦点 PopupWindow（isFocusable = false），窗口与标题视图都拿不到焦点，
     * 原生 TextView 的跑马灯会因焦点缺失/窗口焦点回调而被停掉、反复重置；
     * [MarqueeTitleView] 自带帧循环滚动实现，不依赖系统跑马灯，行为稳定。
     */
    private val titleView: MarqueeTitleView
    private val artistView: TextView
    private val seekBar: SeekBar
    private val currentTimeView: TextView
    private val totalTimeView: TextView
    private val previousButton: ImageView
    private val playButton: ImageView
    private val nextButton: ImageView
    private val aiButton: ImageView

    /** 模块（宿主 App）Resources，用于加载自带图标。 */
    private val moduleRes: Resources = ControlUi.moduleResources(context)

    /** 当前歌曲时长（毫秒），进度换算的数据源。 */
    private var songDurationMs: Long = 0

    /** 用户是否正在拖动进度条（期间抑制外部进度刷新）。 */
    private var isSeeking: Boolean = false

    /** 刚体锁定：最近一次拖动释放后的 seek 目标位置（[NO_SEEK_TARGET] 表示未锁定）。 */
    private var seekTargetPosition: Long = NO_SEEK_TARGET

    /** 最近一次绑定的标题，用于识别"是否真的切歌"。 */
    private var lastBoundTitle: String? = null

    /** 刚体锁定的最长持续时间（毫秒时间戳），超时兜底恢复外部刷新。 */
    private var seekLockExpireAt: Long = 0L


    init {

        card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(CARD_PADDING_DP.dp)
            clipToOutline = true
            outlineProvider = G2RoundedCorner.outlineProvider(CARD_RADIUS_DP.dp.toFloat())
            // 参考图卡片底色：深靛蓝黑，略带半透明（保留一点点透出但不明显）
            background = ControlUi.roundedDrawable(CARD_COLOR, CARD_RADIUS_DP.dp.toFloat())
        }

        coverView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(COVER_SIZE_DP.dp, COVER_SIZE_DP.dp)
            scaleType = ImageView.ScaleType.CENTER_CROP
            clipToOutline = true
            outlineProvider = G2RoundedCorner.outlineProvider(COVER_RADIUS_DP.dp.toFloat())
            background = ControlUi.roundedDrawable(
                COVER_PLACEHOLDER_COLOR, COVER_RADIUS_DP.dp.toFloat()
            )
        }

        titleView = MarqueeTitleView(context).apply {
            textSize = TITLE_TEXT_SIZE_SP.sp
            textColor = TEXT_COLOR_PRIMARY
            textTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            // 单行、滚动与渐隐参数均由 MarqueeTitleView 内部管理
        }

        artistView = TextView(context).apply {
            textSize = ARTIST_TEXT_SIZE_SP
            setTextColor(TEXT_COLOR_SECONDARY)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }

        seekBar = SeekBar(context).apply {
            // 长圆形（胶囊）轨道：非 Material 细线，两端为半圆
            progressDrawable = longRoundedTrack()
            // 去掉拖动圆球（thumb），纯进度条；仍可点击/拖动轨道进行 seek
            thumb = null
            // 取消素材主题自带的按压圆形 ripple
            background = null
            max = SEEK_MAX
            splitTrack = false
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        currentTimeView.text = formatTime(progressToPosition(progress))
                    }
                }

                override fun onStartTrackingTouch(sb: SeekBar) {
                    isSeeking = true
                }

                override fun onStopTrackingTouch(sb: SeekBar) {
                    isSeeking = false
                    val target = progressToPosition(sb.progress)
                    // 进入刚体锁定：等待播放器上报位置接近目标（或超时）前，
                    // 抑制外部进度刷新，避免 seek 生效前被旧位置覆盖来回跳动
                    seekTargetPosition = target
                    seekLockExpireAt = SystemClock.uptimeMillis() + SEEK_LOCK_DURATION_MS
                    actionListener?.onSeekTo(target)
                }
            })
            setPadding(0)
        }

        currentTimeView = TextView(context).apply {
            textSize = TIME_TEXT_SIZE_SP
            setTextColor(TEXT_COLOR_SECONDARY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { weight = 1f }
        }

        totalTimeView = TextView(context).apply {
            textSize = TIME_TEXT_SIZE_SP
            setTextColor(TEXT_COLOR_SECONDARY)
        }

        previousButton = ImageView(context).apply {
            setImageDrawable(
                moduleRes.getDrawable(
                    R.drawable.skip_previous_fill1_24px,
                    context.theme
                )
            )
            setOnTouchListener(ControlUi.pressFeedbackListener())
            setOnClickListener { actionListener?.onPrevious() }
        }

        playButton = ImageView(context).apply {
            setImageDrawable(playPauseIcon(playing = false))
            setOnTouchListener(ControlUi.pressFeedbackListener())
            setOnClickListener { actionListener?.onTogglePlay() }
        }

        nextButton = ImageView(context).apply {
            setImageDrawable(moduleRes.getDrawable(R.drawable.skip_next_fill1_24px, context.theme))
            setOnTouchListener(ControlUi.pressFeedbackListener())
            setOnClickListener { actionListener?.onNext() }
        }

        aiButton = ImageView(context).apply {
            setImageDrawable(moduleRes.getDrawable(R.drawable.gemini_ai, context.theme))
            setOnTouchListener(ControlUi.pressFeedbackListener())
            setOnClickListener { v -> actionListener?.onAiExplain(v) }
        }

        buildInfoRow()
        buildActionRow()
        addView(
            card,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        bindSong(song = null, position = 0L)
    }

    // -------------------------------------------------------------------------
    // 生命周期
    // -------------------------------------------------------------------------

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    // -------------------------------------------------------------------------
    // 对外更新接口
    // -------------------------------------------------------------------------

    /**
     * 切换歌曲：刷新歌名/歌手/总时长与进度。
     *
     * @param systemMeta 系统媒体会话元数据，优先于 Song 的同名字段；
     *                   某个字段缺失时回退到 Song。
     */
    fun bindSong(
        song: Song?,
        position: Long,
        systemMeta: MediaTrackMeta.TrackMeta? = null
    ) {
        val title = systemMeta?.title ?: song?.name
        val artist = systemMeta?.artist ?: (song?.artist).orEmpty()
        val newDurationMs = systemMeta?.durationMs ?: song?.duration ?: 0L
        val titleText = title ?: TITLE_WHEN_NO_SONG

        // 仅真正切歌（标题或时长变化）时解除 seek 锁定；
        // 同歌元数据刷新不重置，避免打断锁定导致 seek 回跳
        if (titleText != lastBoundTitle || newDurationMs != songDurationMs) {
            seekTargetPosition = NO_SEEK_TARGET
        }
        lastBoundTitle = titleText
        songDurationMs = newDurationMs

        // 标题为自绘 View，其 text setter 内部已做"文本相同则不刷新"
        titleView.text = titleText

        setTextIfChanged(artistView, artist)
        setTextIfChanged(totalTimeView, formatTime(songDurationMs))

        updatePosition(position)
    }

    /** 刷新进度（拖动或刚体锁定期间忽略外部刷新）。 */
    fun updatePosition(position: Long) {
        if (isSeeking) return
        if (isSeekLocked(position)) return
        seekBar.progress = positionToProgress(position)
        setTextIfChanged(currentTimeView, formatTime(position))
    }

    private fun setTextIfChanged(view: TextView, text: String) {
        if (view.text.toString() != text) view.text = text
    }

    /**
     * 是否仍处于 seek 刚体锁定：播放器上报位置接近目标（容差内）或锁定超时后解锁。
     * 容差覆盖 1s 粒度的位置上报；超时兜底防止 seek 无效时 UI 长期卡在目标位置。
     */
    private fun isSeekLocked(position: Long): Boolean {
        val target = seekTargetPosition
        if (target == NO_SEEK_TARGET) return false
        val reached = abs(position - target) <= SEEK_TARGET_TOLERANCE_MS
        val timedOut = SystemClock.uptimeMillis() >= seekLockExpireAt
        if (reached || timedOut) {
            seekTargetPosition = NO_SEEK_TARGET
            return false
        }
        return true
    }

    /** 刷新播放/暂停图标。 */
    fun setPlaying(playing: Boolean) {
        playButton.setImageDrawable(playPauseIcon(playing))
    }

    /** 设置封面图片。 */
    fun setCover(bitmap: Bitmap) {
        coverView.setImageBitmap(bitmap)
    }

    /** 用封面主色的暗化色刷新卡片背景。 */
    fun setBackdropColor(color: Int) {
        card.background = ControlUi.roundedDrawable(color, CARD_RADIUS_DP.dp.toFloat())
    }

    /**
     * 长圆（胶囊）轨道 Drawable：底轨 + 已填充进度两层，两端为半圆。
     * 每层以固定高度、垂直居中放置，避免 Material 细线样式。
     */
    private fun longRoundedTrack(): Drawable {
        val h = LONG_ROUNDED_TRACK_HEIGHT_DP.dp
        val radius = h / 2f
        val track = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(SEEK_TRACK_COLOR)
            setSize(0, h)
        }
        val fill = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(TEXT_COLOR_PRIMARY)
            setSize(0, h)
        }
        val clip = ClipDrawable(fill, Gravity.START, ClipDrawable.HORIZONTAL)
        val layers = LayerDrawable(arrayOf(track, clip))
        layers.setLayerGravity(0, Gravity.CENTER_VERTICAL)
        layers.setLayerGravity(1, Gravity.CENTER_VERTICAL)
        return layers
    }

    // -------------------------------------------------------------------------
    // 布局组装
    // -------------------------------------------------------------------------

    /**
     * 歌曲信息行：封面 | 歌名/歌手 | 进度条 | 时间，全在同一水平线。
     */
    private fun buildInfoRow() {
        val titleColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(TITLE_COLUMN_LEFT_PADDING_DP.dp, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { weight = 1f }
           // clipChildren = false
            clipToPadding = false
        }
        titleColumn.addView(titleView)
        titleColumn.addView(artistView)

        titleColumn.addView(
            seekBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = SEEK_BAR_TOP_MARGIN_DP.dp }
        )

        val timeRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, TIME_ROW_TOP_PADDING_DP.dp, 0, 0)
        }
        timeRow.addView(currentTimeView)
        timeRow.addView(totalTimeView)
        titleColumn.addView(timeRow)

        val infoRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, INFO_ROW_BOTTOM_PADDING_DP.dp)
        }
        infoRow.addView(coverView)
        infoRow.addView(titleColumn)
        card.addView(infoRow)
    }

    /**
     * 操作按钮行：左边占位、上一首、播放/暂停、下一首均分宽度，
     * 右下圆形 AI 图标独立最右，与参考图布局一致。
     */
    private fun buildActionRow() {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, ACTION_ROW_TOP_PADDING_DP.dp, 0, 0)
        }
        addActionSlot(row, View(context)) // 左侧占位
        addActionSlot(row, previousButton)
        addActionSlot(row, playButton)
        addActionSlot(row, nextButton)
        addActionSlot(row, aiButton, 26.dp)

        card.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    /** 把动作按钮包进等宽的居中槽位（按钮固定 40dp）。 */
    private fun addActionSlot(
        row: LinearLayout,
        button: View,
        size: Int = ACTION_BUTTON_SIZE_DP.dp
    ) {
        val slot = FrameLayout(context).apply {
            addView(
                button,
                FrameLayout.LayoutParams(
                    size, size, Gravity.CENTER
                )
            )
        }
        row.addView(
            slot,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                weight = 1f
            }
        )
    }

    // -------------------------------------------------------------------------
    // 图标 / 进度与时间换算
    // -------------------------------------------------------------------------

    private fun playPauseIcon(playing: Boolean): Drawable {
        val iconId = if (playing) R.drawable.pause_fill1_24px
        else R.drawable.play_arrow_fill1_24px
        return moduleRes.getDrawable(iconId, context.theme)
    }


    private fun positionToProgress(position: Long): Int {
        if (songDurationMs <= 0) return 0
        return ((position * SEEK_MAX) / songDurationMs).toInt().coerceIn(0, SEEK_MAX)
    }

    private fun progressToPosition(progress: Int): Long = songDurationMs * progress / SEEK_MAX

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return "%d:%02d".format(minutes, seconds)
    }

    // -------------------------------------------------------------------------
    // 常量
    // -------------------------------------------------------------------------

    private companion object {
        const val CARD_RADIUS_DP = 28
        const val COVER_RADIUS_DP = 16
        const val COVER_SIZE_DP = 72
        const val CARD_PADDING_DP = 16

        const val INFO_ROW_BOTTOM_PADDING_DP = 10
        const val TITLE_COLUMN_LEFT_PADDING_DP = 10
        const val SEEK_BAR_TOP_MARGIN_DP = 5
        const val TIME_ROW_TOP_PADDING_DP = 5
        const val ACTION_ROW_TOP_PADDING_DP = 0
        const val ACTION_BUTTON_SIZE_DP = 36
        const val SEEK_MAX = 1000

        /** 长圆（胶囊）进度轨道高度（dp）。 */
        const val LONG_ROUNDED_TRACK_HEIGHT_DP = 5

        /** 无 seek 锁定目标。 */
        const val NO_SEEK_TARGET = Long.MIN_VALUE

        /** 刚体锁定最长持续时间（毫秒）。 */
        const val SEEK_LOCK_DURATION_MS = 1200L

        /** 判定"已到达 seek 目标"的容差（毫秒，覆盖 1s 位置上报粒度）。 */
        const val SEEK_TARGET_TOLERANCE_MS = 2000L

        const val TITLE_TEXT_SIZE_SP = 17f
        const val ARTIST_TEXT_SIZE_SP = 14f
        const val TIME_TEXT_SIZE_SP = 11f

        const val TITLE_WHEN_NO_SONG = "未在播放"
        val CARD_COLOR = Color.argb(244, 28, 32, 46)
        val COVER_PLACEHOLDER_COLOR = Color.argb(70, 255, 255, 255)
        val TEXT_COLOR_PRIMARY = Color.WHITE
        val TEXT_COLOR_SECONDARY = Color.argb(170, 255, 255, 255)
        val SEEK_TRACK_COLOR = Color.argb(60, 255, 255, 255)
    }
}
