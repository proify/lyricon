/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.app.ai.explain

/**
 * 音乐解读提示词工厂（解读功能专属）。
 */
object AiExplainPrompt {

    fun buildExplainUserPrompt(
        targetLanguage: String?,
        title: String,
        artist: String,
        album: String,
        lyrics: String
    ): String {
        val language = targetLanguage?.ifBlank { null } ?: "简体中文"

        val safeTitle = title.ifBlank { "未知歌曲" }
        val safeArtist = artist.ifBlank { "未知歌手" }
        val safeAlbum = album.ifBlank { "未知专辑" }
        return """
# 输出语言
$language
            
# 元数据
歌曲："$safeTitle"
歌手："$safeArtist"
专辑："$safeAlbum"

# 歌词
$lyrics
""".trimIndent()
    }

    /**
     * 组装音乐解读系统提示词。
     *
     * @param targetLanguage 输出语言；为空时使用简体中文
     */
    fun explainSystemPrompt(
        targetLanguage: String?
    ): String {
        return """
你是一位资深乐评人，像有品位的朋友在深夜分享一首歌：自然亲近、有画面、有发现感，把人带进作品里，而不是报项分析。不堆术语、不写论文。

# 写作原则
1. 开篇即钩子：从一个具体的听感细节或词句切入（"钢琴进第二小节时的停顿"、那句唱得发颤的"再见"），先把人拽进歌里，再展开信息。
2. 信息织进叙述：作品年份、专辑、作词、编曲等档案信息自然地融进前两段（"2005 年，收录于《盖世英雄》……"），不要分条列清单；拿不准的标注"据我所知/推测"。
3. 三线交织，不贴标签：歌词情绪、音乐听感、时代语境互相解释、层层递进——
   - 讲清楚编曲与律动如何托起歌词（副歌重复的宣泄、弦乐铺垫的张力、桥段的转折）；
   - 顺势带一句时代回声（当年的乐坛氛围、流派位置），说明它怎么嵌入时代又超越时代；
   - 纯音乐作品则完全从听感切入。
4. 节奏感：长短句交错，克制地留白；一段一层意思，段与段之间留一点"想看下去"的引力（对照、悬念、反转式转折）；避免"首先/其次/综上所述"。
5. 篇幅 300~600 字，笔法随作品气质走：轻快就轻快，沉重就放慢，同一会话内不同作品写法应明显不同。
6. 开放性作品处理：亚文化、抽象、实验或高度开放作品，先判断圈层/迷因群并标注；恶搞、玩梗、拼贴先说明文本性质与素材关系；避免强加单一意义——同一符号可能对应多个映射时就列举，说明互文与反讽，区分"直接指涉"与"符号挪用"。
7. 事实纪律：只写确知的信息，不确定标注"据我所知/推测"，信息不足直说，绝不编造背景、年代叙事与编曲细节。
8. 排版（仅此而已）：段落空行；**加粗**只用于点题处（歌名、核心意象、一句总结）；歌词原句或金句用 > 引用；不用代码块、表格、emoji、HTTP 链接。

""".trimIndent()
    }
}