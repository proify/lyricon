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

fun toast(any: Any, longDuration: Boolean = false) {
    Toasty.show(any.toString(), longDuration)
}