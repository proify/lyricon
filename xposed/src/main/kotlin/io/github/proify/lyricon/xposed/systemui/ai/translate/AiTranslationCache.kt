/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import io.github.proify.android.extensions.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

/** 已完成整首歌翻译的双级缓存：内存 LRU + SQLite 持久化。 */
internal class AiTranslationCache(
    private val maxCacheSize: Int,
    private val generation: AtomicInteger,
    private val scope: CoroutineScope
) {
    private companion object {
        const val TAG = "LyriconAiTranslator"
    }

    private val dbMutex = Mutex()
    private var dbHelper: DatabaseHelper? = null

    private val memory: MutableMap<String, List<TranslationItem>> =
        Collections.synchronizedMap(
            object : LinkedHashMap<String, List<TranslationItem>>(maxCacheSize, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<TranslationItem>>?): Boolean {
                    return size > maxCacheSize
                }
            }
        )

    fun init(context: Context) {
        if (dbHelper == null) {
            synchronized(this) {
                if (dbHelper == null) {
                    Log.d(TAG, "Initializing database...")
                    dbHelper = DatabaseHelper(context.applicationContext)
                }
            }
        }
    }

    fun getFromMemory(key: String): List<TranslationItem>? = memory[key]

    fun putMemory(key: String, items: List<TranslationItem>) {
        memory[key] = items
    }

    suspend fun getFromDb(key: String): List<TranslationItem>? = dbMutex.withLock {
        val db = dbHelper?.readableDatabase ?: return null
        return runCatching {
            db.query(
                DatabaseHelper.TABLE_NAME,
                arrayOf(DatabaseHelper.COLUMN_DATA),
                "${DatabaseHelper.COLUMN_ID} = ?",
                arrayOf(key),
                null, null, null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val jsonData =
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATA))
                    json.decodeFromString<List<TranslationItem>>(jsonData)
                } else null
            }
        }.getOrElse {
            Log.e(TAG, "DB Query error: ${it.message}")
            null
        }
    }

    suspend fun saveToDb(key: String, items: List<TranslationItem>) {
        val jsonData = json.encodeToString(items)
        dbMutex.withLock {
            val db = dbHelper?.writableDatabase ?: return@withLock
            runCatching {
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_ID, key)
                    put(DatabaseHelper.COLUMN_DATA, jsonData)
                    put(DatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis())
                }
                db.insertWithOnConflict(
                    DatabaseHelper.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }.onFailure {
                Log.e(TAG, "Failed to save translation to DB: ${it.message}")
            }
        }
    }

    fun clear(callback: () -> Unit) {
        generation.incrementAndGet()
        memory.clear()
        scope.launch(Dispatchers.IO) {
            dbMutex.withLock {
                runCatching {
                    dbHelper?.writableDatabase?.delete(DatabaseHelper.TABLE_NAME, null, null)
                    Log.d(TAG, "Database cache cleared.")
                    withContext(Dispatchers.Main) { callback() }
                }.onFailure {
                    Log.e(TAG, "Error clearing database: ${it.message}")
                }
            }
        }
    }

    private class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            const val DATABASE_NAME = "lyricon_translation.db"
            const val DATABASE_VERSION = 1
            const val TABLE_NAME = "ai_cache"
            const val COLUMN_ID = "song_id"
            const val COLUMN_DATA = "translation_json"
            const val COLUMN_TIMESTAMP = "created_at"
        }

        override fun onCreate(db: SQLiteDatabase) {
            Log.i(TAG, "Creating translation cache table.")
            db.execSQL(
                """
                CREATE TABLE $TABLE_NAME (
                    $COLUMN_ID TEXT PRIMARY KEY,
                    $COLUMN_DATA TEXT,
                    $COLUMN_TIMESTAMP INTEGER
                )
            """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_timestamp ON $TABLE_NAME($COLUMN_TIMESTAMP)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(TAG, "Upgrading database from $oldVersion to $newVersion. All data will be lost.")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }
}
