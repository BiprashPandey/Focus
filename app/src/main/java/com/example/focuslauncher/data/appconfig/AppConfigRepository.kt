package com.example.focuslauncher.data.appconfig

import com.example.focuslauncher.domain.model.AppConfig
import com.example.focuslauncher.domain.model.AppLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface AppConfigRepository {
    /** Configured packages keyed by package name. Unconfigured means defaults. */
    val configs: Flow<Map<String, AppConfig>>

    suspend fun setLevel(packageName: String, level: AppLevel)

    suspend fun setDailyLimit(packageName: String, minutes: Int?)
}

@Singleton
class RoomAppConfigRepository @Inject constructor(
    private val dao: AppConfigDao,
) : AppConfigRepository {

    override val configs: Flow<Map<String, AppConfig>> = dao.observeAll().map { rows ->
        rows.associate { it.packageName to it.toDomain() }
    }

    override suspend fun setLevel(packageName: String, level: AppLevel) {
        val current = dao.get(packageName)
        if (level == AppLevel.VISIBLE && current?.dailyLimitMin == null && current?.scheduleJson == null) {
            // Default state needs no row; keep the table sparse.
            dao.delete(packageName)
            return
        }
        dao.upsert(
            AppConfigEntity(
                packageName = packageName,
                level = level,
                dailyLimitMin = current?.dailyLimitMin,
                scheduleJson = current?.scheduleJson,
            ),
        )
    }

    override suspend fun setDailyLimit(packageName: String, minutes: Int?) {
        val current = dao.get(packageName)
        if (minutes == null && (current == null || current.level == AppLevel.VISIBLE) && current?.scheduleJson == null) {
            dao.delete(packageName)
            return
        }
        dao.upsert(
            AppConfigEntity(
                packageName = packageName,
                level = current?.level ?: AppLevel.VISIBLE,
                dailyLimitMin = minutes,
                scheduleJson = current?.scheduleJson,
            ),
        )
    }

    private fun AppConfigEntity.toDomain(): AppConfig = AppConfig(
        packageName = packageName,
        level = level,
        dailyLimitMin = dailyLimitMin,
        scheduleJson = scheduleJson,
    )
}
