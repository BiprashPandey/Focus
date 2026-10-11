package com.example.focuslauncher.di

import android.content.Context
import android.content.pm.LauncherApps
import com.example.focuslauncher.data.appconfig.AppConfigRepository
import com.example.focuslauncher.data.appconfig.RoomAppConfigRepository
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.data.apps.LauncherAppsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppsModule {

    @Binds
    abstract fun bindAppsRepository(impl: LauncherAppsRepository): AppsRepository

    @Binds
    abstract fun bindAppConfigRepository(impl: RoomAppConfigRepository): AppConfigRepository

    @Binds
    abstract fun bindUsageRepository(
        impl: com.example.focuslauncher.data.usage.SystemUsageRepository,
    ): com.example.focuslauncher.data.usage.UsageRepository

    companion object {
        @Provides
        fun provideLauncherApps(@ApplicationContext context: Context): LauncherApps =
            context.getSystemService(LauncherApps::class.java)

        @Provides
        fun provideUsageStats(@ApplicationContext context: Context): android.app.usage.UsageStatsManager =
            context.getSystemService(android.app.usage.UsageStatsManager::class.java)

        @Provides
        fun provideAppOps(@ApplicationContext context: Context): android.app.AppOpsManager =
            context.getSystemService(android.app.AppOpsManager::class.java)
    }
}
