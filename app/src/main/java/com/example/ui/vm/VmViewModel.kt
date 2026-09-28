package com.example.ui.vm

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.EmulatorRepository
import com.example.data.local.entity.GameSaveEntity
import com.example.data.local.entity.InstalledAppEntity
import com.example.data.local.entity.VmConfigEntity
import com.example.engine.ApkMetadata
import com.example.engine.Arm32CompatibilityLayer
import com.example.engine.HardwareAccelerationEngine
import com.example.engine.audio.RetroAudioSynthesizer
import com.example.games.AeroSquadronGame
import com.example.games.CustomApkGame
import com.example.games.CyberCommandoGame
import com.example.games.DungeonCrawlerGame
import com.example.games.GameButton
import com.example.games.TurboDriftGame
import com.example.games.VirtualGame
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmulatorRepository
    private val vibrator = application.getSystemService(Application.VIBRATOR_SERVICE) as? Vibrator

    val hwEngine = HardwareAccelerationEngine()
    val armLayer = Arm32CompatibilityLayer()
    val audioSynth = RetroAudioSynthesizer()

    val vmConfig: StateFlow<VmConfigEntity?>
    val installedApps: StateFlow<List<InstalledAppEntity>>
    val gameSaves: StateFlow<List<GameSaveEntity>>

    // Running State
    var activeApp by mutableStateOf<InstalledAppEntity?>(null)
        private set
    var activeGame by mutableStateOf<VirtualGame?>(null)
        private set

    var isQuickSettingsOpen by mutableStateOf(false)
    var isRecentsOpen by mutableStateOf(false)
    var isKeymapperOpen by mutableStateOf(false)
    var showFpsHud by mutableStateOf(true)

    // APK Installer Modal State
    var isApkInstallerOpen by mutableStateOf(false)
    var pendingApkToInstall by mutableStateOf<ApkMetadata?>(null)

    fun openApkInstaller(apk: ApkMetadata? = null) {
        pendingApkToInstall = apk
        isApkInstallerOpen = true
        audioSynth.playClick()
        triggerHaptic()
    }

    fun closeApkInstaller() {
        isApkInstallerOpen = false
        pendingApkToInstall = null
    }

    suspend fun installParsedApk(metadata: ApkMetadata, chosenGenre: String): InstalledAppEntity {
        audioSynth.playCoin()
        triggerHaptic()
        val id = "game.custom_${System.currentTimeMillis() % 100000}"
        val app = InstalledAppEntity(
            id = id,
            name = metadata.appName,
            packageName = metadata.packageName,
            versionName = metadata.versionName,
            architecture = metadata.architecture,
            isGame = true,
            iconType = chosenGenre,
            fileSizeMb = metadata.fileSizeMb,
            summary = metadata.summary,
            isSystemApp = false
        )
        repository.installApp(app)
        return app
    }

    // Virtual Gamepad Layout Customization
    var dpadOffsetX by mutableStateOf(40f)
    var dpadOffsetY by mutableStateOf(80f)
    var buttonsOffsetX by mutableStateOf(40f)
    var buttonsOffsetY by mutableStateOf(80f)
    var buttonScale by mutableStateOf(1.0f)
    var gamepadOpacity by mutableStateOf(0.78f)

    // Recent Virtual Apps
    val recentApps = mutableStateListOf<InstalledAppEntity>()

    // Games Map
    private val gamesRegistry = mapOf<String, () -> VirtualGame>(
        "game.drift32" to { TurboDriftGame() },
        "game.commando32" to { CyberCommandoGame() },
        "game.squadron32" to { AeroSquadronGame() },
        "game.dungeon32" to { DungeonCrawlerGame() }
    )

    init {
        val db = AppDatabase.getInstance(application)
        repository = EmulatorRepository(db.emulatorDao())

        vmConfig = repository.vmConfigFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )
        installedApps = repository.installedAppsFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
        gameSaves = repository.gameSavesFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    fun launchApp(app: InstalledAppEntity) {
        audioSynth.playClick()
        triggerHaptic()
        activeApp = app
        isQuickSettingsOpen = false
        isRecentsOpen = false

        if (!recentApps.any { it.id == app.id }) {
            recentApps.add(0, app)
            if (recentApps.size > 8) recentApps.removeAt(recentApps.lastIndex)
        }

        if (app.isGame) {
            val gameFactory = gamesRegistry[app.id]
            if (gameFactory != null) {
                val game = gameFactory()
                game.init(800f, 600f)
                activeGame = game
            } else {
                // Universal 32-bit hardware-accelerated game engine for custom/imported APKs!
                val customGame = CustomApkGame(app)
                customGame.init(800f, 600f)
                activeGame = customGame
            }
        } else {
            activeGame = null
        }
    }

    fun navigateHome() {
        audioSynth.playClick()
        triggerHaptic()
        saveActiveGameProgress()
        activeApp = null
        activeGame = null
        isQuickSettingsOpen = false
        isRecentsOpen = false
    }

    fun navigateBack() {
        audioSynth.playClick()
        triggerHaptic()
        when {
            isQuickSettingsOpen -> isQuickSettingsOpen = false
            isRecentsOpen -> isRecentsOpen = false
            isKeymapperOpen -> isKeymapperOpen = false
            activeApp != null -> navigateHome()
        }
    }

    fun toggleRecents() {
        audioSynth.playClick()
        triggerHaptic()
        isRecentsOpen = !isRecentsOpen
        isQuickSettingsOpen = false
    }

    fun toggleQuickSettings() {
        audioSynth.playClick()
        triggerHaptic()
        isQuickSettingsOpen = !isQuickSettingsOpen
    }

    fun toggleHwAcceleration(enabled: Boolean) {
        hwEngine.isHwAccelerated = enabled
        updateConfig { it.copy(hwAccelEnabled = enabled) }
    }

    fun toggleMultiThreaded(enabled: Boolean) {
        hwEngine.isMultiThreaded = enabled
        updateConfig { it.copy(multiThreadedRendering = enabled) }
    }

    fun setTargetFps(fps: Int) {
        hwEngine.targetFps = fps
        updateConfig { it.copy(targetFps = fps) }
    }

    fun setShader(shader: String) {
        hwEngine.activeShader = shader
        updateConfig { it.copy(shaderEffect = shader) }
    }

    fun setResolutionScale(scale: Float) {
        hwEngine.resolutionScale = scale
        updateConfig { it.copy(resolutionScale = scale) }
    }

    fun setNavBarType(type: String) {
        updateConfig { it.copy(navBarType = type) }
    }

    fun setWallpaper(wall: String) {
        updateConfig { it.copy(wallpaper = wall) }
    }

    fun toggleSound(enabled: Boolean) {
        audioSynth.isEnabled = enabled
        updateConfig { it.copy(soundEnabled = enabled) }
    }

    fun toggleVibration(enabled: Boolean) {
        updateConfig { it.copy(vibrationEnabled = enabled) }
    }

    fun setArmMode(mode: String) {
        updateConfig { it.copy(armTranslationMode = mode) }
    }

    private fun updateConfig(update: (VmConfigEntity) -> VmConfigEntity) {
        val current = vmConfig.value ?: return
        val updated = update(current)
        viewModelScope.launch {
            repository.updateConfig(updated)
        }
    }

    fun onGameDpad(dx: Float, dy: Float) {
        activeGame?.onDpad(dx, dy)
    }

    fun onGameButtonDown(btn: GameButton) {
        triggerHaptic()
        activeGame?.onButtonDown(btn, audioSynth)
    }

    fun onGameButtonUp(btn: GameButton) {
        activeGame?.onButtonUp(btn)
    }

    fun resetActiveGame() {
        audioSynth.playJump()
        activeGame?.reset()
    }

    fun saveActiveGameProgress() {
        val current = activeGame ?: return
        val appId = activeApp?.id ?: return
        val score = current.getScore()
        viewModelScope.launch {
            repository.saveGameProgress(appId, score, 60L)
        }
    }

    fun installApkPackage(name: String, pkg: String, sizeMb: Float, type: String, isGame: Boolean, summary: String) {
        audioSynth.playCoin()
        triggerHaptic()
        viewModelScope.launch {
            val app = InstalledAppEntity(
                id = if (isGame) "game.custom_${System.currentTimeMillis() % 10000}" else "app.custom_${System.currentTimeMillis() % 10000}",
                name = name,
                packageName = pkg,
                versionName = "1.0-pie",
                architecture = "armeabi-v7a (32-bit)",
                isGame = isGame,
                iconType = type,
                fileSizeMb = sizeMb,
                summary = summary,
                isSystemApp = false
            )
            repository.installApp(app)
        }
    }

    fun uninstallPackage(appId: String) {
        audioSynth.playHit()
        triggerHaptic()
        viewModelScope.launch {
            repository.uninstallApp(appId)
            if (activeApp?.id == appId) {
                navigateHome()
            }
        }
    }

    fun triggerHaptic(durationMs: Long = 25L) {
        val cfg = vmConfig.value
        if (cfg?.vibrationEnabled == false) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun executeTerminalCommand(command: String): String {
        val trimmed = command.trim()
        val cfg = vmConfig.value
        return when {
            trimmed == "uname -a" ->
                "Linux pie-vm 4.14.85-gki-v2 #1 SMP PREEMPT Sun Sep 27 2018 armv7l GNU/Linux"
            trimmed == "getprop ro.build.version.release" -> "9"
            trimmed == "getprop ro.build.version.sdk" -> "28"
            trimmed == "getprop ro.product.cpu.abi" -> "armeabi-v7a"
            trimmed == "getprop ro.product.model" -> "SM-G960F (Galaxy S9 Pie 32-Bit)"
            trimmed == "cat /proc/cpuinfo" ->
                "Processor\t: ARMv7 Processor rev 4 (v7l)\nBogoMIPS\t: 2154.24\nFeatures\t: half thumb fastmult vfp edsp neon vfpv3 tls vfpv4 idiva idivt\nCPU implementer\t: 0x41 (ARM)\nCPU architecture: 7\nHardware\t: Qualcomm SDM845"
            trimmed == "free -m" ->
                "             total        used        free      shared     buff/cache   available\nMem:          ${cfg?.virtualMemoryMb ?: 3072}         ${(cfg?.virtualMemoryMb ?: 3072) / 3}        ${(cfg?.virtualMemoryMb ?: 3072) * 2 / 3}          32         384        ${(cfg?.virtualMemoryMb ?: 3072) * 3 / 5}"
            trimmed == "dumpsys gfxinfo" ->
                "Graphics info for Android 9 Pie:\nHardware Accelerated: ${hwEngine.isHwAccelerated}\nMulti-Threaded Rendering: ${hwEngine.isMultiThreaded}\nTarget FPS: ${hwEngine.targetFps} Hz\nCurrent FPS: ${hwEngine.currentFps}\nFrame Render Time: ${hwEngine.frameTimeMs} ms\nJNI Graphics Hook: libglesv3_hw.so (32-bit)"
            trimmed == "help" ->
                "Available commands:\n- uname -a\n- getprop ro.build.version.release\n- cat /proc/cpuinfo\n- free -m\n- dumpsys gfxinfo\n- ps (process list)\n- clear"
            trimmed == "ps" ->
                "USER     PID   PPID  VSIZE  RSS     WCHAN    PC        NAME\nsystem   101   1     34800  4200    epoll_w  b704c000  zygote32\nsystem   142   101   280000 68000   epoll_w  b704c000  system_server\nu0_a28   840   101   180000 45000   binder_t b704c000  com.android.launcher3\nu0_a40   1204  101   220000 52000   futex_   b704c000  ${activeApp?.packageName ?: "com.android.settings"}"
            trimmed == "clear" -> ""
            else -> "pie32: command not found: $trimmed. Type 'help' for commands."
        }
    }
}
