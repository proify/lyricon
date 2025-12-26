package io.github.proify.lyricon.xposed.hook.systemui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.android.extensions.deflate
import io.github.proify.lyricon.app.bridge.BridgeConstants
import io.github.proify.lyricon.central.BridgeCentral
import io.github.proify.lyricon.common.util.ViewHierarchyParser
import io.github.proify.lyricon.subscriber.LyricSubscriber
import io.github.proify.lyricon.xposed.util.NotificationCoverHelper

object SystemUIHooker : YukiBaseHooker() {
    private val TAG = "SystemUIHooker"
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
                        handleStatusBarView(result<ViewGroup>()!!)
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
        subscriber.service.registerActivePlayerListener(LyricViewController)
        subscriber.notifyRegister()

        Constants.initResourceIds(appContext)
        initDataChannel()

        NotificationCoverHelper.hook(appContext.classLoader)
    }

    private fun initDataChannel() {
        dataChannel.wait<String>(key = BridgeConstants.REQUEST_UPDATE_LYRIC_STYLE) { _ ->
            statusBarViewManager?.updateLyricView()
        }

        dataChannel.wait<String>(key = BridgeConstants.REQUEST_VIEW_TREE) { _ ->
            statusBarViewManager?.let {
                val node = ViewHierarchyParser.buildNodeTree(it.statusBarView)
                dataChannel.put(BridgeConstants.REQUEST_VIEW_TREE_CALLBACK, node.toJson().deflate())
            }
        }
    }

    private fun handleStatusBarView(view: ViewGroup) {
        statusBarViewManager = StatusBarViewManager(view)
        statusBarViewManager?.initialize()
        LyricViewController.statusBarViewManager = statusBarViewManager

        BridgeCentral.sendBootCompleted()
    }

}