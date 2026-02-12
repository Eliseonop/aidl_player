package com.example.demo_aidl

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File

object MusicScanner {

    fun scanMusicFolder(genre: String): List<Song> {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val musicDir = File(downloadDir, "music")
        val genreDir = File(musicDir, genre.lowercase())

        Log.d("MusicScanner", "Buscando en: ${genreDir.absolutePath}")

        if (!genreDir.exists() || !genreDir.isDirectory) {
            Log.w("MusicScanner", "Carpeta no existe: ${genreDir.absolutePath}")
            return emptyList()
        }

        val mp3Files = genreDir.listFiles { file ->
            file.isFile && file.extension.equals("mp3", ignoreCase = true)
        }

        if (mp3Files.isNullOrEmpty()) {
            Log.w("MusicScanner", "No hay archivos MP3 en: ${genreDir.absolutePath}")
            return emptyList()
        }

        Log.d("MusicScanner", "Encontrados ${mp3Files.size} archivos MP3")
        return mp3Files.map { file ->
            Song(
                title = file.nameWithoutExtension,
                path = file.absolutePath
            )
        }.sortedBy { it.title }
    }

    fun getAllGenres(): List<String> {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val musicDir = File(downloadDir, "music")

        Log.d("MusicScanner", "Buscando géneros en: ${musicDir.absolutePath}")

        if (!musicDir.exists() || !musicDir.isDirectory) {
            Log.w("MusicScanner", "Carpeta music no existe: ${musicDir.absolutePath}")
            // Fallback a lista estática si la carpeta no existe
            return listOf("Rock", "Jazz", "Relax", "Salsa", "Cumbia", "Reggaeton")
        }

        // Obtener todas las subcarpetas como géneros
        val genreFolders = musicDir.listFiles { file ->
            file.isDirectory
        }

        if (genreFolders.isNullOrEmpty()) {
            Log.w("MusicScanner", "No hay carpetas de géneros en: ${musicDir.absolutePath}")
            // Fallback a lista estática si no hay carpetas
            return listOf("Rock", "Jazz", "Relax", "Salsa", "Cumbia", "Reggaeton")
        }

        val genres = genreFolders.map { it.name.capitalize() }.sorted()
        Log.d("MusicScanner", "Géneros encontrados: $genres")
        return genres
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }
}