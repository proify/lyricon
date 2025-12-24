package io.github.proify.lyricon.app.ui.activity.lyric.pack

import android.content.SharedPreferences
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.ScrollBehavior
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.IconActions
import io.github.proify.lyricon.app.ui.preference.CheckboxPreference
import io.github.proify.lyricon.app.ui.preference.InputPreference
import io.github.proify.lyricon.app.ui.preference.InputType
import io.github.proify.lyricon.app.ui.preference.RectInputPreference
import io.github.proify.lyricon.app.ui.preference.SwitchPreference
import io.github.proify.lyricon.app.ui.preference.TextColorPreference
import io.github.proify.lyricon.lyric.style.TextStyle
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun TextPage(scrollBehavior: ScrollBehavior, currentSp: SharedPreferences) {

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        item(key = "base") {
            SmallTitle(
                text = "基本",
                insideMargin = PaddingValues(
                    start = 26.dp,
                    top = 0.dp,
                    end = 26.dp,
                    bottom = 10.dp
                )
            )
            Card(
                modifier = Modifier
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                    .fillMaxWidth(),
            ) {
                InputPreference(
                    currentSp,
                    "lyric_style_text_size",
                    inputType = InputType.DOUBLE,
                    maxValue = 100.0,
                    title = "大小",
                    leftAction = { IconActions(painterResource(R.drawable.ic_format_size)) },
                )
                RectInputPreference(
                    currentSp,
                    "lyric_style_text_margins",
                    "边距",
                    defaultValue = TextStyle.Defaults.MARGINS,
                    leftAction = { IconActions(painterResource(R.drawable.ic_margin)) },
                )
                RectInputPreference(
                    currentSp,
                    "lyric_style_text_paddings",
                    "内边距",
                    defaultValue = TextStyle.Defaults.PADDINGS,
                    leftAction = { IconActions(painterResource(R.drawable.ic_padding)) },
                )
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
                    "lyric_style_text_enable_custom_color",
                    title = "自定义颜色",
                    leftAction = { IconActions(painterResource(R.drawable.ic_alette)) },
                )
                TextColorPreference(
                    currentSp,
                    "lyric_style_text_color_light_mode",
                    title = "亮场景下颜色",
                    defaultColor = Color.Black,
                    leftAction = { IconActions(painterResource(R.drawable.ic_brightness7)) },
                )
                TextColorPreference(
                    currentSp,
                    "lyric_style_text_color_dark_mode",
                    title = "暗场景下颜色",
                    defaultColor = Color.White,
                    leftAction = { IconActions(painterResource(R.drawable.ic_darkmode)) },
                )
            }
        }
        item(key = "font") {
            SmallTitle(
                text = "字体",
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
                InputPreference(
                    currentSp,
                    "lyric_style_text_typeface",
                    title = "自定义字体",
                    leftAction = { IconActions(painterResource(R.drawable.ic_fontdownload)) },
                )
                InputPreference(
                    currentSp,
                    inputType = InputType.INTEGER,
                    maxValue = 1000.0,
                    key = "lyric_style_text_weight",
                    title = "字重大小",
                    leftAction = { IconActions(painterResource(R.drawable.ic_fontdownload)) },
                )
                CheckboxPreference(
                    currentSp,
                    key = "lyric_style_text_typeface_bold",
                    title = "加粗样式",
                    leftAction = { IconActions(painterResource(R.drawable.ic_formatbold)) },
                )
                CheckboxPreference(
                    currentSp,
                    key = "lyric_style_text_typeface_italic",
                    title = "斜体样式",
                    leftAction = { IconActions(painterResource(R.drawable.ic_format_italic)) },
                )
            }
        }
        item(key = "marquee") {
            SmallTitle(
                text = "跑马灯",
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
                InputPreference(
                    currentSp,
                    "lyric_style_text_marquee_speed",
                    inputType = InputType.INTEGER,
                    maxValue = 200.0,
                    title = "速度",
                    leftAction = { IconActions(painterResource(R.drawable.ic_speed)) },
                )
                InputPreference(
                    currentSp,
                    "lyric_style_text_marquee_space",
                    inputType = InputType.INTEGER,
                    maxValue = 200.0,
                    title = "鬼影距离",
                    leftAction = { IconActions(painterResource(R.drawable.ic_space_bar)) },
                )
//                SwitchPreference(
//                    currentSp,
//                    "lyric_style_text_marquee_enable_delay",
//                    title = "启用滚动延迟",
//                    leftAction = { IconActions(painterResource(R.drawable.ic_timer)) },
//                )
                InputPreference(
                    currentSp,
                    "lyric_style_text_marquee_first_delay",
                    inputType = InputType.INTEGER,
                    maxValue = 1000.0,
                    title = "初始滚动延迟",
                    leftAction = { IconActions(painterResource(R.drawable.ic_autopause)) },
                )
                InputPreference(
                    currentSp,
                    "lyric_style_text_marquee_delay",
                    inputType = InputType.INTEGER,
                    maxValue = 1000.0,
                    title = "滚动延迟",
                    leftAction = { IconActions(painterResource(R.drawable.ic_autopause)) },
                )
                SwitchPreference(
                    currentSp,
                    "lyric_style_text_marquee_repeat_unlimited",
                    defaultValue = true,
                    title = "启用无限滚动",
                    leftAction = { IconActions(painterResource(R.drawable.ic_all_inclusive)) },
                )
                InputPreference(
                    currentSp,
                    "lyric_style_text_marquee_repeat_count",
                    inputType = InputType.INTEGER,
                    minValue = -1.0,
                    maxValue = 100.0,
                    title = "滚动次数",
                    leftAction = { IconActions(painterResource(R.drawable.ic_pin)) },
                )
                SwitchPreference(
                    currentSp,
                    "lyric_style_text_marquee_stop_at_end",
                    title = "结束时在末尾停止",
                    leftAction = { IconActions(painterResource(R.drawable.ic_stop_circle)) },
                )
                SwitchPreference(
                    currentSp,
                    "lyric_style_text_gradient_progress_style",
                    title = "渐变进度样式",
                    leftAction = { IconActions(painterResource(R.drawable.ic_gradient)) },
                )
            }
        }
    }
}