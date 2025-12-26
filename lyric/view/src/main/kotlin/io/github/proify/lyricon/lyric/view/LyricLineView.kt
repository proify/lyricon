package io.github.proify.lyricon.lyric.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import io.github.proify.lyricon.lyric.model.LyricLine
import java.lang.ref.WeakReference
import kotlin.math.max


@Suppress("unused")
class LyricLineView(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    companion object {
        internal const val TAG = "LyricLineView"
    }

    init {
        isHorizontalFadingEdgeEnabled = true
        setFadingEdgeLength(15.dp)
    }

    internal val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f.sp
    }

    internal var lyricModel = createModel(LyricLine())

    internal var scrollXOffset = 0f

    internal var isScrollFinished = false

    internal val marquee = Marquee(WeakReference(this))

    internal var syllable = Syllable(0.0f)

    private val animationDriver = AnimationDriver()

    fun reset() {
        animationDriver.stop()
        marquee.reset()
        syllable.reset()
        scrollXOffset = 0f
        isScrollFinished = false
        lyricModel = createModel(LyricLine())
        refreshModelSizes()
        invalidate()
    }

    fun setTextSize(size: Float) {
        textPaint.textSize = size

        syllable.backgroundPaint.textSize = size
        syllable.highlightPaint.textSize = size
        refreshModelSizes(false)
    }

    fun setStyle(configs: LyricLineConfig) {
        val textConfig = configs.text
        val marqueeConfig = configs.marquee
        val syllableConfig = configs.syllable

        textPaint.run {
            textSize = textConfig.textSize
            typeface = textConfig.typeface
            color = textConfig.textColor
        }

        syllable.backgroundPaint.run {
            textSize = textConfig.textSize
            typeface = textConfig.typeface
            color = syllableConfig.backgroundColor
        }
        syllable.highlightPaint.run {
            textSize = textConfig.textSize
            typeface = textConfig.typeface
            color = syllableConfig.highlightColor
        }

        marquee.run {
            ghostSpacing = marqueeConfig.ghostSpacing
            scrollSpeed = marqueeConfig.scrollSpeed
            initialDelayMs = marqueeConfig.initialDelay
            loopDelayMs = marqueeConfig.loopDelay
            repeatCount = marqueeConfig.repeatCount
            stopAtEnd = marqueeConfig.stopAtEnd
        }
        refreshModelSizes()
        invalidate()
    }

    fun setPosition(position: Int) {
        if (!isMarqueeMode()) {
            syllable.updateProgress(position)
            animationDriver.start() // 确保驱动器运行以推进动画
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun refreshModelSizes() {
        refreshModelSizes(true)
    }

    fun refreshModelSizes(updateCharOffset: Boolean) {
        lyricModel.updateSizes(textPaint, updateCharOffset)
    }

    // 可调参数：渐隐的“感知范围”占控件宽度的比例（0.0 ~ 1.0）
    private val fadingRangeFraction = 0.18f

    private fun computeFadingRange(): Float {
        val fromWidth = width * fadingRangeFraction
        val maxByLyric = if (lyricWidth > 0f) lyricWidth / 3f else fromWidth
        return fromWidth.coerceAtMost(maxByLyric).coerceAtLeast(1f)
    }

    private fun isFinalLoopAndFinished(): Boolean {
        return marquee.stopAtEnd &&
                marquee.repeatCount > 0 &&
                marquee.currentRepeat >= marquee.repeatCount - 1 &&
                scrollXOffset <= -lyricWidth
    }

    override fun getLeftFadingEdgeStrength(): Float {
        if (!isOverflow()) return 0f
        if (isFinalLoopAndFinished()) return 0f

        // Marquee 模式采用可见文本的左隐量
        if (isMarqueeMode()) {
            val unit = lyricWidth + marquee.ghostSpacing
            if (unit <= 0f) return 0f

            val hiddenLeftPrimary = marquee.getEasedHiddenLeft()
            val primaryRight = -hiddenLeftPrimary + lyricWidth

            val fadeRange = computeFadingRange()

            return if (primaryRight > 0f) {
                // ① primary 仍在视口内 → 按 primary 左隐量渐隐
                (hiddenLeftPrimary / fadeRange).coerceIn(0f, 1f)
            } else {
                // ② primary 完全滚出视口，ghost 出现 → 使用 ghost 的左隐量
                val ghostLeft = primaryRight + marquee.ghostSpacing
                val ghostHiddenLeft = -ghostLeft // ghostLeft < 0 → 被左侧遮挡的像素
                (ghostHiddenLeft / fadeRange).coerceIn(0f, 1f)
            }
        }

        // 非 marquee 常规逻辑
        val hiddenLeft = (-scrollXOffset).coerceAtLeast(0f)
        val fadeRange = computeFadingRange()
        return (hiddenLeft / fadeRange).coerceIn(0f, 1f)
    }

    override fun getRightFadingEdgeStrength(): Float {
        if (!isOverflow()) return 0f
        if (isScrollFinished) return 0f

        if (isMarqueeMode() && isFinalLoopAndFinished()) return 0f

        val unit = lyricWidth + marquee.ghostSpacing
        if (unit <= 0f) return 0f

        val hiddenLeft = marquee.getEasedHiddenLeft().coerceIn(0f, unit)

        val firstRight = -hiddenLeft + lyricWidth
        val ghostLeft = firstRight + marquee.ghostSpacing
        val ghostRight = ghostLeft + lyricWidth

        val rightMost = maxOf(firstRight, ghostRight)
        val hiddenRight = (rightMost - width).coerceAtLeast(0f)

        val fadeRange = computeFadingRange()
        return (hiddenRight / fadeRange).coerceIn(0f, 1f)
    }


    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val w = MeasureSpec.getSize(wSpec)
        val textHeight = (textPaint.descent() - textPaint.ascent()).toInt()
        setMeasuredDimension(w, resolveSize(textHeight, hSpec))
    }

    fun setLyric(line: LyricLine?) {
        reset()
        lyricModel = createModel(line ?: LyricLine())
        refreshModelSizes()
        if (lyricModel.words.isNotEmpty()) {
            syllable.wordsOffsetAnimator.initIfNeeded()
        }
        requestLayout()
        invalidate()
    }

    fun startMarquee() {
        if (isMarqueeMode()) {
            scrollXOffset = 0f
            marquee.start()
            animationDriver.start()
        }
    }

    fun pauseMarquee() {
        if (isMarqueeMode()) {
            marquee.pause()
        }
    }

    internal fun isMarqueeMode() = lyricModel.isPlainText

    internal fun isOverflow() = lyricWidth > width

    internal val lyricWidth get() = lyricModel.width

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isMarqueeMode()) {
            marquee.draw(canvas)
        } else {
            syllable.draw(canvas)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    /**
     * ------------------------
     *  统一动画驱动器（Choreographer）
     * ------------------------
     */
    internal inner class AnimationDriver : Choreographer.FrameCallback {
        private var running = false
        private var lastFrameNanos = 0L

        fun start() {
            if (!running) {
                running = true
                lastFrameNanos = System.nanoTime()
                Choreographer.getInstance().postFrameCallback(this)
            }
        }

        fun stop() {
            if (!running) return
            running = false
            Choreographer.getInstance().removeFrameCallback(this)
        }

        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return

            val deltaNanos = if (lastFrameNanos == 0L) 0L else (frameTimeNanos - lastFrameNanos)
            lastFrameNanos = frameTimeNanos

            var needInvalidate: Boolean

            if (isMarqueeMode()) {
                // 驱动 Marquee（被动 step，保留完整行为）
                marquee.step(deltaNanos)
                needInvalidate = true
            } else {
                // 非跑马灯：推进高亮 & 字偏移
                val changed = syllable.updateFrame(frameTimeNanos)
                needInvalidate = changed
            }

            if (needInvalidate) postInvalidateOnAnimation()

            if (running) Choreographer.getInstance().postFrameCallback(this)
        }
    }

    /**
     * ------------------------
     *  Syllable：负责高亮与字掉落（保留之前的实现思路）
     * ------------------------
     */
    internal inner class Syllable(highlightWidth: Float) {
        val backgroundPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)

        val highlightPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)

        val gradientColors: IntArray = intArrayOf(0, 0, Color.TRANSPARENT)
        val gradientPositions: FloatArray = floatArrayOf(0f, 0f, 1f)

        var cachedShader: LinearGradient? = null
        var cachedShaderWidth: Float = -1f

        var handledStarted = false
        var handledEnded = false
        var highlightWidth = 0f
            set(value) {
                field = value
                if (handledStarted.not()) {
                    lyricPlayListener?.onPlayStarted(this@LyricLineView)
                    handledStarted = true
                } else if (handledEnded.not()) {
                    if (value >= lyricWidth) {
                        lyricPlayListener?.onPlayEnded(this@LyricLineView)
                        handledEnded = true
                    }
                }
                lyricPlayListener?.onPlayProgress(this@LyricLineView, lyricWidth, highlightWidth)
            }

        var highlightLastWidth = 0f
        var highlightTargetWidth = 0f
        var highlightStartNanos = 0L
        var highlightDurationNanos = 0L
        var highlightAnimating = false
        val highlightInterpolator = DecelerateInterpolator()

        val wordsOffsetAnimator = WordsOffsetAnimator()

        var lyricPlayListener: LyricPlayListener? = null

        fun isPlayStarted() = handledStarted
        fun isPlaying() = isPlayStarted() && isPlayFinished().not()
        fun isPlayFinished() = highlightWidth >= lyricWidth

        fun reset() {
            highlightAnimating = false

            highlightWidth = 0f
            handledStarted = false
            handledEnded = false

            highlightLastWidth = 0f
            highlightTargetWidth = 0f
            highlightStartNanos = 0L
            highlightDurationNanos = 0L
            wordsOffsetAnimator.reset()
            scrollXOffset = 0f
        }

        fun updateProgress(position: Int) {
            val words = lyricModel.words
            val current = words.filterByPosition(position).firstOrNull()
            val width = current?.endPosition
                ?: if (position >= lyricModel.end) {
                    lyricWidth
                } else if (position <= lyricModel.begin) {
                    0f
                } else {
                    highlightWidth
                }

            if (current != null && highlightLastWidth == 0f) {
                current.previous?.let {
                    highlightLastWidth = it.endPosition
                    highlightWidth = highlightLastWidth
                    updateScrollXOffset(highlightWidth)
                }
            }

            if (width == highlightTargetWidth) return

            val duration = current?.duration ?: 0
            requestHighlightTo(width, duration)
        }

        private fun requestHighlightTo(target: Float, durationMs: Int) {
            highlightLastWidth = highlightWidth
            highlightTargetWidth = target
            highlightStartNanos = System.nanoTime()
            highlightDurationNanos = max(1L, durationMs.toLong() * 1_000_000L)
            highlightAnimating = true
        }

        fun updateFrame(frameTimeNanos: Long): Boolean {
            var changed = false

            // 高亮推进
            if (highlightAnimating) {
                val elapsed = (frameTimeNanos - highlightStartNanos).coerceAtLeast(0L)
                val progress =
                    (elapsed.toDouble() / highlightDurationNanos.toDouble()).coerceIn(0.0, 1.0)
                val eased = highlightInterpolator.getInterpolation(progress.toFloat())
                val newWidth =
                    highlightLastWidth + (highlightTargetWidth - highlightLastWidth) * eased
                if (newWidth != highlightWidth) {
                    highlightWidth = newWidth
                    updateScrollXOffset(highlightWidth)
                    changed = true
                }
                if (progress >= 1.0) {
                    highlightAnimating = false
                    highlightWidth = highlightTargetWidth
                    highlightLastWidth = highlightTargetWidth
                    changed = true
                }
            }

            // WordsOffset 更新
            val wordsChanged = wordsOffsetAnimator.update(frameTimeNanos)
            changed = changed || wordsChanged

            return changed
        }

        fun draw(canvas: Canvas) {
            val baseline =
                ((height - (textPaint.descent() - textPaint.ascent())) / 2) - textPaint.ascent()

            gradientColors[0] = highlightPaint.color
            gradientColors[1] = highlightPaint.color
            gradientPositions[1] =
                (if (lyricWidth > 0f) (highlightWidth / lyricWidth) else 0f).coerceAtLeast(0.95f)

            fun drawText(paint: Paint) {
                var x = 0f
                lyricModel.words.forEach { word ->
                    for (i in 0 until word.chars.size) {
                        val char = word.chars[i].toString()
                        canvas.drawText(char, x, baseline + word.charOffsetYs[i], paint)
                        x += word.charWidths[i]
                    }
                }
            }

            canvas.withSave {
                if (isOverflow()) {
                    translate(scrollXOffset, 0f)
                } else if (lyricModel.isAlignedRight) {
                    translate(-lyricWidth + width, 0f)
                }

                drawText(backgroundPaint)
                clipRect(0f, 0f, highlightWidth, height.toFloat())

                if (highlightWidth >= lyricWidth) {
                    cachedShader = null
                } else if (cachedShader == null || cachedShaderWidth != highlightWidth) {
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

                highlightPaint.shader = cachedShader

                drawText(highlightPaint)
            }
        }

        fun updateScrollXOffset(highlightWidth: Float) {
            val width = width
            if (isOverflow() && highlightWidth > width / 2) {
                val targetScroll = width / 2f - highlightWidth
                val rightmostScroll = -lyricWidth + width
                val appliedScroll = max(targetScroll, rightmostScroll)

                scrollXOffset = appliedScroll

                if (appliedScroll <= rightmostScroll) {
                    isScrollFinished = true
                }
            } else {
                scrollXOffset = 0f
            }
        }

        /**
         * ------------------------
         * WordsOffsetAnimator
         * ------------------------
         */
        inner class WordsOffsetAnimator {

            private val activeMap = HashMap<ActiveAnimKey, ActiveAnim>()
            private val dropInterpolator = DecelerateInterpolator()
            private val defaultDurationMs = 800L
            private val defaultDurationNanos = defaultDurationMs * 1_000_000L
            private var initialized = false

            fun initIfNeeded() {
                if (!initialized) {
                    initialized = true
                }
            }

            fun update(frameTimeNanos: Long): Boolean {
                var needInvalidate = false

                // 触发记录（按字或整词）
                lyricModel.words.forEachIndexed { wordIndex, word ->
                    if (word.charOffsetMode) {
                        word.chars.forEachIndexed { charIndex, _ ->
                            if (highlightWidth > word.charStartPositions[charIndex]) {
                                val key = ActiveAnimKey(wordIndex, charIndex)
                                if (!activeMap.containsKey(key)) {
                                    activeMap[key] = ActiveAnim(
                                        wordIndex = wordIndex,
                                        charIndex = charIndex,
                                        startNanos = frameTimeNanos,
                                        durationNanos = defaultDurationNanos,
                                        startOffsetY = word.initOffsetY
                                    )
                                }
                            }
                        }
                    } else {
                        val key = ActiveAnimKey(wordIndex, null)
                        if (highlightWidth > word.startPosition && !activeMap.containsKey(key)) {
                            activeMap[key] = ActiveAnim(
                                wordIndex = wordIndex,
                                charIndex = null,
                                startNanos = frameTimeNanos,
                                durationNanos = defaultDurationNanos,
                                startOffsetY = word.initOffsetY
                            )
                        }
                    }
                }

                // 更新动画进度（跳过 finished）
                activeMap.forEach { (_, active) ->
                    if (active.finished) return@forEach

                    val word = lyricModel.words.getOrNull(active.wordIndex) ?: run {
                        active.finished = true
                        return@forEach
                    }

                    val elapsed = (frameTimeNanos - active.startNanos).coerceAtLeast(0L)
                    val progress =
                        (elapsed.toDouble() / active.durationNanos.toDouble()).coerceIn(0.0, 1.0)
                    val eased = dropInterpolator.getInterpolation(progress.toFloat())
                    val currentOffset = active.startOffsetY * (1f - eased)

                    if (active.charIndex == null) {
                        for (i in word.charOffsetYs.indices) {
                            if (word.charOffsetYs[i] != currentOffset) {
                                word.charOffsetYs[i] = currentOffset
                                needInvalidate = true
                            }
                        }
                    } else {
                        val idx = active.charIndex
                        if (idx >= 0 && idx < word.charOffsetYs.size) {
                            if (word.charOffsetYs[idx] != currentOffset) {
                                word.charOffsetYs[idx] = currentOffset
                                needInvalidate = true
                            }
                        }
                    }

                    if (progress >= 1.0) {
                        active.finished = true
                        if (active.charIndex == null) {
                            for (i in word.charOffsetYs.indices) word.charOffsetYs[i] = 0f
                        } else {
                            val idx = active.charIndex
                            word.charOffsetYs[idx] = 0f
                        }
                        needInvalidate = true
                    }
                }

                return needInvalidate
            }

            fun reset() {
                activeMap.clear()
                initialized = false
            }
        }
    }

    /**
     * ------------------------
     *  Model
     * ------------------------
     */
    fun createModel(line: LyricLine): LyricModel {
        val model = LyricModel(
            line.begin,
            line.end,
            line.duration,
            line.text ?: ""
        )

        var previousWord: WordModel? = null
        line.words?.forEach { word ->
            val wordModel = WordModel(
                word.begin,
                word.end,
                word.duration,
                word.text ?: "",
            )
            wordModel.previous = previousWord

            model.words.add(wordModel)
            previousWord?.next = wordModel
            previousWord = wordModel
        }

        model.updateWords()
        return model
    }

}

