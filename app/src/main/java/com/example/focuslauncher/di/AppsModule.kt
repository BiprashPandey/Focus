package com.example.focuslauncher.di

import android.content.Context
import android.content.pm.LauncherApps
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

    companion object {
        @Provides
        fun provideLauncherApps(@ApplicationContext context: Context): LauncherApps =
            context.getSystemService(LauncherApps::class.java)
    }
}
