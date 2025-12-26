package io.github.proify.lyricon.lyric.style

import android.content.SharedPreferences
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PackageStyle(
    var logo: LogoStyle = LogoStyle(),
    var text: TextStyle = TextStyle(),
    var anim: AnimStyle = AnimStyle()
) : AbstractStyle(), Parcelable {

    override fun onLoad(preferences: SharedPreferences) {
        logo.load(preferences)
        text.load(preferences)
        anim.load(preferences)
    }

    override fun onWrite(editor: SharedPreferences.Editor) {
        logo.write(editor)
        text.write(editor)
        anim.write(editor)
    }
}