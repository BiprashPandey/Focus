package com.example.focuslauncher.di

import android.content.Context
import androidx.room.Room
import com.example.focuslauncher.data.appconfig.AppConfigDao
import com.example.focuslauncher.data.appconfig.FocusDatabase
import com.example.focuslauncher.data.appconfig.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FocusDatabase =
        Room.databaseBuilder(context, FocusDatabase::class.java, "focus.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideAppConfigDao(database: FocusDatabase): AppConfigDao = database.appConfigDao()

    @Provides
    fun provideFocusProfileDao(
        database: FocusDatabase,
    ): com.example.focuslauncher.data.focus.FocusProfileDao = database.focusProfileDao()

    @Provides
    fun provideFocusSessionDao(
        database: FocusDatabase,
    ): com.example.focuslauncher.data.focus.FocusSessionDao = database.focusSessionDao()
}
