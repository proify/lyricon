package io.github.proify.lyricon.xposed.hook.systemui.lyric

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import io.github.proify.lyricon.common.extensions.dp
import io.github.proify.lyricon.common.util.CommonUtils
import io.github.proify.lyricon.common.util.SVGHelper
import io.github.proify.lyricon.lyric.bridge.provider.ProviderLogo
import io.github.proify.lyricon.lyric.style.LogoStyle
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.lyric.style.RectF
import io.github.proify.lyricon.xposed.hook.systemui.LyricViewController
import io.github.proify.lyricon.xposed.util.NotificationCoverHelper
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor
import io.github.proify.lyricon.xposed.util.StatusColor

class LyricLogo(context: Context) : ImageView(context),
    StatusBarColorMonitor.OnColorChangeListener {

    var linkedTextView: TextView? = null

    var providerLogo: ProviderLogo? = null
        set(value) {
            field = value
            refreshLogoDisplay()
        }

    private var currentStatusColor: StatusColor = StatusColor(Color.BLACK, false)
    private var currentStyle: LyricStyle? = null
    private var rotationAnimator: ObjectAnimator? = null

    companion object {
        private const val DEFAULT_ROTATION_DURATION_MS = 12_000L
    }

    fun applyStyle(style: LyricStyle) {
        this.currentStyle = style
        val logoConfig = style.packageStyle.logo

        val defaultSize = calculateDefaultSize(style)
        val logoWidth = if (logoConfig.width <= 0) defaultSize else logoConfig.width.dp
        val logoHeight = if (logoConfig.height <= 0) defaultSize else logoConfig.height.dp

        updateLayoutParams(logoWidth, logoHeight, logoConfig.margins)
        refreshLogoDisplay()
    }

    private fun calculateDefaultSize(style: LyricStyle): Int {
        val configuredTextSize = style.packageStyle.text.textSize

        return when {
            configuredTextSize > 0 -> configuredTextSize.dp
            linkedTextView != null -> (linkedTextView!!.textSize * 1.3f).dp
            else -> 14.dp
        }
    }

    private fun updateLayoutParams(width: Int, height: Int, margins: RectF) {
        val params = (layoutParams as? LayoutParams) ?: LayoutParams(width, height)

        params.apply {
            this.width = width
            this.height = height
            leftMargin = margins.left.dp
            topMargin = margins.top.dp
            rightMargin = margins.right.dp
            bottomMargin = margins.bottom.dp
        }

        layoutParams = params
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        super.setImageBitmap(bitmap)
        visibility = if (bitmap != null) VISIBLE else GONE

        val logoConfig = currentStyle?.packageStyle?.logo
        if (bitmap != null && logoConfig?.style == LogoStyle.STYLE_COVER_CIRCLE) {
            startRotationAnimation()
        } else {
            stopRotationAnimation()
        }
    }

    private fun applyProviderLogo() {
        val logo = providerLogo
        if (logo == null) {
            setImageBitmap(null)
            return
        }

        val bitmap = when (logo.type) {
            ProviderLogo.TYPE_BITMAP -> logo.toBitmap()
            ProviderLogo.TYPE_SVG -> convertSvgToBitmap(logo)
            else -> null
        }

        setImageBitmap(bitmap)
    }

    private fun convertSvgToBitmap(logo: ProviderLogo): Bitmap? {
        val svgString = logo.toSvg()
        if (svgString.isNullOrBlank()) return null

        return SVGHelper.create(svgString)?.createBitmap(width, height)
    }
    
    fun refreshLogoDisplay() {
        val logoConfig = currentStyle?.packageStyle?.logo
        if (logoConfig == null) {
            applyProviderLogo()
            return
        }

        when (logoConfig.style) {
            LogoStyle.STYLE_COVER_SQUIRCLE,
            LogoStyle.STYLE_COVER_CIRCLE -> applyCoverStyleLogo(logoConfig.style)

            else -> applyProviderLogo()
        }
    }

    private fun applyCoverStyleLogo(logoStyle: Int) {
        val coverFile = NotificationCoverHelper.getCoverFile(LyricViewController.activePackage)
        val coverBitmap = CommonUtils.fileToBitmap(coverFile)

        if (coverBitmap == null) {
            applyProviderLogo()
            return
        }

        val styledBitmap = when (logoStyle) {
            LogoStyle.STYLE_COVER_SQUIRCLE -> {
                CommonUtils.getRoundedCornerBitmap(
                    coverBitmap,
                    10.dp.toFloat()
                )
            }

            LogoStyle.STYLE_COVER_CIRCLE -> {
                CommonUtils.getRoundedCornerBitmap(
                    coverBitmap,
                    coverBitmap.width.toFloat()
                )
            }

            else -> coverBitmap
        }

        setImageBitmap(styledBitmap)
    }

    private fun startRotationAnimation() {
        stopRotationAnimation()

        rotationAnimator = ObjectAnimator.ofFloat(
            this,
            "rotation",
            0f,
            360f
        ).apply {
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

    override fun onColorChange(color: StatusColor) {
        this.currentStatusColor = color
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRotationAnimation()
    }
}