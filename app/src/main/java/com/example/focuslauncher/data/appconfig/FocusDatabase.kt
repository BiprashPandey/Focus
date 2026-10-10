package com.example.focuslauncher.data.appconfig

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AppConfigEntity::class], version = 1, exportSchema = false)
@TypeConverters(AppLevelConverters::class)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
}
