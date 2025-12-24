package io.github.proify.lyricon.app.util

import android.widget.Toast
import io.github.proify.lyricon.app.Application

open class Toasty {

    companion object {
        fun show(text: CharSequence, longDuration: Boolean = false) {
            Toast.makeText(
                Application.instance,
                text,
                if (longDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }

        fun show(text: Int) = show(Application.instance.getString(text), true)

        fun showLong(text: CharSequence) = show(text, false)
    }

}