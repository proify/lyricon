package io.github.proify.lyricon.app.bridge

import android.content.Context
import androidx.annotation.Keep
import com.highcapable.yukihookapi.YukiHookAPI
import io.github.proify.lyricon.common.Constants
import java.io.File

object Bridge {

    fun isModuleActive(): Boolean = YukiHookAPI.Status.isXposedModuleActive

    @Keep
    fun getPreferenceDirectory(context: Context): File {
        return File(context.filesDir, "preferences")
    }

    @Keep
    fun getPreferenceFile(context: Context, name: String): File {
        return File(getPreferenceDirectory(context), getPreferenceFormatedFileName(name))
    }

    fun getPreferenceFormatedFileName(name: String) = "${name}.json"

    object LyricStylePrefs {

        const val DEFAULT_PACKAGE_NAME = Constants.APP_PACKAGE_NAME

        const val PREF_NAME_BASE_STYLE = "baseLyricStyle"

        const val PREF_PACKAGE_STYLE_MANAGER = "packageStyleManager"

        const val KEY_ENABLED_PACKAGES = "enables"

        fun getPackageStylePreferenceName(packageName: String): String =
            "package_style_${packageName.replace(".", "_")}"

    }

}