package com.example.data.local

import com.example.data.local.dao.EmulatorDao
import com.example.data.local.entity.GameSaveEntity
import com.example.data.local.entity.InstalledAppEntity
import com.example.data.local.entity.VmConfigEntity
import kotlinx.coroutines.flow.Flow

class EmulatorRepository(private val dao: EmulatorDao) {

    val vmConfigFlow: Flow<VmConfigEntity?> = dao.getVmConfigFlow()
    val installedAppsFlow: Flow<List<InstalledAppEntity>> = dao.getAllInstalledAppsFlow()
    val gameSavesFlow: Flow<List<GameSaveEntity>> = dao.getAllGameSavesFlow()

    suspend fun initializeDefaultsIfNeeded() {
        val currentConfig = dao.getVmConfigOnce()
        if (currentConfig == null) {
            dao.insertOrUpdateConfig(
                VmConfigEntity(
                    id = 1,
                    hwAccelEnabled = true,
                    multiThreadedRendering = true,
                    targetFps = 60,
                    resolutionScale = 1.0f,
                    shaderEffect = "CRT_SCANLINES",
                    vibrationEnabled = true,
                    virtualMemoryMb = 3072,
                    armTranslationMode = "JIT_FAST",
                    navBarType = "PILL",
                    wallpaper = "PIE_DEFAULT",
                    gamepadOpacity = 0.8f,
                    gamepadScale = 1.0f,
                    showPerformanceHud = true,
                    soundEnabled = true
                )
            )

            // Seed default Android 9 Pie System Apps and 32-bit Games
            val defaultApps = listOf(
                // 32-bit Games
                InstalledAppEntity(
                    id = "game.drift32",
                    name = "Turbo Drift 32",
                    packageName = "com.retro32.turbodrift",
                    versionName = "2.4.1",
                    architecture = "armeabi-v7a (32-bit)",
                    isGame = true,
                    iconType = "RACER",
                    fileSizeMb = 48.6f,
                    summary = "High-speed pseudo-3D arcade racer with nitro boost and curve physics."
                ),
                InstalledAppEntity(
                    id = "game.commando32",
                    name = "Cyber Commando",
                    packageName = "com.retro32.commando99",
                    versionName = "1.9.9",
                    architecture = "armeabi-v7a (32-bit)",
                    isGame = true,
                    iconType = "ACTION",
                    fileSizeMb = 36.2f,
                    summary = "32-bit run-and-gun side-scrolling platformer with multi-weapon arsenal."
                ),
                InstalledAppEntity(
                    id = "game.squadron32",
                    name = "Aero Squadron 32",
                    packageName = "com.arcade32.aerosquadron",
                    versionName = "3.1.0",
                    architecture = "armeabi-v7a (32-bit)",
                    isGame = true,
                    iconType = "SHMUP",
                    fileSizeMb = 29.8f,
                    summary = "Vertical bullet hell arcade space shooter with laser upgrades & screen-clearing bombs."
                ),
                InstalledAppEntity(
                    id = "game.dungeon32",
                    name = "Dungeon Crawler 32",
                    packageName = "com.retro32.dungeoncrawl",
                    versionName = "1.0.4",
                    architecture = "armeabi-v7a (32-bit)",
                    isGame = true,
                    iconType = "RPG",
                    fileSizeMb = 42.1f,
                    summary = "Top-down retro dungeon adventure with sword slashing, magic, and loot."
                ),
                // System Apps
                InstalledAppEntity(
                    id = "app.settings",
                    name = "Settings",
                    packageName = "com.android.settings",
                    versionName = "9.0 (API 28)",
                    architecture = "System (Pie 28)",
                    isGame = false,
                    iconType = "SETTINGS",
                    fileSizeMb = 14.2f,
                    summary = "Android 9 System Settings, Hardware Acceleration & Virtual Hardware.",
                    isSystemApp = true
                ),
                InstalledAppEntity(
                    id = "app.files",
                    name = "Files",
                    packageName = "com.android.documentsui",
                    versionName = "9.0",
                    architecture = "System (Pie 28)",
                    isGame = false,
                    iconType = "FILES",
                    fileSizeMb = 8.5f,
                    summary = "Manage virtual SD storage, 32-bit shared objects (.so), and game ROMs.",
                    isSystemApp = true
                ),
                InstalledAppEntity(
                    id = "app.store",
                    name = "32-Bit Hub",
                    packageName = "com.android.vending32",
                    versionName = "12.8.3",
                    architecture = "System (Pie 28)",
                    isGame = false,
                    iconType = "STORE",
                    fileSizeMb = 18.0f,
                    summary = "Catalog & package installer for 32-bit legacy titles and homebrew ROMs.",
                    isSystemApp = true
                ),
                InstalledAppEntity(
                    id = "app.terminal",
                    name = "Pie Shell",
                    packageName = "com.android.terminal32",
                    versionName = "1.1",
                    architecture = "System (Pie 28)",
                    isGame = false,
                    iconType = "TERMINAL",
                    fileSizeMb = 4.2f,
                    summary = "Virtual ARMv7-a Linux kernel console, dumpsys gfxinfo & memory inspector.",
                    isSystemApp = true
                )
            )
            dao.insertApps(defaultApps)

            // Seed initial game saves
            val defaultSaves = listOf(
                GameSaveEntity(gameId = "game.drift32", gameTitle = "Turbo Drift 32", highScore = 12450, playTimeSeconds = 240),
                GameSaveEntity(gameId = "game.commando32", gameTitle = "Cyber Commando", highScore = 8300, playTimeSeconds = 180),
                GameSaveEntity(gameId = "game.squadron32", gameTitle = "Aero Squadron 32", highScore = 25900, playTimeSeconds = 310),
                GameSaveEntity(gameId = "game.dungeon32", gameTitle = "Dungeon Crawler 32", highScore = 5400, playTimeSeconds = 150)
            )
            for (save in defaultSaves) {
                dao.saveGameState(save)
            }
        }
    }

    suspend fun updateConfig(config: VmConfigEntity) {
        dao.insertOrUpdateConfig(config)
    }

    suspend fun installApp(app: InstalledAppEntity) {
        dao.insertApp(app)
    }

    suspend fun uninstallApp(appId: String) {
        dao.uninstallApp(appId)
    }

    suspend fun saveGameProgress(gameId: String, score: Int, addedPlayTimeSec: Long) {
        dao.addPlayTime(gameId, addedPlayTimeSec)
        dao.updateHighScore(gameId, score)
    }
}
