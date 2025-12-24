package io.github.proify.lyricon.app.ui.preference

import android.content.SharedPreferences
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.SwitchColors
import top.yukonga.miuix.kmp.basic.SwitchDefaults
import top.yukonga.miuix.kmp.extra.SuperSwitch

@Composable
fun SwitchPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: Boolean = false,
    title: String,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    leftAction: @Composable (() -> Unit)? = null,
    rightActions: @Composable RowScope.() -> Unit = {},
    switchColors: SwitchColors = SwitchDefaults.switchColors(),
    modifier: Modifier = Modifier,
    insideMargin: PaddingValues = BasicComponentDefaults.InsideMargin,
    onClick: (() -> Unit)? = null,
    holdDownState: Boolean = false,
    enabled: Boolean = true
) {
    val checked = rememberBooleanPreference(sharedPreferences, key, defaultValue)

    val hapticFeedback = LocalHapticFeedback.current

    SuperSwitch(
        checked = checked.value,
        onCheckedChange = {
            hapticFeedback.performHapticFeedback(
                if (it) HapticFeedbackType.ToggleOn
                else HapticFeedbackType.ToggleOff
            )
            checked.value = it
        },
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        leftAction = leftAction,
        rightActions = rightActions,
        switchColors = switchColors,
        modifier = modifier,
        insideMargin = insideMargin,
        onClick = onClick,
        holdDownState = holdDownState,
        enabled = enabled
    )
}