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

package io.github.proify.lyricon.xposed.hook.systemui.lyricview

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import io.github.proify.android.extensions.dp
import io.github.proify.android.extensions.toBitmap
import io.github.proify.android.extensions.toRoundedCorner
import io.github.proify.android.extensions.visibilityIfChanged
import io.github.proify.lyricon.common.util.SVGHelper
import io.github.proify.lyricon.lyric.style.LogoStyle
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.lyric.style.RectF
import io.github.proify.lyricon.provider.ProviderLogo
import io.github.proify.lyricon.xposed.hook.systemui.LyricViewController
import io.github.proify.lyricon.xposed.util.NotificationCoverHelper
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor
import io.github.proify.lyricon.xposed.util.StatusColor
import java.io.File
import kotlin.math.roundToInt

class LyricLogoView(context: Context) : ImageView(context),
    StatusBarColorMonitor.OnColorChangeListener, NotificationCoverHelper.OnCoverUpdateListener {

    var linkedTextView: TextView? = null

    var providerLogo: ProviderLogo? = null
        set(value) {
            field = value
            refreshLogoDisplay()
        }

    private var currentStatusColor: StatusColor = StatusColor(Color.BLACK, false)
    private var currentStyle: LyricStyle? = null
    private var rotationAnimator: ObjectAnimator? = null
    private var currentLogoType: LogoType = LogoType.PROVIDER_LOGO

    private enum class LogoType {
        PROVIDER_LOGO,
        COVER
    }

    companion object {
        private const val DEFAULT_ROTATION_DURATION_MS = 12_000L
        private const val TEXT_SIZE_MULTIPLIER = 1.2f
        private const val DEFAULT_TEXT_SIZE_DP = 14
        private const val SQUIRCLE_CORNER_RADIUS_DP = 12
        const val VIEW_TAG = "lyricon:logo_view"
    }

    init {
        tag = VIEW_TAG
    }

    fun applyStyle(style: LyricStyle) {
        currentStyle = style
        val logoConfig = style.packageStyle.logo

        applyLayoutParams(style, logoConfig)
        refreshLogoDisplay()
    }

    private fun applyLayoutParams(style: LyricStyle, logoStyle: LogoStyle) {
        val defaultSize = calculateDefaultSize(style)
        val width = if (logoStyle.width <= 0) defaultSize else logoStyle.width.dp
        val height = if (logoStyle.height <= 0) defaultSize else logoStyle.height.dp

        updateLayoutParams(width, height, logoStyle.margins)
    }

    private fun calculateDefaultSize(style: LyricStyle): Int {
        val configuredSize = style.packageStyle.text.textSize

        return when {
            configuredSize > 0 -> configuredSize.dp
            linkedTextView != null -> (linkedTextView!!.textSize * TEXT_SIZE_MULTIPLIER).roundToInt()
            else -> DEFAULT_TEXT_SIZE_DP.dp
        }
    }

    private fun updateLayoutParams(width: Int, height: Int, margins: RectF) {
        layoutParams = (layoutParams as? LayoutParams ?: LayoutParams(width, height)).apply {
            this.width = width
            this.height = height
            leftMargin = margins.left.dp
            topMargin = margins.top.dp
            rightMargin = margins.right.dp
            bottomMargin = margins.bottom.dp
        }
    }

    fun refreshLogoDisplay() {
        val logoConfig = currentStyle?.packageStyle?.logo ?: return

        when (logoConfig.style) {
            LogoStyle.STYLE_COVER_SQUIRCLE,
            LogoStyle.STYLE_COVER_CIRCLE -> applyCoverStyleLogo(logoConfig.style)

            else -> applyProviderLogo()
        }
    }

    private fun applyProviderLogo() {
        val logo = providerLogo
        if (logo == null) {
            setBitmap(null, LogoType.PROVIDER_LOGO)
            return
        }

        val bitmap = when (logo.type) {
            ProviderLogo.TYPE_BITMAP -> logo.toBitmap()
            ProviderLogo.TYPE_SVG -> convertSvgToBitmap(logo)
            else -> null
        }

        setBitmap(bitmap, LogoType.PROVIDER_LOGO)
    }

    private fun applyCoverStyleLogo(logoStyle: Int) {
        val coverBitmap = loadCoverBitmap()

        if (coverBitmap == null) {
            applyProviderLogo()
            return
        }

        val styledBitmap = styleCoverBitmap(coverBitmap, logoStyle)
        setBitmap(styledBitmap, LogoType.COVER)
    }

    private fun loadCoverBitmap(): Bitmap? {
        val coverFile = NotificationCoverHelper.getCoverFile(LyricViewController.activePackage)
        return coverFile.toBitmap()
    }

    private fun styleCoverBitmap(bitmap: Bitmap, style: Int): Bitmap {
        val cornerRadius = when (style) {
            LogoStyle.STYLE_COVER_SQUIRCLE -> SQUIRCLE_CORNER_RADIUS_DP.dp.toFloat()
            LogoStyle.STYLE_COVER_CIRCLE -> bitmap.width.toFloat()
            else -> 0f
        }

        return bitmap.toRoundedCorner(cornerRadius, true)
    }

    private fun convertSvgToBitmap(logo: ProviderLogo): Bitmap? {
        val svgString = logo.toSvg()
        if (svgString.isNullOrBlank()) return null

        return SVGHelper.create(svgString)?.createBitmap(width, height)
    }

    private fun setBitmap(bitmap: Bitmap?, type: LogoType) {
        currentLogoType = type
        super.setImageBitmap(bitmap)
        visibilityIfChanged = if (bitmap != null) VISIBLE else GONE

        handleAnimationState(bitmap)
        updateLogoColor()
    }

    private fun handleAnimationState(bitmap: Bitmap?) {
        val shouldRotate = bitmap != null &&
                currentStyle?.packageStyle?.logo?.style == LogoStyle.STYLE_COVER_CIRCLE

        if (shouldRotate) {
            startRotationAnimation()
        } else {
            stopRotationAnimation()
        }
    }

    private fun startRotationAnimation() {
        stopRotationAnimation()

        rotationAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f).apply {
            duration = DEFAULT_ROTATION_DURATION_MS
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopRotationAnimation() {
        rotationAnimator?.cancel()
        rotationAnimator = null
        rotation = 0f
    }

    private fun updateLogoColor() {
        imageTintList = when (currentLogoType) {
            LogoType.PROVIDER_LOGO -> calculateProviderLogoTint()
            LogoType.COVER -> null
        }
    }

    private fun calculateProviderLogoTint(): ColorStateList? {
        val logoStyle = currentStyle?.packageStyle?.logo ?: return ColorStateList.valueOf(
            currentStatusColor.color
        )
        val logoColor = logoStyle.color(currentStatusColor.lightMode)

        if (!logoStyle.enableCustomColor) {
            return ColorStateList.valueOf(currentStatusColor.color)
        }

        val finalColor = when {
            logoColor.followTextColor -> resolveTextFollowingColor()
            logoColor.color != 0 -> logoColor.color
            else -> currentStatusColor.color
        }

        return ColorStateList.valueOf(finalColor)
    }

    private fun resolveTextFollowingColor(): Int {
        val textStyle = currentStyle?.packageStyle?.text
        val enableCustomTextColor = textStyle?.enableCustomTextColor == true

        if (!enableCustomTextColor) {
            return currentStatusColor.color
        }

        val textColor = textStyle.color(currentStatusColor.lightMode)
        return when {
            textColor != null && textColor.normal != 0 -> textColor.normal
            else -> currentStatusColor.color
        }
    }

    override fun onColorChange(color: StatusColor) {
        currentStatusColor = color
        updateLogoColor()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRotationAnimation()
    }

    override fun onCoverUpdated(packageName: String, coverFile: File) {
        if (currentLogoType == LogoType.COVER && packageName == LyricViewController.activePackage) {
            refreshLogoDisplay()
        }
    }
}