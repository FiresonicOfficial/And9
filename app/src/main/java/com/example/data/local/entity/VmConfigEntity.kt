package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores configuration for the Android 9 Virtual Machine & Hardware Acceleration Engine.
 */
@Entity(tableName = "vm_config")
data class VmConfigEntity(
    @PrimaryKey val id: Int = 1,
    val hwAccelEnabled: Boolean = true,
    val multiThreadedRendering: Boolean = true,
    val targetFps: Int = 60,
    val resolutionScale: Float = 1.0f,
    val shaderEffect: String = "CRT_SCANLINES", // "OFF", "CRT_SCANLINES", "NEON_GLOW", "SMOOTH"
    val vibrationEnabled: Boolean = true,
    val virtualMemoryMb: Int = 3072, // 32-bit ARM max limit is 4096 MB
    val armTranslationMode: String = "JIT_FAST", // "JIT_FAST", "INTERPRETER", "COMPAT_STRICT"
    val navBarType: String = "PILL", // "PILL" (Android 9 2-button), "THREE_BUTTON"
    val wallpaper: String = "PIE_DEFAULT", // "PIE_DEFAULT", "CYBER_DARK", "RETRO_NEON", "DEEP_PURPLE"
    val gamepadOpacity: Float = 0.78f,
    val gamepadScale: Float = 1.0f,
    val showPerformanceHud: Boolean = true,
    val soundEnabled: Boolean = true
)
