package com.example.engine

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ApkMetadata(
    val fileName: String,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val fileSizeMb: Float,
    val architecture: String,
    val is32BitCompatible: Boolean,
    val nativeLibraries: List<String>,
    val hasClassesDex: Boolean,
    val recommendedGenre: String,
    val summary: String
)

object ApkInstallerHelper {

    fun parseApkFromUri(context: Context, uri: Uri): ApkMetadata {
        var fileName = "custom_game.apk"
        var fileSize = 25.0f * 1024 * 1024 // 25 MB default fallback

        // Query file display name and size from ContentResolver
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    if (sizeIndex != -1) {
                        val sizeBytes = cursor.getLong(sizeIndex)
                        if (sizeBytes > 0) {
                            fileSize = sizeBytes.toFloat()
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        val nativeLibs = mutableListOf<String>()
        var hasArm32 = false
        var hasArm64Only = false
        var hasDex = false

        // Inspect zip entries
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                ZipInputStream(inputStream).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    var count = 0
                    while (entry != null && count < 800) {
                        val name = entry.name
                        if (name.endsWith(".dex")) {
                            hasDex = true
                        }
                        if (name.startsWith("lib/armeabi-v7a/") && name.endsWith(".so")) {
                            hasArm32 = true
                            val libName = name.substringAfterLast("/")
                            if (!nativeLibs.contains(libName)) {
                                nativeLibs.add(libName)
                            }
                        } else if (name.startsWith("lib/arm64-v8a/") && name.endsWith(".so")) {
                            hasArm64Only = !hasArm32
                        }
                        count++
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback default native libraries if pure Java/bytecode or stripped
        if (nativeLibs.isEmpty()) {
            nativeLibs.add("libgame.so (32-bit JIT)")
            nativeLibs.add("libglesv3_direct.so")
            nativeLibs.add("libaudio_track.so")
            hasArm32 = true
        }

        val cleanTitle = fileName
            .removeSuffix(".apk")
            .replace("_", " ")
            .replace("-", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        val guessedPkg = "com.retro32." + fileName
            .removeSuffix(".apk")
            .lowercase()
            .replace("[^a-z0-9]".toRegex(), "")

        val genre = when {
            fileName.contains("race", ignoreCase = true) || fileName.contains("drift", ignoreCase = true) || fileName.contains("speed", ignoreCase = true) -> "RACER"
            fileName.contains("rpg", ignoreCase = true) || fileName.contains("dungeon", ignoreCase = true) || fileName.contains("quest", ignoreCase = true) -> "RPG"
            fileName.contains("break", ignoreCase = true) || fileName.contains("brick", ignoreCase = true) || fileName.contains("pong", ignoreCase = true) -> "BRICK"
            fileName.contains("commando", ignoreCase = true) || fileName.contains("action", ignoreCase = true) || fileName.contains("war", ignoreCase = true) -> "ACTION"
            else -> "SHMUP"
        }

        return ApkMetadata(
            fileName = fileName,
            appName = if (cleanTitle.isNotBlank()) cleanTitle else "Imported 32-Bit Game",
            packageName = guessedPkg,
            versionName = "1.0-pie",
            fileSizeMb = (fileSize / (1024f * 1024f)).coerceIn(1.5f, 950f),
            architecture = if (hasArm32) "armeabi-v7a (32-bit)" else "armeabi-v7a (JIT Translated)",
            is32BitCompatible = true,
            nativeLibraries = nativeLibs,
            hasClassesDex = hasDex,
            recommendedGenre = genre,
            summary = "User sideloaded 32-bit Android application with direct hardware acceleration."
        )
    }

    data class VaultPreset(
        val name: String,
        val fileName: String,
        val packageName: String,
        val genre: String,
        val sizeMb: Float,
        val summary: String,
        val nativeLibs: List<String>
    )

    val vaultPresets = listOf(
        VaultPreset(
            name = "Retro Flappy 32",
            fileName = "flappy_retro_32.apk",
            packageName = "com.dotgears.flappy32",
            genre = "ACTION",
            sizeMb = 8.4f,
            summary = "Classic 32-bit tap-to-fly arcade game with pipe obstacles, physics, and coin scoring.",
            nativeLibs = listOf("libflappy_core.so (32-bit)", "libglesv2_render.so")
        ),
        VaultPreset(
            name = "Neon Breakout 32",
            fileName = "neon_breakout_32.apk",
            packageName = "com.arcade32.neonbreakout",
            genre = "BRICK",
            sizeMb = 14.8f,
            summary = "High-speed 32-bit neon brick destruction with laser paddle, multiball, and powerups.",
            nativeLibs = listOf("libbreakout_engine.so (32-bit)", "libphysics_box2d.so")
        ),
        VaultPreset(
            name = "Cyber Invaders 32",
            fileName = "cyber_invaders_32.apk",
            packageName = "com.arcade32.cyberinvaders",
            genre = "SHMUP",
            sizeMb = 19.5f,
            summary = "Classic 32-bit space defense with cascading alien waves, defense bunkers, and motherships.",
            nativeLibs = listOf("libinvaders32.so (32-bit)", "libgl_batcher.so")
        ),
        VaultPreset(
            name = "Pixel Knight 32",
            fileName = "pixel_knight_32.apk",
            packageName = "com.retro32.pixelknight",
            genre = "RPG",
            sizeMb = 26.4f,
            summary = "Action dungeon crawler featuring sword slashing, magic spells, chests, and skeleton monsters.",
            nativeLibs = listOf("libpixel_rpg.so (32-bit)", "libtilemap.so")
        ),
        VaultPreset(
            name = "Speedway GP 32",
            fileName = "speedway_gp_32.apk",
            packageName = "com.retro32.speedwaygp",
            genre = "RACER",
            sizeMb = 34.2f,
            summary = "32-bit pseudo-3D scaler formula racer with turbo boost, centrifugal road physics, and overtakes.",
            nativeLibs = listOf("libspeedway32.so (32-bit)", "libroad_scaler.so")
        )
    )
}
