package com.example.focuslauncher.data.dock

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.focuslauncher.domain.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dockDataStore: DataStore<Preferences> by preferencesDataStore(name = "dock")

/** Persists the 4 home-dock slots as [AppInfo.key] strings. */
@Singleton
class DockRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val dockKeys: Flow<List<String?>> = context.dockDataStore.data.map { prefs ->
        List(DOCK_SIZE) { index -> prefs[stringPreferencesKey(keyFor(index))] }
    }

    suspend fun setSlot(index: Int, appKey: String?) {
        require(index in 0 until DOCK_SIZE)
        context.dockDataStore.edit { prefs ->
            val key = stringPreferencesKey(keyFor(index))
            if (appKey == null) prefs.remove(key) else prefs[key] = appKey
        }
    }

    companion object {
        const val DOCK_SIZE = 4

        fun keyFor(index: Int): String = "dock_slot_$index"

        /** Maps saved keys to apps; unknown/missing keys become empty slots. */
        fun resolveDock(keys: List<String?>, apps: List<AppInfo>): List<AppInfo?> {
            val byKey = apps.associateBy { it.key }
            return List(DOCK_SIZE) { index -> keys.getOrNull(index)?.let { byKey[it] } }
        }
    }
}
