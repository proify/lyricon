/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.control

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import io.github.proify.android.extensions.dp

/**
 * 控制窗口进出场动画（原始版：从顶部滑落弹开 + 过冲回弹；关闭时收缩回顶部）。
 *
 * @author Tomakino
 * @since 2026
 */
object ControlAnimations {

    private const val ENTER_DURATION_MS = 420L
    private const val EXIT_DURATION_MS = 220L
    private const val ENTER_OFFSET_DP = 36
    private const val EXIT_OFFSET_DP = 40
    private const val ENTER_SCALE = 0.9f
    private const val OVERSHOOT_TENSION = 0.75f

    /** 从状态栏下沿弹出：略高、略小、透明起步，同时带回弹过冲。 */
    fun playEnter(view: View) {
        view.translationY = -ENTER_OFFSET_DP.dp.toFloat()
        view.scaleX = ENTER_SCALE
        view.scaleY = ENTER_SCALE
        view.alpha = 0f

        view.animate()
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(ENTER_DURATION_MS)
            .setInterpolator(OvershootInterpolator(OVERSHOOT_TENSION))
            .start()
    }

    /** 关闭：向上收缩 + 淡出，结束后回调 [onEnd]。 */
    fun playExit(view: View, onEnd: () -> Unit) {
        view.animate()
            .translationY(-EXIT_OFFSET_DP.dp.toFloat())
            .scaleX(ENTER_SCALE)
            .scaleY(ENTER_SCALE)
            .alpha(0f)
            .setDuration(EXIT_DURATION_MS)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = onEnd()
            })
            .start()
    }
}
