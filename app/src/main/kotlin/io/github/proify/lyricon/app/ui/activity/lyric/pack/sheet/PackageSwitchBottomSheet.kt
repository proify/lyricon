package io.github.proify.lyricon.app.ui.activity.lyric.pack.sheet

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.SwipeableItem
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperCheckbox
import io.github.proify.lyricon.app.util.LyricPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.New
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

data class PackageSwitchUiState(
    val configureds: List<PackageItem> = emptyList(),
    val enableds: Set<String> = emptySet(),

    val selectedPackage: String = "",
    val isLoading: Boolean = true
)

class PackageSwitchViewModel(app: Application) : AndroidViewModel(app) {

    private val packageManager: PackageManager = app.packageManager
    val defaultAppInfo: ApplicationInfo = app.applicationInfo

    private val _uiState = MutableStateFlow(PackageSwitchUiState())
    val uiState: StateFlow<PackageSwitchUiState> = _uiState

    init {
        loadConfiguredPackages()
    }

    suspend fun loadConfiguredPackages(
        packageManager: PackageManager,
        defaultAppInfo: ApplicationInfo
    ): List<PackageItem> = withContext(Dispatchers.IO) {
        val packages = mutableListOf<PackageItem>()
        val configuredNames = LyricPrefs.getConfiguredPackageNames()

        configuredNames.forEach { packageName ->
            runCatching {
                val info = packageManager.getApplicationInfo(packageName, 0)
                packages.add(PackageItem(info))
            }
        }

        if (!packages.any { it.applicationInfo.packageName == defaultAppInfo.packageName }) {
            packages.add(PackageItem(applicationInfo = defaultAppInfo))
        }
        packages
    }

    private fun loadConfiguredPackages() {
        viewModelScope.launch {
            val packages = loadConfiguredPackages(packageManager, defaultAppInfo)
            val selected = packages.firstOrNull()?.applicationInfo?.packageName ?: ""

            val enableds = LyricPrefs.getEnabledPackageNames()
            _uiState.value = PackageSwitchUiState(
                configureds = packages,
                enableds = enableds,
                selectedPackage = selected,
                isLoading = false
            )
        }
    }

    fun selectPackage(packageName: String) {
        _uiState.update { it.copy(selectedPackage = packageName) }
    }

    fun setPackageEnabled(packageName: Array<String>, enabled: Boolean) {
        _uiState.update { state ->
            val enabledPackages = LyricPrefs.getEnabledPackageNames().toMutableSet()
            if (enabled) enabledPackages.addAll(packageName) else enabledPackages.removeAll(
                packageName.toSet()
            )

            LyricPrefs.setEnabledPackageNames(enabledPackages)
            state.copy(enableds = enabledPackages)
        }
    }

    fun setConfiguredPackages(newPackages: List<PackageItem>) {
        _uiState.update { state ->
            LyricPrefs.setConfiguredPackageNames(newPackages.map { it.applicationInfo.packageName }
                .toSet())
            state.copy(configureds = newPackages)
        }
    }

    fun removeConfiguredPackage(packageName: String) {
        setPackageEnabled(arrayOf(packageName), false)
        _uiState.update { state ->
            LyricPrefs.setConfiguredPackageNames(
                LyricPrefs.getConfiguredPackageNames().filterNot { it == packageName }.toSet()
            )
            state.copy(configureds = state.configureds.filterNot { it.applicationInfo.packageName == packageName })
        }
    }
}

