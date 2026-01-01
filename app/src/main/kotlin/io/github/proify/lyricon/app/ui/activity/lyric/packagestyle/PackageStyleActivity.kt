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

package io.github.proify.lyricon.app.ui.activity.lyric.packagestyle

import android.content.SharedPreferences
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.activity.lyric.BaseLyricActivity
import io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.page.AnimPage
import io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.page.LogoPage
import io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.page.TextPage
import io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.sheet.PackageSwitchBottomSheet
import io.github.proify.lyricon.app.ui.compose.AppToolBarContainer
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.MiuixScrollBehavior
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.ScrollBehavior
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.app.util.Utils.commitEdit
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.TabRowWithContour

class PackageStyleActivity : BaseLyricActivity() {

    private val viewModel: PackageStyleViewModel by viewModels()

    companion object {
        const val DEFAULT_PACKAGE_NAME = LyricPrefs.DEFAULT_PACKAGE_NAME
        private const val TAB_COUNT = 3
    }

    class PackageStyleViewModel : ViewModel() {
        val showBottomSheet = mutableStateOf(false)
        val currentPackageName = mutableStateOf(DEFAULT_PACKAGE_NAME)
        val refreshTrigger = mutableIntStateOf(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainContent() }
    }

    @Composable
    private fun MainContent() {
        val pagerState = rememberPagerState(pageCount = { TAB_COUNT })
        val scrollBehavior = MiuixScrollBehavior()
        val hazeState: HazeState = rememberHazeState()
        val defaultTitle = stringResource(R.string.default_style)
        val title = remember { mutableStateOf(defaultTitle) }

        AppToolBarContainer(
            title = title.value,
            canBack = true,
            actions = {},
            titleDropdown = true,
            titleOnClick = { viewModel.showBottomSheet.value = true },
            scrollBehavior = scrollBehavior,
            hazeState = hazeState,
        ) { paddingValues ->

            PackageSwitchBottomSheet(
                viewModel.showBottomSheet,
                onSelect = {
                    window.decorView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

                    viewModel.currentPackageName.value = it
                    title.value = if (it == DEFAULT_PACKAGE_NAME) defaultTitle else runCatching {
                        val app = packageManager.getApplicationInfo(it, 0)
                        packageManager.getApplicationLabel(app).toString()
                    }.getOrElse { defaultTitle }
                },
                onReset = {
                    LyricPrefs.getSharedPreferences(LyricPrefs.getPackagePrefName(it))
                        .commitEdit {
                            clear()
                        }
                    viewModel.refreshTrigger.intValue++
                },
                onEnable = { string: String, bool: Boolean ->
                    updateLyricStyle()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .hazeSource(hazeState)
                    .padding(top = paddingValues.calculateTopPadding() - 5.dp)
            ) {
                StyleTabRow(pagerState, scrollBehavior)
                Spacer(modifier = Modifier.height(10.dp))
                StyleContentPager(pagerState, scrollBehavior)
            }
        }
    }

    @Composable
    private fun StyleTabRow(pagerState: PagerState, scrollBehavior: ScrollBehavior) {
        val tabs = remember {
            listOf(
                getString(R.string.tab_style_text),
                getString(R.string.tab_style_icon),
                getString(R.string.tab_style_anim)
            )
        }
        val selectedTabIndex = remember { mutableIntStateOf(0) }
        val coroutineScope = rememberCoroutineScope()

        TabRowWithContour(
            height = 50.dp,
            modifier = Modifier
                .padding(horizontal = 13.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            tabs = tabs,
            selectedTabIndex = selectedTabIndex.intValue,
            onTabSelected = {
                selectedTabIndex.intValue = it
                coroutineScope.launch {
                    pagerState.animateScrollToPage(it)
                }
            }
        )

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .collect { selectedTabIndex.intValue = it }
        }
    }

    private var currentSp: SharedPreferences? = null

    override fun onDestroy() {
        super.onDestroy()
        currentSp?.unregisterOnSharedPreferenceChangeListener(this)
    }

    @Composable
    private fun StyleContentPager(
        pagerState: PagerState,
        scrollBehavior: ScrollBehavior
    ) {
        val packageName = viewModel.currentPackageName.value
        val refreshTrigger = viewModel.refreshTrigger.intValue

        currentSp?.unregisterOnSharedPreferenceChangeListener(this)
        currentSp = remember(packageName, refreshTrigger) {
            LyricPrefs.getSharedPreferences(LyricPrefs.getPackagePrefName(packageName))
        }
        currentSp?.registerOnSharedPreferenceChangeListener(this)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize(),
        ) { page ->
            val currentSp = currentSp
            if (currentSp == null) {
                Box { }
                return@HorizontalPager
            }
            when (page) {
                0 -> TextPage(scrollBehavior, currentSp)
                1 -> LogoPage(scrollBehavior, currentSp)
                2 -> AnimPage(scrollBehavior)
            }
        }
    }
}