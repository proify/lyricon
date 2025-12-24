package io.github.proify.lyricon.lyric.subscriber

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import io.github.proify.lyricon.lyric.bridge.core.Constants
import io.github.proify.lyricon.lyric.bridge.subscriber.SubscriberInfo
import io.github.proify.lyricon.lyric.subscriber.service.SubscriberService
import io.github.proify.lyricon.lyric.subscriber.service.SubscriberServiceImpl

class Subscriber(var context: Context) {
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