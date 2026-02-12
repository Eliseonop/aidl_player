package com.example.demo_aidl

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tcontur.aidl.protocol.Command
import com.tcontur.aidl.protocol.Response
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.READ_MEDIA_AUDIO),
                0
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
        }

        // Iniciar servicio en foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, RemoteService::class.java))
        }

        MusicLibrary.init()

        setContent {
            MaterialTheme {
                MusicPlayerScreen()
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// DATA CLASSES
// ══════════════════════════════════════════════════════════

data class Song(val title: String, val path: String)

// ══════════════════════════════════════════════════════════
// CONTROL ACTIONS (Envía comandos al servicio local)
// ══════════════════════════════════════════════════════════

object ControlActions {
    private var serviceInstance: RemoteService? = null

    fun setService(service: RemoteService) {
        serviceInstance = service
    }

    fun executeCommand(command: String) {
        serviceInstance?.executeCommand(command)
    }

    fun sendPlayGenre(genre: String) {
        serviceInstance?.executeCommand(Command.PlayGenre(genre).toProtocol())
    }

    fun sendNext(genre: String) {
        serviceInstance?.executeCommand(Command.Next(genre).toProtocol())
    }

    fun sendPrevious(genre: String) {
        serviceInstance?.executeCommand(Command.Previous(genre).toProtocol())
    }

    fun sendPause() {
        serviceInstance?.executeCommand(Command.Pause.toProtocol())
    }

    fun sendResume() {
        serviceInstance?.executeCommand(Command.Resume.toProtocol())
    }

    fun sendStop() {
        serviceInstance?.executeCommand(Command.Stop.toProtocol())
    }
}

// ══════════════════════════════════════════════════════════
// MUSIC LIBRARY STATE
// ══════════════════════════════════════════════════════════

object MusicLibrary {
    private val _currentGenre = MutableStateFlow("Rock")
    val currentGenre: StateFlow<String> = _currentGenre

    private val _playingInfo = MutableStateFlow<PlayingInfo?>(null)
    val playingInfo: StateFlow<PlayingInfo?> = _playingInfo

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    data class PlayingInfo(
        val genre: String,
        val songName: String,
        val index: Int,
        val total: Int
    )

    fun setGenre(genre: String) {
        _currentGenre.value = genre
    }

    fun updatePlayingInfo(info: PlayingInfo?) {
        _playingInfo.value = info
        if (info != null) {
            _currentGenre.value = info.genre
            _isPaused.value = false
        }
    }

    fun updatePaused(paused: Boolean) {
        _isPaused.value = paused
    }

    fun updateStopped() {
        _playingInfo.value = null
        _isPaused.value = false
    }

    fun updateFromCommand(command: String) {
        // Parsear respuesta del servicio
        val response = Response.fromProtocol(command)
        when (response) {
            is Response.Playing -> {
                updatePlayingInfo(PlayingInfo(
                    genre = response.genre,
                    songName = response.songName,
                    index = response.index,
                    total = response.total
                ))
            }
            is Response.Paused -> {
                updatePaused(true)
            }
            is Response.Stopped -> {
                updateStopped()
            }
            else -> {}
        }
    }

    fun init() {
        setGenre("Rock")
    }
}

// ══════════════════════════════════════════════════════════
// MAIN SCREEN
// ══════════════════════════════════════════════════════════

@Composable
fun MusicPlayerScreen() {
    val currentGenre by MusicLibrary.currentGenre.collectAsStateWithLifecycle()
    val commandState by UiState.lastCommand.collectAsStateWithLifecycle()
    val playingInfo by MusicLibrary.playingInfo.collectAsStateWithLifecycle()
    val isPaused by MusicLibrary.isPaused.collectAsStateWithLifecycle()

    // Actualizar estado desde comandos
    LaunchedEffect(commandState) {
        MusicLibrary.updateFromCommand(commandState)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        // Main content area (genres navbar + songs list)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left navbar - Genres
            GenreNavBar(
                currentGenre = currentGenre,
                onGenreClick = { genre ->
                    MusicLibrary.setGenre(genre)
                }
            )

            // Right panel - Songs list
            SongsList(
                genre = currentGenre,
                playingInfo = playingInfo,
                isPaused = isPaused
            )
        }

        // Bottom controls
        BottomControls(
            playingInfo = playingInfo,
            isPaused = isPaused
        )
    }
}

// ══════════════════════════════════════════════════════════
// GENRE NAVBAR (LEFT SIDE)
// ══════════════════════════════════════════════════════════

@Composable
fun GenreNavBar(
    currentGenre: String,
    onGenreClick: (String) -> Unit
) {
    // Obtener géneros dinámicamente del sistema de archivos
    val genres = remember { MusicScanner.getAllGenres() }

    // Alternativa estática (comentada):
    // val genres = listOf("Rock", "Jazz", "Relax", "Salsa", "Cumbia", "Reggaeton")

    Column(
        modifier = Modifier
            .width(150.dp)
            .fillMaxHeight()
            .background(Color(0xFF121212))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Géneros",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(genres.size) { index ->
                GenreNavItem(
                    genre = genres[index],
                    isSelected = genres[index] == currentGenre,
                    onClick = { onGenreClick(genres[index]) }
                )
            }
        }
    }
}

