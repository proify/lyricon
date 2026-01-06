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
package io.github.proify.lyricon.xposed.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.lyricon.lyric.style.VisibilityRule

/**
 * 视图可见性控制器
 * 根据规则管理 ViewGroup 中子视图的可见性
 */
class ViewVisibilityController(private val rootViewGroup: ViewGroup) {

    /**
     * 根据规则更新视图可见性
     * @param rules 可见性规则列表
     * @param isPlaying 是否正在播放
     */
    fun applyVisibilityRules(rules: List<VisibilityRule>, isPlaying: Boolean) {
        YLog.debug("Applying visibility rules... " + rules)
        if (rules.isEmpty()) return

        rules.forEach { rule ->
            applyRuleToView(rule, isPlaying)
        }
    }

    private fun applyRuleToView(rule: VisibilityRule, isPlaying: Boolean) {
        val viewId = rule.id
        if (viewId.isNullOrBlank()) return

        val targetView = findViewByResourceName(rootViewGroup, viewId) ?: return

        // 标记视图以便追踪
        targetView.setTag(ViewVisibilityTracker.TRACKING_TAG_ID, TRACKED_MARKER)

        when (rule.mode) {
            VisibilityRule.MODE_NORMAL -> restoreOriginalVisibility(targetView)
            VisibilityRule.MODE_ALWAYS_VISIBLE -> setVisibility(targetView, View.VISIBLE)
            VisibilityRule.MODE_ALWAYS_HIDDEN -> setVisibility(targetView, View.GONE)
            VisibilityRule.MODE_HIDE_WHEN_PLAYING -> applyPlaybackRule(targetView, isPlaying)
        }
    }

    private fun restoreOriginalVisibility(view: View) {
        val originalVisibility = ViewVisibilityTracker.getOriginalVisibility(view.id)
        if (originalVisibility != VISIBILITY_UNKNOWN) {
            view.visibility = originalVisibility
        }
    }

    private fun setVisibility(view: View, visibility: Int) {
        view.visibility = when (visibility) {
            View.VISIBLE -> ViewVisibilityTracker.CUSTOM_VISIBLE
            View.GONE -> ViewVisibilityTracker.CUSTOM_GONE
            else -> visibility
        }
    }

    private fun applyPlaybackRule(view: View, isPlaying: Boolean) {
        if (isPlaying) {
            setVisibility(view, View.GONE)
        } else {
            restoreOriginalVisibility(view)
        }
    }

    /**
     * 根据资源名称递归查找视图
     */
    private fun findViewByResourceName(view: View, targetResourceName: String): View? {
        if (ResourceMapper.getIdName(view) == targetResourceName) {
            return view
        }

        if (view is ViewGroup) {
            view.forEach { child ->
                findViewByResourceName(child, targetResourceName)?.let { return it }
            }
        }
        return null
    }

    companion object {
        private const val TRACKED_MARKER = "tracked"
        private const val VISIBILITY_UNKNOWN = -1
    }
}