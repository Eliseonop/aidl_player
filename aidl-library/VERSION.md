# 📋 AIDL Library - Control de Versiones

## Versión Actual: 0.1.0

### ✨ Características de Esta Versión

- ✅ Protocolo estructurado tipo-seguro
- ✅ Géneros dinámicos desde sistema de archivos
- ✅ Comunicación bidireccional completa
- ✅ Auto-discovery de servicios
- ✅ Sincronización automática de estado
- ✅ Extension functions convenientes

### 🔢 Cómo Verificar la Versión

```kotlin
import com.tcontur.aidl.BuildConfig

val version = BuildConfig.LIBRARY_VERSION  // "0.1.0"
val versionCode = BuildConfig.VERSION_CODE // 1

Log.d("AIDL", "📦 Librería v$version (code: $versionCode)")
```

---

## 📜 Historial de Versiones

### v0.1.0 (Febrero 2026) - 🎵 Initial Release

**🎯 Objetivo:** Crear una librería AIDL dinámica y bidireccional para reproducción de música

#### Características Principales

**1. Protocolo Estructurado**
```kotlin
// Formato del protocolo
"PLAY|GENRE|Rock"  // Formato: ACCION|RECURSO|PARAM1|PARAM2
```

**2. API Tipada**
```kotlin
// Comandos (sealed class)
Command.PlayGenre("Rock")           → "PLAY|GENRE|Rock"
Command.Pause                        → "PAUSE|PLAYBACK"
Command.GetGenres                    → "GET|GENRES"

// Respuestas (sealed class)
Response.Playing(...)                ← "PLAYING|Rock|Song|0|10"
Response.Genres(listOf("Rock"...))   ← "GENRES|Rock|Jazz|..."
```

**3. Extension Functions**
```kotlin
// API conveniente y type-safe
repository.playGenre("Rock")
repository.pause()
repository.next("Rock")
```

**4. Géneros Dinámicos**
- El servicio escanea `/Downloads/music/` automáticamente
- Cada subcarpeta es un género
- No necesita lista hardcodeada
- Fallback a lista estática si no hay carpetas

**5. Auto-Discovery**
```kotlin
// Conexión simple sin especificar package
AidlClientBase(
    context = context,
    serviceAction = "com.tcontur.aidl.REMOTE_SERVICE"
    // servicePackage es opcional
)
```

**6. Envío Automático al Conectar**
```
Cliente conecta → Servicio automáticamente envía:
  1. GENRES|Rock|Jazz|Salsa|...
  2. STATUS|PLAYING|Rock|Song|0|10|100
```

**7. Sincronización Bidireccional Mejorada**
- Servicio actualiza su propia UI cuando recibe comandos
- Clientes reciben notificaciones de cambios
- Estado consistente en todas las apps conectadas

**8. Duración y Progreso de Reproducción** ⭐ NEW
- `Response.Playing` incluye `currentPositionMs` y `durationMs`
- `Response.Status` incluye progreso actual de reproducción
- `Response.Progress` para obtener solo el progreso
- Comando `GET|PROGRESS` para solicitar progreso en tiempo real

#### Comandos Disponibles

| Comando | Formato | Descripción |
|---------|---------|-------------|
| `GET\|STATUS` | `GET\|STATUS` | Obtiene estado completo del reproductor (incluye progreso) |
| `GET\|CURRENT_SONG` | `GET\|CURRENT_SONG` | Obtiene canción actual (incluye progreso) |
| `GET\|PROGRESS` | `GET\|PROGRESS` | Obtiene solo progreso de reproducción |
| `PLAY\|INDEX` | `PLAY\|INDEX\|genre\|index` | Reproduce por índice |

#### Respuestas Disponibles

| Respuesta | Formato | Descripción |
|-----------|---------|-------------|
| `PLAYING` | `PLAYING\|genre\|song\|idx\|tot\|posMs\|durMs` | Reproduciendo (con duración) |
| `STATUS` | `STATUS\|state\|genre\|song\|idx\|tot\|vol\|posMs\|durMs` | Estado completo (con progreso) |
| `PROGRESS` | `PROGRESS\|currentMs\|durationMs` | Solo progreso actual |
| `GENRES` | `GENRES\|genre1\|genre2\|...` | Lista de géneros |

#### Uso de la API

**1. Enviar comandos:**
```kotlin
// Usar extension functions
repository.playGenre("Rock")
repository.pause()
repository.next("Rock")
```

**2. Procesar respuestas:**
```kotlin
when (val response = Response.fromProtocol(message)) {
    is Response.Playing -> {
        val genre = response.genre
        val songName = response.songName
        val currentMin = (response.currentPositionMs / 1000) / 60
        val durationMin = (response.durationMs / 1000) / 60
        println("$genre - $songName [$currentMin:xx / $durationMin:xx]")
    }
    is Response.Progress -> {
        val progress = response.currentMs.toFloat() / response.durationMs.toFloat()
        println("Progreso: ${(progress * 100).toInt()}%")
    }
    is Response.Genres -> {
        val genres = response.genres
    }
    else -> {}
}
```

#### Archivos Modificados

- `aidl-library/src/main/java/com/tcontur/aidl/protocol/Command.kt` - NEW
- `aidl-library/src/main/java/com/tcontur/aidl/protocol/Response.kt` - NEW
- `aidl-library/src/main/java/com/tcontur/aidl/AidlClientExtensions.kt` - UPDATED
- `aidl-library/src/main/java/com/tcontur/aidl/AidlServiceBase.kt` - UPDATED
- `aidl-library/build.gradle.kts` - UPDATED (versioning)

