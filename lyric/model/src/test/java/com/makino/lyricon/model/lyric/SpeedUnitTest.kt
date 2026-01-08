@file:Suppress("ReplacePrintlnWithLogging")

package com.makino.lyricon.model.lyric

import com.makino.lyricon.model.lyric.provider.ILyricPositionProvider
import com.makino.lyricon.model.lyric.provider.aaa
import com.makino.lyricon.model.lyric.provider.base.Base
import com.makino.lyricon.model.lyric.provider.v1.V1
import com.makino.lyricon.model.lyric.provider.v2.V2
import io.github.proify.lyricon.lyric.model.LyricLine
import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import java.io.File
import kotlin.math.roundToLong
import kotlin.system.measureNanoTime

// ================= 测试配置 =================
object TestConfig {
    const val LYRIC_COUNT = 100_000
    const val SINGLE_POINT_TRIALS = 2_000
    const val CONTINUOUS_DURATION_SEC = 10
    val FRAME_RATES = listOf(30, 60, 120)
    const val HOT_JITTER_COUNT = 20_000
    const val HOT_JITTER_RADIUS_MS = 300L
    const val FORWARD_SEEK_STEP_MS = 16L

    const val RANDOM_SEEK_COUNT = 10_000 // 新增

}

// ================= 场景权重 =================
enum class BenchmarkScene(val title: String, val weight: Double) {
    SINGLE_POINT("单点查询", 0.15),
    CONTINUOUS("连续播放", 0.35),
    RANDOM_SEEK("随机跳转", 0.30),
    HOT_JITTER("热点抖动", 0.15),
    FORWARD_SEEK("单向 Seek", 0.05)
}

// ================= Provider 工厂 =================
interface ProviderFactory {
    val name: String
    fun create(source: List<ILyricTiming>): ILyricPositionProvider
}

private val PROVIDERS = listOf(

    object : ProviderFactory {
        override val name = "base"
        override fun create(source: List<ILyricTiming>) = Base(source)
    },

    object : ProviderFactory {
        override val name = "V2"
        override fun create(source: List<ILyricTiming>) = V2(source)
    },
    object : ProviderFactory {
        override val name = "V3"
        override fun create(source: List<ILyricTiming>) = aaa(source)
    },
    object : ProviderFactory {
        override val name = "V1"
        override fun create(source: List<ILyricTiming>) = V1(source)
    },

)

// ================= Benchmark =================
class SpeedUnitTest {

    @Test
    fun benchmarkAndExportMarkdown() = runBlocking {

        // ------------------ 生成固定歌词 ------------------
        val lyrics = generateFixedLyrics()
        val totalDuration = lyrics.last().end

        val providers = PROVIDERS.associate { it.name to it.create(lyrics) }
        val baselineName = providers.keys.first()

        warmup(providers.values)

        val sceneResults = LinkedHashMap<BenchmarkScene, Map<String, Stats>>()

        // -------- 单点查询 --------
        sceneResults[BenchmarkScene.SINGLE_POINT] = providers.mapValues {
            measureSingleWithStats(it.value, totalDuration / 2, TestConfig.SINGLE_POINT_TRIALS)
        }

        // -------- 连续播放 --------
        val continuousPositions = generateContinuousPositions(totalDuration, TestConfig.FRAME_RATES)
        sceneResults[BenchmarkScene.CONTINUOUS] =
            measureSceneParallel(providers, continuousPositions)

        // -------- 固定“随机跳转” --------
        val randomPositions =
            LongArray(TestConfig.RANDOM_SEEK_COUNT) { it * (totalDuration / TestConfig.RANDOM_SEEK_COUNT) }
        sceneResults[BenchmarkScene.RANDOM_SEEK] = measureSceneParallel(providers, randomPositions)

        // -------- 固定热点抖动 --------
        val hotspotCenters = listOf(totalDuration / 3, totalDuration / 2, totalDuration * 2 / 3)
        val hotPositions = generateMultiHotPositions(
            hotspotCenters,
            TestConfig.HOT_JITTER_COUNT / hotspotCenters.size,
            TestConfig.HOT_JITTER_RADIUS_MS,
            totalDuration
        )
        sceneResults[BenchmarkScene.HOT_JITTER] = measureSceneParallel(providers, hotPositions)

        // -------- 单向 Seek --------
        val forwardPositions =
            generateForwardSeekPositions(TestConfig.FORWARD_SEEK_STEP_MS, totalDuration)
        sceneResults[BenchmarkScene.FORWARD_SEEK] =
            measureSceneParallel(providers, forwardPositions)

        // -------- Markdown 输出 --------
        val markdown = buildMarkdownWithImprovement(sceneResults, baselineName)
        File("LyricPositionProvider-Benchmark.md").writeText(markdown)
    }

