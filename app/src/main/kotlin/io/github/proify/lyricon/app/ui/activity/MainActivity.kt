/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.proify.lyricon.app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import io.github.proify.lyricon.app.BuildConfig
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.bridge.Bridge
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
import io.github.proify.lyricon.app.util.Utils
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
    private val model = MyViewModel()

    class MyViewModel : ViewModel() {
        val showRestartFailDialog = mutableStateOf(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(Modifier.fillMaxSize()) {
                MainContent()
            }
        }

        collectEvent<SettingChangedEvent>(state = Lifecycle.State.CREATED) {
            recreate()
        }
    }

    @Composable
    private fun MainContent() {
        AppToolBarListContainer(
            title = stringResource(id = R.string.app_name),
            actions = { Actions() },
            scaffoldContent = {
                RestartFailDialog()
            }
        ) { scope ->

            scope.item("state") {
                val isModuleActive = Bridge.isModuleActive()
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    insideMargin = PaddingValues(vertical = 7.dp),
                    colors = CardColors(
                        if (isModuleActive) Color(0xFF4CAF50) else Color(0xFFEF5350),
                        White
                    ),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = {
                    }
                ) {
                    BasicComponent(
                        leftAction = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier.size(26.dp),
                                    imageVector = ImageVector.vectorResource(
                                        if (isModuleActive)
                                            R.drawable.ic_check_circle
                                        else
                                            R.drawable.ic_sentiment_dissatisfied
                                    ),
                                    tint = White,
                                    contentDescription = null
                                )
                            }
                        },
                        title = stringResource(if (isModuleActive) R.string.module_status_activated else R.string.module_status_not_activated),
                        titleColor = BasicComponentColors(
                            color = White,
                            disabledColor = White
                        ),
                        summary = stringResource(
                            id = R.string.module_status_summary,
                            BuildConfig.VERSION_NAME
                        ),
                        summaryColor = BasicComponentColors(
                            color = Color(0xAFFFFFFF),
                            disabledColor = White
                        )
                    )
                }
            }

            scope.item("style") {
                val context = LocalContext.current
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                ) {
                    SuperArrow(
                        leftAction = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp)
                                    .background(Color(0xFF009688), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_android),
                                    tint = White,
                                    contentDescription = null
                                )
                            }
                        },
                        title = stringResource(id = R.string.item_base_lyric_style),
                        summary = stringResource(id = R.string.item_summary_base_lyric_style),
                        onClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    BasicLyricStyleActivity::class.java
                                )
                            )
                        }
                    )
                    SuperArrow(
                        leftAction = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp)
                                    .background(Color(0xFFFF9800), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    painter = painterResource(id = R.drawable.ic_palette_swatch_variant),
                                    tint = White,
                                    contentDescription = null
                                )
                            }
                        },
                        title = stringResource(id = R.string.item_package_style_manager),
                        summary = stringResource(id = R.string.item_summary_package_style_manager),
                        onClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    PackageStyleActivity::class.java
                                )
                            )
                        }
                    )
                }
            }

            scope.item("provider") {
                val context = LocalContext.current
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                ) {
                    SuperArrow(
                        leftAction = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp)
                                    .background(Color(0xFF2196f3), CircleShape),

                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_extension),
                                    tint = White,
                                    contentDescription = null
                                )
                            }
                        },
                        title = stringResource(id = R.string.item_provider_manager),
                        summary = stringResource(id = R.string.item_summary_provider_manager),
                        onClick = {
                            startActivity(
                                Intent(
                                    context,
                                    LyricProviderActivity::class.java
                                )
                            )
                        }
                    )
                }
            }
            scope.item("other") {
                val context = LocalContext.current
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                ) {
                    SuperArrow(
                        leftAction = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp)
                                    .background(Color(0xFF607d8b), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_settings),
                                    tint = White,
                                    contentDescription = null
                                )
                            }
                        },
                        title = stringResource(id = R.string.item_app_settings),
                        summary = stringResource(id = R.string.item_summary_app_settings),
                        onClick = {
                            startActivity(Intent(context, SettingsActivity::class.java))
                        }
                    )
                    SuperArrow(
                        leftAction = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp)
                                    .background(Color(0xFF4caf50), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_info_fill),
                                    tint = White,
                                    contentDescription = null
                                )
                            }
                        },
                        title = stringResource(id = R.string.item_about_app),
                        summary = stringResource(id = R.string.item_summary_about_app),
                        onClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    AboutActivity::class.java
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun RestartFailDialog() {
        SuperDialog(
            title = stringResource(id = R.string.restart_fail),
            summary = stringResource(id = R.string.message_app_restart_fail),
            show = model.showRestartFailDialog,
            onDismissRequest = { model.showRestartFailDialog.value = false }
        ) {
            TextButton(
                text = stringResource(id = R.string.ok),
                onClick = { model.showRestartFailDialog.value = false },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun Actions() {
        val showPopup = remember { mutableStateOf(false) }
        Box(modifier = Modifier.padding(end = 14.dp)) {
            IconButton(
//                modifier = Modifier
//                    .size(40.dp)
//                    .background(MiuixTheme.colorScheme.surfaceContainer, CircleShape),
                onClick = { showPopup.value = true }
            ) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = MiuixIcons.Useful.Refresh,
                    contentDescription = stringResource(id = R.string.restart),
                    tint = MiuixTheme.colorScheme.onSurface
                )
            }

            ListPopup(
                show = showPopup,
                alignment = PopupPositionProvider.Align.TopRight,
                onDismissRequest = { showPopup.value = false }
            ) {
                val items = listOf(
                    stringResource(id = R.string.restart_sui),
                    stringResource(id = R.string.restart_app)
                )
                ListPopupColumn {
                    items.forEachIndexed { index, string ->
                        DropdownImpl(
                            text = string,
                            optionSize = items.size,
                            isSelected = false,
                            onSelectedIndexChange = {
                                when (index) {
                                    0 -> killSystemUi()
                                    1 -> restartApp()
                                }
                                showPopup.value = false
                            },
                            index = index
                        )
                    }
                }
            }
        }
    }

    fun restartApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun killSystemUi() {
        if (Utils.killSystemUi().result == -1) {
            model.showRestartFailDialog.value = true
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun MainContentPreview() {
        MainContent()
    }

}