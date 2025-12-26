package io.github.proify.lyricon.app.ui.activity.lyric

import android.content.SharedPreferences
import com.highcapable.yukihookapi.hook.factory.dataChannel
import io.github.proify.lyricon.app.Application
import io.github.proify.lyricon.app.bridge.BridgeConstants
import io.github.proify.lyricon.app.ui.activity.BaseActivity
import io.github.proify.lyricon.common.PackageNames

abstract class BaseLyricActivity : BaseActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    val systemUIChannel by lazy {
        dataChannel(packageName = PackageNames.SYSTEM_UI)
    }

    fun updateLyricStyle() {
        Application.MAIN_HANDLER.postDelayed({
            systemUIChannel.put(BridgeConstants.REQUEST_UPDATE_LYRIC_STYLE)
        }, 0)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        if (key?.startsWith("lyric_style_") == true) {
            updateLyricStyle()
        }
    }
}