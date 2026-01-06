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

package io.github.proify.android.extensions

import android.view.View
import android.view.ViewGroup

var View.visibilityIfChanged
    get() = visibility
    set(value) {
        if (visibility != value) visibility = value
    }

inline fun ViewGroup.setOnHierarchyChangeListener(
    crossinline block: HierarchyChangeListener.() -> Unit
) {
    val listener = HierarchyChangeListener().apply(block)

    setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewAdded(parent: View?, child: View?) {
            child?.let { listener.onAddView(this@setOnHierarchyChangeListener, it) }
        }

        override fun onChildViewRemoved(parent: View?, child: View?) {
            child?.let { listener.onRemoveView(this@setOnHierarchyChangeListener, it) }
        }
    })
}

class HierarchyChangeListener {
    var onAddView: (ViewGroup, View) -> Unit = { _, _ -> }
    var onRemoveView: (ViewGroup, View) -> Unit = { _, _ -> }

    fun onAdd(block: (ViewGroup, View) -> Unit) {
        onAddView = block
    }

    fun onRemove(block: (ViewGroup, View) -> Unit) {
        onRemoveView = block
    }
}