@Composable
fun PackageSwitchBottomSheet(
    show: MutableState<Boolean>,
    onSelect: (String) -> Unit,
    onRestore: (String) -> Unit,
    onEnable: (String, Boolean) -> Unit,
) {
    val viewModel: PackageSwitchViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    val showAddSheet = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun selectPackage(pkg: String) {
        viewModel.selectPackage(pkg)
        onSelect(pkg)
    }

    fun restorePackage(pkg: String) {
        onRestore(pkg)
    }

    PackageSelectionBottomSheet(
        show = showAddSheet,
        initialSelectedPackages = state.configureds.map { it.applicationInfo.packageName }.toSet(),
        onSelectionChanged = { selectedPackages ->

            scope.launch {
                val currentList = state.configureds.toMutableList()

                val removeList =
                    currentList.filter { !selectedPackages.contains(it.applicationInfo.packageName) }

                viewModel.setPackageEnabled(
                    removeList.map { it.applicationInfo.packageName }.toTypedArray(),
                    false
                )
                currentList.removeAll(removeList)

                selectedPackages.forEach { selected ->
                    if (currentList.none { it.applicationInfo.packageName == selected }) {
                        val context = viewModel.getApplication<Application>()
                        val appInfo = context.packageManager.getApplicationInfo(selected, 0)
                        currentList.add(PackageItem(appInfo))
                    }
                }
                viewModel.setConfiguredPackages(currentList)

                if (removeList.any { it.applicationInfo.packageName == state.selectedPackage }) {
                    selectPackage(viewModel.defaultAppInfo.packageName)
                }
            }
        }
    )
    PackageSwitchSheetContent(
        viewModel = viewModel,
        show = show,
        state = state,
        onAddClick = { showAddSheet.value = true },
        onSelect = { pkg ->
            selectPackage(pkg)
            show.value = false
        },
        onRestore = { pkg ->
            restorePackage(pkg)

        },
        onEnable = { pkg, enabled ->
            viewModel.setPackageEnabled(arrayOf(pkg), enabled)
            onEnable(pkg, enabled)
        },
        onDelete = { pkg ->
            if (pkg != viewModel.defaultAppInfo.packageName) {
                viewModel.removeConfiguredPackage(pkg)
                restorePackage(pkg)
                if (state.selectedPackage == pkg) {
                    selectPackage(viewModel.defaultAppInfo.packageName)
                }
            }
        }
    )
}

@Composable
private fun PackageSwitchSheetContent(
    show: MutableState<Boolean>,
    state: PackageSwitchUiState,
    onAddClick: () -> Unit,
    onSelect: (String) -> Unit,
    onRestore: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEnable: (String, Boolean) -> Unit,
    viewModel: PackageSwitchViewModel
) {
    val packageItems = state.configureds
    SuperBottomSheet(
        insideMargin = DpSize(0.dp, 0.dp),
        show = show,
        title = stringResource(R.string.package_style_manager),
        backgroundColor = MiuixTheme.colorScheme.surface,
        onDismissRequest = { show.value = false },
        rightAction = {
            Row {
                IconButton(onClick = onAddClick) {
                    Icon(
                        modifier = Modifier.size(26.dp),
                        imageVector = MiuixIcons.Useful.New,
                        contentDescription = stringResource(R.string.add),
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .overScrollVertical()
                .animateContentSize()
        ) {
            items(
                items = packageItems,
                key = { it.applicationInfo.packageName }
            ) { item ->
                val packageName = item.applicationInfo.packageName
                PackageListItem(
                    isEnabled = packageName == viewModel.defaultAppInfo.packageName || state.enableds.contains(
                        packageName
                    ),
                    item = item,
                    isSelected = state.selectedPackage == packageName,
                    onSelect = { onSelect(packageName) },
                    onRestore = { onRestore(packageName) },
                    onDelete = { onDelete(packageName) },
                    onEnable = { enabled -> onEnable(packageName, enabled) },
                    displayEnable = packageName != viewModel.defaultAppInfo.packageName,
                    modifier = Modifier.padding(bottom = 13.dp)
                )
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageListItem(
    item: PackageItem,
    isSelected: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onEnable: (Boolean) -> Unit,
    displayEnable: Boolean,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    SwipeableItem(
        modifier = modifier,
        rightActions = { close ->
            TooltipBox(
                positionProvider =
                    TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = {
                    PlainTooltip {
                        Text(
                            stringResource(R.string.restore),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                },
                state = rememberTooltipState(),
            ) {
                Row {
                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFF9800), CircleShape),
                        onClick = {
                            onRestore()
                            close()
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_reset_settings),
                            contentDescription = stringResource(R.string.restore),
                            tint = MiuixTheme.colorScheme.onError
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
            if (!item.isDefault) {
                TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = {
                        PlainTooltip {
                            Text(
                                stringResource(R.string.delete),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    },
                    state = rememberTooltipState(),
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MiuixTheme.colorScheme.error, CircleShape),
                        onClick = {
                            onDelete()
                            close()
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete),
                            tint = MiuixTheme.colorScheme.onError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
        },
    ) { closeAction ->
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            SuperCheckbox(
                leftAction = {
                    AsyncAppIcon(
                        application = item.applicationInfo,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                },
                rightActions = {
                    if (displayEnable) {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                onEnable(it)
                            }
                        )
                    }
                },
                title = item.getLabel(),
                summary = if (isEnabled) stringResource(R.string.status_enabled) else stringResource(
                    R.string.status_not_enabled
                ),
                checked = isSelected,
                onCheckedChange = {
                    onSelect()
                    closeAction()
                }
            )
        }
    }
}