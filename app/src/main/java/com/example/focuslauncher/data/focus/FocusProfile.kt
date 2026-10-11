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

    @Query("SELECT COUNT(*) FROM focus_profiles")
    suspend fun count(): Int

    @Insert
    suspend fun insert(profile: FocusProfile): Long

    @Query("DELETE FROM focus_profiles WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE focus_profiles SET allowedAppsJson = :json WHERE id = :id")
    suspend fun updateAllowed(id: Long, json: String)

    @Query("UPDATE focus_profiles SET dndEnabled = :enabled WHERE id = :id")
    suspend fun updateDnd(id: Long, enabled: Boolean)

    @Query("UPDATE focus_profiles SET scheduleJson = :json WHERE id = :id")
    suspend fun updateSchedule(id: Long, json: String?)
}
