package com.example.focuslauncher.domain.model

/** Per-app gating config. Only stored when the user changes a default. */
data class AppConfig(
    val packageName: String,
    val level: AppLevel = AppLevel.VISIBLE,
    val dailyLimitMin: Int? = null,
    val scheduleJson: String? = null,
)
