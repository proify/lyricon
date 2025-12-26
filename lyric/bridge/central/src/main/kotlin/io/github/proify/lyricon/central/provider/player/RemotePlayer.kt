package io.github.proify.lyricon.central.provider.player

import android.os.Handler
import android.os.Looper
import android.os.SharedMemory
import android.system.OsConstants
import android.util.Log
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.IRemotePlayer
import io.github.proify.lyricon.provider.ProviderInfo
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class RemotePlayer(
    info: ProviderInfo,
    private val playerListener: PlayerListener
) : IRemotePlayer.Stub() {

    companion object {
        private const val TAG = "RemotePlayer"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val recorder = PlayerRecorder(info)

    private var positionSharedMemory: SharedMemory? = null
    private var positionReadBuffer: ByteBuffer? = null

    @Volatile
    private var isPositionTaskRunning = false

    @Volatile
    private var positionUpdateInterval = 50L

    private var positionUpdateTask: ScheduledFuture<*>? = null

    private val executor = Executors.newSingleThreadScheduledExecutor(object : ThreadFactory {
        override fun newThread(r: Runnable): Thread {
            return Thread(r, "PositionUpdater").apply {
                isDaemon = true
                uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { t, e ->
                    Log.e(TAG, "PositionUpdater uncaught", e)
                }
            }
        }
    });

    init {
        initSharedMemory()
    }

    private fun initSharedMemory() {
        try {
            positionSharedMemory =
                SharedMemory.create(
                    "music_progress_${android.os.Process.myPid()}",
                    16
                ).apply {
                    setProtect(OsConstants.PROT_READ or OsConstants.PROT_WRITE)
                }
            positionReadBuffer = positionSharedMemory?.mapReadOnly()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize position shared region", e)
        }
    }

    fun release() {
        stopPositionUpdateTask()
        positionReadBuffer?.let {
            SharedMemory.unmap(it)
        }
        positionSharedMemory?.close()
        positionReadBuffer = null
        positionSharedMemory = null

        executor.shutdown()
    }

    private fun readCurrentPositionFromMemory(): Int {
        val buffer = positionReadBuffer ?: return 0
        return try {
            buffer.getInt(0).coerceAtLeast(0)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read position from shared memory", e)
            0
        }
    }

    private fun startPositionUpdateTask() {
        if (isPositionTaskRunning) return
        isPositionTaskRunning = true

        positionUpdateTask = executor.scheduleAtFixedRate(object : Runnable {
            override fun run() {
                val position = readCurrentPositionFromMemory()
                updatePositionInternal(position)
            }

        }, 0, positionUpdateInterval, TimeUnit.MILLISECONDS)
    }

    override fun setPositionUpdateInterval(interval: Int) {
        positionUpdateInterval = interval.coerceAtLeast(16).toLong()
        if (isPositionTaskRunning) {
            stopPositionUpdateTask()
            startPositionUpdateTask()
        }
    }

    private fun stopPositionUpdateTask() {
        isPositionTaskRunning = false
        positionUpdateTask?.cancel(false)
    }

    override fun setSong(song: Song?) {
        recorder.song = song
        recorder.text = null
        playerListener.onSongChanged(recorder, song)
    }

    override fun setPlaybackState(isPlaying: Boolean) {
        recorder.isPlaying = isPlaying
        playerListener.onPlaybackStateChanged(recorder, isPlaying)

        if (isPlaying) {
            startPositionUpdateTask()
        } else {
            stopPositionUpdateTask()
        }
    }

    override fun seekTo(position: Int) {
        val safePosition = position.coerceAtLeast(0)
        recorder.lastPosition = safePosition
        playerListener.onSeekTo(recorder, safePosition)
    }

    private fun updatePositionInternal(position: Int) {
        if (position == recorder.lastPosition) {
            return
        }
        val safePosition = position.coerceAtLeast(0)
        recorder.lastPosition = safePosition
        playerListener.onPositionChanged(recorder, safePosition)
    }

    override fun sendText(text: String?) {
        recorder.song = null
        recorder.text = text
        playerListener.onPostText(recorder, text)
    }

    override fun getPositionUpdateSharedMemory(): SharedMemory? {
        return positionSharedMemory
    }
}