---

## 🔄 Migración entre Proyectos

Para mantener sincronizadas ambas versiones de la librería entre **demo_aidl** y otras aplicaciones:

### Opción 1: Copiar Completa (Recomendada)

```bash
# Desde demo_aidl hacia otra app
cp -r demo_aidl/aidl-library/ tu-app/aidl-library/
```

### Opción 2: Usar AAR

```bash
# En demo_aidl
./gradlew :aidl-library:assembleRelease

# Copiar AAR
cp aidl-library/build/outputs/aar/aidl-library-release.aar \
   tu-app/app/libs/

# En tu-app/app/build.gradle.kts
dependencies {
    implementation(files("libs/aidl-library-release.aar"))
}
```

### Verificación de Sincronización

```kotlin
// En ambos proyectos
Log.d("Version", "demo_aidl: ${BuildConfig.LIBRARY_VERSION}")
Log.d("Version", "tu-app: ${BuildConfig.LIBRARY_VERSION}")

// Ambas deben mostrar: "0.1.0"
```

---

## 🔗 Compatibilidad

### Versiones de Android

| Componente | Versión |
|------------|---------|
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Compile SDK** | 35 (Android 14) |
| **Target SDK** | Flexible (recomendado 34+) |

### Dependencias

| Librería | Versión Mínima |
|----------|----------------|
| **Kotlin** | 1.9.0+ |
| **Coroutines** | 1.7.3+ |
| **Compose** | 1.5.0+ (opcional, solo para UI) |
| **Material3** | 1.1.0+ (opcional, solo para UI) |

### Compatibilidad entre Versiones

| Cliente | Servicio | Compatible |
|---------|----------|------------|
| v0.1.0  | v0.1.0   | ✅ Totalmente |

---

## 📐 Versionado Semántico

Esta librería sigue [Semantic Versioning 2.0.0](https://semver.org/)

```
MAJOR.MINOR.PATCH

MAJOR: Cambios incompatibles en la API
MINOR: Nuevas funcionalidades compatibles hacia atrás
PATCH: Bug fixes compatibles hacia atrás
```

### Reglas de VERSION_CODE

```kotlin
VERSION_CODE = Incremental (1, 2, 3, ...)

Ejemplos:
v0.1.0 → 1
v0.2.0 → 2
v0.3.0 → 3
v1.0.0 → 10
```

---

## 🛣️ Roadmap

### v0.2.0 (Planeado)

**Nuevas Características:**
- ✨ Comandos de volumen (SET|VOLUME, VOLUME|UP, VOLUME|DOWN)
- ✨ Comando SEEK|TO para navegar en la canción
- ✨ Metadata extendida (artista, álbum, duración)

**Mejoras:**
- 🔧 Optimización de escaneo de archivos
- 🔧 Cache de géneros y playlists
- 🔧 Reintentos automáticos de conexión

### v1.0.0 (Futuro)

**Cambios Mayores:**
- 🚀 Soporte para múltiples formatos de audio (FLAC, OGG, AAC)
- 🚀 Sistema de ecualizador
- 🚀 Queue de reproducción
- 🚀 Favoritos y ratings

---

## 📝 Notas de Desarrollo

### Incrementar Versión

**1. Actualizar `aidl-library/build.gradle.kts`:**
```kotlin
val libraryVersion = "0.2.0"  // Nueva versión

defaultConfig {
    buildConfigField("String", "LIBRARY_VERSION", "\"$libraryVersion\"")
    buildConfigField("long", "VERSION_CODE", "2")  // 0.2.0 = 2
}
```

**2. Actualizar este archivo (VERSION.md)**

**3. Sync y rebuild:**
```bash
./gradlew :aidl-library:clean
./gradlew :aidl-library:assembleDebug
```

**4. Copiar a otras aplicaciones** (si aplica)

### Testing de Compatibilidad

Antes de lanzar una nueva versión:

```kotlin
// Test 1: Verificar protocolo
@Test
fun testProtocolCompatibility() {
    val command = Command.PlayGenre("Rock")
    val protocol = command.toProtocol()
    assertEquals("PLAY|GENRE|Rock", protocol)

    val parsed = Command.fromProtocol(protocol)
    assertTrue(parsed is Command.PlayGenre)
    assertEquals("Rock", (parsed as Command.PlayGenre).genre)
}

// Test 2: Verificar respuestas
@Test
fun testResponseParsing() {
    val protocol = "PLAYING|Rock|Song1|0|10"
    val response = Response.fromProtocol(protocol)

    assertTrue(response is Response.Playing)
    assertEquals("Rock", (response as Response.Playing).genre)
}
```

---

## ⚠️ Advertencias Importantes

1. **NO** modifiques el formato del protocolo sin incrementar MAJOR version
2. **SIEMPRE** actualiza VERSION.md al cambiar la versión
3. **VERIFICA** que ambos proyectos usen la misma versión
4. **DOCUMENTA** todos los breaking changes
5. **MANTÉN** compatibilidad hacia atrás en versiones MINOR

---

## 📞 Soporte

Para reportar problemas o sugerir mejoras:

1. Verifica que ambos proyectos usen la misma versión
2. Revisa la sección de Troubleshooting en README.md
3. Verifica los logs: `adb logcat | grep AIDL`

---

**Mantenido por:** Equipo de Desarrollo
**Última actualización:** Febrero 2026
**Versión actual:** 0.1.0 (CODE: 1)
