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