    // ================= Markdown =================
    private fun buildMarkdownWithImprovement(
        scenes: Map<BenchmarkScene, Map<String, Stats>>,
        baselineName: String
    ): String = buildString {
        appendLine("# LyricPositionProvider 综合性能基准（固定版）\n")

        scenes.forEach { (scene, stats) ->
            appendLine("## ${scene.title}")
            appendLine("| 版本 | 平均(ns) | P95(ns) | P99(ns) | 相对 ${baselineName} 提升 |")
            appendLine("|---|---|---|---|---|")
            val baselineAvg = stats.getValue(baselineName).avg.toDouble()
            stats.forEach { (name, s) ->
                val improvement = ((baselineAvg / s.avg - 1) * 100)
                appendLine("| $name | ${s.avg} | ${s.p95} | ${s.p99} | ${"%.2f".format(improvement)}% |")
            }
            appendLine()
        }

        appendLine("## 综合评分（加权）")
        appendLine("| 排名 | 版本 | 综合得分 |")
        appendLine("|---|---|---|")
        calculateOverallScores(scenes, baselineName).sortedByDescending { it.score }
            .forEachIndexed { idx, item ->
                appendLine("| ${idx + 1} | ${item.name} | ${"%.4f".format(item.score)} |")
            }
    }

    // ================= 综合评分 =================
    private data class OverallScore(val name: String, val score: Double)

    private fun calculateOverallScores(
        scenes: Map<BenchmarkScene, Map<String, Stats>>,
        baselineName: String
    ): List<OverallScore> {
        val providers = scenes.values.first().keys
        return providers.map { name ->
            var total = 0.0
            scenes.forEach { (scene, stats) ->
                val baselineAvg = stats.getValue(baselineName).avg
                val currentAvg = stats.getValue(name).avg
                total += (baselineAvg.toDouble() / currentAvg) * scene.weight
            }
            OverallScore(name, total)
        }
    }

    // ================= Stats =================
    private data class Stats(val avg: Long, val p95: Long, val p99: Long)

    // ================= 并行测量 =================
    private suspend fun measureSceneParallel(
        providers: Map<String, ILyricPositionProvider>,
        positions: LongArray
    ): Map<String, Stats> = coroutineScope {
        providers.map { (name, provider) ->
            async { name to measureSequenceWithStatsParallel(provider, positions) }
        }.awaitAll().toMap()
    }

    private suspend fun measureSequenceWithStatsParallel(
        provider: ILyricPositionProvider,
        positions: LongArray
    ): Stats = withContext(Dispatchers.Default) {
        val times = positions.map { pos ->
            async { measureNanoTime { provider.testfind(pos) {} } }
        }.awaitAll().sorted()
        val n = times.size
        Stats(
            avg = times.average().roundToLong(),
            p95 = times[((n * 0.95).toInt()).coerceAtMost(n - 1)],
            p99 = times[((n * 0.99).toInt()).coerceAtMost(n - 1)]
        )
    }

    private fun measureSingleWithStats(
        provider: ILyricPositionProvider,
        pos: Long,
        trials: Int
    ): Stats {
        val times = LongArray(trials) { measureNanoTime { provider.testfind(pos) {} } }.sorted()
        return Stats(
            avg = times.average().roundToLong(),
            p95 = times[((trials * 0.95).toInt()).coerceAtMost(trials - 1)],
            p99 = times[((trials * 0.99).toInt()).coerceAtMost(trials - 1)]
        )
    }

    // ================= 工具 =================
    private fun warmup(providers: Collection<ILyricPositionProvider>) {
        repeat(3) {
            for (p in 0L..5000L step 5) {
                providers.forEach { it.testfind(p) {} }
            }
        }
    }

    private fun generateFixedLyrics(): List<LyricLine> {
        var start = 0L
        val duration = 50L // 每行固定 50ms
        return List(TestConfig.LYRIC_COUNT) {
            LyricLine(begin = start, end = start + duration, duration = duration).also {
                start = it.end
            }
        }
    }

    private fun generateContinuousPositions(totalDuration: Long, fpsList: List<Int>): LongArray {
        return fpsList.flatMap { fps ->
            val frameCount = fps * TestConfig.CONTINUOUS_DURATION_SEC
            (0 until frameCount).map { ((1000.0 / fps) * it).toLong().coerceAtMost(totalDuration) }
        }.toLongArray()
    }

    private fun generateMultiHotPositions(
        centers: List<Long>,
        countPerCenter: Int,
        radius: Long,
        totalDuration: Long
    ): LongArray {
        return centers.flatMap { center ->
            List(countPerCenter) { i ->
                val step = (i % (2 * radius + 1)) - radius
                (center + step).coerceIn(0, totalDuration)
            }
        }.toLongArray()
    }

    private fun generateForwardSeekPositions(stepMs: Long, totalDuration: Long): LongArray {
        val list = ArrayList<Long>()
        var t = 0L
        while (t <= totalDuration) {
            list += t
            t += stepMs
        }
        return list.toLongArray()
    }
}
