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
@file:Suppress("unused")

package io.github.proify.lyricon.lyric.view.line

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.TextPaint
import android.view.animation.Interpolator
import androidx.core.graphics.withSave
import io.github.proify.lyricon.lyric.model.extensions.filterByPosition
import io.github.proify.lyricon.lyric.view.LyricPlayListener
import io.github.proify.lyricon.lyric.view.util.Interpolators
import kotlin.math.max

/**
 * 歌词音节控制器
 *
 * - [HighlightAnimator] 管理高亮进度动画
 * - [DropAnimationManager] 管理字符下落动画
 * - [ScrollController] 管理歌词滚动
 */
internal class Syllable(val view: LyricLineView) {
    val backgroundPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    val highlightPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)

    var lyricPlayListener: LyricPlayListener? = null

    private val highlightAnimator = HighlightAnimator()
    private val dropAnimationManager = DropAnimationManager()
    private val scrollController = ScrollController()
    private val renderer = LyricRenderer()

    fun isPlayStarted() = highlightAnimator.isStarted
    fun isPlaying() = highlightAnimator.isPlaying
    fun isPlayFinished() = highlightAnimator.isFinished

    fun reset() {
        highlightAnimator.reset()
        dropAnimationManager.reset()
        scrollController.reset()
        view.scrollXOffset = 0f
    }

    fun updateProgress(position: Long) {
        val lyricModel = view.lyricModel
        val words = lyricModel.words
        val current = words.filterByPosition(position).firstOrNull()

        val targetWidth = when {
            current != null -> current.endPosition
            position >= lyricModel.end -> view.lyricWidth
            position <= lyricModel.begin -> 0f
            else -> highlightAnimator.currentWidth
        }

        // 处理初始定位
        if (current != null && highlightAnimator.currentWidth == 0f) {
            current.previous?.let {
                highlightAnimator.jumpTo(it.endPosition)
                scrollController.updateScroll(it.endPosition, view)
            }
        }

        if (targetWidth != highlightAnimator.targetWidth) {
            val start = current?.begin ?: 0
            val end = current?.end ?: 0
            val duration = end - start
            highlightAnimator.animateTo(targetWidth, duration)
        }
    }

    fun updateFrame(frameTimeNanos: Long): Boolean {
        val highlightChanged = highlightAnimator.update(frameTimeNanos)

        if (highlightChanged) {
            // 高亮位置变化时，通知下落动画管理器和滚动控制器
            val currentWidth = highlightAnimator.currentWidth
            dropAnimationManager.checkTriggers(currentWidth, view.lyricModel.words, frameTimeNanos)
            scrollController.updateScroll(currentWidth, view)
            notifyPlayProgress()
        }

        val dropChanged = dropAnimationManager.update(frameTimeNanos)

        return highlightChanged || dropChanged
    }

    fun draw(canvas: Canvas) {
        renderer.draw(
            canvas = canvas,
            view = view,
            backgroundPaint = backgroundPaint,
            highlightPaint = highlightPaint,
            highlightWidth = highlightAnimator.currentWidth
        )
    }

    private fun notifyPlayProgress() {
        val width = highlightAnimator.currentWidth
        val totalWidth = view.lyricWidth

        if (!highlightAnimator.isStarted && width > 0f) {
            highlightAnimator.isStarted = true
            lyricPlayListener?.onPlayStarted(view)
        }

        if (!highlightAnimator.isFinished && width >= totalWidth) {
            highlightAnimator.isFinished = true
            lyricPlayListener?.onPlayEnded(view)
        }

        lyricPlayListener?.onPlayProgress(view, totalWidth, width)
    }

    /**
     * 高亮动画控制器
     * 职责：管理歌词高亮进度的动画
     */
    private class HighlightAnimator {
        private val interpolator: Interpolator = Interpolators.linear

        var currentWidth = 0f
            private set
        var targetWidth = 0f
            private set

        private var startWidth = 0f
        private var startTime = 0L
        private var duration = 0L
        private var isAnimating = false

        var isStarted = false
        var isFinished = false

        val isPlaying: Boolean
            get() = isStarted && !isFinished

        fun reset() {
            currentWidth = 0f
            targetWidth = 0f
            startWidth = 0f
            startTime = 0L
            duration = 0L
            isAnimating = false
            isStarted = false
            isFinished = false
        }

        fun jumpTo(width: Float) {
            currentWidth = width
            targetWidth = width
            startWidth = width
            isAnimating = false
        }

        fun animateTo(target: Float, durationMs: Long) {
            startWidth = currentWidth
            targetWidth = target
            startTime = System.nanoTime()
            duration = max(1L, durationMs * 1_000_000L)
            isAnimating = true
        }

        fun update(frameTimeNanos: Long): Boolean {
            if (!isAnimating) return false

            val elapsed = (frameTimeNanos - startTime).coerceAtLeast(0L)
            val progress = (elapsed.toDouble() / duration.toDouble()).coerceIn(0.0, 1.0)
            val eased = interpolator.getInterpolation(progress.toFloat())
            val newWidth = startWidth + (targetWidth - startWidth) * eased

            val changed = newWidth != currentWidth
            currentWidth = newWidth

            if (progress >= 1.0) {
                currentWidth = targetWidth
                startWidth = targetWidth
                isAnimating = false
            }

            return changed
        }
    }

    private inner class DropAnimationManager() {
        private val interpolator = Interpolators.decelerate
        private val durationNanos = 700L * 1_000_000L

        // 活跃的动画列表（按触发时间排序）
        private val activeAnimations = mutableListOf<CharDropAnimation>()

        // 触发点缓存（按位置排序，用于快速查找）
        private val triggerPoints = mutableListOf<AnimationTrigger>()
        private var lastCheckedIndex = 0

        fun reset() {
            activeAnimations.clear()
            triggerPoints.clear()
            lastCheckedIndex = 0

            // 重置所有字符的偏移
            view.lyricModel.words.forEach { word ->
                word.charOffsetYArray.forEach { offset ->
                    offset.value = offset.from
                }
            }
        }

        /**
         * 预计算所有动画的触发点
         * 只在歌词模型变化时调用一次
         */
        fun prepareTriggers(words: List<WordModel>) {
            triggerPoints.clear()

            words.forEachIndexed { wordIndex, word ->
                if (word.charOffsetMode) {
                    // 逐字模式：为每个字符创建触发点
                    word.chars.forEachIndexed { charIndex, _ ->
                        triggerPoints.add(
                            AnimationTrigger(
                                position = word.charStartPositions[charIndex],
                                wordIndex = wordIndex,
                                charIndex = charIndex,
                                isWholeWord = false
                            )
                        )
                    }
                } else {
                    // 整词模式：为整个单词创建一个触发点
                    triggerPoints.add(
                        AnimationTrigger(
                            position = word.startPosition,
                            wordIndex = wordIndex,
                            charIndex = -1,
                            isWholeWord = true
                        )
                    )
                }
            }

            // 按位置排序，方便后续快速查找
            triggerPoints.sortBy { it.position }
        }

        /**
         * 检查并触发新动画
         * 使用二分查找优化性能
         */
        fun checkTriggers(highlightWidth: Float, words: List<WordModel>, frameTimeNanos: Long) {
            // 确保触发点已初始化
            if (triggerPoints.isEmpty()) {
                prepareTriggers(words)
            }

            // 从上次检查的位置开始，找到所有应该触发的动画
            while (lastCheckedIndex < triggerPoints.size) {
                val trigger = triggerPoints[lastCheckedIndex]

                if (highlightWidth <= trigger.position) {
                    break // 还没到这个触发点
                }

                // 触发动画
                val word = words.getOrNull(trigger.wordIndex) ?: continue
                startAnimation(trigger, word, frameTimeNanos)

                lastCheckedIndex++
            }
        }

        private fun startAnimation(
            trigger: AnimationTrigger,
            word: WordModel,
            frameTimeNanos: Long
        ) {
            activeAnimations.add(
                CharDropAnimation(
                    wordIndex = trigger.wordIndex,
                    charIndex = trigger.charIndex,
                    isWholeWord = trigger.isWholeWord,
                    startTime = frameTimeNanos,
                    startOffset = word.offsetY
                )
            )
        }

        fun update(frameTimeNanos: Long): Boolean {
            if (activeAnimations.isEmpty()) return false

            var hasChanges = false
            val words = view.lyricModel.words

            // 反向遍历，方便删除已完成的动画
            val iterator = activeAnimations.listIterator(activeAnimations.size)
            while (iterator.hasPrevious()) {
                val animation = iterator.previous()

                val elapsed = (frameTimeNanos - animation.startTime).coerceAtLeast(0L)
                val progress = (elapsed.toDouble() / durationNanos.toDouble()).coerceIn(0.0, 1.0)
                val eased = interpolator.getInterpolation(progress.toFloat())
                val currentOffset = animation.startOffset * (1f - eased)

                val word = words.getOrNull(animation.wordIndex)
                if (word != null) {
                    if (animation.isWholeWord) {
                        // 更新整个单词的所有字符
                        word.charOffsetYArray.forEach { offset ->
                            offset.value = currentOffset
                        }
                    } else {
                        // 更新单个字符
                        val charIndex = animation.charIndex
                        if (charIndex in word.charOffsetYArray.indices) {
                            word.charOffsetYArray[charIndex].value = currentOffset
                        }
                    }
                    hasChanges = true
                }

                // 动画完成，移除并归位
                if (progress >= 1.0) {
                    word?.let { finalizeAnimation(animation, it) }
                    iterator.remove()
                }
            }

            return hasChanges
        }

        private fun finalizeAnimation(animation: CharDropAnimation, word: WordModel) {
            if (animation.isWholeWord) {
                word.charOffsetYArray.forEach { offset ->
                    offset.value = offset.to
                }
            } else {
                val charIndex = animation.charIndex
                if (charIndex in word.charOffsetYArray.indices) {
                    word.charOffsetYArray[charIndex].value = word.charOffsetYArray[charIndex].to
                }
            }
        }

    }

    /**
     * 滚动控制器
     * 职责：根据高亮位置计算并更新滚动偏移
     */
    private inner class ScrollController {
        fun reset() {
            view.scrollXOffset = 0f
            view.isScrollFinished = false
        }

        fun updateScroll(highlightWidth: Float, view: LyricLineView) {
            val viewWidth = view.measuredWidth

            if (!view.isOverflow()) {
                view.scrollXOffset = 0f
                return
            }

            // 当高亮超过屏幕中点时，开始滚动
            if (highlightWidth > viewWidth / 2) {
                val targetScroll = viewWidth / 2f - highlightWidth
                val rightmostScroll = -view.lyricWidth + viewWidth
                val appliedScroll = max(targetScroll, rightmostScroll)

                view.scrollXOffset = appliedScroll

                if (appliedScroll <= rightmostScroll) {
                    view.isScrollFinished = true
                }
            } else {
                view.scrollXOffset = 0f
            }
        }
    }

    /**
     * 歌词渲染器
     * 职责：负责绘制背景文字和高亮文字
     */
    private class LyricRenderer {
        private val gradientColors = intArrayOf(0, 0, Color.TRANSPARENT)
        private val gradientPositions = floatArrayOf(0f, 0f, 1f)

        private var cachedShader: LinearGradient? = null
        private var cachedShaderWidth = -1f

        fun draw(
            canvas: Canvas,
            view: LyricLineView,
            backgroundPaint: TextPaint,
            highlightPaint: TextPaint,
            highlightWidth: Float
        ) {
            val lyricModel = view.lyricModel
            val textPaint = view.textPaint
            val lyricWidth = lyricModel.width
            val height = view.measuredHeight
            val width = view.measuredWidth

            val baseline =
                ((height - (textPaint.descent() - textPaint.ascent())) / 2) - textPaint.ascent()

            // 准备渐变着色器
            gradientColors[0] = highlightPaint.color
            gradientColors[1] = highlightPaint.color
            gradientPositions[1] =
                (if (lyricWidth > 0f) (highlightWidth / lyricWidth) else 0f).coerceAtLeast(0.95f)

            canvas.withSave {
                // 处理对齐和滚动
                when {
                    view.isOverflow() -> translate(view.scrollXOffset, 0f)
                    lyricModel.isAlignedRight -> translate(-lyricWidth + width, 0f)
                }

                // 绘制背景文字
                drawLyricText(canvas, lyricModel.words, baseline, backgroundPaint)

                // 裁剪并绘制高亮文字
                clipRect(0f, 0f, highlightWidth, height.toFloat())

                if (highlightWidth >= lyricWidth) {
                    cachedShader = null
                } else {
                    if (cachedShader == null || cachedShaderWidth != highlightWidth) {
                        cachedShader = LinearGradient(
                            0f,
                            0f,
                            highlightWidth,
                            0f,
                            gradientColors,
                            gradientPositions,
                            Shader.TileMode.CLAMP
                        )
                        cachedShaderWidth = highlightWidth
                    }
                }

                highlightPaint.shader = cachedShader
                drawLyricText(canvas, lyricModel.words, baseline, highlightPaint)
            }
        }

        private fun drawLyricText(
            canvas: Canvas,
            words: List<WordModel>,
            baseline: Float,
            paint: Paint
        ) {
            var x = 0f
            words.forEach { word ->
                word.chars.forEachIndexed { charIndex, char ->
                    val offsetY = word.getCharOffsetY(charIndex)
                    canvas.drawText(char.toString(), x, baseline + offsetY, paint)
                    x += word.charWidths[charIndex]
                }
            }
        }
    }
}


/**
 * 动画触发点
 */
private data class AnimationTrigger(
    val position: Float,      // 触发位置
    val wordIndex: Int,       // 单词索引
    val charIndex: Int,       // 字符索引（-1表示整词）
    val isWholeWord: Boolean  // 是否为整词动画
)

/**
 * 单个字符下落动画
 */
private data class CharDropAnimation(
    val wordIndex: Int,
    val charIndex: Int,
    val isWholeWord: Boolean,
    val startTime: Long,
    val startOffset: Float
)

internal fun List<WordModel>.toText(): String = joinToString("") { it.text }