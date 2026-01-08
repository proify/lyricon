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

package io.github.proify.lyricon.app.ui.activity.lyric

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.proify.lyricon.app.Application
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.activity.BaseActivity
import io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.sheet.AppCache
import io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.sheet.AsyncAppIcon
import io.github.proify.lyricon.app.ui.compose.AppToolBarListContainer
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentDefaults
import io.github.proify.lyricon.app.util.SignatureValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

class LyricProviderActivity : BaseActivity() {
    companion object {
        private val CERTIFIED_SIGNATURE =
            arrayOf(
                //proifySign
                "d75a43f76dbe80d816046f952b8d0f5f7abd71c9bd7b57786d5367c488bd5816"
            )
    }

    private val viewModel: ProviderViewModel by viewModels()

    data class PackageItem(
        val applicationInfo: PackageInfo,
        val description: String?,
        val home: String?,
        val category: String?,
        val author: String?,
        val certified: Boolean
    ) {
        fun getLabel(): String {
            val cached = AppCache.getCachedLabel(applicationInfo.packageName)
            if (cached != null) return cached

            val context = Application.instance
            val packageManager = context.packageManager
            val label = applicationInfo.applicationInfo?.loadLabel(packageManager).toString()
            AppCache.cacheLabel(applicationInfo.packageName, label)
            return label
        }
    }

    data class UiState(
        val allApps: List<PackageItem> = emptyList(),
        val isLoading: Boolean = false,
    )

    class ProviderViewModel(application: android.app.Application) : AndroidViewModel(application) {

        private val packageManager: PackageManager = application.packageManager

        private val _uiState = MutableStateFlow(UiState())
        val uiState: StateFlow<UiState> = _uiState

        init {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val apps = withContext(Dispatchers.IO) {
                    val items = mutableListOf<PackageItem>()
                    val packageInfos =
                        packageManager.getInstalledPackages(PackageManager.GET_META_DATA or PackageManager.GET_SIGNING_CERTIFICATES)

                    for (packageInfo in packageInfos) {
                        val metaData = packageInfo.applicationInfo?.metaData ?: continue
                        if (!metaData.getBoolean("lyricon_module")) continue
                        val description = metaData.getString("lyricon_module_description")
                        val category = metaData.getString("lyricon_module_category")
                        val home = metaData.getString("lyricon_module_home")
                        val author = metaData.getString("lyricon_module_author")

                        val certified =
                            SignatureValidator.validateSignature(packageInfo, *CERTIFIED_SIGNATURE)

                        items.add(
                            PackageItem(
                                applicationInfo = packageInfo,
                                description = description,
                                home = home,
                                category = category,
                                author = author,
                                certified = certified
                            )
                        )
                    }
                    return@withContext items
                }
                _uiState.value = _uiState.value.copy(isLoading = false, allApps = apps)
            }
        }
    }

    private data class AppCategory(
        val name: String?,
        val items: MutableList<PackageItem>
    )

    @Composable
    private fun categorizeApps(apps: List<PackageItem>): List<AppCategory> {
        val map = mutableMapOf<String, AppCategory>()
        for (app in apps) {
            val category = app.category ?: stringResource(R.string.other)
            val categoryItem = map.getOrPut(category) {
                AppCategory(category, mutableListOf())
            }
            categoryItem.items.add(app)
        }

        if (map.size == 1 && map.containsKey(stringResource(R.string.other))) {
            return listOf(
                AppCategory(
                    "",
                    map[stringResource(R.string.other)]!!.items
                )
            )
        }
        return map.values.toList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Content() }
    }

    @Composable
    private fun Content() {
        val state by viewModel.uiState.collectAsState()
        val categorized = categorizeApps(state.allApps)
        val showEmpty = remember(state.allApps) {
            derivedStateOf { state.allApps.isEmpty() }
        }

        AppToolBarListContainer(
            title = getString(R.string.activity_lyric_provider),
            showEmpty = showEmpty.value,
            canBack = true,
            emptyContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .overScrollVertical(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("😅", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("没有可用的歌词提供者")
                }
            }) { scope ->
            categorized.forEach { category ->
                if (category.name?.isNotBlank() == true) {
                    scope.item(key = "header-${category.name}") {
                        SmallTitle(
                            text = category.name,
                            insideMargin = PaddingValues(28.dp, 0.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                scope.items(category.items, key = { it.applicationInfo.packageName }) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 0.dp)
                            .fillMaxWidth(),
                        insideMargin = PaddingValues(0.dp, 0.dp),
                    ) {
                        val checked by remember { mutableStateOf(false) }

                        val title = it.getLabel()
                        val titleColor = BasicComponentDefaults.titleColor()
                        val summaryColor = BasicComponentDefaults.summaryColor()
                        val description = it.description
                        val certified = it.certified

                        val info =
                            it.applicationInfo.versionName + ", 作者 " + (it.author ?: "未知")

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            val indication = LocalIndication.current
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = indication,
                                        interactionSource = interactionSource,
                                        onClick = {
                                        }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    it.applicationInfo.applicationInfo?.let { it1 ->
                                        AsyncAppIcon(
                                            application = it1,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontSize = MiuixTheme.textStyles.headline1.fontSize,
                                            fontWeight = FontWeight.Medium,
                                            color = titleColor.color(true)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (certified) {
                                                Icon(
                                                    modifier = Modifier.size(24.dp),
                                                    painter = painterResource(id = R.drawable.verified_24px),
                                                    contentDescription = null,
                                                    tint = Color(color = 0XFF66BB6A)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = info,
                                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                color = summaryColor.color(true)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Switch(checked, onCheckedChange = {})
                                }
                            }

                            if (description != null) {
                                Text(
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 16.dp
                                    ),
                                    text = description,
                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                    color = summaryColor.color(true)
                                )
                            }
                        }

                    }
                    Spacer(modifier = Modifier.height(13.dp))
                }
            }

            scope.item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    private fun ContentPreview() {
        Content()
    }
}