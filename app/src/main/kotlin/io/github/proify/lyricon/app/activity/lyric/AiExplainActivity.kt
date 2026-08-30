/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.app.activity.lyric

import android.app.Activity
import android.graphics.Color
import android.os.SystemClock
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.markdownPadding
import com.mikepenz.markdown.model.rememberStreamingMarkdownState
import io.github.proify.android.extensions.md5
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ai.explain.AiExplainCache
import io.github.proify.lyricon.app.ai.explain.AiExplainCached
import io.github.proify.lyricon.app.ai.explain.AiExplainClient
import io.github.proify.lyricon.app.compose.IconActions
import io.github.proify.lyricon.app.compose.theme.AppTheme
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.app.util.toast
import io.github.proify.lyricon.lyric.ai.core.AiConfig
import io.github.proify.lyricon.lyric.ai.explain.AiExplainContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import io.github.proify.lyricon.lyric.style.TextStyle as LyricTextStyle

/**
 * AI 音乐解读（透明 Activity）
 *
 * 由状态栏歌词控制窗口(SystemUI 进程)启动：
 * 1. 读取歌曲元数据与歌词上下文(Intent extras)；
 * 2. 在本进程内通过 [AiExplainClient] 请求 OpenAI 兼容服务，SSE 流式解析；
 * 3. 用 miuix [WindowBottomSheet] 居中底部展示：
 *    - 歌曲信息
 *    - 思考过程(模型 reasoning_content/reasoning 增量)
 *    - 解读正文(可选中复制)
 *    - 内容最后面的「复制 / 重试」操作
 *
 * @author Tomakino
 * @since 2026
 */
class AiExplainActivity : AbstractLyricActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // 强制系统栏透明 + 浅色图标：底部弹层覆盖在当前应用之上，
        // 状态栏不再显示黑色条，图标叠加在弹层 scrim 上清晰可见。
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        // 关闭系统对比度 scrim，避免状态栏出现半透明黑条
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false
        setContent {
            AppTheme {
                AiExplainSheet(
                    title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
                    artist = intent.getStringExtra(EXTRA_ARTIST).orEmpty(),
                    album = intent.getStringExtra(EXTRA_ALBUM).orEmpty(),
                    lyrics = intent.getStringExtra(EXTRA_LYRICS).orEmpty(),
                )
            }
        }
    }

    companion object {
        const val EXTRA_TITLE = AiExplainContract.EXTRA_TITLE
        const val EXTRA_ARTIST = AiExplainContract.EXTRA_ARTIST
        const val EXTRA_ALBUM = AiExplainContract.EXTRA_ALBUM
        const val EXTRA_LYRICS = AiExplainContract.EXTRA_LYRICS
    }
}

