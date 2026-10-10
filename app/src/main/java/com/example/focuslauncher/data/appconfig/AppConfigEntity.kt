package com.example.focuslauncher.data.appconfig

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.focuslauncher.domain.model.AppLevel

@Entity(tableName = "app_configs")
data class AppConfigEntity(
    @PrimaryKey val packageName: String,
    val level: AppLevel,
    val dailyLimitMin: Int?,
    val scheduleJson: String?,
)

class AppLevelConverters {
    @TypeConverter
    fun toLevel(name: String): AppLevel = AppLevel.valueOf(name)

    @TypeConverter
    fun fromLevel(level: AppLevel): String = level.name
}
