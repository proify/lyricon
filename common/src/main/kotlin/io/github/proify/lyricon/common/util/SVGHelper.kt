/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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