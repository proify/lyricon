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

package io.github.proify.lyricon.central

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

object BridgeCentral {
    private lateinit var context: Context
    private val receiver = CentralReceiver
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        this.context = context.applicationContext
        registerReceiver()
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(Constants.ACTION_REGISTER_PROVIDER)
            addAction(Constants.ACTION_REGISTER_SUBSCRIBER)
        }

        ContextCompat.registerReceiver(
            context, receiver, filter, ContextCompat.RECEIVER_EXPORTED
        )
    }

    fun sendBootCompleted() {
        context.sendBroadcast(Intent(Constants.ACTION_CENTRAL_BOOT_COMPLETED))
    }
}