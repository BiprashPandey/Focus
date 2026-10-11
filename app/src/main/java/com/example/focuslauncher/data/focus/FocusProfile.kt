package com.example.focuslauncher.data.focus

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "focus_profiles")
data class FocusProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val allowedAppsJson: String = "[]",
    val dndEnabled: Boolean = false,
    val scheduleJson: String? = null,
)

@Dao
interface FocusProfileDao {
    @Query("SELECT * FROM focus_profiles ORDER BY id")
    fun observeAll(): Flow<List<FocusProfile>>

    @Query("SELECT * FROM focus_profiles WHERE id = :id")
    suspend fun get(id: Long): FocusProfile?

    @Insert
    suspend fun insert(profile: FocusProfile): Long

    @Query("DELETE FROM focus_profiles WHERE id = :id")
    suspend fun delete(id: Long)
}
