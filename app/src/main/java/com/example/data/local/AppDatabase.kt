package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.EmulatorDao
import com.example.data.local.entity.GameSaveEntity
import com.example.data.local.entity.InstalledAppEntity
import com.example.data.local.entity.VmConfigEntity

@Database(
    entities = [
        VmConfigEntity::class,
        InstalledAppEntity::class,
        GameSaveEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun emulatorDao(): EmulatorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pie_emulator_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
