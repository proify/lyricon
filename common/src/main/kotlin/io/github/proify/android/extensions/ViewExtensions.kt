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