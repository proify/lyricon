package io.github.proify.lyricon.lyric.view.util

import android.animation.LayoutTransition
import android.animation.TimeInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

@Suppress("ConstPropertyName")
class LayoutTransitionX : LayoutTransition() {

    init {
        setDuration(CHANGE_APPEARING, mChangingAppearingDuration)
        setDuration(CHANGE_DISAPPEARING, mChangingDisappearingDuration)
        setDuration(CHANGING, mChangingDuration)
        setDuration(APPEARING, mAppearingDuration)
        setDuration(DISAPPEARING, mDisappearingDuration)

        setInterpolator(CHANGE_APPEARING, sChangingAppearingInterpolator)
        setInterpolator(CHANGE_DISAPPEARING, sChangingDisappearingInterpolator)
        setInterpolator(CHANGING, sChangingInterpolator)

        setInterpolator(APPEARING, sAppearingInterpolator)
        setInterpolator(DISAPPEARING, sDisappearingInterpolator)
    }

    companion object {
        val ACCEL_DECEL_INTERPOLATOR: TimeInterpolator = FastOutSlowInInterpolator()
        val DECEL_INTERPOLATOR: TimeInterpolator = LinearOutSlowInInterpolator()

        val sAppearingInterpolator: TimeInterpolator = ACCEL_DECEL_INTERPOLATOR
        val sDisappearingInterpolator: TimeInterpolator = ACCEL_DECEL_INTERPOLATOR
        val sChangingAppearingInterpolator: TimeInterpolator = DECEL_INTERPOLATOR
        val sChangingDisappearingInterpolator: TimeInterpolator = DECEL_INTERPOLATOR
        val sChangingInterpolator: TimeInterpolator = DECEL_INTERPOLATOR

        const val DEFAULT_DURATION: Long = 400
        const val mChangingAppearingDuration: Long = DEFAULT_DURATION
        const val mChangingDisappearingDuration: Long = DEFAULT_DURATION
        const val mChangingDuration: Long = DEFAULT_DURATION
        const val mAppearingDuration: Long = DEFAULT_DURATION
        const val mDisappearingDuration: Long = DEFAULT_DURATION
    }
}