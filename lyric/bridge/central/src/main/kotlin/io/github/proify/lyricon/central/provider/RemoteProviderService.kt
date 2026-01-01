/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.proify.lyricon.central.provider

import io.github.proify.lyricon.central.provider.player.ProviderActivePlayerDispatcher
import io.github.proify.lyricon.central.provider.player.RemotePlayer
import io.github.proify.lyricon.provider.IRemotePlayer
import io.github.proify.lyricon.provider.IRemoteService

class RemoteProviderService(private val provider: RemoteProvider) : IRemoteService.Stub() {

    private val remotePlayer: IRemotePlayer =
        RemotePlayer(provider.providerInfo, ProviderActivePlayerDispatcher)

    override fun getPlayer(): IRemotePlayer = remotePlayer

    fun release() {
        if (remotePlayer is RemotePlayer) {
            remotePlayer.release()
        }
    }

    override fun disconnect() {
        provider.destroy()
    }

}