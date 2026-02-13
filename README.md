# 🎵 demo_aidl - Servicio de Música AIDL

Servicio AIDL de música multiplataforma para comunicación entre procesos (IPC) en Android.

**📌 Versión:** 0.1.0
**📅 Última actualización:** Febrero 2026
**🔧 Min SDK:** 24 (Android 7.0)

---

## 📚 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Arquitectura](#-arquitectura)
- [Diagramas de Flujo](#-diagramas-de-flujo)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Protocolo de Comunicación](#-protocolo-de-comunicación)
- [Integración](#-integración)
- [API Reference](#-api-reference)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Descripción General

**demo_aidl** es una aplicación de demostración que implementa un reproductor de música controlado vía AIDL (Android Interface Definition Language). Permite que **otras aplicaciones** controlen la reproducción de música de forma remota a través de IPC.

### ✨ Características Principales

- ✅ **Reproductor de Música**: Reproduce archivos MP3 organizados por géneros
- ✅ **Control Remoto**: Otras apps pueden controlar play/pause/next/previous
- ✅ **Comunicación Bidireccional**: El servicio notifica cambios de estado a todos los clientes
- ✅ **Auto-Discovery**: Los clientes encuentran el servicio automáticamente
- ✅ **Géneros Dinámicos**: Lee carpetas del sistema de archivos
- ✅ **Protocolo Tipado**: API type-safe con `Command` y `Response`
- ✅ **UI Moderna**: Interfaz con Jetpack Compose y Material3

### 🎼 Estructura de Archivos de Música

```
/storage/emulated/0/Download/music/
├── rock/
│   ├── song1.mp3
│   └── song2.mp3
├── jazz/
│   ├── song3.mp3
│   └── song4.mp3
├── salsa/
│   └── song5.mp3
└── cumbia/
    └── song6.mp3
```

**Reglas:**
- Cada subcarpeta de `/music/` representa un género
- Los nombres de carpetas se capitalizan automáticamente (rock → Rock)
- Solo se reconocen archivos `.mp3`

---

## 🏗️ Arquitectura

### Componentes del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                     demo_aidl (Servicio)                    │
├─────────────────────────────────────────────────────────────┤
│  MainActivity.kt          │  UI con Compose                 │
│  RemoteService.kt         │  Servicio AIDL + MediaPlayer    │
│  MusicScanner.kt          │  Escanea sistema de archivos    │
│  MusicLibrary.kt          │  Estado compartido (Flows)      │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ AIDL IPC
                 ↓
┌─────────────────────────────────────────────────────────────┐
│              Aplicaciones Cliente (Otras Apps)              │
├─────────────────────────────────────────────────────────────┤
│  AidlRepository.kt        │  Extiende AidlClientBase        │
│  MediaPlayerViewModel.kt  │  Lógica de negocio              │
│  MediaPlayerScreen.kt     │  UI del cliente                 │
└─────────────────────────────────────────────────────────────┘
```

### Patrón de Comunicación

```
┌──────────────┐                          ┌──────────────┐
│   Cliente    │◄────── Callbacks ────────│   Servicio   │
│  (App Ctrl)  │                          │ (demo_aidl)  │
│              │──────── Commands ───────►│              │
└──────────────┘                          └──────────────┘
   Observa:                                  Notifica:
   - genres                                  - GENRES
   - playingInfo                             - PLAYING
   - isPaused                                - PAUSED
   - connectionState                         - STOPPED
```

---

## 📊 Diagramas de Flujo

### 1. Flujo de Conexión Inicial

```
┌─────────────────────────────────────────────────────────────┐
│ FASE 1: CONEXIÓN                                            │
└─────────────────────────────────────────────────────────────┘

Cliente                          Servicio
  │                                 │
  │  1. connect()                   │
  ├────────────────────────────────►│
  │                                 │
  │  2. bindService()               │
  ├────────────────────────────────►│
  │                                 │
  │                                 │  3. onClientConnected()
  │                                 │     clientCount++
  │                                 │
  │                                 │  4. handleGetGenres()
  │                                 │     ↓ Escanea /music/
  │  ◄────────────────────────────┤
  │  5. GENRES|Rock|Jazz|Salsa     │
  │                                 │
  │                                 │  6. handleGetStatus()
  │  ◄────────────────────────────┤
  │  7. STATUS|STOPPED||||         │
  │                                 │
  │  ConnectionState = CONNECTED    │
  │                                 │

┌─────────────────────────────────────────────────────────────┐
│ RESULTADO: Cliente recibe géneros disponibles y estado      │
│ actual del reproductor automáticamente                      │
└─────────────────────────────────────────────────────────────┘
```

### 2. Flujo de Reproducción

```
┌─────────────────────────────────────────────────────────────┐
│ CASO: Usuario hace click en género "Rock"                   │
└─────────────────────────────────────────────────────────────┘

Cliente (UI)                 ViewModel                 Servicio
  │                             │                         │
  │  Click "Rock"               │                         │
  ├────────────────────────────►│                         │
  │                             │                         │
  │                             │  playGenre("Rock")      │
  │                             ├────────────────────────►│
  │                             │                         │
  │                             │                         │  handlePlayGenre()
  │                             │                         │  ├─ loadGenreIfNeeded()
  │                             │                         │  │  └─ scanMusicFolder("rock")
  │                             │                         │  │     └─ Encuentra 10 .mp3
  │                             │                         │  │
  │                             │                         │  └─ playSong(index=0)
  │                             │                         │     ├─ MediaPlayer.setDataSource()
  │                             │                         │     ├─ MediaPlayer.prepare()
  │                             │                         │     └─ MediaPlayer.start()
  │                             │                         │
  │                             │  ◄─────────────────────┤
  │                             │  PLAYING|Rock|Song1|0|10│
  │  ◄─────────────────────────┤                         │
  │  Actualiza UI:              │                         │
  │  - Género: Rock             │                         │
  │  - Canción: Song1           │                         │
  │  - Posición: 1/10           │                         │
  │  - Botón: ⏸ (pause)        │                         │
  │                             │                         │

┌─────────────────────────────────────────────────────────────┐
│ RESULTADO: Música se reproduce y UI se sincroniza           │
└─────────────────────────────────────────────────────────────┘
```

### 3. Flujo de Pausa/Resume

```
┌─────────────────────────────────────────────────────────────┐
│ CASO: Usuario pausa la canción actual                       │
└─────────────────────────────────────────────────────────────┘

Cliente                          Servicio
  │                                 │
  │  Click botón ⏸                 │
  ├────────────────────────────────►│  pause()
  │                                 │
  │                                 │  handlePause()
  │                                 │  ├─ MediaPlayer.pause()
  │                                 │  └─ isPlaying = false
  │                                 │
  │  ◄────────────────────────────┤
  │  PAUSED                         │
  │                                 │
  │  Actualiza UI:                  │
  │  - isPaused = true              │
  │  - Botón cambia a: ▶ (play)   │
  │                                 │
  │                                 │
  │  Click botón ▶                 │
  ├────────────────────────────────►│  resume()
  │                                 │
  │                                 │  handleResume()
  │                                 │  ├─ MediaPlayer.start()
  │                                 │  └─ isPlaying = true
  │                                 │
  │  ◄────────────────────────────┤
  │  PLAYING|Rock|Song1|0|10        │
  │                                 │
  │  Actualiza UI:                  │
  │  - isPaused = false             │
  │  - Botón cambia a: ⏸ (pause)  │
  │                                 │
```

### 4. Flujo de Navegación (Next/Previous)

```
┌─────────────────────────────────────────────────────────────┐
│ CASO: Usuario hace click en "Next"                          │
└─────────────────────────────────────────────────────────────┘

Cliente                          Servicio
  │                                 │
  │  Click "⏭ Next"                │
  ├────────────────────────────────►│  next("Rock")
  │                                 │
  │                                 │  handleNext()
  │                                 │  ├─ currentSongIndex++
  │                                 │  │  (0 → 1)
  │                                 │  │
  │                                 │  └─ playSong(currentSongs[1])
  │                                 │     ├─ MediaPlayer.reset()
  │                                 │     ├─ setDataSource(Song2.mp3)
  │                                 │     ├─ prepare()
  │                                 │     └─ start()
  │                                 │
  │  ◄────────────────────────────┤
  │  PLAYING|Rock|Song2|1|10        │
  │                                 │
  │  Actualiza UI:                  │
  │  - Canción: Song2               │
  │  - Posición: 2/10               │
  │                                 │
```

### 5. Flujo de Sincronización Bidireccional

```
┌─────────────────────────────────────────────────────────────┐
│ CASO: App Cliente envía comando → demo_aidl se actualiza    │
└─────────────────────────────────────────────────────────────┘

App Cliente                  demo_aidl               demo_aidl UI
     │                          │                         │
     │  PLAY|GENRE|Salsa        │                         │
     ├─────────────────────────►│                         │
     │                          │  onCommandReceived()    │
     │                          │  ├─ handlePlayGenre()   │
     │                          │  └─ playSong()          │
     │                          │                         │
     │                          │  notifyAll()            │
     │  ◄───────────────────────┤────────────────────────►│
     │  PLAYING|Salsa|Song|0|5  │  MusicLibrary.update()  │
     │                          │                         │
     │  UI actualiza ✅         │          UI actualiza ✅│
     │  - Género: Salsa         │          - Género: Salsa│
     │  - Song activa           │          - Song activa  │
     │                          │                         │

┌─────────────────────────────────────────────────────────────┐
│ RESULTADO: Ambas UIs se sincronizan automáticamente         │
└─────────────────────────────────────────────────────────────┘
```

### 6. Flujo de Desconexión

```
Cliente                          Servicio
  │                                 │
  │  disconnect()                   │
  ├────────────────────────────────►│
  │                                 │
  │  unbindService()                │
  ├────────────────────────────────►│
  │                                 │
  │                                 │  onClientDisconnected()
  │                                 │  ├─ clientCount--
  │                                 │  └─ updateNotification()
  │                                 │
  │  ConnectionState = DISCONNECTED │
  │  Limpia estado:                 │
  │  - playingInfo = null           │
  │  - genres = []                  │
  │  - isPaused = false             │
```

---

## 📦 Estructura del Proyecto

```
demo_aidl/
├── app/
│   ├── src/main/java/com/example/demo_aidl/
│   │   ├── MainActivity.kt              # UI con Compose (Navbar + Lista)
│   │   ├── RemoteService.kt             # Servicio AIDL (extiende AidlServiceBase)
│   │   ├── MusicScanner.kt              # Escanea carpetas de música
│   │   ├── UiState.kt                   # Estado global de UI
│   │   └── Song.kt                      # Data class
│   │
│   ├── src/main/AndroidManifest.xml     # Declara servicio exportado
│   └── build.gradle.kts                 # Dependencias de app
│
├── aidl-library/                         # Librería compartible ⭐
│   ├── src/main/aidl/com/tcontur/aidl/
│   │   ├── IRemoteService.aidl          # Interface AIDL del servicio
│   │   └── IRemoteCallback.aidl         # Interface AIDL de callbacks
│   │
│   ├── src/main/java/com/tcontur/aidl/
│   │   ├── AidlServiceBase.kt           # Clase base para servicios
│   │   ├── AidlClientBase.kt            # Clase base para clientes
│   │   ├── ConnectionState.kt           # Enum de estados
│   │   ├── AidlClientExtensions.kt      # Extension functions
│   │   │
│   │   └── protocol/
│   │       ├── Command.kt               # Comandos tipados (sealed class)
│   │       └── Response.kt              # Respuestas tipadas (sealed class)
│   │
│   ├── VERSION.md                       # Historial de versiones
│   └── build.gradle.kts                 # Config de librería
│
├── README.md                             # Este archivo
└── settings.gradle.kts                  # Includes de módulos
```

---

## 🔌 Protocolo de Comunicación

### Formato del Protocolo

```
ACCION|RECURSO|PARAMETRO1|PARAMETRO2|...
```

### Comandos Disponibles

| Comando | Formato | Ejemplo | Descripción |
|---------|---------|---------|-------------|
| **Ping** | `PING` | `PING` | Verifica conexión |
| **Play Song** | `PLAY\|SONG\|genre\|songName` | `PLAY\|SONG\|Rock\|Bohemian Rhapsody` | Reproduce canción específica |
| **Play Genre** | `PLAY\|GENRE\|genre` | `PLAY\|GENRE\|Rock` | Reproduce primera canción del género |
| **Play Index** | `PLAY\|INDEX\|genre\|index` | `PLAY\|INDEX\|Rock\|5` | Reproduce canción por índice (0-based) |
| **Pause** | `PAUSE\|PLAYBACK` | `PAUSE\|PLAYBACK` | Pausa reproducción actual |
| **Resume** | `RESUME\|PLAYBACK` | `RESUME\|PLAYBACK` | Reanuda reproducción pausada |
| **Stop** | `STOP\|PLAYBACK` | `STOP\|PLAYBACK` | Detiene reproducción y limpia estado |
| **Next** | `NEXT\|GENRE\|genre` | `NEXT\|GENRE\|Rock` | Siguiente canción del género |
| **Previous** | `PREV\|GENRE\|genre` | `PREV\|GENRE\|Rock` | Canción anterior del género |
| **Get Playlist** | `GET\|PLAYLIST\|genre` | `GET\|PLAYLIST\|Rock` | Obtiene lista de canciones del género |
| **Get Genres** | `GET\|GENRES` | `GET\|GENRES` | Obtiene lista de géneros disponibles |
| **Get Status** | `GET\|STATUS` | `GET\|STATUS` | Obtiene estado actual del reproductor (incluye duración/progreso) |
| **Get Current** | `GET\|CURRENT_SONG` | `GET\|CURRENT_SONG` | Obtiene canción actual (incluye duración/progreso) |
| **Get Progress** | `GET\|PROGRESS` | `GET\|PROGRESS` | Obtiene solo progreso de reproducción actual |

### Respuestas del Servicio

| Respuesta | Formato | Ejemplo |
|-----------|---------|---------|
| **Playing** | `PLAYING\|genre\|songName\|index\|total\|posMs\|durMs` | `PLAYING\|Rock\|Bohemian Rhapsody\|0\|10\|45000\|354000` |
| **Paused** | `PAUSED` | `PAUSED` |
| **Stopped** | `STOPPED` | `STOPPED` |
| **Progress** | `PROGRESS\|currentMs\|durationMs` | `PROGRESS\|45000\|354000` |
| **Playlist** | `PLAYLIST\|genre\|song1\|song2\|...` | `PLAYLIST\|Rock\|Song1\|Song2\|Song3` |
| **Genres** | `GENRES\|genre1\|genre2\|...` | `GENRES\|Rock\|Jazz\|Salsa\|Cumbia` |
| **Status** | `STATUS\|state\|genre\|song\|idx\|tot\|vol\|posMs\|durMs` | `STATUS\|PLAYING\|Rock\|Song1\|0\|10\|100\|45000\|354000` |
| **Error** | `ERROR\|mensaje` | `ERROR\|Canción no encontrada` |
| **Pong** | `PONG` | `PONG` |

### API Tipada (Recomendada)

```kotlin
// Comandos
Command.PlayGenre("Rock")          → "PLAY|GENRE|Rock"
Command.Pause                       → "PAUSE|PLAYBACK"
Command.Next("Rock")                → "NEXT|GENRE|Rock"
Command.GetGenres                   → "GET|GENRES"

// Respuestas
Response.Playing(...)               ← "PLAYING|Rock|Song|0|10|45000|354000"
Response.Paused                     ← "PAUSED"
Response.Genres(listOf("Rock"...))  ← "GENRES|Rock|Jazz|..."
Response.Progress(45000, 354000)    ← "PROGRESS|45000|354000"
```

### 🎵 Duración y Progreso de Reproducción

**Nuevas capacidades v0.1.0:**

Todas las respuestas relacionadas con reproducción ahora incluyen:
- **currentPositionMs**: Posición actual en milisegundos
- **durationMs**: Duración total de la canción en milisegundos

#### Respuestas que incluyen progreso:

```kotlin
// Al reproducir una canción
Response.Playing(
    genre = "Rock",
    songName = "Bohemian Rhapsody",
    index = 0,
    total = 10,
    currentPositionMs = 45000,    // 45 segundos
    durationMs = 354000           // 5:54 minutos
)

// Al solicitar el estado completo
Response.Status(
    state = "PLAYING",
    genre = "Rock",
    songName = "Bohemian Rhapsody",
    index = 0,
    total = 10,
    volume = 100,
    currentPositionMs = 45000,
    durationMs = 354000
)

// Solo progreso (actualización en tiempo real)
Response.Progress(
    currentMs = 45000,
    durationMs = 354000
)
```

#### Cómo obtener el progreso:

```kotlin
// Opción 1: Al conectarse (automático)
// El servicio envía STATUS completo con progreso

// Opción 2: Solicitar progreso explícitamente
repository.getProgress()  // Envía GET|PROGRESS

// Opción 3: Escuchar respuestas PLAYING
repository.onMessageReceived { message ->
    when (val response = Response.fromProtocol(message)) {
        is Response.Playing -> {
            val currentSec = response.currentPositionMs / 1000
            val durationSec = response.durationMs / 1000
            println("Progreso: $currentSec / $durationSec segundos")
        }
    }
}
```

#### Formatear tiempo para UI:

```kotlin
fun formatTime(milliseconds: Long): String {
    val minutes = (milliseconds / 1000) / 60
    val seconds = (milliseconds / 1000) % 60
    return "%d:%02d".format(minutes, seconds)
}

// Uso:
val currentTime = formatTime(response.currentPositionMs)  // "0:45"
val duration = formatTime(response.durationMs)            // "5:54"
```

---

## 🚀 Integración en Otra App - (APP TCONTUR SIRVE COMO CLIENTE)

### Paso 1: Copiar aidl-library

```
tu-proyecto/
├── app/
└── aidl-library/  ← Copiar esta carpeta desde demo_aidl
```

### Paso 2: Configurar settings.gradle.kts

```kotlin
rootProject.name = "MiApp"
include(":app")
include(":aidl-library")  // ← Agregar
```

### Paso 3: Agregar dependencia

**app/build.gradle.kts:**
```kotlin
dependencies {
    implementation(project(":aidl-library"))
    // ... otras dependencias
}
```

### Paso 4: Configurar AndroidManifest.xml

**app/src/main/AndroidManifest.xml:**
```xml
<manifest>
    <!-- Agregar queries para encontrar el servicio -->
    <queries>
        <intent>
            <action android:name="com.tcontur.aidl.REMOTE_SERVICE"/>
        </intent>
    </queries>

    <application>
        <!-- Tu app -->
    </application>
</manifest>
```

### Paso 5: Crear AidlRepository

```kotlin
package com.tuapp.data.repository

import android.content.Context
import android.util.Log
import com.tcontur.aidl.AidlClientBase
import com.tcontur.aidl.ConnectionState
import com.tcontur.aidl.protocol.Response
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AidlRepository @Inject constructor(
    @ApplicationContext context: Context
) : AidlClientBase(
    context = context,
    serviceAction = "com.tcontur.aidl.REMOTE_SERVICE"
) {

    private val _playingInfo = MutableStateFlow<PlayingInfo?>(null)
    val playingInfo: StateFlow<PlayingInfo?> = _playingInfo

    private val _genres = MutableStateFlow<List<String>>(emptyList())
    val genres: StateFlow<List<String>> = _genres

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    data class PlayingInfo(
        val genre: String,
        val songName: String,
        val index: Int,
        val total: Int,
        val currentPositionMs: Long = 0,
        val durationMs: Long = 0
    )

    override fun onMessageReceived(message: String) {
        Log.d("AidlRepository", "📩 $message")

        when (val response = Response.fromProtocol(message)) {
            is Response.Playing -> {
                _playingInfo.value = PlayingInfo(
                    genre = response.genre,
                    songName = response.songName,
                    index = response.index,
                    total = response.total,
                    currentPositionMs = response.currentPositionMs,
                    durationMs = response.durationMs
                )
                _isPaused.value = false
            }
            is Response.Paused -> {
                _isPaused.value = true
            }
            is Response.Stopped -> {
                _playingInfo.value = null
                _isPaused.value = false
            }
            is Response.Genres -> {
                _genres.value = response.genres
            }
            else -> Log.d("AidlRepository", "ℹ️ $message")
        }
    }

    override fun onConnectionChanged(state: ConnectionState) {
        super.onConnectionChanged(state)
        when (state) {
            ConnectionState.CONNECTED -> {
                Log.d("AidlRepository", "✅ Conectado")
            }
            ConnectionState.DISCONNECTED -> {
                _playingInfo.value = null
                _genres.value = emptyList()
            }
            else -> {}
        }
    }
}
```

### Paso 6: Usar en ViewModel

```kotlin
@HiltViewModel
class MediaPlayerViewModel @Inject constructor(
    private val aidlRepository: AidlRepository
) : ViewModel() {

    val connectionState = aidlRepository.connectionState
    val playingInfo = aidlRepository.playingInfo
    val genres = aidlRepository.genres
    val isPaused = aidlRepository.isPaused

    fun connect() = aidlRepository.connect()
    fun disconnect() = aidlRepository.disconnect()

    fun playGenre(genre: String) = aidlRepository.playGenre(genre)
    fun pause() = aidlRepository.pause()
    fun resume() = aidlRepository.resume()
    fun next(genre: String) = aidlRepository.next(genre)
    fun previous(genre: String) = aidlRepository.previous(genre)
}
```

### Paso 7: UI con Compose

```kotlin
@Composable
fun MediaPlayerScreen(viewModel: MediaPlayerViewModel) {
    val state by viewModel.connectionState.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val playingInfo by viewModel.playingInfo.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()

    Column {
        // Estado de conexión
        Text("Estado: ${state.name}")

        // Conectar
        Button(onClick = { viewModel.connect() }) {
            Text("Conectar a demo_aidl")
        }

        // Géneros
        genres.forEach { genre ->
            Button(onClick = { viewModel.playGenre(genre) }) {
                Text("🎵 $genre")
            }
        }

        // Canción actual
        playingInfo?.let {
            Column {
                Text("${it.genre} - ${it.songName} (${it.index + 1}/${it.total})")

                // Mostrar duración y progreso
                if (it.durationMs > 0) {
                    val currentMin = (it.currentPositionMs / 1000) / 60
                    val currentSec = (it.currentPositionMs / 1000) % 60
                    val durationMin = (it.durationMs / 1000) / 60
                    val durationSec = (it.durationMs / 1000) % 60

                    Text("⏱ %d:%02d / %d:%02d".format(currentMin, currentSec, durationMin, durationSec))

                    // Barra de progreso
                    val progress = it.currentPositionMs.toFloat() / it.durationMs.toFloat()
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Controles
        Row {
            Button(onClick = { playingInfo?.let { viewModel.previous(it.genre) } }) {
                Text("⏮")
            }
            Button(onClick = {
                if (isPaused) viewModel.resume()
                else viewModel.pause()
            }) {
                Text(if (isPaused) "▶" else "⏸")
            }
            Button(onClick = { playingInfo?.let { viewModel.next(it.genre) } }) {
                Text("⏭")
            }
        }
    }
}
```

---

## 📖 API Reference

### AidlServiceBase (Servidor)

**Métodos abstractos:**
```kotlin
abstract fun onCommandReceived(command: String)
```

**Métodos opcionales:**
```kotlin
override fun onClientConnected(clientCount: Int)
override fun onClientDisconnected(clientCount: Int)
```

**Métodos disponibles:**
```kotlin
notifyAll(message: String)           // Envía mensaje a todos los clientes
getConnectedClientsCount(): Int      // Cuenta de clientes conectados
```

### AidlClientBase (Cliente)

**Métodos abstractos:**
```kotlin
abstract fun onMessageReceived(message: String)
```

**Métodos opcionales:**
```kotlin
override fun onConnectionChanged(state: ConnectionState)
```

**Métodos disponibles:**
```kotlin
connect()                            // Conectar al servicio
disconnect()                         // Desconectar
sendCommand(command: String)         // Enviar comando raw
isConnected(): Boolean               // Estado de conexión

// Extension functions (más conveniente):
playGenre(genre: String)
playSong(genre: String, songName: String)
playIndex(genre: String, index: Int)
pause()
resume()
stop()
next(genre: String)
previous(genre: String)
getGenres()
getStatus()                          // Incluye duración y posición actual
getProgress()                        // Solo duración y posición
getPlaylist(genre: String)
```

**Flows observables:**
```kotlin
val connectionState: StateFlow<ConnectionState>
val messages: StateFlow<String>
```

### ConnectionState

```kotlin
enum class ConnectionState {
    DISCONNECTED,  // No conectado
    CONNECTING,    // Intentando conectar
    CONNECTED,     // Listo para enviar comandos
    ERROR          // Error de conexión
}
```

---

## 🐛 Troubleshooting

### Error: "Servicio no encontrado"

**Causa:** El servicio demo_aidl no está instalado o no se puede encontrar.

**Solución:**
1. Verifica que demo_aidl esté instalado: `adb shell pm list packages | grep demo_aidl`
2. Verifica que `<queries>` esté en el AndroidManifest del cliente
3. Verifica que el servicio tenga `android:exported="true"`

### Error: "bindService() returned false"

**Causa:** El sistema Android no puede vincular el servicio.

**Solución:**
1. Agrega `<queries>` en AndroidManifest del cliente
2. Verifica que el servicio esté declarado con `android:exported="true"`
3. Reinstala ambas apps

### Error: "Unresolved reference IRemoteService"

**Causa:** Los archivos AIDL no se generaron.

**Solución:**
```kotlin
// En aidl-library/build.gradle.kts
android {
    buildFeatures {
        aidl = true  // ← Asegúrate de tener esto
    }
}
```
Luego: Sync Gradle → Rebuild Project

### No recibo callbacks del servicio

**Causa:** No estás conectado o el estado no es CONNECTED.

**Solución:**
```kotlin
viewModelScope.launch {
    repository.connectionState.collect { state ->
        if (state == ConnectionState.CONNECTED) {
            // Ahora puedes enviar comandos
            repository.playGenre("Rock")
        }
    }
}
```

### Los géneros no aparecen

**Causa:** No hay carpetas en `/Downloads/music/` o no hay música.

**Solución:**
1. Crear carpetas: `adb shell mkdir -p /storage/emulated/0/Download/music/rock`
2. Subir música: `adb push song.mp3 /storage/emulated/0/Download/music/rock/`
3. Reiniciar demo_aidl

---

## 📁 Archivos AIDL

### IRemoteService.aidl
```java
package com.tcontur.aidl;
import com.tcontur.aidl.IRemoteCallback;

interface IRemoteService {
    void sendCommand(String command);
    void registerCallback(IRemoteCallback callback);
    void unregisterCallback(IRemoteCallback callback);
}
```

### IRemoteCallback.aidl
```java
package com.tcontur.aidl;

interface IRemoteCallback {
    void onMessage(String message);
}
```

---

## 🔢 Versión de la Librería

**Verificar versión en runtime:**
```kotlin
import com.tcontur.aidl.BuildConfig

val version = BuildConfig.LIBRARY_VERSION      // "2.0.0"
val versionCode = BuildConfig.VERSION_CODE     // 200

Log.d("AIDL", "Usando aidl-library v$version")
```

**Ver historial completo:** Consulta [VERSION.md](aidl-library/VERSION.md)

---

## 📦 Exportar como AAR

```bash
cd demo_aidl
./gradlew :aidl-library:assembleRelease
```

El archivo estará en: `aidl-library/build/outputs/aar/aidl-library-release.aar`

**Usar en otro proyecto:**
```kotlin
// Copiar el .aar a tu-proyecto/app/libs/
dependencies {
    implementation(files("libs/aidl-library-release.aar"))
}
```

---

## ✅ Checklist de Integración

- [ ] ✅ Copiar `aidl-library` al proyecto
- [ ] ✅ Actualizar `settings.gradle.kts`
- [ ] ✅ Agregar `implementation(project(":aidl-library"))` en app
- [ ] ✅ Agregar `<queries>` en AndroidManifest
- [ ] ✅ Crear clase que extienda `AidlClientBase`
- [ ] ✅ Implementar `onMessageReceived()`
- [ ] ✅ Llamar `connect()` antes de enviar comandos
- [ ] ✅ Observar `connectionState` para verificar conexión
- [ ] ✅ Probar con demo_aidl instalado

---

## 📄 Licencia

Este proyecto es una demostración para integración AIDL.

---

**Desarrollado con ❤️ usando Kotlin, Jetpack Compose y AIDL**
**Versión:** 0.1.0 | **Última actualización:** Febrero 2026
