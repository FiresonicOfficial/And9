package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app or game installed inside the Android 9 (Pie) virtual sandbox.
 */
@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey val id: String,
    val name: String,
    val packageName: String,
    val versionName: String,
    val architecture: String, // "armeabi-v7a (32-bit)", "x86 (32-bit)"
    val isGame: Boolean,
    val iconType: String,
    val installDate: Long = System.currentTimeMillis(),
    val fileSizeMb: Float,
    val summary: String,
    val isSystemApp: Boolean = false
)
