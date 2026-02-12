package com.tcontur.aidl.protocol

/**
 * Respuestas tipadas del servidor AIDL
 *
 * Formato del string: TIPO|DATO1|DATO2|...
 */
sealed class Response {
    abstract fun toProtocol(): String

    // ═══════════════════════════════════════════════════════════
    // PLAYBACK STATUS
    // ═══════════════════════════════════════════════════════════

    data class Playing(
        val genre: String,
        val songName: String,
        val index: Int,
        val total: Int,
        val currentPositionMs: Long = 0,
        val durationMs: Long = 0
    ) : Response() {
        override fun toProtocol() = "PLAYING|$genre|$songName|$index|$total|$currentPositionMs|$durationMs"
    }

    object Paused : Response() {
        override fun toProtocol() = "PAUSED"
    }

    object Stopped : Response() {
        override fun toProtocol() = "STOPPED"
    }

    data class Progress(val currentMs: Long, val durationMs: Long) : Response() {
        override fun toProtocol() = "PROGRESS|$currentMs|$durationMs"
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYLIST
    // ═══════════════════════════════════════════════════════════

    data class Playlist(val genre: String, val songs: List<String>) : Response() {
        override fun toProtocol() = "PLAYLIST|$genre|${songs.joinToString("|")}"
    }

    data class Genres(val genres: List<String>) : Response() {
        override fun toProtocol() = "GENRES|${genres.joinToString("|")}"
    }

    // ═══════════════════════════════════════════════════════════
    // VOLUME
    // ═══════════════════════════════════════════════════════════

    data class Volume(val level: Int) : Response() {
        override fun toProtocol() = "VOLUME|$level"
    }

    // ═══════════════════════════════════════════════════════════
    // STATUS
    // ═══════════════════════════════════════════════════════════

    data class Status(
        val state: String, // PLAYING, PAUSED, STOPPED
        val genre: String?,
        val songName: String?,
        val index: Int?,
        val total: Int?,
        val volume: Int,
        val currentPositionMs: Long = 0,
        val durationMs: Long = 0
    ) : Response() {
        override fun toProtocol() = buildString {
            append("STATUS|$state|${genre ?: ""}|${songName ?: ""}|${index ?: 0}|${total ?: 0}|$volume|$currentPositionMs|$durationMs")
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ERRORS
    // ═══════════════════════════════════════════════════════════

    data class Error(val message: String) : Response() {
        override fun toProtocol() = "ERROR|$message"
    }

    data class NoSongs(val genre: String) : Response() {
        override fun toProtocol() = "ERROR|No hay canciones en $genre"
    }

    // ═══════════════════════════════════════════════════════════
    // HEALTH
    // ═══════════════════════════════════════════════════════════

    object Pong : Response() {
        override fun toProtocol() = "PONG"
    }

    data class Connection(val connected: Boolean) : Response() {
        override fun toProtocol() = "CONNECTION|${if (connected) "conectado" else "desconectado"}"
    }

    companion object {
        /**
         * Parser para convertir string de protocolo a Response
         */
        fun fromProtocol(protocol: String): Response? {
            val parts = protocol.split("|")
            if (parts.isEmpty()) return null

            return when (parts[0]) {
                "PONG" -> Pong

                "PLAYING" -> Playing(
                    genre = parts.getOrNull(1) ?: return null,
                    songName = parts.getOrNull(2) ?: return null,
                    index = parts.getOrNull(3)?.toIntOrNull() ?: return null,
                    total = parts.getOrNull(4)?.toIntOrNull() ?: return null,
                    currentPositionMs = parts.getOrNull(5)?.toLongOrNull() ?: 0,
                    durationMs = parts.getOrNull(6)?.toLongOrNull() ?: 0
                )

                "PAUSED" -> Paused
                "STOPPED" -> Stopped

                "PROGRESS" -> Progress(
                    currentMs = parts.getOrNull(1)?.toLongOrNull() ?: return null,
                    durationMs = parts.getOrNull(2)?.toLongOrNull() ?: return null
                )

                "PLAYLIST" -> Playlist(
                    genre = parts.getOrNull(1) ?: return null,
                    songs = parts.drop(2)
                )

                "GENRES" -> Genres(parts.drop(1))

                "VOLUME" -> Volume(parts.getOrNull(1)?.toIntOrNull() ?: return null)

                "STATUS" -> Status(
                    state = parts.getOrNull(1) ?: return null,
                    genre = parts.getOrNull(2)?.takeIf { it.isNotEmpty() },
                    songName = parts.getOrNull(3)?.takeIf { it.isNotEmpty() },
                    index = parts.getOrNull(4)?.toIntOrNull(),
                    total = parts.getOrNull(5)?.toIntOrNull(),
                    volume = parts.getOrNull(6)?.toIntOrNull() ?: 100,
                    currentPositionMs = parts.getOrNull(7)?.toLongOrNull() ?: 0,
                    durationMs = parts.getOrNull(8)?.toLongOrNull() ?: 0
                )

                "ERROR" -> Error(parts.drop(1).joinToString("|"))

                "CONNECTION" -> Connection(parts.getOrNull(1) == "conectado")

                else -> null
            }
        }
    }
}
