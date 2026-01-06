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

package io.github.proify.lyricon.xposed.hook.systemui

import android.util.Log.v
import android.view.LayoutInflater
import android.view.ViewGroup
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.android.extensions.deflate
import io.github.proify.lyricon.app.bridge.AppBridgeConstants
import io.github.proify.lyricon.central.BridgeCentral
import io.github.proify.lyricon.common.util.ViewHierarchyParser
import io.github.proify.lyricon.subscriber.LyricSubscriber
import io.github.proify.lyricon.xposed.util.LyricPrefs
import io.github.proify.lyricon.xposed.util.NotificationCoverHelper
import io.github.proify.lyricon.xposed.util.ViewVisibilityTracker

object SystemUIHooker : YukiBaseHooker() {
    private var layoutInflaterResult: YukiMemberHookCreator.MemberHookCreator.Result? = null
    private var statusBarViewManager: StatusBarViewManager? = null

    private lateinit var subscriber: LyricSubscriber

    override fun onHook() {
        onAppLifecycle {
            onCreate { onAppCreate() }
        }
    }

    private fun onAppCreate() {
        init()

        layoutInflaterResult = LayoutInflater::class.resolve()
            .firstMethod {
                name = "inflate"
                parameters(Int::class.java, ViewGroup::class.java, Boolean::class.java)
            }.hook {
                after {
                    val id = args(0).int()
                    if (id == Constants.statusBarLayoutId) {
                        setupStatusBarView(result<ViewGroup>()!!)
                        layoutInflaterResult?.remove()
                        layoutInflaterResult = null
                    }
                }
            }
    }

    private fun init() {
        val appContext = appContext
        if (appContext == null) {
            YLog.error("SystemUIHooker.onInit appContext is null")
            return
        }
        BridgeCentral.initialize(appContext)
        YLog.debug("SystemUIHooker.onInit")

        subscriber = LyricSubscriber(appContext)
        subscriber.service.apply {
            registerActivePlayerListener(LyricViewController)
        }
        subscriber.notifyRegister()

        Constants.initResourceIds(appContext)
        initDataChannel()

        NotificationCoverHelper.hook(appContext.classLoader)
        ViewVisibilityTracker.initialize(appContext.classLoader)
    }

    private fun initDataChannel() {
        dataChannel.wait(key = AppBridgeConstants.REQUEST_UPDATE_LYRIC_STYLE) {
            val style = LyricPrefs.getLyricStyle()
            statusBarViewManager?.updateLyricStyle(style)
        }
        dataChannel.wait<String>(key = AppBridgeConstants.REQUEST_HIGHLIGHT_VIEW) { id ->
            statusBarViewManager?.hightlightView(id)
        }
        dataChannel.wait<String>(key = AppBridgeConstants.REQUEST_VIEW_TREE) { _ ->
            statusBarViewManager?.let {
                val node = ViewHierarchyParser.buildNodeTree(it.statusBarView)
                dataChannel.put(
                    AppBridgeConstants.REQUEST_VIEW_TREE_CALLBACK,
                    node.toJson().deflate()
                )
            }
        }
    }

    private fun setupStatusBarView(view: ViewGroup) {
        statusBarViewManager = StatusBarViewManager(
            view,
            LyricPrefs.getLyricStyle()
        )
        LyricViewController.statusBarViewManager = statusBarViewManager
        BridgeCentral.sendBootCompleted()
    }
}