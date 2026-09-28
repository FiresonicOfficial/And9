package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GameSaveEntity
import com.example.data.local.entity.InstalledAppEntity
import com.example.data.local.entity.VmConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmulatorDao {

    @Query("SELECT * FROM vm_config WHERE id = 1 LIMIT 1")
    fun getVmConfigFlow(): Flow<VmConfigEntity?>

    @Query("SELECT * FROM vm_config WHERE id = 1 LIMIT 1")
    suspend fun getVmConfigOnce(): VmConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: VmConfigEntity)

    @Query("SELECT * FROM installed_apps ORDER BY isSystemApp ASC, name ASC")
    fun getAllInstalledAppsFlow(): Flow<List<InstalledAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: InstalledAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<InstalledAppEntity>)

    @Query("DELETE FROM installed_apps WHERE id = :appId AND isSystemApp = 0")
    suspend fun uninstallApp(appId: String)

    @Query("SELECT * FROM game_saves WHERE gameId = :gameId LIMIT 1")
    fun getGameSaveFlow(gameId: String): Flow<GameSaveEntity?>

    @Query("SELECT * FROM game_saves")
    fun getAllGameSavesFlow(): Flow<List<GameSaveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(save: GameSaveEntity)

    @Query("UPDATE game_saves SET highScore = :highScore WHERE gameId = :gameId")
    suspend fun updateHighScore(gameId: String, highScore: Int)

    @Query("UPDATE game_saves SET playTimeSeconds = playTimeSeconds + :addedSeconds WHERE gameId = :gameId")
    suspend fun addPlayTime(gameId: String, addedSeconds: Long)
}
