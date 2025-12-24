package io.github.proify.lyricon.app.ui.activity.lyric

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.activity.lyric.tree.AnchorViewTreeActivity
import io.github.proify.lyricon.app.ui.activity.lyric.tree.ViewRulesTreeActivity
import io.github.proify.lyricon.app.ui.compose.AppToolBarListContainer
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.IconActions
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperArrow
import io.github.proify.lyricon.app.ui.preference.InputPreference
import io.github.proify.lyricon.app.ui.preference.InputType
import io.github.proify.lyricon.app.ui.preference.RectInputPreference
import io.github.proify.lyricon.app.ui.preference.rememberStringPreference
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.lyric.style.BasicStyle
import top.yukonga.miuix.kmp.extra.SpinnerEntry
import top.yukonga.miuix.kmp.extra.SuperSpinner

class BasicLyricStyleActivity : BaseLyricActivity() {

    val preferences by lazy { LyricPrefs.basicStylePrefs }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences.registerOnSharedPreferenceChangeListener(this)
        setContent {
            Content()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    @Composable
    private fun Content() {
        AppToolBarListContainer(canBack = true) { scope ->
            scope.item {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {
        val context = LocalContext.current

        Card(
            modifier = Modifier
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                .fillMaxWidth(),
        ) {
            val anchor = rememberStringPreference(
                preferences, "lyric_style_base_anchor",
                BasicStyle.Defaults.ANCHOR
            )

            SuperArrow(
                title = "锚点",
                leftAction = { IconActions(painterResource(R.drawable.ic_locationon)) },
                summary = anchor.value,
                onClick = {
                    context.startActivity(Intent(context, AnchorViewTreeActivity::class.java))
                })

            val insertionOrder = preferences.getInt(
                "lyric_style_base_insertion_order",
                BasicStyle.Defaults.INSERTION_ORDER
            )
            val insertionOrderSelectedIndex = remember { mutableIntStateOf(0) }

            val insertionOrderOptions = listOf(
                SpinnerEntry(title = "之前"),
                SpinnerEntry(title = "之后"),
            )
            val insertionOrderOptionKeys = listOf(
                BasicStyle.INSERTION_ORDER_BEFORE,
                BasicStyle.INSERTION_ORDER_AFTER
            )

            insertionOrderOptionKeys.forEachIndexed { index, key ->
                if (insertionOrder == key) {
                    insertionOrderSelectedIndex.intValue = index
                }
            }

            SuperSpinner(
                leftAction = { IconActions(painterResource(R.drawable.ic_stack)) },
                title = "插入顺序",
                items = insertionOrderOptions,
                selectedIndex = insertionOrderSelectedIndex.intValue,
                onSelectedIndexChange = {
                    insertionOrderSelectedIndex.intValue = it
                    preferences.edit {
                        putInt("lyric_style_base_insertion_order", insertionOrderOptionKeys[it])
                    }
                }
            )

            InputPreference(
                preferences,
                "lyric_style_base_width",
                leftAction = { IconActions(painterResource(R.drawable.ic_width_normal)) },
                inputType = InputType.DOUBLE,
                maxValue = 1000.0,
                title = "宽度",
            )
            InputPreference(
                preferences,
                "lyric_style_base_width_in_coloros_capsule_mode",
                leftAction = { IconActions(painterResource(R.drawable.ic_width_normal)) },
                inputType = InputType.DOUBLE,
                maxValue = 1000.0,
                title = "宽度（流体云模式下）",
            )
            RectInputPreference(
                preferences,
                "lyric_style_base_margins",
                "边距",
                leftAction = { IconActions(painterResource(R.drawable.ic_margin)) },
            )
            RectInputPreference(
                preferences,
                "lyric_style_base_paddings",
                "内边距",
                leftAction = { IconActions(painterResource(R.drawable.ic_padding)) },
            )
        }

        Card(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
        ) {
            SuperArrow(
                leftAction = { IconActions(painterResource(R.drawable.ic_visibility)) },
                title = stringResource(id = R.string.item_title_view_rules),
                onClick = {
                    context.startActivity(Intent(context, ViewRulesTreeActivity::class.java))
                }
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun ContentPreview() {
        Content()
    }

}