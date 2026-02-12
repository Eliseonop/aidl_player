package com.tcontur.aidl.protocol

/**
 * Comandos tipados para comunicación AIDL
 *
 * Formato del string: ACCION|RECURSO|PARAM1|PARAM2|...
 */
sealed class Command {
    abstract fun toProtocol(): String

    data class PlaySong(val genre: String, val songName: String) : Command() {
        override fun toProtocol() = "PLAY|SONG|$genre|$songName"
    }

    data class PlayGenre(val genre: String) : Command() {
        override fun toProtocol() = "PLAY|GENRE|$genre"
    }

    data class PlayIndex(val genre: String, val index: Int) : Command() {
        override fun toProtocol() = "PLAY|INDEX|$genre|$index"
    }

    object Pause : Command() {
        override fun toProtocol() = "PAUSE|PLAYBACK"
    }

    object Resume : Command() {
        override fun toProtocol() = "RESUME|PLAYBACK"
    }

    object Stop : Command() {
        override fun toProtocol() = "STOP|PLAYBACK"
    }

    data class Next(val genre: String) : Command() {
        override fun toProtocol() = "NEXT|GENRE|$genre"
    }

    data class Previous(val genre: String) : Command() {
        override fun toProtocol() = "PREV|GENRE|$genre"
    }

    data class SetVolume(val level: Int) : Command() {
        override fun toProtocol() = "VOLUME|SET|$level"
    }

    object VolumeUp : Command() {
        override fun toProtocol() = "VOLUME|UP"
    }

    object VolumeDown : Command() {
        override fun toProtocol() = "VOLUME|DOWN"
    }


    data class SeekTo(val positionMs: Long) : Command() {
        override fun toProtocol() = "SEEK|POSITION|$positionMs"
    }


    data class GetPlaylist(val genre: String) : Command() {
        override fun toProtocol() = "GET|PLAYLIST|$genre"
    }

    object GetCurrentSong : Command() {
        override fun toProtocol() = "GET|CURRENT_SONG"
    }

    object GetStatus : Command() {
        override fun toProtocol() = "GET|STATUS"
    }

    object GetGenres : Command() {
        override fun toProtocol() = "GET|GENRES"
    }

    object GetProgress : Command() {
        override fun toProtocol() = "GET|PROGRESS"
    }

    // ═══════════════════════════════════════════════════════════
    // HEALTH
    // ═══════════════════════════════════════════════════════════

    object Ping : Command() {
        override fun toProtocol() = "PING"
    }

    companion object {
        /**
         * Parser para convertir string de protocolo a Command
         */
        fun fromProtocol(protocol: String): Command? {
            val parts = protocol.split("|")
            if (parts.isEmpty()) return null

            return when (parts[0]) {
                "PING" -> Ping

                "PLAY" -> when (parts.getOrNull(1)) {
                    "SONG" -> PlaySong(
                        genre = parts.getOrNull(2) ?: return null,
                        songName = parts.getOrNull(3) ?: return null
                    )
                    "GENRE" -> PlayGenre(parts.getOrNull(2) ?: return null)
                    "INDEX" -> PlayIndex(
                        genre = parts.getOrNull(2) ?: return null,
                        index = parts.getOrNull(3)?.toIntOrNull() ?: return null
                    )
                    else -> null
                }

                "PAUSE" -> Pause
                "RESUME" -> Resume
                "STOP" -> Stop

                "NEXT" -> Next(parts.getOrNull(2) ?: return null)
                "PREV" -> Previous(parts.getOrNull(2) ?: return null)

                "VOLUME" -> when (parts.getOrNull(1)) {
                    "SET" -> SetVolume(parts.getOrNull(2)?.toIntOrNull() ?: return null)
                    "UP" -> VolumeUp
                    "DOWN" -> VolumeDown
                    else -> null
                }

                "SEEK" -> SeekTo(parts.getOrNull(2)?.toLongOrNull() ?: return null)

                "GET" -> when (parts.getOrNull(1)) {
                    "PLAYLIST" -> GetPlaylist(parts.getOrNull(2) ?: return null)
                    "CURRENT_SONG" -> GetCurrentSong
                    "STATUS" -> GetStatus
                    "GENRES" -> GetGenres
                    "PROGRESS" -> GetProgress
                    else -> null
                }

                else -> null
            }
        }
    }
}
