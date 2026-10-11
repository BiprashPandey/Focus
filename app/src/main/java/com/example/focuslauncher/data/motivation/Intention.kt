package com.example.focuslauncher.data.motivation

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/** One line per day, keyed by ISO date (`2026-10-11`). */
@Entity(tableName = "intentions")
data class Intention(
    @PrimaryKey val date: String,
    val text: String,
    val completed: Boolean = false,
)

@Dao
interface IntentionDao {
    @Query("SELECT * FROM intentions WHERE date = :date")
    suspend fun get(date: String): Intention?

    @Upsert
    suspend fun upsert(intention: Intention)
}
