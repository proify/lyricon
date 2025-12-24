package io.github.proify.lyricon.app.ui.activity.lyric

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.highcapable.yukihookapi.hook.factory.dataChannel
import io.github.proify.lyricon.app.bridge.BridgeConstants
import io.github.proify.lyricon.app.ui.activity.BaseActivity
import io.github.proify.lyricon.common.PackageNames

open class BaseLyricActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    val systemUIChannel by lazy { dataChannel(packageName = PackageNames.SYSTEM_UI) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun updateLyricStyle() {
        systemUIChannel.put(BridgeConstants.REQUEST_UPDATE_LYRIC_STYLE)
    }

    private val handle = Handler(Looper.getMainLooper())

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        if (key?.startsWith("lyric_style_") == true) {
            handle.postDelayed({
                updateLyricStyle()
            }, 0)
        }
    }
}