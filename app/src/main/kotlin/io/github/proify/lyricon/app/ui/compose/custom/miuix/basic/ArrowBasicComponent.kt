/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package io.github.proify.lyricon.app.ui.compose.custom.miuix.basic
//
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.RowScope
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import top.yukonga.miuix.kmp.basic.Icon
//import top.yukonga.miuix.kmp.icon.MiuixIcons
//import top.yukonga.miuix.kmp.icon.icons.basic.ArrowRight
//import top.yukonga.miuix.kmp.theme.MiuixTheme
//
///**
// * A basic component with Miuix style. Widely used in other extension components.
// *
// * @param title The title of the [BasicComponent].
// * @param titleColor The color of the title.
// * @param summary The summary of the [BasicComponent].
// * @param summaryColor The color of the summary.
// * @param leftAction The [Composable] content that on the left side of the [BasicComponent].
// * @param rightActions The [Composable] content on the right side of the [BasicComponent].
// * @param modifier The modifier to be applied to the [BasicComponent].
// * @param insideMargin The margin inside the [BasicComponent].
// * @param onClick The callback when the [BasicComponent] is clicked.
// * @param holdDownState Used to determine whether it is in the pressed state.
// * @param enabled Whether the [BasicComponent] is enabled.
// * @param interactionSource The [MutableInteractionSource] for the [BasicComponent].
// */
//@Composable
//fun ArrowBasicComponent(
//    title: String? = null,
//    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
//    summary: String? = null,
//    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
//    leftAction: @Composable (() -> Unit)? = null,
//    rightActions: @Composable (RowScope.() -> Unit)? = {
//        Icon(
//            imageVector = MiuixIcons.Basic.ArrowRight,
//            tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
//            contentDescription = null
//        )
//    },
//    modifier: Modifier = Modifier,
//    insideMargin: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
//    onClick: (() -> Unit)? = null,
//    holdDownState: Boolean = false,
//    enabled: Boolean = true,
//    interactionSource: MutableInteractionSource? = null,
//) {
//    BasicComponent(
//        title = title,
//        titleColor = titleColor,
//        summary = summary,
//        summaryColor = summaryColor,
//        leftAction = leftAction,
//        rightActions = rightActions,
//        modifier = modifier,
//        insideMargin = insideMargin,
//        onClick = onClick,
//        holdDownState = holdDownState,
//        enabled = enabled,
//        interactionSource = interactionSource
//    )
//}
