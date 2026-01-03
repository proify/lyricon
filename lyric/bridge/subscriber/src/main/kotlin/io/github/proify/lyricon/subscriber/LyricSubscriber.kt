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

package io.github.proify.lyricon.subscriber

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import io.github.proify.lyricon.subscriber.service.SubscriberService
import io.github.proify.lyricon.subscriber.service.SubscriberServiceImpl

//不完善，目前只内部使用
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