package io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.page

import android.content.SharedPreferences
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.ScrollBehavior
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.IconActions
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperCheckbox
import io.github.proify.lyricon.app.ui.preference.InputPreference
import io.github.proify.lyricon.app.ui.preference.InputType
import io.github.proify.lyricon.app.ui.preference.LogoColorPreference
import io.github.proify.lyricon.app.ui.preference.RectInputPreference
import io.github.proify.lyricon.app.ui.preference.SwitchPreference
import io.github.proify.lyricon.app.ui.preference.rememberIntPreference
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.lyric.style.LogoStyle
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun LogoPage(
    scrollBehavior: ScrollBehavior,
    sharedPreferences: SharedPreferences
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        item(key = "enable") {
            SmallTitle(
                text = stringResource(R.string.item_logo_section_basic),
                insideMargin = PaddingValues(
                    start = 26.dp,
                    end = 26.dp,
                    bottom = 10.dp
                )
            )
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                SwitchPreference(
                    sharedPreferences,
                    "lyric_style_logo_enable",
                    defaultValue = LogoStyle.Defaults.ENABLE,
                    leftAction = {
                        IconActions(painterResource(R.drawable.ic_music_note))
                    },
                    title = stringResource(R.string.item_logo_enable),
                )
                InputPreference(
                    sharedPreferences,
                    "lyric_style_logo_width",
                    syncKeys = arrayOf("lyric_style_logo_height"),
                    inputType = InputType.DOUBLE,
                    maxValue = 100.0,
                    title = stringResource(R.string.item_logo_size),
                    leftAction = { IconActions(painterResource(R.drawable.ic_format_size)) },
                )
                RectInputPreference(
                    sharedPreferences,
                    "lyric_style_logo_margins",
                    stringResource(R.string.item_logo_margins),
                    LogoStyle.Defaults.MARGINS,
                    leftAction = {
                        IconActions(painterResource(R.drawable.ic_margin))
                    },
                )
            }
        }

        item(key = "coloros") {
            SmallTitle(
                text = stringResource(R.string.item_logo_section_coloros),
                insideMargin = PaddingValues(
                    start = 26.dp,
                    top = 16.dp,
                    end = 26.dp,
                    bottom = 10.dp
                )
            )

            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                SwitchPreference(
                    sharedPreferences,
                    "lyric_style_logo_hide_in_coloros_capsule_mode",
                    defaultValue = LogoStyle.Defaults.HIDE_IN_COLOROS_CAPSULE_MODE,
                    leftAction = {
                        IconActions(painterResource(R.drawable.ic_visibility_off))
                    },
                    title = stringResource(R.string.item_logo_hide_in_coloros_capsule_mode),
                )
            }
        }

        item(key = "logo_options") {
            SmallTitle(
                text = stringResource(R.string.item_logo_section_style),
                insideMargin = PaddingValues(
                    start = 26.dp,
                    top = 16.dp,
                    end = 26.dp,
                    bottom = 10.dp
                )
            )

            Card(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
                    .fillMaxWidth()
            ) {

                val logoStyle = rememberIntPreference(
                    sharedPreferences,
                    "lyric_style_logo_style",
                    LogoStyle.Defaults.STYLE
                )

                val styleNameRes = listOf(
                    R.string.item_logo_style_default,
                    R.string.item_logo_style_cover_square,
                    R.string.item_logo_style_cover_circle
                )

                val styleValues = listOf(
                    LogoStyle.STYLE_DEFAULT,
                    LogoStyle.STYLE_COVER_SQUIRCLE,
                    LogoStyle.STYLE_COVER_CIRCLE
                )

                val checkedIndex = styleValues.indexOf(logoStyle.value)

                styleNameRes.forEachIndexed { index, resId ->
                    SuperCheckbox(
                        title = stringResource(resId),
                        checked = checkedIndex == index,
                        onCheckedChange = {
                            sharedPreferences.commitEdit {
                                putInt(
                                    "lyric_style_logo_style",
                                    styleValues[index]
                                )
                            }
                        }
                    )
                }
            }
        }

        item(key = "color") {
            SmallTitle(
                text = stringResource(R.string.item_logo_section_color),
                insideMargin = PaddingValues(
                    start = 26.dp,
                    top = 16.dp,
                    end = 26.dp,
                    bottom = 10.dp
                )
            )

            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                SwitchPreference(
                    sharedPreferences,
                    "lyric_style_logo_enable_custom_color",
                    title = stringResource(R.string.item_logo_custom_color),
                    leftAction = {
                        IconActions(painterResource(R.drawable.ic_palette))
                    },
                )

                LogoColorPreference(
                    sharedPreferences,
                    "lyric_style_logo_color_light_mode",
                    defaultColor = Color.Black,
                    title = stringResource(R.string.item_logo_color_light),
                    leftAction = {
                        IconActions(painterResource(R.drawable.ic_brightness7))
                    },
                )

                LogoColorPreference(
                    sharedPreferences,
                    "lyric_style_logo_color_dark_mode",
                    defaultColor = Color.White,
                    title = stringResource(R.string.item_logo_color_dark),
                    leftAction = {
                        IconActions(painterResource(R.drawable.ic_darkmode))
                    },
                )
            }
        }
        item(key = "spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}