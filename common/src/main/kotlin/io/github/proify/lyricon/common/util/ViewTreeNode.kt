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