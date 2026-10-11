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
import com.example.focuslauncher.data.motivation.Intention
import com.example.focuslauncher.data.motivation.IntentionDao
import com.example.focuslauncher.data.notes.Note
import com.example.focuslauncher.data.notes.NoteDao
import com.example.focuslauncher.data.todos.Todo
import com.example.focuslauncher.data.todos.TodoDao

@Database(
    entities = [
        AppConfigEntity::class,
        FocusProfile::class,
        FocusSession::class,
        Todo::class,
        Note::class,
        Intention::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(AppLevelConverters::class)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
    abstract fun focusProfileDao(): FocusProfileDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun todoDao(): TodoDao
    abstract fun noteDao(): NoteDao
    abstract fun intentionDao(): IntentionDao
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `todos` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`notes` TEXT, " +
                "`dueAt` INTEGER, " +
                "`priority` INTEGER NOT NULL DEFAULT 1, " +
                "`doneAt` INTEGER, " +
                "`createdAt` INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notes` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT, " +
                "`body` TEXT NOT NULL, " +
                "`pinned` INTEGER NOT NULL DEFAULT 0, " +
                "`updatedAt` INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `intentions` (" +
                "`date` TEXT NOT NULL PRIMARY KEY, " +
                "`text` TEXT NOT NULL, " +
                "`completed` INTEGER NOT NULL DEFAULT 0)",
        )
    }
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
