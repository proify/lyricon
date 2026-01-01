/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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