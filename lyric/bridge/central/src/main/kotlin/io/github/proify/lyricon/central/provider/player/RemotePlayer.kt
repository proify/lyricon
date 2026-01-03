package io.github.proify.lyricon.central.provider.player

import android.os.SharedMemory
import android.system.OsConstants
import android.util.Log
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.IRemotePlayer
import io.github.proify.lyricon.provider.ProviderInfo
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
import kotlin.math.max

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

    // ---------- SharedMemory ----------
    private var positionSharedMemory: SharedMemory? = null

    @Volatile
    private var positionReadBuffer: ByteBuffer? = null

    // ---------- Coroutine ----------
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

    // ---------- Lifecycle ----------

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

    // ---------- SharedMemory ----------

    private fun initSharedMemory() {
        try {
            positionSharedMemory = SharedMemory.create(
                "music_position_${android.os.Process.myPid()}",
                Int.SIZE_BYTES
            ).apply {
                setProtect(OsConstants.PROT_READ or OsConstants.PROT_WRITE)
            }

            positionReadBuffer = positionSharedMemory!!.mapReadOnly()

            Log.i(TAG, "SharedMemory initialized")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to init SharedMemory", t)
        }
    }

    private fun readPosition(): Int {
        val buffer = positionReadBuffer ?: return 0
        return try {
            synchronized(buffer) {
                max(0, buffer.getInt(0))
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
    override fun setSong(songByteArray: ByteArray?) {
        check(!released.get()) { "Player is released" }

        val song = songByteArray?.let {
            try {
                val start = System.currentTimeMillis()
                val parsed = json.decodeFromStream(
                    Song.serializer(),
                    it.inputStream()
                )
                Log.d(TAG, "Song parsed in ${System.currentTimeMillis() - start} ms")
                parsed
            } catch (t: Throwable) {
                Log.e(TAG, "Song parse failed", t)
                null
            }
        }

        recorder.song = song
        recorder.text = null

        Log.i(TAG, "Song changed")
        playerListener.onSongChanged(recorder, song)
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

    override fun seekTo(position: Int) {
        check(!released.get()) { "Player is released" }

        val safe = max(0, position)
        recorder.lastPosition = safe

        Log.d(TAG, "SeekTo $safe")
        playerListener.onSeekTo(recorder, safe)
    }

    override fun sendText(text: String?) {
        check(!released.get()) { "Player is released" }

        recorder.song = null
        recorder.text = text

        Log.i(TAG, "Receive text")
        playerListener.onPostText(recorder, text)
    }

    override fun getPositionUpdateSharedMemory(): SharedMemory? {
        return positionSharedMemory
    }
}