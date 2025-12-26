package io.github.proify.lyricon.common.util

import android.graphics.Bitmap
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import io.github.proify.android.extensions.toBitmap
import java.io.File
import java.io.FileInputStream

data class SVGHelper(val svg: SVG) {
    fun createPicture(picture: Picture, width: Int, height: Int): Picture {
        val newPicture = Picture()

        val canvas = newPicture.beginRecording(width, height)
        canvas.drawPicture(picture, Rect(0, 0, width, height))
        newPicture.endRecording()
        return newPicture
    }

    fun createPicture(width: Int, height: Int): Picture {
        return createPicture(renderToPicture(), width, height)
    }

    fun createPicture(size: Int): Picture {
        return createPicture(size, size)
    }

    fun createDrawable(picture: Picture, width: Int, height: Int): PictureDrawable {
        return PictureDrawable(createPicture(picture, width, height))
    }

    fun renderToPicture(): Picture {
        return svg.renderToPicture()
    }

    fun createDrawable(size: Int): PictureDrawable {
        return createDrawable(size, size)
    }

    fun createDrawable(width: Int, height: Int): PictureDrawable {
        return createDrawable(renderToPicture(), width, height)
    }

    fun createBitmap(): Bitmap {
        val drawable = createDrawable()
        return drawable.toBitmap()
    }

    fun createDrawable(): Drawable {
        val picture = renderToPicture()
        return createDrawable(picture, picture.getWidth(), picture.getWidth())
    }

    fun createBitmap(size: Int): Bitmap {
        return createBitmap(size, size)
    }

    fun createBitmap(width: Int, height: Int): Bitmap {
        return createDrawable(width, height).toBitmap()
    }

    companion object {
        fun create(svg: String): SVGHelper? {
            try {
                val svg = SVG.getFromString(svg)
                return SVGHelper(svg)

            } catch (ignored: Exception) {
            }
            return null
        }

        fun create(file: File): SVGHelper? {
            try {
                FileInputStream(file).use { `is` ->
                    val svg = SVG.getFromInputStream(`is`)
                    return SVGHelper(svg)
                }
            } catch (ignored: Exception) {
            }
            return null
        }
    }
}