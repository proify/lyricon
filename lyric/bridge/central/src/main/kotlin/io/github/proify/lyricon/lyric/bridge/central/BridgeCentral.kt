package io.github.proify.lyricon.lyric.bridge.central

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import io.github.proify.lyricon.lyric.bridge.core.Constants

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