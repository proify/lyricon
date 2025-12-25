package io.github.proify.lyricon.app.ui.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.activity.BaseActivity
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.MiuixScrollBehavior
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.ScrollBehavior
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.TopAppBar
import io.github.proify.lyricon.app.ui.theme.AppTheme
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun NavigationBackIcon(
    context: Context = LocalContext.current,
    backEvent: () -> Unit = {
        if (context is BaseActivity) context.onBackPressedDispatcher.onBackPressed()
    },
) {

    IconButton(onClick = {
        backEvent.invoke()
    }) {
        Icon(
            modifier = Modifier.size(26.dp),
            imageVector = MiuixIcons.Useful.Back,
            contentDescription = stringResource(R.string.back)
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun BlurTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Transparent,
    largeTitle: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    defaultWindowInsetsPadding: Boolean = true,
    horizontalPadding: Dp = 20.dp,
    hazeState: HazeState? = null,
    titleDropdown: Boolean = false,
    titleOnClick: () -> Unit = {},
) {
    val blurRadius = if (isSystemInDarkTheme()) 50.dp else 25.dp
    val hazeStyle = HazeStyle(
        blurRadius = blurRadius,
        noiseFactor = 0f,
        backgroundColor = MiuixTheme.colorScheme.surface,
        tint = HazeTint(
            MiuixTheme.colorScheme.surface.copy(
                if (scrollBehavior == null || scrollBehavior.state.collapsedFraction <= 0f) {
                    1f
                } else {
                    lerp(1f, 0.76f, (scrollBehavior.state.collapsedFraction))
                }
            )
        )
    )

    TopAppBar(
        title = title,
        modifier = if (hazeState != null) modifier.hazeEffect(hazeState, hazeStyle) else modifier,
        color = color,
        largeTitle = largeTitle,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        horizontalPadding = horizontalPadding,
        titleDropdown = titleDropdown,
        titleOnClick = titleOnClick
    )
}

@Composable
fun getCurrentTitle(): String? {
    val context = LocalContext.current
    return if (context is Activity) context.title?.toString() else null
}

@Composable
fun AppToolBarListContainer(
    context: Context = LocalContext.current,
    backEvent: () -> Unit = {
        if (context is BaseActivity) context.onBackPressedDispatcher.onBackPressed()
    },
    title: Any? = getCurrentTitle(),
    canBack: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    scaffoldContent: @Composable () -> Unit = {},
    titleDropdown: Boolean = false,
    titleOnClick: () -> Unit = {},
    showEmpty: Boolean = false,
    emptyContent: @Composable () -> Unit = {},
    content: (LazyListScope) -> Unit
) {
    AppTheme {
        val hazeState = rememberHazeState()
        val scrollBehavior = MiuixScrollBehavior()

        Scaffold(
            bottomBar = bottomBar,
            topBar = {
                BlurTopAppBar(
                    hazeState = hazeState,
                    navigationIcon = { if (canBack) NavigationBackIcon(backEvent = backEvent) },
                    title = if (title is Int) stringResource(title) else title.toString(),
                    scrollBehavior = scrollBehavior,
                    actions = actions,
                    titleDropdown = titleDropdown,
                    titleOnClick = titleOnClick
                )
            }
        ) { paddingValues ->
            scaffoldContent()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .hazeSource(hazeState)
                ) {
                    content(this)
                }
                if (showEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    ) {
                        emptyContent()
                    }
                }
            }
        }
    }
}

@Composable
fun AppToolBarContainer(
    context: Context = LocalContext.current,
    backEvent: () -> Unit = {
        if (context is BaseActivity) context.onBackPressedDispatcher.onBackPressed()
    },
    title: Any? = getCurrentTitle(),
    canBack: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    titleDropdown: Boolean = false,
    titleOnClick: () -> Unit = {},
    scrollBehavior: ScrollBehavior = MiuixScrollBehavior(),
    hazeState: HazeState = rememberHazeState(),
    content: @Composable (PaddingValues) -> Unit,
) {
    AppTheme {

        Scaffold(
            bottomBar = bottomBar,
            topBar = {
                BlurTopAppBar(
                    hazeState = hazeState,
                    navigationIcon = { if (canBack) NavigationBackIcon(backEvent = backEvent) },
                    title = if (title is Int) stringResource(title) else title.toString(),
                    scrollBehavior = scrollBehavior,
                    actions = actions,
                    titleDropdown = titleDropdown,
                    titleOnClick = titleOnClick
                )
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

