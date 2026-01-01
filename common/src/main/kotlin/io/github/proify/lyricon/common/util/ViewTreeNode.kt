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

import android.view.View
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.lang.ref.WeakReference

@Serializable
data class ViewTreeNode(
    @SerialName("id")
    var id: String? = null,
    @SerialName("name")
    var name: String? = null,
    @Transient
    var view: WeakReference<View>? = null
) {

    var children: MutableList<ViewTreeNode> = mutableListOf()

    fun addChild(child: ViewTreeNode) {
        children.add(child)
    }

    fun findById(id: String): ViewTreeNode? {
        if (id.isBlank()) return null
        if (this.id == id) return this

        for (child in children) {
            val node = child.findById(id)
            if (node != null) {
                return node
            }
        }
        return null
    }

    fun toJson(): String = Json.encodeToString(this)

}