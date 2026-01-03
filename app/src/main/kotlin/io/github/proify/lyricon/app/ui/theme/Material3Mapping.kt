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

package io.github.proify.lyricon.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.Colors

// Helpers to flatten semi-transparent colors to visually-equivalent opaque colors
private fun compositeOver(fg: Color, bg: Color): Color {
    val fa = fg.alpha
    val ba = bg.alpha
    val outA = fa + ba * (1f - fa)
    if (outA == 0f) return Color(0f, 0f, 0f, 0f)
    val r = (fg.red * fa + bg.red * ba * (1f - fa)) / outA
    val g = (fg.green * fa + bg.green * ba * (1f - fa)) / outA
    val b = (fg.blue * fa + bg.blue * ba * (1f - fa)) / outA
    return Color(r, g, b, outA)
}

private fun opaqueOver(fg: Color, bg: Color): Color {
    val c = compositeOver(fg, bg)
    return Color(c.red, c.green, c.blue, 1f)
}

private fun ensureOpaqueOver(fg: Color, bg: Color): Color =
    if (fg.alpha >= 1f) fg else opaqueOver(fg, bg)

internal fun mapMd3ToMiuixColorsCommon(cs: ColorScheme, dark: Boolean): Colors {
    val baseSurface = cs.surface
    val baseSurfaceContainer = cs.surfaceContainer
    val baseSurfaceContainerHigh = cs.surfaceContainerHigh

    // Flatten secondaryContainer/onSecondaryContainer on surface
    val secondaryContainerOpaque = ensureOpaqueOver(cs.secondaryContainer, baseSurface)
    val onSecondaryContainerOpaque =
        ensureOpaqueOver(cs.onSecondaryContainer, secondaryContainerOpaque)

    // Flatten secondaryVariant/onSecondaryVariant on surface
    val secondaryVariantSource = cs.primary.copy(alpha = 0.24f)
    val secondaryVariantOpaque = ensureOpaqueOver(secondaryVariantSource, baseSurface)
    val onSecondaryVariantOpaque = ensureOpaqueOver(cs.secondary, secondaryVariantOpaque)

    // Flatten all other alpha usages to opaque
    val disabledPrimaryOpaque = ensureOpaqueOver(cs.primary.copy(alpha = 0.38f), baseSurface)
    val disabledOnPrimaryOpaque =
        ensureOpaqueOver(cs.onPrimary.copy(alpha = 0.38f), disabledPrimaryOpaque)

    val disabledPrimaryButtonOpaque = ensureOpaqueOver(cs.primary.copy(alpha = 0.38f), baseSurface)
    val disabledOnPrimaryButtonOpaque =
        ensureOpaqueOver(cs.onPrimary.copy(alpha = 0.6f), disabledPrimaryButtonOpaque)

    val disabledPrimarySliderOpaque = ensureOpaqueOver(cs.primary.copy(alpha = 0.38f), baseSurface)

    val secondaryOpaque = ensureOpaqueOver(cs.secondary.copy(alpha = 0.38f), baseSurface)

    val disabledSecondaryOpaque = ensureOpaqueOver(cs.secondary.copy(alpha = 0.24f), baseSurface)
    val disabledOnSecondaryOpaque =
        ensureOpaqueOver(cs.onSecondary.copy(alpha = 0.42f), disabledSecondaryOpaque)

    val disabledSecondaryVariantOpaque =
        ensureOpaqueOver(cs.secondary.copy(alpha = 0.12f), baseSurface)
    val disabledOnSecondaryVariantOpaque =
        ensureOpaqueOver(cs.secondary.copy(alpha = 0.24f), disabledSecondaryVariantOpaque)

    val onSurfaceSecondaryOpaque = ensureOpaqueOver(cs.onSurface.copy(alpha = 0.8f), baseSurface)
    val onSurfaceVariantSummaryOpaque =
        ensureOpaqueOver(cs.onSurface.copy(alpha = 0.6f), baseSurfaceContainer)
    val onSurfaceVariantActionsOpaque =
        ensureOpaqueOver(cs.onSurface.copy(alpha = 0.6f), baseSurfaceContainer)

    val onSurfaceContainerVariantOpaque =
        ensureOpaqueOver(cs.onSurfaceVariant.copy(alpha = 0.6f), baseSurfaceContainer)
    val onSurfaceContainerHighOpaque =
        ensureOpaqueOver(cs.onSurface.copy(alpha = 0.8f), baseSurfaceContainerHigh)

    return Colors(
        primary = cs.primary,
        onPrimary = cs.onPrimary,
        primaryVariant = cs.primaryFixed,
        onPrimaryVariant = cs.onPrimaryFixed,
        error = cs.error,
        onError = cs.onError,
        errorContainer = cs.errorContainer,
        onErrorContainer = cs.onErrorContainer,
        disabledPrimary = disabledPrimaryOpaque,
        disabledOnPrimary = disabledOnPrimaryOpaque,
        disabledPrimaryButton = disabledPrimaryButtonOpaque,
        disabledOnPrimaryButton = disabledOnPrimaryButtonOpaque,
        disabledPrimarySlider = disabledPrimarySliderOpaque,
        primaryContainer = cs.primaryContainer,
        onPrimaryContainer = cs.onPrimaryContainer,
        secondary = secondaryOpaque,
        onSecondary = cs.onSecondary,
        secondaryVariant = secondaryVariantOpaque,
        onSecondaryVariant = onSecondaryVariantOpaque,
        disabledSecondary = disabledSecondaryOpaque,
        disabledOnSecondary = disabledOnSecondaryOpaque,
        disabledSecondaryVariant = disabledSecondaryVariantOpaque,
        disabledOnSecondaryVariant = disabledOnSecondaryVariantOpaque,
        secondaryContainer = secondaryContainerOpaque,
        onSecondaryContainer = onSecondaryContainerOpaque,
        secondaryContainerVariant = secondaryContainerOpaque,
        onSecondaryContainerVariant = onSecondaryContainerOpaque,
        tertiaryContainer = cs.tertiaryContainer,
        onTertiaryContainer = cs.onTertiaryContainer,
        tertiaryContainerVariant = cs.onTertiaryContainer,
        background = cs.background,
        onBackground = cs.onBackground,
        onBackgroundVariant = cs.primary,
        surface = cs.surface,
        onSurface = cs.onSurface,
        surfaceVariant = cs.surfaceVariant,
        onSurfaceSecondary = onSurfaceSecondaryOpaque,
        onSurfaceVariantSummary = onSurfaceVariantSummaryOpaque,
        onSurfaceVariantActions = onSurfaceVariantActionsOpaque,
        disabledOnSurface = cs.onSurface,
        surfaceContainer = cs.surfaceContainer,
        onSurfaceContainer = cs.onSurface,
        onSurfaceContainerVariant = onSurfaceContainerVariantOpaque,
        surfaceContainerHigh = cs.surfaceVariant,
        onSurfaceContainerHigh = onSurfaceContainerHighOpaque,
        surfaceContainerHighest = cs.surfaceContainerHighest,
        onSurfaceContainerHighest = cs.onSurface,
        outline = cs.outline,
        dividerLine = cs.outlineVariant,
        windowDimming = if (dark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.3f),
        sliderKeyPoint = onSecondaryContainerOpaque,
        sliderKeyPointForeground = cs.onPrimary,
    )
}
