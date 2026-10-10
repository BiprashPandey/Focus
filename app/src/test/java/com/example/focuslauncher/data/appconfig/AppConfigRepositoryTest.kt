package com.example.focuslauncher.data.appconfig

import com.example.focuslauncher.domain.model.AppLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppConfigRepositoryTest {

    private class FakeDao : AppConfigDao {
        private val rows = MutableStateFlow(mapOf<String, AppConfigEntity>())

        override fun observeAll(): Flow<List<AppConfigEntity>> =
            rows.map { it.values.toList() }

        override suspend fun get(packageName: String): AppConfigEntity? = rows.value[packageName]

        override suspend fun upsert(entity: AppConfigEntity) {
            rows.value = rows.value + (entity.packageName to entity)
        }

        override suspend fun delete(packageName: String) {
            rows.value = rows.value - packageName
        }

        fun stored(packageName: String): AppConfigEntity? = rows.value[packageName]
    }

    @Test
    fun setLevel_storesLevelWithDefaults() = runBlocking {
        val dao = FakeDao()
        val repo = RoomAppConfigRepository(dao)
        repo.setLevel("com.game", AppLevel.HIDDEN)
        val configs = repo.configs.first()
        assertEquals(AppLevel.HIDDEN, configs.getValue("com.game").level)
        assertNull(configs.getValue("com.game").dailyLimitMin)
    }

    @Test
    fun setLevel_visibleWithNoExtrasRemovesRow() = runBlocking {
        val dao = FakeDao()
        val repo = RoomAppConfigRepository(dao)
        repo.setLevel("com.game", AppLevel.HIDDEN)
        repo.setLevel("com.game", AppLevel.VISIBLE)
        assertNull(dao.stored("com.game"))
        assertEquals(emptyMap<String, Any>(), repo.configs.first())
    }

    @Test
    fun setDailyLimit_preservesLevel() = runBlocking {
        val dao = FakeDao()
        val repo = RoomAppConfigRepository(dao)
        repo.setLevel("com.game", AppLevel.PAUSE_GATED)
        repo.setDailyLimit("com.game", 30)
        val stored = dao.stored("com.game")!!
        assertEquals(AppLevel.PAUSE_GATED, stored.level)
        assertEquals(30, stored.dailyLimitMin)
    }

    @Test
    fun setDailyLimit_nullWithNoExtrasRemovesRow() = runBlocking {
        val dao = FakeDao()
        val repo = RoomAppConfigRepository(dao)
        repo.setDailyLimit("com.game", 30)
        repo.setDailyLimit("com.game", null)
        assertNull(dao.stored("com.game"))
    }
}
