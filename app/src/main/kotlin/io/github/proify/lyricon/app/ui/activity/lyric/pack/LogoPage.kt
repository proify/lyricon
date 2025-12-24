package io.github.proify.lyricon.app.ui.activity.lyric.pack

import android.content.SharedPreferences
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.ScrollBehavior
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.IconActions
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperCheckbox
import io.github.proify.lyricon.app.ui.preference.RectInputPreference
import io.github.proify.lyricon.app.ui.preference.SwitchPreference
import io.github.proify.lyricon.app.ui.preference.rememberIntPreference
import io.github.proify.lyricon.lyric.style.LogoStyle
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun LogoPage(scrollBehavior: ScrollBehavior, currentSp: SharedPreferences) {

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {

        item(key = "enable") {
            Card(
                modifier = Modifier
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                    .fillMaxWidth(),
            ) {
                SwitchPreference(
                    currentSp,
                    "lyric_style_logo_enable",
                    defaultValue = LogoStyle.Defaults.ENABLE,
                    leftAction = { IconActions(painterResource(R.drawable.ic_music_note)) },
                    title = "启用",
                )
                RectInputPreference(
                    currentSp,
                    "lyric_style_logo_margins",
                    "边距",
                    LogoStyle.Defaults.MARGINS,
                    leftAction = { IconActions(painterResource(R.drawable.ic_margin)) },
                )
            }

        }

        item(key = "coloros") {
            SmallTitle(
                text = "ColorOS",
                insideMargin = PaddingValues(
                    start = 26.dp,
                    top = 16.dp,
                    end = 26.dp,
                    bottom = 10.dp
                )
            )
            Card(
                modifier = Modifier
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                    .fillMaxWidth(),
            ) {

                SwitchPreference(
                    currentSp,
                    "lyric_style_logo_hide_in_coloros_capsule_mode",
                    defaultValue = LogoStyle.Defaults.HIDE_IN_COLOROS_CAPSULE_MODE,
                    leftAction = { IconActions(painterResource(R.drawable.ic_visibility_off)) },
                    title = "在流体云模式下隐藏"
                )
            }
        }

        item(key = "logo_options") {
            SmallTitle(
                text = "样式",
                insideMargin = PaddingValues(
                    start = 26.dp,
                    top = 16.dp,
                    end = 26.dp,
                    bottom = 10.dp
                )
            )
            Card(
                modifier = Modifier
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
            ) {

                val logoStyle =
                    rememberIntPreference(
                        currentSp,
                        "lyric_style_logo_style",
                        LogoStyle.Defaults.STYLE
                    )

                val logoStyleNames = remember { listOf("默认", "音乐封面(方形)", "音乐封面(圆形)") }
                val logoStyleValues =
                    remember {
                        listOf(
                            LogoStyle.STYLE_DEFAULT,
                            LogoStyle.STYLE_COVER_SQUIRCLE,
                            LogoStyle.STYLE_COVER_CIRCLE
                        )
                    }
                val checkedIndex = logoStyleValues.indexOf(logoStyle.value)

                logoStyleNames.forEachIndexed { index, item ->
                    SuperCheckbox(
                        title = item,
                        checked = checkedIndex == index,
                        onCheckedChange = {
                            currentSp.edit {
                                putInt("lyric_style_logo_style", logoStyleValues[index])
                            }
                        }
                    )
                }
            }
        }
        item(key = "color") {
            SmallTitle(
                text = "颜色",
                insideMargin = PaddingValues(
                    start = 26.dp,
                    top = 16.dp,
                    end = 26.dp,
                    bottom = 10.dp
                )
            )
            Card(
                modifier = Modifier
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                    .fillMaxWidth(),
            ) {

                SwitchPreference(
                    currentSp,
                    "lyric_style_logo_enable_custom_color",
                    title = "自定义颜色",
                    leftAction = { IconActions(painterResource(R.drawable.ic_alette)) },
                )
                SuperArrow(
                    title = "亮场景下颜色",
                    leftAction = { IconActions(painterResource(R.drawable.ic_brightness7)) },
                )
                SuperArrow(
                    title = "暗场景下颜色",
                    leftAction = { IconActions(painterResource(R.drawable.ic_darkmode)) },
                )
            }
        }
    }
}