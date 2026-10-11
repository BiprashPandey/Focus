package com.example.focuslauncher.data.appconfig

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.focuslauncher.data.focus.FocusProfile
import com.example.focuslauncher.data.focus.FocusProfileDao
import com.example.focuslauncher.data.focus.FocusSession
import com.example.focuslauncher.data.focus.FocusSessionDao

@Database(
    entities = [AppConfigEntity::class, FocusProfile::class, FocusSession::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(AppLevelConverters::class)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
    abstract fun focusProfileDao(): FocusProfileDao
    abstract fun focusSessionDao(): FocusSessionDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `focus_profiles` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`allowedAppsJson` TEXT NOT NULL DEFAULT '[]', " +
                "`dndEnabled` INTEGER NOT NULL DEFAULT 0, " +
                "`scheduleJson` TEXT)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `focus_sessions` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`profileId` INTEGER NOT NULL, " +
                "`startAt` INTEGER NOT NULL, " +
                "`endAt` INTEGER NOT NULL, " +
                "`plannedMin` INTEGER NOT NULL, " +
                "`actualEndAt` INTEGER, " +
                "`endedEarly` INTEGER NOT NULL DEFAULT 0, " +
                "`exitReason` TEXT)",
        )
    }
}