data class WordModel(
    var begin: Int = 0,
    var end: Int = 0,
    var duration: Int = 0,
    var text: String,
) {
    var previous: WordModel? = null
    var next: WordModel? = null

    var textWidth: Float = 0f

    var startPosition = 0f
    var endPosition: Float = 0f

    var charStartPositions: FloatArray = floatArrayOf()
    var charEndPositions: FloatArray = floatArrayOf()

    var chars: CharArray = charArrayOf()
    var charWidths = FloatArray(text.length)

    var charOffsetMode: Boolean = false
    var initOffsetY: Float = 0f
    var charOffsetYs: FloatArray = floatArrayOf()

    /**
     * @param updateCharOffset 是否更新字符偏移，如果为true，在更新文字大小时会重置字符偏移
     */
    fun updateSizes(previous: WordModel?, paint: Paint, updateCharOffset: Boolean) {
        chars = text.toCharArray()
        paint.getTextWidths(chars, 0, chars.size, this.charWidths)
        textWidth = charWidths.sum()

        initOffsetY = (paint.textSize * 0.07f)

        if (updateCharOffset) {
            charOffsetYs = FloatArray(chars.size) { initOffsetY }
        }

        startPosition = previous?.endPosition ?: 0f
        endPosition = startPosition.plus(textWidth)

        charStartPositions = FloatArray(chars.size)
        charEndPositions = FloatArray(chars.size)

        var lastCharEndWidth = startPosition
        for (i in 0 until chars.size) {
            val charWidth = charWidths[i]
            charStartPositions[i] = lastCharEndWidth
            lastCharEndWidth += charWidth
            charEndPositions[i] = lastCharEndWidth
        }

        charOffsetMode = chars.all { isSingleCharAnimTarget(it) }
    }

    fun isSingleCharAnimTarget(c: Char): Boolean {
        return isChinese(c)
    }

    fun isChinese(c: Char): Boolean {
        return c in '\u4e00'..'\u9fff'
    }
}

