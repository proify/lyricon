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
package io.github.proify.lyricon.lyric.view.line

import android.content.res.Resources
import android.graphics.Canvas
import android.view.animation.LinearInterpolator
import androidx.core.graphics.withTranslation
import io.github.proify.lyricon.lyric.view.util.dp
import java.lang.ref.WeakReference

internal class Marquee(val view: WeakReference<LyricLineView>) {
    companion object {
        private const val DEFAULT_SCROLL_SPEED = 40f
    }

    var currentRepeat = 0
    var ghostSpacing = 40f.dp
    var scrollSpeed: Float =
        DEFAULT_SCROLL_SPEED * Resources.getSystem().displayMetrics.density / 1000f
        set(value) {
            field = value * Resources.getSystem().displayMetrics.density / 1000f
        }

    var initialDelayMs = 400
    var loopDelayMs = 800
    var repeatCount = -1
    var stopAtEnd = false

    var interpolator = LinearInterpolator()

    // internal state
    private var isRunning = false
    private var pendingStart = false
    private var pendingDelayRemainingNanos: Long = 0L
    private var fadingProgress = 1f // 控制 fading 进度（1.0 - 0.0）

    private var pendingLoopDelay = false
    private var pendingLoopEndNanos: Long = 0L
    private var pendingLoopOriginalDelayNanos: Long = 0L

    internal var currentScrolled: Float = 0f
        private set

    fun start() {
        if (repeatCount == 0) return
        if (isRunning || pendingStart) return

        view.get()?.let {
            it.scrollXOffset = 0f
            it.isScrollFinished = false
        }
        currentRepeat = 0
        currentScrolled = 0f
        fadingProgress = 1f // 重置 fading 为最大值
        pendingLoopDelay = false

        if (initialDelayMs > 0) {
            pendingStart = true
            pendingDelayRemainingNanos = initialDelayMs * 1_000_000L
            isRunning = false
        } else {
            pendingStart = false
            isRunning = true
        }
    }

    fun pause() {
        isRunning = false
        pendingStart = false
        pendingDelayRemainingNanos = 0L
        pendingLoopDelay = false
        pendingLoopEndNanos = 0L
        pendingLoopOriginalDelayNanos = 0L
    }

    fun reset() {
        isRunning = false
        pendingStart = false
        pendingDelayRemainingNanos = 0L
        currentRepeat = 0
        currentScrolled = 0f
        fadingProgress = 1f // 重置 fading 为最大值
        pendingLoopDelay = false
        pendingLoopEndNanos = 0L
        pendingLoopOriginalDelayNanos = 0L
        view.get()?.let {
            it.scrollXOffset = 0f
            it.isScrollFinished = false
            it.invalidate()
        }
    }

    private fun scheduleDelay(
        delayMs: Long,
        isLoopDelay: Boolean = false
    ) {
        if (delayMs <= 0L) {
            pendingStart = false
            isRunning = true
            pendingDelayRemainingNanos = 0L
            pendingLoopDelay = false
            pendingLoopEndNanos = 0L
            pendingLoopOriginalDelayNanos = 0L
        } else {
            pendingStart = true
            pendingDelayRemainingNanos = delayMs * 1_000_000L
            isRunning = false
            if (isLoopDelay) {
                pendingLoopDelay = true
                pendingLoopOriginalDelayNanos = pendingDelayRemainingNanos
                pendingLoopEndNanos = System.nanoTime() + pendingDelayRemainingNanos
            } else {
                pendingLoopDelay = false
                pendingLoopEndNanos = 0L
                pendingLoopOriginalDelayNanos = 0L
            }
        }
    }

    fun step(deltaNanos: Long) {
        if (!isRunning && !pendingStart) return

        val itView = view.get() ?: return

        if (pendingStart) {
            pendingDelayRemainingNanos -= deltaNanos
            if (pendingDelayRemainingNanos <= 0L) {
                pendingStart = false
                pendingLoopDelay = false
                isRunning = true
            } else {
                return
            }
        }
        if (!isRunning) return

        val lyricWidth = itView.lyricWidth
        val width = itView.width

        if (lyricWidth <= width) {
            itView.scrollXOffset = 0f
            return
        }

        val unit = lyricWidth + ghostSpacing
        if (unit <= 0f) return

        val deltaPx = scrollSpeed * (deltaNanos / 1_000_000f)
        currentScrolled += deltaPx

        while (currentScrolled >= unit) {
            currentScrolled -= unit
            currentRepeat++

            if (loopDelayMs > 0 && (repeatCount < 0 || currentRepeat <= repeatCount - 1)) {
                scheduleDelay(loopDelayMs.toLong(), isLoopDelay = true)
            }
        }

        val progress = (currentScrolled / unit).coerceIn(0f, 1f)
        val eased = interpolator.getInterpolation(progress)
        itView.scrollXOffset = -eased * unit

        val maxOffset = width - lyricWidth
        if (stopAtEnd && repeatCount > 0 && currentRepeat >= repeatCount - 1 && itView.scrollXOffset <= maxOffset) {
            itView.scrollXOffset = maxOffset
            itView.isScrollFinished = true
            pause()
        } else if (repeatCount in 1..currentRepeat) {
            reset()
        }
    }

    /**
     * 控制 fading 进度，loopDelay 期间平滑过渡
     */
    fun getFadingProgress(): Float {
        if (!pendingLoopDelay) return 1f // loopDelay 结束，保持最大 fading

        // 在 loopDelay 期间，我们逐渐减少 fading
        val now = System.nanoTime()
        val remaining = (pendingLoopEndNanos - now).coerceAtLeast(0L)
        val total = pendingLoopOriginalDelayNanos.coerceAtLeast(1L)

        // 计算渐隐进度：0 表示完全减小，1 表示最大 fading
        fadingProgress = (1.0f - (remaining.toFloat() / total.toFloat())).coerceIn(0f, 1f)
        return fadingProgress
    }

    /**
     * 返回“轮内已被隐藏在左侧的像素”（连续，范围 0 .. unit）
     * 如果处于 loopDelay，则从 unit 平滑过渡到正常 easedHiddenLeft（over the remaining hold time）
     */
    fun getEasedHiddenLeft(): Float {
        val itView = view.get() ?: return 0f
        val lyricWidth = itView.lyricWidth
        val unit = lyricWidth + ghostSpacing
        if (unit <= 0f) return 0f

        val progress = (currentScrolled / unit).coerceIn(0f, 1f)
        val easedNormal = interpolator.getInterpolation(progress) * unit

        // 返回 fading 过渡的进度，确保 loopDelay 时 fading 逐步减少
        return easedNormal * getFadingProgress()
    }

    fun draw(canvas: Canvas) {
        view.get()?.let {
            val scrollOffsetX = it.scrollXOffset
            val lyricWidth = it.lyricModel.width
            val width = it.width
            val paint = it.textPaint
            val text = it.lyricModel.text
            val baseline =
                ((it.height - (paint.descent() - paint.ascent())) / 2) - paint.ascent()

            if (repeatCount == 0 || lyricWidth <= width) {
                canvas.drawText(text, 0f, baseline, paint)
            } else {
                // primary 文本
                canvas.withTranslation(scrollOffsetX, 0f) {
                    drawText(text, 0f, baseline, paint)
                }

                // 鬼影：继承 primary 的 x（primaryX + lyricWidth + ghostSpacing）
                if (scrollOffsetX + lyricWidth < width) {
                    canvas.withTranslation(scrollOffsetX + lyricWidth + ghostSpacing, 0f) {
                        drawText(text, 0f, baseline, paint)
                    }
                }
            }
        }
    }
}
