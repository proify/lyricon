package io.github.proify.lyricon.lyric.provider.service

import android.os.DeadObjectException
import android.os.RemoteException
import android.util.Log
import io.github.proify.lyricon.lyric.bridge.provider.IRemotePlayer
import io.github.proify.lyricon.lyric.model.Song
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

internal class PlayerImpl : PlayerProxy {

    private val remotePlayerRef = AtomicReference<IRemotePlayer?>(null)

    private val remotePlayer: IRemotePlayer?
        get() = remotePlayerRef.get()

    override fun bindPlayer(player: IRemotePlayer?) {
        remotePlayerRef.set(player)
        Log.d(TAG, "Bind player: $player")
    }

    override fun setSong(song: Song?): Boolean =
        executeRemoteCall("setSong") { it.setSong(song) }

    override fun setPlaybackState(isPlaying: Boolean): Boolean =
        executeRemoteCall("setPlaybackState") { it.setPlaybackState(isPlaying) }

    override fun seekTo(positionMs: Int): Boolean =
        executeRemoteCall("seekTo") { it.seekTo(max(0, positionMs)) }

    override fun updatePosition(positionMs: Int): Boolean =
        executeRemoteCall("updatePosition") { it.updatePosition(max(0, positionMs)) }

    override fun sendText(text: String?): Boolean =
        executeRemoteCall("sendText") { it.sendText(text) }

    override val isActivated: Boolean
        get() = remotePlayer?.asBinder()?.isBinderAlive ?: false

    private inline fun executeRemoteCall(
        methodName: String,
        block: (IRemotePlayer) -> Unit
    ): Boolean {
        val player = remotePlayer ?: return false

        return try {
            block(player)
            true
        } catch (e: DeadObjectException) {
            Log.e(TAG, "[$methodName] Remote player died", e)
            remotePlayerRef.compareAndSet(player, null)
            false
        } catch (e: RemoteException) {
            Log.e(TAG, "[$methodName] Remote call failed", e)
            false
        }
    }

    companion object {
        private const val TAG = "PlayerImpl"
    }
}