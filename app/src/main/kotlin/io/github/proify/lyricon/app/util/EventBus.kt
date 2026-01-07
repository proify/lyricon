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

@file:Suppress("unused")

package io.github.proify.lyricon.app.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 全局事件总线,基于 SharedFlow 实现
 *
 * 特性:
 * - 支持类型安全的事件分发
 * - 自动管理生命周期
 * - 防止内存泄漏
 */
object EventBus {
    val bus: MutableSharedFlow<Any> =
        MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    /**
     * 发送事件(非挂起,适用于非协程环境)
     * @return 是否成功发送
     */
    fun post(event: Any): Boolean = bus.tryEmit(event)

    /**
     * 发送事件(挂起函数,保证发送成功)
     */
    suspend fun emit(event: Any) {
        bus.emit(event)
    }

    /**
     * 获取指定类型事件的 Flow
     * @param T 事件类型
     */
    inline fun <reified T> flow(): Flow<T> =
        bus
            .filterIsInstance<T>()

    /**
     * 在 LifecycleOwner 中安全收集事件
     * 自动在指定生命周期状态下启动和停止收集
     *
     * @param owner 生命周期持有者
     * @param state 最小生命周期状态,默认 STARTED
     * @param block 事件处理回调
     */
    inline fun <reified T> collect(
        owner: LifecycleOwner,
        state: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline block: suspend (T) -> Unit,
    ) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(state) {
                flow<T>().collect { block(it) }
            }
        }
    }

    /**
     * 在指定 CoroutineScope 中收集事件
     *
     * @param scope 协程作用域
     * @param block 事件处理回调
     */
    inline fun <reified T> collect(
        scope: CoroutineScope,
        crossinline block: suspend (T) -> Unit,
    ) {
        scope.launch {
            flow<T>().collect { block(it) }
        }
    }

    /**
     * 收集单次事件后自动取消
     *
     * @param scope 协程作用域
     * @param block 事件处理回调
     */
    inline fun <reified T> collectOnce(
        scope: CoroutineScope,
        crossinline block: suspend (T) -> Unit,
    ) {
        scope.launch {
            block(flow<T>().first())
        }
    }

    /**
     * 获取订阅者数量(用于调试)
     */
    val subscriberCount: Int
        get() = bus.subscriptionCount.value
}

/**
 * LifecycleOwner 快速订阅事件
 */
inline fun <reified T> LifecycleOwner.collectEvent(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend (T) -> Unit,
) {
    EventBus.collect(this, state, block)
}

/**
 * CoroutineScope 快速订阅事件
 */
inline fun <reified T> CoroutineScope.collectEvent(crossinline block: suspend (T) -> Unit) {
    EventBus.collect(this, block)
}