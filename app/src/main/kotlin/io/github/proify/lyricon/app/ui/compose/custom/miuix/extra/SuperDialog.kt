package io.github.proify.lyricon.app.ui.compose.custom.miuix.extra

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.extra.SuperDialogDefaults

@Suppress("unused")
@Composable
fun SuperDialog(
    show: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = SuperDialogDefaults.titleColor(),
    summary: String? = null,
    summaryColor: Color = SuperDialogDefaults.summaryColor(),
    backgroundColor: Color = SuperDialogDefaults.backgroundColor(),
    enableWindowDim: Boolean = true,
    onDismissRequest: (() -> Unit)? = {
        show.value = false
    },
    outsideMargin: DpSize = SuperDialogDefaults.outsideMargin,
    insideMargin: DpSize = SuperDialogDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    content: @Composable () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(show.value) {
        if (show.value.not()) keyboardController?.hide()
    }

    top.yukonga.miuix.kmp.extra.SuperDialog(
        show,
        modifier,
        title,
        titleColor,
        summary,
        summaryColor,
        backgroundColor,
        enableWindowDim,
        onDismissRequest,
        outsideMargin,
        insideMargin,
        defaultWindowInsetsPadding
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}