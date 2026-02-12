package com.tcontur.aidl

import com.tcontur.aidl.protocol.Command

/**
 * Extensiones para enviar comandos tipados
 */
fun AidlClientBase.send(command: Command) {
    sendCommand(command.toProtocol())
}

// Shortcuts
fun AidlClientBase.playSong(genre: String, songName: String) =
    send(Command.PlaySong(genre, songName))

fun AidlClientBase.playGenre(genre: String) =
    send(Command.PlayGenre(genre))

fun AidlClientBase.playIndex(genre: String, index: Int) =
    send(Command.PlayIndex(genre, index))

fun AidlClientBase.pause() =
    send(Command.Pause)

fun AidlClientBase.resume() =
    send(Command.Resume)

fun AidlClientBase.stop() =
    send(Command.Stop)

fun AidlClientBase.next(genre: String) =
    send(Command.Next(genre))

fun AidlClientBase.previous(genre: String) =
    send(Command.Previous(genre))

fun AidlClientBase.getPlaylist(genre: String) =
    send(Command.GetPlaylist(genre))

fun AidlClientBase.getCurrentSong() =
    send(Command.GetCurrentSong)

fun AidlClientBase.getStatus() =
    send(Command.GetStatus)

fun AidlClientBase.getGenres() =
    send(Command.GetGenres)

fun AidlClientBase.ping() =
    send(Command.Ping)
