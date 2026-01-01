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

package io.github.proify.lyricon.subscriber

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import io.github.proify.lyricon.subscriber.service.SubscriberService
import io.github.proify.lyricon.subscriber.service.SubscriberServiceImpl

class LyricSubscriber(var context: Context) {
    val subscriberInfo: SubscriberInfo =
        SubscriberInfo(context.packageName, context.applicationInfo.processName)

    private val binder: RemoteSubscriberBinder

    val service: SubscriberService

    private val broadcastReceiver: EventBroadcastReceiver = EventBroadcastReceiver()

    init {
        val serviceImpl = SubscriberServiceImpl(this)
        binder = RemoteSubscriberBinder(this, serviceImpl)
        service = serviceImpl

        initReceiver()
    }

    private fun initReceiver() {
        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_CENTRAL_BOOT_COMPLETED)

        ContextCompat.registerReceiver(
            context,
            broadcastReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    val isActivate: Boolean get() = service.isActivate

    fun notifyRegister() {
        val bundle = Bundle()
        bundle.putBinder(Constants.EXTRA_BINDER, binder)

        val intent = Intent(Constants.ACTION_REGISTER_SUBSCRIBER)
            .setPackage(Constants.CENTRAL_PACKAGE_NAME)
            .putExtra(Constants.EXTRA_BUNDLE, bundle)
        context.sendBroadcast(intent)
    }

    class EventBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context?, intent: Intent?
        ) {
            when (intent?.action) {
                Constants.ACTION_CENTRAL_BOOT_COMPLETED -> {

                }
            }
        }
    }

    companion object {
        private const val TAG = "Subscriber"
    }
}