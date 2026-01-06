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

import android.os.Parcelable
import android.view.View
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.lang.ref.WeakReference

@Serializable
@Parcelize
data class ViewTreeNode(
    var id: String? = null,
    var name: String,
    var children: List<ViewTreeNode>? = null,
    @Transient
    @IgnoredOnParcel
    var view: WeakReference<View>? = null
) : Parcelable {

    fun findById(id: String?): ViewTreeNode? {
        if (this.id == id) return this
        children?.forEach {
            it.findById(id)?.let { return it }
        }
        return null
    }

    fun toJson(): String = Json.encodeToString(this)
}