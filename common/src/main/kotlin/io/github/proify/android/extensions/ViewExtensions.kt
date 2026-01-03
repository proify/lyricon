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

inline fun ViewGroup.doOnChildrenChanged(
    crossinline onAdded: (child: View, count: Int) -> Unit = { _, _ -> },
    crossinline onRemoved: (child: View, count: Int) -> Unit = { _, _ -> }
) {
    setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewAdded(parent: View?, child: View?) {
            child?.let { onAdded(it, childCount) }
        }

        override fun onChildViewRemoved(parent: View?, child: View?) {
            child?.let { onRemoved(it, childCount) }
        }
    })
}