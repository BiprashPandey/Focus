package com.example.focuslauncher.data.appconfig

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_configs")
    fun observeAll(): Flow<List<AppConfigEntity>>

    @Query("SELECT * FROM app_configs WHERE packageName = :packageName")
    suspend fun get(packageName: String): AppConfigEntity?

    @Upsert
    suspend fun upsert(entity: AppConfigEntity)

    @Query("DELETE FROM app_configs WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
