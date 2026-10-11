package com.example.focuslauncher.data.focus

import com.example.focuslauncher.domain.focus.FocusState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSessionDao : FocusSessionDao {
    val rows = mutableListOf<FocusSession>()
    private var nextId = 1L

    override suspend fun openSession(): FocusSession? =
        rows.filter { it.actualEndAt == null }.maxByOrNull { it.startAt }

    override suspend fun insert(session: FocusSession): Long {
        val id = nextId++
        rows.add(session.copy(id = id))
        return id
    }

    override suspend fun close(id: Long, actualEnd: Long, early: Boolean, reason: String?) {
        val i = rows.indexOfFirst { it.id == id }
        rows[i] = rows[i].copy(actualEndAt = actualEnd, endedEarly = early, exitReason = reason)
    }
}

private class FakeProfileDao : FocusProfileDao {
    val rows = mutableListOf<FocusProfile>()
    private var nextId = 1L

    override fun observeAll(): kotlinx.coroutines.flow.Flow<List<FocusProfile>> =
        kotlinx.coroutines.flow.flowOf(rows.toList())

    override suspend fun get(id: Long): FocusProfile? = rows.firstOrNull { it.id == id }

    override suspend fun count(): Int = rows.size

    override suspend fun insert(profile: FocusProfile): Long {
        val id = nextId++
        rows.add(profile.copy(id = id))
        return id
    }

    override suspend fun delete(id: Long) {
        rows.removeAll { it.id == id }
    }

    override suspend fun updateAllowed(id: Long, json: String) {
        val i = rows.indexOfFirst { it.id == id }
        if (i >= 0) rows[i] = rows[i].copy(allowedAppsJson = json)
    }

    override suspend fun updateDnd(id: Long, enabled: Boolean) {
        val i = rows.indexOfFirst { it.id == id }
        if (i >= 0) rows[i] = rows[i].copy(dndEnabled = enabled)
    }

    override suspend fun updateSchedule(id: Long, json: String?) {
        val i = rows.indexOfFirst { it.id == id }
        if (i >= 0) rows[i] = rows[i].copy(scheduleJson = json)
    }
}

class FocusManagerTest {

    private fun manager(sessions: FakeSessionDao = FakeSessionDao()): FocusManager =
        FocusManager(sessions, FakeProfileDao())

    @Test
    fun start_emitsActiveWithPlannedEnd() = runBlocking {
        val m = manager()
        val active = m.start(profileId = 7L, durationMin = 25)
        assertEquals(7L, active.profileId)
        assertEquals(25 * 60_000L, active.endsAt - active.startedAt)
        assertEquals(active, m.state.first())
    }

    @Test
    fun stop_closesEarlyWithReason() = runBlocking {
        val sessions = FakeSessionDao()
        val m = manager(sessions)
        val active = m.start(1L, 50)
        m.stop("changed mind")
        assertEquals(FocusState.Idle, m.state.first())
        val row = sessions.rows.first { it.id == active.sessionId }
        assertTrue(row.endedEarly)
        assertEquals("changed mind", row.exitReason)
        assertTrue(row.actualEndAt!! >= row.startAt)
    }

    @Test
    fun refresh_resumesRunningSession() = runBlocking {
        val sessions = FakeSessionDao()
        val now = System.currentTimeMillis()
        sessions.rows.add(
            FocusSession(
                id = 3L,
                profileId = 1L,
                startAt = now - 60_000L,
                endAt = now + 24 * 60_000L,
                plannedMin = 25,
            ),
        )
        val m = manager(sessions)
        m.refresh()
        val state = m.state.first()
        assertTrue(state is FocusState.Active)
        assertEquals(3L, (state as FocusState.Active).sessionId)
    }

    @Test
    fun refresh_closesExpiredSessionAsCompleted() = runBlocking {
        val sessions = FakeSessionDao()
        val now = System.currentTimeMillis()
        sessions.rows.add(
            FocusSession(
                id = 4L,
                profileId = 1L,
                startAt = now - 60_000L,
                endAt = now - 1_000L,
                plannedMin = 1,
            ),
        )
        val m = manager(sessions)
        m.refresh()
        assertEquals(FocusState.Idle, m.state.first())
        val row = sessions.rows.first { it.id == 4L }
        assertEquals(row.endAt, row.actualEndAt)
        assertEquals(false, row.endedEarly)
    }

    @Test
    fun refresh_withNoSession_isIdle() = runBlocking {
        val m = manager()
        m.refresh()
        assertEquals(FocusState.Idle, m.state.first())
    }

    @Test
    fun createProfile_returnsId() = runBlocking {
        val m = manager()
        val id = m.createProfile("Study")
        assertTrue(id > 0)
    }
}
