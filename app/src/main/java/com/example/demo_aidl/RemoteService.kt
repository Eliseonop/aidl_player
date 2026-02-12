package com.example.demo_aidl

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tcontur.aidl.AidlServiceBase
import com.tcontur.aidl.protocol.Command
import com.tcontur.aidl.protocol.Response

class RemoteService : AidlServiceBase() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentTitle = "Esperando..."
    private var currentGenre = ""
    private var currentSongIndex = 0
    private var currentSongs: List<Song> = emptyList()

    companion object {
        const val CHANNEL_ID = "media_channel"
        const val NOTIFICATION_ID = 100
        // Acciones internas de notificación (no confundir con comandos AIDL)
        private const val ACTION_NOTIFICATION_PLAY = "com.example.demo_aidl.NOTIFICATION_PLAY"
        private const val ACTION_NOTIFICATION_STOP = "com.example.demo_aidl.NOTIFICATION_STOP"
    }

    override fun onCommandReceived(command: String) {
        Log.d("RemoteService", "📥 Comando: $command")
        UiState.updateCommand(command)

        // Parsear comando usando protocolo
        val cmd = Command.fromProtocol(command)
        if (cmd == null) {
            Log.w("RemoteService", "⚠️ Comando desconocido: $command")
            notifyAll(Response.Error("Comando desconocido").toProtocol())
            return
        }

        when (cmd) {
            is Command.PlaySong -> handlePlaySong(cmd.genre, cmd.songName)
            is Command.PlayGenre -> handlePlayGenre(cmd.genre)
            is Command.PlayIndex -> handlePlayIndex(cmd.genre, cmd.index)
            is Command.Pause -> handlePause()
            is Command.Resume -> handleResume()
            is Command.Stop -> handleStop()
            is Command.Next -> handleNext(cmd.genre)
            is Command.Previous -> handlePrevious(cmd.genre)
            is Command.GetPlaylist -> handleGetPlaylist(cmd.genre)
            is Command.GetGenres -> handleGetGenres()
            is Command.GetStatus -> handleGetStatus()
            is Command.GetCurrentSong -> handleGetCurrentSong()
            is Command.Ping -> notifyAll(Response.Pong.toProtocol())
            else -> {
                Log.w("RemoteService", "⚠️ Comando no implementado: $cmd")
                notifyAll(Response.Error("Comando no implementado").toProtocol())
            }
        }
    }

    override fun onClientConnected(clientCount: Int) {
        super.onClientConnected(clientCount)
        UiState.updateCommand("Clientes conectados: $clientCount")
        updateNotification()

        // Enviar información inicial automáticamente cuando un cliente se conecta
        handleGetGenres()  // Lista de géneros disponibles
        handleGetStatus()  // Estado actual del reproductor
    }

    override fun onClientDisconnected(clientCount: Int) {
        super.onClientDisconnected(clientCount)
        UiState.updateCommand("Clientes conectados: $clientCount")
        updateNotification()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        showInitialNotification()
        initMediaPlayer()
        ControlActions.setService(this)
    }

    // Método público para que MainActivity pueda enviar comandos
    fun executeCommand(command: String) {
        onCommandReceived(command)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_NOTIFICATION_PLAY -> if (isPlaying) handlePause() else handleResume()
            ACTION_NOTIFICATION_STOP -> handleStop()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproductor Media",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Control de reproducción"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showInitialNotification() {
        updateNotification()
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                Log.d("RemoteService", "Canción completada, reproduciendo siguiente")
                playNextSong()
            }
            setOnErrorListener { _, what, extra ->
                Log.e("RemoteService", "Error MediaPlayer: what=$what, extra=$extra")
                notifyAll("Error reproduciendo audio")
                false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // PLAYBACK HANDLERS
    // ══════════════════════════════════════════════════════════

    private fun handlePlaySong(genre: String, songName: String) {
        loadGenreIfNeeded(genre)

        if (currentSongs.isEmpty()) {
            notifyAll(Response.NoSongs(genre).toProtocol())
            return
        }

        // Buscar por nombre
        val index = currentSongs.indexOfFirst { it.title.contains(songName, ignoreCase = true) }
        if (index == -1) {
            notifyAll(Response.Error("Canción '$songName' no encontrada en $genre").toProtocol())
            return
        }

        currentSongIndex = index
        playSong(currentSongs[index])
    }

    private fun handlePlayGenre(genre: String) {
        loadGenreIfNeeded(genre)

        if (currentSongs.isEmpty()) {
            notifyAll(Response.NoSongs(genre).toProtocol())
            return
        }

        currentSongIndex = 0
        playSong(currentSongs[0])
        MusicLibrary.setGenre(genre)
    }

    private fun handlePlayIndex(genre: String, index: Int) {
        loadGenreIfNeeded(genre)

        if (currentSongs.isEmpty()) {
            notifyAll(Response.NoSongs(genre).toProtocol())
            return
        }

        if (index !in currentSongs.indices) {
            notifyAll(Response.Error("Índice $index fuera de rango").toProtocol())
            return
        }

        currentSongIndex = index
        playSong(currentSongs[index])
    }

    private fun handleNext(genre: String) {
        loadGenreIfNeeded(genre)

        if (currentSongs.isEmpty()) {
            notifyAll(Response.NoSongs(genre).toProtocol())
            return
        }

        currentSongIndex = (currentSongIndex + 1) % currentSongs.size
        playSong(currentSongs[currentSongIndex])
    }

    private fun handlePrevious(genre: String) {
        loadGenreIfNeeded(genre)

        if (currentSongs.isEmpty()) {
            notifyAll(Response.NoSongs(genre).toProtocol())
            return
        }

        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else currentSongs.size - 1
        playSong(currentSongs[currentSongIndex])
    }

    private fun loadGenreIfNeeded(genre: String) {
        if (currentSongs.isEmpty() || genre != currentGenre) {
            currentGenre = genre
            currentSongs = MusicScanner.scanMusicFolder(genre)
        }
    }

    // ══════════════════════════════════════════════════════════
    // QUERY HANDLERS
    // ══════════════════════════════════════════════════════════

    private fun handleGetPlaylist(genre: String) {
        val songs = MusicScanner.scanMusicFolder(genre)
        val response = Response.Playlist(genre, songs.map { it.title })
        notifyAll(response.toProtocol())
    }

    private fun handleGetGenres() {
        // Obtener géneros dinámicamente escaneando carpetas
        val genres = MusicScanner.getAllGenres()
        val response = Response.Genres(genres)
        val protocol = response.toProtocol()
        notifyAll(protocol)

        Log.d("RemoteService", "📂 Géneros enviados: $genres")
    }

    private fun handleGetCurrentSong() {
        if (currentSongs.isEmpty() || currentSongIndex >= currentSongs.size) {
            notifyAll(Response.Error("No hay canción actual").toProtocol())
            return
        }

        val song = currentSongs[currentSongIndex]
        val response = Response.Playing(
            genre = currentGenre,
            songName = song.title,
            index = currentSongIndex,
            total = currentSongs.size
        )
        notifyAll(response.toProtocol())
    }

    private fun handleGetStatus() {
        val state = when {
            isPlaying -> "PLAYING"
            currentTitle != "Esperando..." && currentTitle != "Detenido" -> "PAUSED"
            else -> "STOPPED"
        }

        val response = Response.Status(
            state = state,
            genre = if (currentGenre.isNotEmpty()) currentGenre else null,
            songName = if (currentSongs.isNotEmpty()) currentSongs.getOrNull(currentSongIndex)?.title else null,
            index = if (currentSongs.isNotEmpty()) currentSongIndex else null,
            total = if (currentSongs.isNotEmpty()) currentSongs.size else null,
            volume = 100
        )
        notifyAll(response.toProtocol())
    }

    private fun playNextSong() {
        if (currentSongs.isEmpty()) return

        currentSongIndex = (currentSongIndex + 1) % currentSongs.size
        playSong(currentSongs[currentSongIndex])
    }

    private fun playSong(song: Song) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(song.path)
            mediaPlayer?.prepare()
            mediaPlayer?.start()

            isPlaying = true
            currentTitle = song.title

            val response = Response.Playing(
                genre = currentGenre,
                songName = song.title,
                index = currentSongIndex,
                total = currentSongs.size
            )
            val protocol = response.toProtocol()
            notifyAll(protocol)

            // Actualizar UI local
            MusicLibrary.updateFromCommand(protocol)
            UiState.updateCommand(protocol)

            updateNotification()

            Log.d("RemoteService", "🎵 Reproduciendo: ${song.title}")
        } catch (e: Exception) {
            Log.e("RemoteService", "❌ Error reproduciendo: ${e.message}", e)
            notifyAll(Response.Error("Error reproduciendo: ${e.message}").toProtocol())
            isPlaying = false
            updateNotification()
        }
    }

    private fun handlePause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false
                val protocol = Response.Paused.toProtocol()
                notifyAll(protocol)

                // Actualizar UI local
                MusicLibrary.updateFromCommand(protocol)
                UiState.updateCommand(protocol)

                updateNotification()
                Log.d("RemoteService", "⏸️ Pausado")
            }
        } catch (e: Exception) {
            Log.e("RemoteService", "❌ Error pausando: ${e.message}", e)
            notifyAll(Response.Error("Error pausando").toProtocol())
        }
    }

    private fun handleResume() {
        try {
            if (currentSongs.isEmpty()) {
                notifyAll(Response.Error("No hay canción para reproducir").toProtocol())
                return
            }

            mediaPlayer?.start()
            isPlaying = true

            val response = Response.Playing(
                genre = currentGenre,
                songName = currentTitle,
                index = currentSongIndex,
                total = currentSongs.size
            )
            val protocol = response.toProtocol()
            notifyAll(protocol)

            // Actualizar UI local
            MusicLibrary.updateFromCommand(protocol)
            UiState.updateCommand(protocol)

            updateNotification()
            Log.d("RemoteService", "▶️ Reanudado")
        } catch (e: Exception) {
            Log.e("RemoteService", "❌ Error reanudando: ${e.message}", e)
            notifyAll(Response.Error("Error reanudando").toProtocol())
        }
    }

    private fun handleStop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()

            isPlaying = false
            currentTitle = "Detenido"
            currentGenre = ""
            currentSongIndex = 0

            val protocol = Response.Stopped.toProtocol()
            notifyAll(protocol)

            // Actualizar UI local
            MusicLibrary.updateFromCommand(protocol)
            UiState.updateCommand(protocol)

            currentSongs = emptyList()
            updateNotification()
            Log.d("RemoteService", "⏹️ Detenido")
        } catch (e: Exception) {
            Log.e("RemoteService", "❌ Error deteniendo: ${e.message}", e)
            notifyAll(Response.Error("Error deteniendo").toProtocol())
        }
    }

    private fun updateNotification() {
        val playPauseIntent = Intent(this, RemoteService::class.java).apply {
            action = ACTION_NOTIFICATION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, RemoteService::class.java).apply {
            action = ACTION_NOTIFICATION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (currentTitle == "Detenido" || currentTitle == "Esperando...") {
            currentTitle
        } else {
            currentTitle
        }

        val subtitle = buildString {
            if (currentGenre.isNotEmpty()) append("$currentGenre • ")
            append("Clientes: ${getConnectedClientsCount()}")
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1))
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pausar" else "Reproducir",
                playPausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Detener",
                stopPendingIntent
            )
            .build()

        startForeground(NOTIFICATION_ID, notification)
        Log.d("RemoteService", "Notificación: $title | $subtitle")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("RemoteService", "Error liberando MediaPlayer", e)
        }
    }
}