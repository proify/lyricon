package io.github.proify.lyricon.lyric.model.interfaces

interface DeepCopyable<T : DeepCopyable<T>> {
    /**
     * 返回当前对象的深拷贝
     */
    fun deepCopy(): T
}