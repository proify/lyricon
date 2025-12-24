package io.github.proify.lyricon.common.util

import android.content.SharedPreferences
import io.github.proify.lyricon.common.extensions.setPermission644
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * 基于 JSON 文件的 SharedPreferences 实现
 *
 * 支持多进程安全读写,使用文件锁机制防止并发冲突。
 *
 * ## 特性
 * - **线程安全**: 使用读写锁保护内存数据结构
 * - **进程安全**: 使用文件锁协调跨进程访问
 * - **原子写入**: 临时文件 + 原子重命名,防止数据损坏
 * - **自动合并**: 写入前重新读取,合并其他进程的修改
 * - **容错处理**: 文件读取/解析失败时的降级策略
 *
 * ## 使用示例
 * ```kotlin
 * val prefs = JsonSharedPreferences(File(context.filesDir, "config.json"))
 *
 * // 读取
 * val name = prefs.getString("user_name", "Guest")
 * val age = prefs.getInt("age", 0)
 *
 * // 写入
 * prefs.edit()
 *     .putString("user_name", "Alice")
 *     .putInt("age", 25)
 *     .apply()
 * ```
 *
 * ## 文件格式
 * ```json
 * {
 *   "user_name": "Alice",
 *   "age": 25,
 *   "is_premium": true,
 *   "tags": ["vip", "active"]
 * }
 * ```
 *
 * ## 注意事项
 * - 会在 JSON 文件同目录下创建 `.lock` 文件用于进程同步
 * - 如果文件系统不支持文件锁,会降级到单进程线程安全模式
 * - Float 值会转换为 Double 存储(JSON 标准)
 * - 不支持存储 null 值,putString(key, null) 等同于 remove(key)
 *
 * @param jsonFile JSON 配置文件路径
 * @constructor 创建实例并自动加载现有数据
 *
 * @author Proify and claude
 * @author Claude
 *
 * @since 1.0.0
 */
