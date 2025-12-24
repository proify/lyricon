package io.github.proify.lyricon.lyric.provider

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import io.github.proify.lyricon.lyric.bridge.core.Constants
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

internal object CentralServiceReceiver {

    private val isInitialized = AtomicBoolean(false)
    private val listeners = CopyOnWriteArraySet<ServiceListener>()

    val initialized: Boolean
        get() = isInitialized.get()

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