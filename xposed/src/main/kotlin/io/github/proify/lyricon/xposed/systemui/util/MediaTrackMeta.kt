/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.util

import android.media.MediaMetadata

/**
 * 系统媒体元数据源 (Media Track Meta)
 *
 * 从 Android MediaSession（MediaController.metadata）提取
 * 当前歌曲的标题 / 歌手 / 时长。
 *
 * 设计动机：播放器 provider（网易云、QQ音乐等）通过 AIDL 上报的 Song
 * 中，name / artist / duration 由各厂商 SDK 自行填充，经常为空、占位符或带额外后缀；
 * 而系统侧元数据由播放器媒体会话直接维护，最贴近真实播放内容。
 * 因此控制窗口等展示场景应当**优先使用系统元数据，再回退到 Song**。
 *
 * @author Tomakino
 * @since 2026
 */
object MediaTrackMeta {

    /** 解析后的曲目元数据；字段缺失时为 null，由调用方决定回退策略。 */
    data class TrackMeta(
        val title: String?,
        val artist: String?,
        val album: String?,
        val durationMs: Long?
    )

    /**
     * 解析指定播放器包名当前对应的系统元数据。
     *
     * @param packageName 活跃播放器的包名（一般为 LyricViewController.activePackage）
     * @return 解析结果；无匹配会话或无元数据时返回 null
     */
    fun resolve(packageName: String?): TrackMeta? {
        if (packageName.isNullOrBlank()) return null
        val controller = SystemUIMediaUtils.getController(packageName) ?: return null
        val metadata = controller.metadata ?: return null
        return extract(metadata)
    }

    /**
     * 从 [MediaMetadata] 中提取标题/歌手/时长。
     * 空白字符串、非正数时长（-1 或 0 表示未知）一律视为缺失。
     */
    fun extract(metadata: MediaMetadata): TrackMeta {
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
        val durationMs = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)

        return TrackMeta(
            title = title?.takeIf { it.isNotBlank() },
            artist = artist?.takeIf { it.isNotBlank() },
            album = album?.takeIf { it.isNotBlank() },
            durationMs = durationMs.takeIf { it > 0 }
        )
    }
}
