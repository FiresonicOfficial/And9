package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores savestates, high scores, playtime, and progress for 32-bit games.
 */
@Entity(tableName = "game_saves")
data class GameSaveEntity(
    @PrimaryKey val gameId: String,
    val gameTitle: String,
    val highScore: Int = 0,
    val playTimeSeconds: Long = 0L,
    val lastSaveTimestamp: Long = System.currentTimeMillis(),
    val saveStateJson: String = ""
)
