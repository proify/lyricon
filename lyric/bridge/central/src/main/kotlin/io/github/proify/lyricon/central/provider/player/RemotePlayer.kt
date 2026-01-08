package io.github.proify.lyricon.central.provider.player

import android.os.SharedMemory
import android.system.OsConstants
import android.util.Log
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.IRemotePlayer
import io.github.proify.lyricon.provider.ProviderInfo
import io.github.proify.lyricon.provider.extensions.inflate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class RemotePlayer(
    info: ProviderInfo,
    private val playerListener: PlayerListener
) : IRemotePlayer.Stub() {

    companion object {
        private const val TAG = "RemotePlayer"

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }
    }

    private val recorder = PlayerRecorder(info)

    private var positionSharedMemory: SharedMemory? = null

    @Volatile
    private var positionReadBuffer: ByteBuffer? = null

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    @Volatile
    private var positionJob: Job? = null

    @Volatile
    private var positionUpdateIntervalMs: Long = 50L

    private val released = AtomicBoolean(false)

    init {
        initSharedMemory()
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return

        Log.i(TAG, "release()")

        stopPositionUpdate()

        positionReadBuffer?.let {
            SharedMemory.unmap(it)
        }

        positionReadBuffer = null
        positionSharedMemory?.close()
        positionSharedMemory = null

        scope.cancel()
    }

    private fun initSharedMemory() {
        try {
            positionSharedMemory = SharedMemory.create(
                "lyricon_music_position_${android.os.Process.myPid()}",
                Long.SIZE_BYTES
            ).apply {
                setProtect(OsConstants.PROT_READ or OsConstants.PROT_WRITE)
                positionReadBuffer = mapReadOnly()
            }
            Log.i(TAG, "SharedMemory initialized")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to init SharedMemory", t)
        }
    }

    private fun readPosition(): Long {
        val buffer = positionReadBuffer ?: return 0
        return try {
            synchronized(buffer) {
                val position = buffer.getLong(0)
                position.coerceIn(0, recorder.song?.duration ?: 0)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Read position failed", t)
            0
        }
    }

    // ---------- Position Update ----------

    private fun startPositionUpdate() {
        if (positionJob != null) return

        Log.d(TAG, "Start position updater ,interval $positionUpdateIntervalMs ms")

        positionJob = scope.launch {
            while (isActive) {
                val position = readPosition()
                if (position != recorder.lastPosition) {
                    recorder.lastPosition = position
                    playerListener.onPositionChanged(recorder, position)
                }
                delay(positionUpdateIntervalMs)
            }
        }
    }

    private fun stopPositionUpdate() {
        positionJob?.cancel()
        positionJob = null
        Log.d(TAG, "Stop position updater")
    }

    override fun setPositionUpdateInterval(interval: Int) {
        check(!released.get()) { "Player is released" }

        positionUpdateIntervalMs = interval.coerceAtLeast(16).toLong()
        Log.i(TAG, "Update interval = $positionUpdateIntervalMs ms")

        if (positionJob != null) {
            stopPositionUpdate()
            startPositionUpdate()
        }
    }

    // ---------- AIDL ----------

    @OptIn(ExperimentalSerializationApi::class)
    override fun setSong(bytes: ByteArray?) {
        check(!released.get()) { "Player is released" }

        val song = bytes?.let {
            try {
                val start = System.currentTimeMillis()
                val decompressedBytes = bytes.inflate()
                val song = json.decodeFromStream(
                    Song.serializer(),
                    decompressedBytes.inputStream()
                )
                Log.d(TAG, "Song parsed in ${System.currentTimeMillis() - start} ms")
                song
            } catch (t: Throwable) {
                Log.e(TAG, "Song parse failed", t)
                null
            }
        }

        val normalized = song?.normalize()
        recorder.song = normalized
        recorder.text = null

        Log.i(TAG, "Song changed")
        playerListener.onSongChanged(recorder, normalized)
    }

    override fun setPlaybackState(isPlaying: Boolean) {
        check(!released.get()) { "Player is released" }

        recorder.isPlaying = isPlaying
        playerListener.onPlaybackStateChanged(recorder, isPlaying)

        Log.i(TAG, "Playback state = $isPlaying")

        if (isPlaying) {
            startPositionUpdate()
        } else {
            stopPositionUpdate()
        }
    }

    override fun seekTo(position: Long) {
        check(!released.get()) { "Player is released" }

        val safe = position.coerceIn(0, recorder.song?.duration ?: 0)
        recorder.lastPosition = safe

        playerListener.onSeekTo(recorder, safe)
    }

    override fun sendText(text: String?) {
        check(!released.get()) { "Player is released" }

        recorder.song = null
        recorder.text = text

        playerListener.onPostText(recorder, text)
    }

    override fun getPositionUpdateSharedMemory(): SharedMemory? {
        return positionSharedMemory
    }
}