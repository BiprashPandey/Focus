package com.example.focuslauncher.data.focus

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val startAt: Long,
    val endAt: Long,
    val plannedMin: Int,
    val actualEndAt: Long? = null,
    val endedEarly: Boolean = false,
    val exitReason: String? = null,
)

@Dao
interface FocusSessionDao {
    /** Latest still-open session, if any. At most one should be open at a time. */
    @Query("SELECT * FROM focus_sessions WHERE actualEndAt IS NULL ORDER BY startAt DESC LIMIT 1")
    suspend fun openSession(): FocusSession?

    @Insert
    suspend fun insert(session: FocusSession): Long

    /** Close a session: set its actual end, whether it ended early, and why. */
    @Query("UPDATE focus_sessions SET actualEndAt = :actualEnd, endedEarly = :early, exitReason = :reason WHERE id = :id")
    suspend fun close(id: Long, actualEnd: Long, early: Boolean, reason: String?)
}