data class LyricModel(
    var begin: Int = 0,
    var end: Int = 0,
    var duration: Int = 0,
    var text: String,
    var words: MutableList<WordModel> = mutableListOf(),
    var isAlignedRight: Boolean = false,
) {
    var width: Float = 0f
    val isPlainText: Boolean get() = words.isEmpty()
    var wordText: String = ""

    fun updateWords() {
        wordText = words.toText()
    }

    fun updateSizes(paint: Paint, updateCharOffset: Boolean) {
        width = paint.measureText(text)

        var previous: WordModel? = null
        words.forEach {
            it.updateSizes(previous, paint, updateCharOffset)
            previous = it
        }
    }

}

internal fun MutableList<WordModel>.toText(): String {
    return joinToString("") { it.text }
}

internal fun MutableList<WordModel>.filterByPosition(position: Int) =
    filter { it.begin <= position && it.end >= position }


class Marquee(val view: WeakReference<LyricLineView>) {

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
        @Suppress("SameParameterValue") isLoopDelay: Boolean = false
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


private data class ActiveAnimKey(val wordIndex: Int, val charIndex: Int?)

private data class ActiveAnim(
    val wordIndex: Int,
    val charIndex: Int?,
    val startNanos: Long,
    val durationNanos: Long,
    val startOffsetY: Float,
    var finished: Boolean = false
)