@Composable
private fun AiExplainSheet(title: String, artist: String, album: String, lyrics: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var reasoningText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var reasoningVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var contentEpoch by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var job by remember { mutableStateOf<Job?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var thinkingExpanded by remember { mutableStateOf(false) }

    // 增量经 Channel 单消费者串行消费（渲染树内消费，按窗口聚合后 append 到
    // streaming state）：不丢块、不重复、不重建整树——流式期间树稳定不闪烁。
    var reasoningChannel by remember { mutableStateOf(Channel<String>(Channel.UNLIMITED)) }
    var contentChannel by remember { mutableStateOf(Channel<String>(Channel.UNLIMITED)) }

    fun startRequest(forceRefresh: Boolean = false) {
        job?.cancel()
        // 新请求换新 Channel：旧请求回调的迟到增量写入旧通道，不会与新请求内容混合
        reasoningChannel = Channel(Channel.UNLIMITED)
        contentChannel = Channel(Channel.UNLIMITED)
        reasoningText = ""
        contentText = ""
        reasoningVisible = false
        contentVisible = false
        contentEpoch++
        isLoading = true

        job = scope.launch {
            if (lyrics.isBlank()) {
                isLoading = false
                contentVisible = true
                contentChannel.trySend("暂无歌词可解读")
                return@launch
            }

            val configs = resolveConfig()
            if (configs == null || !configs.isUsable) {
                isLoading = false
                contentVisible = true
                contentChannel.trySend("请在「AI 实验室」中配置好 API 后再试")
                return@launch
            }

            // 缓存 key 纳入模型与服务信息：更换模型/服务商后不命中旧解读
            val cacheKey =
                "${title}|${artist}|$lyrics|${configs.provider}|${configs.baseUrl}|${configs.model}"
                    .md5()

            // 仅在首次加载读缓存；手动重试 = 强制重新调用 AI（成功后覆盖旧缓存）
            if (!forceRefresh) {
                val cached = withContext(Dispatchers.IO) {
                    AiExplainCache.get(context, cacheKey)
                }
                if (cached != null && cached.content.isNotEmpty()) {
                    isLoading = false
                    // 缓存命中：同样经通道单次投递，渲染路径唯一
                    if (cached.reasoning.isNotEmpty()) {
                        reasoningVisible = true
                        reasoningChannel.trySend(cached.reasoning)
                    }
                    contentVisible = true
                    contentChannel.trySend(cached.content)
                    return@launch
                }
            }

            val result = AiExplainClient.stream(
                configs = configs,
                // 输出语言（业务参数，来自翻译目标语言设置）
                targetLanguage = runCatching {
                    LyricPrefs.basicStylePrefs.getString(
                        LyricTextStyle.Companion.KEY_AI_TRANSLATION_TARGET_LANGUAGE,
                        LyricTextStyle.Defaults.AI_TRANSLATION_TARGET_LANGUAGE_DISPLAY_NAME
                    )
                }.getOrNull(),
                title = title,
                artist = artist,
                album = album,
                lyrics = lyrics,
                onReasoning = { chunk ->
                    if (chunk.isNotEmpty()) {
                        reasoningVisible = true
                        reasoningChannel.trySend(chunk)
                    }
                },
                onContent = { chunk ->
                    if (chunk.isNotEmpty()) {
                        contentVisible = true
                        contentChannel.trySend(chunk)
                    }
                },
            )

            isLoading = false
            if (result == null) {
                contentVisible = true
                contentChannel.trySend("AI 解释失败，请检查网络与配置")
            } else {
                val snapshot = result
                scope.launch(Dispatchers.IO) {
                    AiExplainCache.put(
                        context,
                        cacheKey,
                        AiExplainCached(
                            reasoning = snapshot.reasoning,
                            content = snapshot.content,
                            time = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) { startRequest() }

    // 流式滚动节流：token 高频到达时用 scrollTo 而非逐 token 动画（≥100ms 一次）
    var lastAutoScrollAt by remember { mutableLongStateOf(0L) }
    LaunchedEffect(contentText) {
        if (isLoading && contentText.isNotEmpty()) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastAutoScrollAt >= 100L) {
                lastAutoScrollAt = now
                scrollState.scrollTo(scrollState.maxValue)
            }
        }
    }
    // 结束加载时平滑滚到底
    LaunchedEffect(isLoading) {
        if (!isLoading) scrollState.animateScrollTo(scrollState.maxValue)
    }

    // 右上角"更多"菜单：复制 / 重试
    val menuEntries = remember(isLoading, contentText.isNotEmpty()) {
        listOf(
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = "复制解读",
//                        icon = {
//                            Icon(
//                                painter = painterResource(R.drawable.ic_copy),
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .width(20.dp)
//                                    .height(20.dp),
//                            )
//                        },
                        onClick = {
                            showMenu = false
                            if (contentText.isNotEmpty()) {
                                clipboard.setText(AnnotatedString(contentText))
                                toast("已复制到剪贴板")
                            } else {
                                toast("暂无可复制的内容")
                            }
                        },
                    ),
                    DropdownItem(
                        text = if (isLoading) "重试生成" else "重试",
//                        icon = {
//                            Icon(
//                                painter = painterResource(R.drawable.ic_refresh),
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .width(20.dp)
//                                    .height(20.dp),
//                            )
//                        },
                        enabled = !isLoading,
                        onClick = {
                            showMenu = false
                            // 重试 = 跳过缓存，强制重新调用 AI
                            startRequest(forceRefresh = true)
                        },
                    ),
                )
            )
        )
    }

    WindowBottomSheet(
        show = true,
        title = "音乐解读",
        endAction = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = MiuixIcons.More,
                        contentDescription = "更多操作",
                        // tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                WindowCascadingListPopup(
                    show = showMenu,
                    entries = menuEntries,
                    onDismissRequest = { showMenu = false },
                    alignment = PopupPositionProvider.Align.End,
                )
            }
        },
        onDismissRequest = { (context as? Activity)?.finish() },
        backgroundColor = MiuixTheme.colorScheme.surface,
        insideMargin = DpSize(0.dp, 0.dp),
        enableNestedScroll = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {


            // ---- 思考过程（可折叠，默认折叠；state 与消费循环常驻，折叠不丢内容） ----
            if (reasoningVisible) {
                key(contentEpoch) {
                    val reasoningStream = rememberStreamingMarkdownState()
                    LaunchedEffect(Unit) {
                        val channel = reasoningChannel
                        while (true) {
                            val first = withTimeoutOrNull(120L) { channel.receive() } ?: continue
                            val batch = StringBuilder()
                            batch.append(first)
                            while (true) {
                                val extra = withTimeoutOrNull(50L) { channel.receive() } ?: break
                                batch.append(extra)
                            }
                            val text = batch.toString()
                            reasoningStream.append(text)
                            reasoningText += text
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        insideMargin = PaddingValues(0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { thinkingExpanded = !thinkingExpanded }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconActions(painterResource(R.drawable.psychology_24px))
                                Text(
                                    text = "思考过程",
                                    color = MiuixTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .weight(1f)
                                )
                                Icon(
                                    painter = painterResource(R.drawable.keyboard_arrow_right_24px),
                                    contentDescription = if (thinkingExpanded) "收起思考" else "展开思考",
                                    modifier = Modifier.rotate(if (thinkingExpanded) 90f else 0f),
                                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            // 仅控制 Markdown 显隐：streaming state 在 key(epoch) 内常驻，
                            // 关闭折叠不会销毁消费循环与已渲染内容
                            if (thinkingExpanded) {
                                Spacer(Modifier.height(6.dp))
                                Markdown(
                                    streamingMarkdownState = reasoningStream,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = rememberAiMarkdownColors(),
                                    typography = rememberAiMarkdownTypography(),
                                    padding = markdownPadding(
                                        block = 16.dp,
                                        list = 12.dp,
                                        listItemTop = 6.dp,
                                        listItemBottom = 10.dp,
                                        indentList = 12.dp,
                                        listIndent = 12.dp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            // ---- 等待 AI 响应：加载指示，避免页面空白 ----
            if (isLoading && !contentVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(28.dp)
                            .height(28.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "AI 正在解读音乐…",
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        fontSize = 13.sp
                    )
                }
            }

            // ---- 解读正文（增量 append 渲染：树稳定，段间距 16dp） ----
            if (contentVisible) {
                SelectionContainer {
                    AiStreamingMarkdownText(
                        flow = contentChannel,
                        epoch = contentEpoch,
                        onDelta = { contentText += it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * AI 解读 Markdown 流式渲染：
 * 从 [Channel] 单消费者串行消费增量（120ms 窗口聚合），append 到 streaming state——
 * 渲染树保持稳定、只更新尾部，因此不闪烁；空白增量（空格/换行/标点）原样保留。
 *
 * paragraph 块间距由 [markdownPadding] 显式设置（库默认 block=0 会挤成一团）。
 */
@Composable
private fun AiStreamingMarkdownText(
    flow: Channel<String>,
    epoch: Int,
    onDelta: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    key(epoch) {
        val streamingState = rememberStreamingMarkdownState()
        LaunchedEffect(Unit) {
            val channel = flow
            while (true) {
                val first = withTimeoutOrNull(120L) { channel.receive() } ?: continue
                val batch = StringBuilder()
                batch.append(first)
                while (true) {
                    val extra = withTimeoutOrNull(50L) { channel.receive() } ?: break
                    batch.append(extra)
                }
                val text = batch.toString()
                streamingState.append(text)
                onDelta(text)
            }
        }
        Markdown(
            streamingMarkdownState = streamingState,
            modifier = modifier,
            colors = rememberAiMarkdownColors(),
            typography = rememberAiMarkdownTypography(),
            padding = markdownPadding(
                block = 5.dp,
//                list = 12.dp,
//                listItemTop = 6.dp,
//                listItemBottom = 10.dp,
//                indentList = 12.dp,
//                listIndent = 12.dp
            )
        )
    }
}

@Composable
private fun rememberAiMarkdownColors(): DefaultMarkdownColors {
    val scheme = MiuixTheme.colorScheme
    return remember(scheme) {
        DefaultMarkdownColors(
            text = scheme.onSurface,
            codeBackground = scheme.surfaceContainer,
            inlineCodeBackground = scheme.surfaceContainer,
            dividerColor = scheme.onSurfaceVariantSummary,
            tableBackground = scheme.surfaceContainer,
        )
    }
}

/**
 * 移动端舒适阅读排版：
 * - 正文 15sp / 行高 26sp（≈1.73），长文阅读不累
 * - 标题按 23/20/18/16/15sp 收敛，避免小屏被大标题压满
 * - 代码 13sp；引用与正文一致
 * - 链接使用主题 primary 色（明暗自适应）
 */
@Composable
private fun rememberAiMarkdownTypography(): DefaultMarkdownTypography {
    val scheme = MiuixTheme.colorScheme
    return remember(scheme) {
        DefaultMarkdownTypography(
            h1 = TextStyle(fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
            h2 = TextStyle(fontSize = 20.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold),
            h3 = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
            h4 = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
            h5 = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
            h6 = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
            text = TextStyle(fontSize = 15.sp, lineHeight = 26.sp),
            code = TextStyle(fontSize = 13.sp, lineHeight = 20.sp),
            inlineCode = TextStyle(fontSize = 13.sp, lineHeight = 20.sp),
            quote = TextStyle(fontSize = 15.sp, lineHeight = 26.sp),
            paragraph = TextStyle(fontSize = 15.sp, lineHeight = 26.sp),
            ordered = TextStyle(fontSize = 15.sp, lineHeight = 26.sp),
            bullet = TextStyle(fontSize = 15.sp, lineHeight = 26.sp),
            list = TextStyle(fontSize = 15.sp, lineHeight = 26.sp),
            textLink = TextLinkStyles(
                style = SpanStyle(color = scheme.primary)
            ),
            table = TextStyle(fontSize = 15.sp, lineHeight = 26.sp),
            alertTitle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold),
        )
    }
}

/**
 * 读取统一 AI 配置；冷启动时 XposedService 可能尚未绑定，
 * 短暂重试以确保读到模块写入的配置。
 *
 * 未配置 API Key 时无需等待——配置明显不可用，直接返回避免白转 3 秒加载圈。
 */
private suspend fun resolveConfig(): AiConfig? {
    val preferences = LyricPrefs.basicStylePrefs
    if (preferences.getString(AiConfig.KEY_AI_CONFIG_API_KEY, null).isNullOrBlank()) {
        return runCatching { AiConfig.fromPreferences(preferences) }.getOrNull()
    }
    repeat(10) {
        val config = runCatching { AiConfig.fromPreferences(preferences) }.getOrNull()
        if (config != null && config.isUsable) return config
        delay(300)
    }
    return runCatching { AiConfig.fromPreferences(preferences) }.getOrNull()
}
