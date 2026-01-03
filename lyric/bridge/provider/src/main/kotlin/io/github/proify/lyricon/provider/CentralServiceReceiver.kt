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

package io.github.proify.lyricon.provider

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

internal object CentralServiceReceiver {
    private val isInitialized = AtomicBoolean(false)
    private val listeners = CopyOnWriteArraySet<ServiceListener>()

    fun addServiceListener(listener: ServiceListener) {
        listeners += listener
    }

    fun removeServiceListener(listener: ServiceListener) {
        listeners -= listener
    }

    fun initialize(context: Context) {
        if (!isInitialized.compareAndSet(false, true)) return

        ContextCompat.registerReceiver(
            context.applicationContext,
            ServiceReceiver,
            IntentFilter(Constants.ACTION_CENTRAL_BOOT_COMPLETED),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private object ServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.ACTION_CENTRAL_BOOT_COMPLETED) {
                listeners.forEach { it.onServiceBootCompleted() }
            }
        }
    }

    fun interface ServiceListener {
        fun onServiceBootCompleted()
    }
}