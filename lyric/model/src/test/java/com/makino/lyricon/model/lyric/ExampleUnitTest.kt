import io.github.proify.lyricon.lyric.model.LyricLine
import io.github.proify.lyricon.lyric.model.filterByPositionOrPrevious
import org.junit.Test
import kotlin.random.Random

class ExampleUnitTest {

    @Test
    fun testLyricFilter() {
        var start = 0
        val maxOverlap = 50    // 最大允许重叠时间 5 秒
        val minDuration = 30      // 每句歌词最短持续 3 秒
        val maxDuration = 100     // 每句歌词最长持续 10 秒

        val lyrics = List(4000000) { i ->

            // 随机生成每句歌词的时长
            val duration = Random.nextInt(minDuration, maxDuration)

            // 随机生成偏移，允许重叠
            val overlap = Random.nextInt(0, maxOverlap + 1)

            val line = LyricLine(
                begin = start,
                end = start + duration,
                text = "这是第 ${i + 1} 句歌词"
            )

            // 下一句开始时间可能会与当前句重叠
            start = line.end - overlap

            line
        }

        println("=== 性能测试 ===")
        println("歌词总数: ${lyrics.size}")
        println("总时长: ${lyrics.last().end}秒\n")

        testPosition(lyrics, 100, "开始位置")
        testPosition(lyrics, lyrics.last().end / 2, "中间位置")
        testPosition(lyrics, lyrics.last().end, "结束位置")
    }

    fun testPosition(lyrics: List<LyricLine>, position: Int, label: String) {
        val start = System.currentTimeMillis()
        val current = lyrics.filterByPositionOrPrevious(position)
        println("[$label] 歌词: ${current.map { it.text }}, 耗时: ${System.currentTimeMillis() - start}ms")
    }
}