@Composable
fun GenreNavItem(
    genre: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1DB954) else Color(0xFF282828)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                genre,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// SONGS LIST (RIGHT SIDE)
// ══════════════════════════════════════════════════════════

@Composable
fun SongsList(
    genre: String,
    playingInfo: MusicLibrary.PlayingInfo?,
    isPaused: Boolean
) {
    val songs = remember(genre) { MusicScanner.scanMusicFolder(genre) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Text(
            "🎵 $genre",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay canciones en $genre",
                    fontSize = 16.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(songs.size) { index ->
                    val isCurrentSong = playingInfo?.songName == songs[index].title && playingInfo.genre == genre
                    SongItem(
                        song = songs[index],
                        isPlaying = isCurrentSong && !isPaused,
                        isPaused = isCurrentSong && isPaused,
                        onPlayClick = {
                            // Si es la canción actual y está pausada, resumir
                            if (isCurrentSong && isPaused) {
                                ControlActions.sendResume()
                            }
                            // Si es la canción actual y está reproduciendo, pausar
                            else if (isCurrentSong && !isPaused) {
                                ControlActions.sendPause()
                            }
                            // Si es otra canción, reproducir desde el inicio
                            else {
                                ControlActions.sendPlayGenre(genre)
                                ControlActions.executeCommand(Command.PlayIndex(genre, index).toProtocol())
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean,
    isPaused: Boolean,
    onPlayClick: () -> Unit
) {
    // Determinar si esta canción está activa (reproduciendo o pausada)
    val isActive = isPlaying || isPaused

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1DB954).copy(alpha = 0.2f) else Color(0xFF282828)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                song.title,
                fontSize = 14.sp,
                color = if (isActive) Color(0xFF1DB954) else Color.White,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    // Si está reproduciendo (no pausado) → mostrar pausa
                    // Si está pausado o es otra canción → mostrar play
                    if (isPlaying) "⏸" else "▶",
                    fontSize = 20.sp,
                    color = Color(0xFF1DB954)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// BOTTOM CONTROLS
// ══════════════════════════════════════════════════════════

@Composable
fun BottomControls(
    playingInfo: MusicLibrary.PlayingInfo?,
    isPaused: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Now playing info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (playingInfo != null) {
                    Text(
                        playingInfo.songName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${playingInfo.genre} • ${playingInfo.index + 1}/${playingInfo.total}",
                        fontSize = 12.sp,
                        color = Color(0xFFB3B3B3)
                    )
                } else {
                    Text(
                        "Ninguna canción",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                IconButton(
                    onClick = {
                        if (playingInfo != null) {
                            ControlActions.sendPrevious(playingInfo.genre)
                        }
                    },
                    enabled = playingInfo != null
                ) {
                    Text(
                        "⏮",
                        fontSize = 28.sp,
                        color = if (playingInfo != null) Color.White else Color(0xFF666666)
                    )
                }

                // Play/Pause button
                IconButton(
                    onClick = {
                        if (playingInfo != null) {
                            if (isPaused) {
                                ControlActions.sendResume()
                            } else {
                                ControlActions.sendPause()
                            }
                        }
                    },
                    enabled = playingInfo != null
                ) {
                    Text(
                        if (isPaused) "▶" else "⏸",
                        fontSize = 32.sp,
                        color = if (playingInfo != null) Color(0xFF1DB954) else Color(0xFF666666)
                    )
                }

                // Next button
                IconButton(
                    onClick = {
                        if (playingInfo != null) {
                            ControlActions.sendNext(playingInfo.genre)
                        }
                    },
                    enabled = playingInfo != null
                ) {
                    Text(
                        "⏭",
                        fontSize = 28.sp,
                        color = if (playingInfo != null) Color.White else Color(0xFF666666)
                    )
                }
            }
        }
    }
}