class JsonSharedPreferences(
    private val jsonFile: File,
    private val mode: Int = 0
) : SharedPreferences {

    /** 线程锁,保护内存数据结构 */
    private val lock = ReentrantReadWriteLock()

    /** 文件锁文件,用于跨进程同步 */
    private val lockFile = File(jsonFile.parentFile, "${jsonFile.name}.lock")

    /** 文件最后修改时间,用于检测外部修改 */
    private var lastModified = 0L

    /** 文件大小,用于检测外部修改 */
    private var lastFileSize = 0L

    /** 内存中的数据缓存 */
    private var data: JSONObject = JSONObject()

    /** 变更监听器集合(线程安全) */
    private val listeners =
        CopyOnWriteArraySet<SharedPreferences.OnSharedPreferenceChangeListener>()

    init {
        reloadIfNeeded(force = true)
    }

    companion object {
        const val MODE_WORLD_READABLE = 1
    }

    /**
     * 检查文件是否被外部修改
     *
     * 通过比较文件的修改时间和大小来判断。
     * 此方法只读取文件元数据,不会重新加载文件内容。
     *
     * @return true 表示文件已被修改
     */
    fun hasFileChanged(): Boolean = lock.read {
        if (!jsonFile.exists()) {
            return lastModified != 0L || lastFileSize != 0L
        }
        try {
            jsonFile.lastModified() != lastModified || jsonFile.length() != lastFileSize
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 获取文件共享锁并执行代码块
     *
     * 共享锁允许多个进程同时读取,但会阻止写入。
     * 使用 Java NIO 的 FileChannel.lock() 实现跨进程同步。
     *
     * @param block 在共享锁保护下执行的代码
     * @return 代码块的返回值
     * @throws IOException 如果锁文件操作失败(会降级执行)
     */
    private fun <T> withSharedLock(block: () -> T): T {
        return try {
            lockFile.parentFile?.mkdirs()
            RandomAccessFile(lockFile, "rw").use { raf ->
                raf.channel.lock(0, Long.MAX_VALUE, true).use {
                    block()
                }
            }
        } catch (_: IOException) {
            // 如果无法获取锁文件,直接执行(降级处理)
            block()
        }
    }

    /**
     * 获取文件独占锁并执行代码块
     *
     * 独占锁会阻止其他进程的读写操作。
     * 用于写入时确保数据一致性。
     *
     * @param block 在独占锁保护下执行的代码
     * @return 代码块的返回值
     * @throws IOException 如果锁文件操作失败(会降级执行)
     */
    private fun <T> withExclusiveLock(block: () -> T): T {
        return try {
            lockFile.parentFile?.mkdirs()
            RandomAccessFile(lockFile, "rw").use { raf ->
                raf.channel.lock(0, Long.MAX_VALUE, false).use {
                    block()
                }
            }
        } catch (e: IOException) {
            // 如果无法获取锁文件,直接执行(降级处理)
            block()
        }
    }

    /**
     * 如果文件被修改则重新加载
     *
     * 工作流程:
     * 1. 检查文件是否被修改(比对时间戳和大小)
     * 2. 获取共享锁,防止读取时被其他进程修改
     * 3. 双重检查,避免并发重复加载
     * 4. 读取并解析 JSON 文件
     * 5. 更新内存缓存和元数据
     * 6. 释放锁
     *
     * @param force 是否强制重新加载,忽略变更检查
     */
    private fun reloadIfNeeded(force: Boolean = false) {
        if (!force && !hasFileChanged()) return

        withSharedLock {
            lock.write {
                // 双重检查,避免重复加载
                if (!force && !hasFileChangedUnsafe()) return@withSharedLock

                data = try {
                    if (jsonFile.exists()) {
                        val text = jsonFile.readText()
                        if (text.isBlank()) {
                            JSONObject()
                        } else {
                            JSONObject(text)
                        }
                    } else {
                        JSONObject()
                    }
                } catch (_: IOException) {
                    // 文件读取失败,保留旧数据
                    data
                } catch (_: JSONException) {
                    // JSON 解析失败,重置为空对象
                    JSONObject()
                } catch (_: SecurityException) {
                    // 权限不足,保留旧数据
                    data
                } catch (_: Exception) {
                    // 其他异常,保留旧数据
                    data
                }

                lastModified = try {
                    if (jsonFile.exists()) jsonFile.lastModified() else 0L
                } catch (_: SecurityException) {
                    0L
                }

                lastFileSize = try {
                    if (jsonFile.exists()) jsonFile.length() else 0L
                } catch (_: SecurityException) {
                    0L
                }
            }
        }
    }

    /**
     * 检查文件是否被修改(无锁版本)
     *
     * 仅供内部使用,调用前必须已持有锁。
     *
     * @return true 表示文件已被修改
     */
    private fun hasFileChangedUnsafe(): Boolean {
        if (!jsonFile.exists()) {
            return lastModified != 0L || lastFileSize != 0L
        }
        return try {
            jsonFile.lastModified() != lastModified || jsonFile.length() != lastFileSize
        } catch (_: SecurityException) {
            false
        }
    }

    /**
     * 获取键对应的原始值
     *
     * @param key 键名
     * @return 值对象,如果不存在则返回 null
     */
    private fun getValue(key: String?): Any? {
        if (key.isNullOrBlank()) return null
        reloadIfNeeded()
        return lock.read {
            if (data.has(key)) data.opt(key) else null
        }
    }

    /**
     * 获取所有键值对
     *
     * @return 包含所有数据的不可变 Map
     */
    override fun getAll(): Map<String, *> {
        reloadIfNeeded()
        return lock.read {
            data.keys().asSequence()
                .filterNotNull()
                .associateWith { data.opt(it) }
        }
    }

    /**
     * 检查是否包含指定的键
     *
     * @param key 键名
     * @return true 表示存在该键
     */
    override fun contains(key: String?): Boolean {
        if (key.isNullOrBlank()) return false
        reloadIfNeeded()
        return lock.read { data.has(key) }
    }

    /**
     * 获取字符串值
     *
     * 支持类型转换:
     * - Number/Boolean → 转换为字符串
     * - 其他类型 → 返回默认值
     *
     * @param key 键名
     * @param defValue 默认值
     * @return 字符串值或默认值
     */
    override fun getString(key: String?, defValue: String?): String? {
        val value = getValue(key) ?: return defValue
        return when (value) {
            is String -> value
            is Number, is Boolean -> value.toString()
            else -> defValue
        }
    }

    /**
     * 获取整数值
     *
     * 支持类型转换:
     * - Long/Double/Float → 检查范围后转换
     * - String → 尝试解析
     * - Boolean → true=1, false=0
     * - 其他类型/溢出 → 返回默认值
     *
     * @param key 键名
     * @param defValue 默认值
     * @return 整数值或默认值
     */
    override fun getInt(key: String?, defValue: Int): Int {
        val value = getValue(key) ?: return defValue
        return when (value) {
            is Int -> value
            is Long -> if (value in Int.MIN_VALUE..Int.MAX_VALUE) value.toInt() else defValue
            is Double, is Float -> {
                val num = value.toDouble()
                if (num.isFinite() && num in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble()) {
                    num.toInt()
                } else defValue
            }

            is String -> value.toIntOrNull() ?: defValue
            is Boolean -> if (value) 1 else 0
            else -> defValue
        }
    }

    /**
     * 获取长整数值
     *
     * 支持类型转换:
     * - Int/Double/Float → 检查范围后转换
     * - String → 尝试解析
     * - Boolean → true=1L, false=0L
     * - 其他类型/溢出 → 返回默认值
     *
     * @param key 键名
     * @param defValue 默认值
     * @return 长整数值或默认值
     */
    override fun getLong(key: String?, defValue: Long): Long {
        val value = getValue(key) ?: return defValue
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double, is Float -> {
                val num = value.toDouble()
                if (num.isFinite() && num in Long.MIN_VALUE.toDouble()..Long.MAX_VALUE.toDouble()) {
                    num.toLong()
                } else defValue
            }

            is String -> value.toLongOrNull() ?: defValue
            is Boolean -> if (value) 1L else 0L
            else -> defValue
        }
    }

    /**
     * 获取浮点数值
     *
     * 支持类型转换:
     * - Double → 检查有限性后转换
     * - Int/Long → 直接转换
     * - String → 尝试解析
     * - Boolean → true=1f, false=0f
     * - 其他类型/NaN/Infinity → 返回默认值
     *
     * @param key 键名
     * @param defValue 默认值
     * @return 浮点数值或默认值
     */
    override fun getFloat(key: String?, defValue: Float): Float {
        val value = getValue(key) ?: return defValue
        return when (value) {
            is Float -> value
            is Double -> if (value.isFinite()) value.toFloat() else defValue
            is Int, is Long -> value.toFloat()
            is String -> value.toFloatOrNull() ?: defValue
            is Boolean -> if (value) 1f else 0f
            else -> defValue
        }
    }

    /**
     * 获取布尔值
     *
     * 支持类型转换:
     * - Number → 非零为 true
     * - String → "true"(忽略大小写)或 "1" 或可解析为非零数字
     * - 其他类型 → 返回默认值
     *
     * @param key 键名
     * @param defValue 默认值
     * @return 布尔值或默认值
     */
    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        val value = getValue(key) ?: return defValue
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value.equals("true", ignoreCase = true) ||
                    value.equals("1", ignoreCase = false) ||
                    value.toIntOrNull()?.let { it != 0 } ?: false

            else -> defValue
        }
    }

    /**
     * 获取字符串集合
     *
     * 从 JSONArray 转换为 Set,自动过滤 null 值。
     *
     * @param key 键名
     * @param defValues 默认值
     * @return 字符串集合或默认值
     */
    override fun getStringSet(key: String?, defValues: Set<String>?): Set<String> {
        return when (val value = getValue(key)) {
            is JSONArray -> buildSet {
                for (i in 0 until value.length()) {
                    val str = value.optString(i, null)
                    if (str != null) add(str)
                }
            }

            else -> defValues?.toSet() ?: emptySet()
        }
    }

    /**
     * 创建编辑器用于修改数据
     *
     * @return 新的编辑器实例
     */
    override fun edit(): SharedPreferences.Editor = EditorImpl()

    /**
     * 注册变更监听器
     *
     * 当数据被修改时,会在 apply()/commit() 完成后通知所有监听器。
     *
     * @param listener 监听器实例
     */
    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        listener?.let { listeners.add(it) }
    }

    /**
     * 注销变更监听器
     *
     * @param listener 要移除的监听器实例
     */
    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        listener?.let { listeners.remove(it) }
    }

    /**
     * SharedPreferences.Editor 实现
     *
     * 支持链式调用,所有修改会暂存在内存中,
     * 直到调用 apply() 或 commit() 才会写入文件。
     */
    private inner class EditorImpl : SharedPreferences.Editor {

        /** 待写入的数据 */
        private val pending = JSONObject()

        /** 待删除的键 */
        private val removed = mutableSetOf<String>()

        /** 是否清空所有数据 */
        private var clearAll = false

        /**
         * 存储布尔值
         *
         * @param key 键名(不能为空或空白)
         * @param value 布尔值
         * @return 当前编辑器实例(支持链式调用)
         */
        override fun putBoolean(key: String?, value: Boolean) = apply {
            key?.takeIf { it.isNotBlank() }?.let { pending.put(it, value) }
        }

        /**
         * 存储整数值
         *
         * @param key 键名(不能为空或空白)
         * @param value 整数值
         * @return 当前编辑器实例(支持链式调用)
         */
        override fun putInt(key: String?, value: Int) = apply {
            key?.takeIf { it.isNotBlank() }?.let { pending.put(it, value) }
        }

        /**
         * 存储长整数值
         *
         * @param key 键名(不能为空或空白)
         * @param value 长整数值
         * @return 当前编辑器实例(支持链式调用)
         */
        override fun putLong(key: String?, value: Long) = apply {
            key?.takeIf { it.isNotBlank() }?.let { pending.put(it, value) }
        }

        /**
         * 存储浮点数值
         *
         * 注意: Float 会转换为 Double 存储(JSON 标准),
         * 并且会拒绝 NaN 和 Infinity。
         *
         * @param key 键名(不能为空或空白)
         * @param value 浮点数值(必须是有限数)
         * @return 当前编辑器实例(支持链式调用)
         */
        override fun putFloat(key: String?, value: Float) = apply {
            key?.takeIf { it.isNotBlank() }?.let {
                if (value.isFinite()) {
                    pending.put(it, value.toDouble())
                }
            }
        }

        /**
         * 存储字符串值
         *
         * 注意: 传入 null 等同于调用 remove(key)。
         *
         * @param key 键名(不能为空或空白)
         * @param value 字符串值(null 表示删除)
         * @return 当前编辑器实例(支持链式调用)
         */
        override fun putString(key: String?, value: String?) = apply {
            key?.takeIf { it.isNotBlank() }?.let {
                if (value == null) {
                    removed.add(it)
                } else {
                    pending.put(it, value)
                }
            }
        }

        /**
         * 存储字符串集合
         *
         * 自动过滤 null 值,存储为 JSONArray。
         *
         * @param key 键名(不能为空或空白)
         * @param values 字符串集合
         * @return 当前编辑器实例(支持链式调用)
         */
        override fun putStringSet(key: String?, values: Set<String>?) = apply {
            key?.takeIf { it.isNotBlank() }?.let {
                val array = JSONArray()
                values?.forEach { v -> array.put(v) }
                pending.put(it, array)
            }
        }

        /**
         * 删除指定的键
         *
         * @param key 键名(不能为空或空白)
         * @return 当前编辑器实例(支持链式调用)
         */
        override fun remove(key: String?) = apply {
            key?.takeIf { it.isNotBlank() }?.let { removed.add(it) }
        }

        /**
         * 清空所有数据
         *
         * 注意: 会删除文件中的所有键值对,
         * 但之后通过 put* 方法添加的数据会被保留。
         *
         * @return 当前编辑器实例(支持链式调用)
         */
        override fun clear() = apply { clearAll = true }

        /**
         * 同步提交修改
         *
         * 工作流程:
         * 1. 获取独占文件锁
         * 2. 重新读取文件(合并其他进程的修改)
         * 3. 应用本次修改
         * 4. 原子写入文件
         * 5. 释放锁
         * 6. 通知监听器
         *
         * @return true 表示写入成功, false 表示失败
         */
        override fun commit(): Boolean {
            return try {
                applyInternal()
                true
            } catch (_: Exception) {
                false
            }
        }

        /**
         * 异步提交修改
         *
         * 与 commit() 相同,但不抛出异常,失败时静默。
         * 通常用于非关键数据的保存。
         */
        override fun apply() {
            try {
                applyInternal()
            } catch (_: Exception) {
                // apply() 不抛出异常,静默失败
            }
        }

        /**
         * 内部提交实现
         *
         * 执行实际的文件读写操作:
         * 1. 获取独占锁,阻止其他进程访问
         * 2. 重新读取文件内容(读-修改-写模式)
         * 3. 合并其他进程的修改
         * 4. 应用本次修改
         * 5. 原子写入(临时文件 + 重命名)
         * 6. 通知所有监听器
         *
         * @throws IOException 文件操作失败
         * @throws SecurityException 权限不足
         */
        private fun applyInternal() {
            val changedKeys = mutableSetOf<String>()

            withExclusiveLock {
                // 在写锁保护下,先重新读取最新数据(其他进程可能已修改)
                val freshData = try {
                    if (jsonFile.exists()) {
                        val text = jsonFile.readText()
                        if (text.isBlank()) JSONObject() else JSONObject(text)
                    } else {
                        JSONObject()
                    }
                } catch (_: Exception) {
                    // 读取失败,使用当前内存数据
                    lock.read { JSONObject(data.toString()) }
                }

                lock.write {
                    data = freshData

                    if (clearAll) {
                        changedKeys.addAll(data.keys().asSequence().filterNotNull())
                        data = JSONObject()
                    }

                    removed.forEach {
                        if (data.has(it)) {
                            data.remove(it)
                            changedKeys.add(it)
                        }
                    }

                    pending.keys().forEach { key ->
                        val oldValue = data.opt(key)
                        val newValue = pending.get(key)
                        if (oldValue != newValue) {
                            data.put(key, newValue)
                            changedKeys.add(key)
                        }
                    }

                    try {
                        jsonFile.parentFile?.mkdirs()

                        // 原子写入:先写临时文件,再重命名
                        val tempFile = File(jsonFile.parentFile, "${jsonFile.name}.tmp")
                        tempFile.writeText(data.toString())

                        // 确保数据刷到磁盘
                        RandomAccessFile(tempFile, "rw").use { raf ->
                            raf.fd.sync()
                        }

                        // 原子重命名
                        if (!tempFile.renameTo(jsonFile)) {
                            // 重命名失败,手动复制并删除
                            tempFile.copyTo(jsonFile, overwrite = true)
                            tempFile.delete()
                        }

                        lastModified = jsonFile.lastModified()
                        lastFileSize = jsonFile.length()

                        if (mode == MODE_WORLD_READABLE) {
                            jsonFile.setPermission644()
                        }

                    } catch (e: IOException) {
                        throw IOException("无法写入配置文件: ${jsonFile.absolutePath}", e)
                    } catch (e: SecurityException) {
                        throw SecurityException(
                            "没有写入配置文件的权限: ${jsonFile.absolutePath}",
                            e
                        )
                    }
                }
            }

            // 在锁外通知监听器,避免死锁
            if (changedKeys.isNotEmpty()) {
                listeners.forEach { listener ->
                    changedKeys.forEach { key ->
                        try {
                            listener.onSharedPreferenceChanged(this@JsonSharedPreferences, key)
                        } catch (_: Exception) {
                            // 防止监听器异常影响其他监听器
                        }
                    }
                }
            }
        }
    }
}