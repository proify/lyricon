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

package io.github.proify.lyricon.app.ui.activity.lyric.packagestyle

import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import io.github.proify.lyricon.app.updateLyricStyle
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.app.util.Utils.commitEdit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.TabRowWithContour

const val DEFAULT_PACKAGE_NAME: String = LyricPrefs.DEFAULT_PACKAGE_NAME
private const val TAB_COUNT = 3

class PackageStyleViewModel(
    private val context: Context,
    private val onLyricStyleUpdate: () -> Unit,
) : ViewModel() {
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    private val _currentPackageName = MutableStateFlow(DEFAULT_PACKAGE_NAME)
    val currentPackageName: StateFlow<String> = _currentPackageName.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    private val _currentSharedPreferences = MutableStateFlow<SharedPreferences?>(null)
    val currentSharedPreferences: StateFlow<SharedPreferences?> =
        _currentSharedPreferences.asStateFlow()

    private var prefsChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    init {
        updateSharedPreferences(DEFAULT_PACKAGE_NAME)
    }

    fun showBottomSheet() {
        _showBottomSheet.value = true
    }

    fun hideBottomSheet() {
        _showBottomSheet.value = false
    }

    fun selectPackage(packageName: String) {
        _currentPackageName.value = packageName
        updateSharedPreferences(packageName)
    }

    fun resetPackage(packageName: String) {
        LyricPrefs
            .getSharedPreferences(LyricPrefs.getPackagePrefName(packageName))
            .commitEdit { clear() }
        _refreshTrigger.value++
        updateSharedPreferences(packageName)
    }

    fun onPackageEnabled() {
        onLyricStyleUpdate()
    }

    fun getPackageLabel(packageName: String): String =
        if (packageName == DEFAULT_PACKAGE_NAME) {
            context.getString(R.string.default_style)
        } else {
            runCatching {
                val app = context.packageManager.getApplicationInfo(packageName, 0)
                context.packageManager.getApplicationLabel(app).toString()
            }.getOrElse { context.getString(R.string.default_style) }
        }

    private fun updateSharedPreferences(packageName: String) {
        _currentSharedPreferences.value?.let { sp ->
            prefsChangeListener?.let { listener ->
                sp.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

        val newSp = LyricPrefs.getSharedPreferences(LyricPrefs.getPackagePrefName(packageName))
        val newListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                onLyricStyleUpdate()
            }

        newSp.registerOnSharedPreferenceChangeListener(newListener)

        _currentSharedPreferences.value = newSp
        prefsChangeListener = newListener
    }

    override fun onCleared() {
        super.onCleared()
        _currentSharedPreferences.value?.let { sp ->
            prefsChangeListener?.let { listener ->
                sp.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }
}

class PackageStyleViewModelFactory(
    private val context: Context,
    private val onLyricStyleUpdate: () -> Unit,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PackageStyleViewModel::class.java)) {
            return PackageStyleViewModel(context, onLyricStyleUpdate) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PackageStyleActivity : BaseLyricActivity() {
    private val viewModel: PackageStyleViewModel by viewModels {
        PackageStyleViewModelFactory(
            context = applicationContext,
            onLyricStyleUpdate = { updateLyricStyle() },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PackageStyleScreen(viewModel = viewModel)
        }
    }
}

@Composable
private fun PackageStyleScreen(viewModel: PackageStyleViewModel) {
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val currentPackageName by viewModel.currentPackageName.collectAsState()
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()
    val currentSp by viewModel.currentSharedPreferences.collectAsState()
    val view = LocalView.current
    val pagerState = rememberPagerState(pageCount = { TAB_COUNT })
    val scrollBehavior = MiuixScrollBehavior()
    val hazeState = rememberHazeState()

    val title by remember(currentPackageName) {
        derivedStateOf {
            viewModel.getPackageLabel(
                currentPackageName,
            )
        }
    }

    AppToolBarContainer(
        title = title,
        canBack = true,
        actions = {},
        titleDropdown = true,
        titleOnClick = { viewModel.showBottomSheet() },
        scrollBehavior = scrollBehavior,
        hazeState = hazeState,
    ) { paddingValues ->

        PackageSwitchBottomSheet(
            show = showBottomSheet,
            onDismiss = { viewModel.hideBottomSheet() },
            onSelect = { packageName ->
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                viewModel.selectPackage(packageName)
            },
            onReset = { packageName ->
                viewModel.resetPackage(packageName)
            },
            onEnable = { _, _ ->
                viewModel.onPackageEnabled()
            },
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .hazeSource(hazeState)
                    .padding(top = paddingValues.calculateTopPadding() - 5.dp),
        ) {
            StyleTabRow(pagerState, scrollBehavior)
            Spacer(modifier = Modifier.height(10.dp))
            StyleContentPager(
                pagerState = pagerState,
                scrollBehavior = scrollBehavior,
                sharedPreferences = currentSp,
                refreshTrigger = refreshTrigger,
            )
        }
    }
}

@Composable
private fun StyleTabRow(
    pagerState: PagerState,
    scrollBehavior: ScrollBehavior,
) {
    val tabs =
        listOf(
            stringResource(R.string.tab_style_text),
            stringResource(R.string.tab_style_icon),
            stringResource(R.string.tab_style_anim),
        )

    val selectedTabIndex = remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    TabRowWithContour(
        height = 50.dp,
        modifier =
            Modifier
                .padding(horizontal = 13.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        tabs = tabs,
        selectedTabIndex = selectedTabIndex.intValue,
        onTabSelected = { index ->
            selectedTabIndex.intValue = index
            coroutineScope.launch {
                pagerState.animateScrollToPage(index)
            }
        },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { selectedTabIndex.intValue = it }
    }
}

@Composable
private fun StyleContentPager(
    pagerState: PagerState,
    scrollBehavior: ScrollBehavior,
    sharedPreferences: SharedPreferences?,
    refreshTrigger: Int,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { page -> "$page-$refreshTrigger" },
    ) { page ->
        if (sharedPreferences == null) {
            Box(modifier = Modifier.fillMaxSize())
            return@HorizontalPager
        }

        when (page) {
            0 -> TextPage(scrollBehavior, sharedPreferences)
            1 -> LogoPage(scrollBehavior, sharedPreferences)
            2 -> AnimPage(scrollBehavior)
        }
    }
}