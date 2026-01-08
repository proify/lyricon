/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.proify.lyricon.app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import io.github.proify.lyricon.app.BuildConfig
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.bridge.AppBridge
import io.github.proify.lyricon.app.event.SettingChangedEvent
import io.github.proify.lyricon.app.ui.activity.lyric.BasicLyricStyleActivity
import io.github.proify.lyricon.app.ui.activity.lyric.LyricProviderActivity
import io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.PackageStyleActivity
import io.github.proify.lyricon.app.ui.compose.AppToolBarListContainer
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponent
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentColors
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.CardColors
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperArrow
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperDialog
import io.github.proify.lyricon.app.util.Utils.killSystemUI
import io.github.proify.lyricon.app.util.Utils.restartApp
import io.github.proify.lyricon.app.util.collectEvent
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

class MainActivity : BaseActivity() {
    private val model: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent(
                showRestartFailDialog = model.showRestartFailDialog,
                onRestartSysUi = { killSystemUI() },
                onRestartApp = { restartApp() },
            )
        }

        collectEvent<SettingChangedEvent>(state = Lifecycle.State.CREATED) {
            recreate()
        }
    }

    class MyViewModel : ViewModel() {
        val showRestartFailDialog: MutableState<Boolean> = mutableStateOf(false)
    }
}

@Composable
fun MainContent(
    showRestartFailDialog: MutableState<Boolean>,
    onRestartSysUi: () -> Unit,
    onRestartApp: () -> Unit,
) {
    val context = LocalContext.current
    AppToolBarListContainer(
        title = stringResource(id = R.string.app_name),
        actions = {
            Actions(
                onRestartSysUi = onRestartSysUi,
                onRestartApp = onRestartApp,
            )
        },
        scaffoldContent = {
            RestartFailDialog(showRestartFailDialog)
        },
    ) { scope ->
        scope.item("state") {
            val isModuleActive = AppBridge.isModuleActive()
            Card(
                modifier =
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                insideMargin = PaddingValues(vertical = 7.dp),
                colors =
                    CardColors(
                        if (isModuleActive) Color(color = 0xFF4CAF50) else Color(color = 0xFFEF5350),
                        White,
                    ),
                pressFeedbackType = PressFeedbackType.Sink,
                onClick = {},
            ) {
                BasicComponent(
                    leftAction = {
                        Box(
                            modifier =
                                Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                modifier = Modifier.size(26.dp),
                                imageVector =
                                    ImageVector.vectorResource(
                                        if (isModuleActive) {
                                            R.drawable.ic_check_circle
                                        } else {
                                            R.drawable.ic_sentiment_dissatisfied
                                        },
                                    ),
                                tint = White,
                                contentDescription = null,
                            )
                        }
                    },
                    title =
                        stringResource(
                            if (isModuleActive) {
                                R.string.module_status_activated
                            } else {
                                R.string.module_status_not_activated
                            },
                        ),
                    titleColor = BasicComponentColors(color = White, disabledColor = White),
                    summary =
                        stringResource(
                            id = R.string.module_status_summary,
                            BuildConfig.VERSION_NAME,
                        ),
                    summaryColor =
                        BasicComponentColors(
                            color = Color(color = 0xAFFFFFFF),
                            disabledColor = White,
                        ),
                )
            }
        }
        scope.item("style") {
            Card(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
            ) {
                SuperArrow(
                    leftAction = {
                        IconBox(Color(color = 0xFF009688), R.drawable.ic_android)
                    },
                    title = stringResource(id = R.string.item_base_lyric_style),
                    summary = stringResource(id = R.string.item_summary_base_lyric_style),
                    onClick = {
                        context.startActivity(Intent(context, BasicLyricStyleActivity::class.java))
                    },
                )
                SuperArrow(
                    leftAction = {
                        IconBox(
                            Color(color = 0xFFFF9800),
                            R.drawable.ic_palette_swatch_variant,
                            iconSize = 22.dp,
                        )
                    },
                    title = stringResource(id = R.string.item_package_style_manager),
                    summary = stringResource(id = R.string.item_summary_package_style_manager),
                    onClick = {
                        context.startActivity(Intent(context, PackageStyleActivity::class.java))
                    },
                )
            }
        }
        scope.item("provider") {
            Card(
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
            ) {
                SuperArrow(
                    leftAction = {
                        IconBox(Color(color = 0xFF2196F3), R.drawable.ic_extension)
                    },
                    title = stringResource(id = R.string.item_provider_manager),
                    summary = stringResource(id = R.string.item_summary_provider_manager),
                    onClick = {
                        context.startActivity(Intent(context, LyricProviderActivity::class.java))
                    },
                )
            }
        }
        scope.item("other") {
            Card(
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
            ) {
                SuperArrow(
                    leftAction = {
                        IconBox(Color(color = 0xFF607D8B), R.drawable.ic_settings)
                    },
                    title = stringResource(id = R.string.item_app_settings),
                    summary = stringResource(id = R.string.item_summary_app_settings),
                    onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                )
                SuperArrow(
                    leftAction = {
                        IconBox(Color(color = 0xFF4CAF50), R.drawable.ic_info_fill)
                    },
                    title = stringResource(id = R.string.item_about_app),
                    summary = stringResource(id = R.string.item_summary_about_app),
                    onClick = {
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    },
                )
            }
        }
    }
}

@Composable
fun IconBox(
    backgroundColor: Color,
    iconRes: Int,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier =
            Modifier
                .padding(end = 16.dp)
                .size(40.dp)
                .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            modifier = if (iconSize != 24.dp) Modifier.size(iconSize) else Modifier,
            tint = White,
            contentDescription = null,
        )
    }
}

@Composable
fun RestartFailDialog(showState: MutableState<Boolean>) {
    SuperDialog(
        title = stringResource(R.string.restart_fail),
        summary = stringResource(R.string.message_app_restart_fail),
        show = showState,
        onDismissRequest = { showState.value = false },
    ) {
        TextButton(
            text = stringResource(R.string.ok),
            onClick = { showState.value = false },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun Actions(
    onRestartSysUi: () -> Unit,
    onRestartApp: () -> Unit,
) {
    val showPopup = remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(end = 14.dp)) {
        IconButton(
            onClick = { showPopup.value = true },
        ) {
            Icon(
                modifier = Modifier.size(26.dp),
                imageVector = MiuixIcons.Useful.Refresh,
                contentDescription = stringResource(id = R.string.restart),
                tint = MiuixTheme.colorScheme.onSurface,
            )
        }
        ListPopup(
            show = showPopup,
            alignment = PopupPositionProvider.Align.TopRight,
            onDismissRequest = { showPopup.value = false },
        ) {
            val items =
                listOf(
                    stringResource(R.string.restart_sui),
                    stringResource(R.string.restart_app),
                )
            ListPopupColumn {
                items.forEachIndexed { index, string ->
                    DropdownImpl(
                        text = string,
                        optionSize = items.size,
                        isSelected = false,
                        onSelectedIndexChange = {
                            if (index == 0) onRestartSysUi() else onRestartApp()
                            showPopup.value = false
                        },
                        index = index,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    val fakeShowDialog = remember { mutableStateOf(false) }
    MiuixTheme {
        MainContent(
            showRestartFailDialog = fakeShowDialog,
            onRestartSysUi = {},
            onRestartApp = {},
        )
    }
}