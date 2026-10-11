package com.example.focuslauncher.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.settingsStore by preferencesDataStore(name = "settings")

private val TONE = stringPreferencesKey("tone")
private val CARD_ORDER = stringPreferencesKey("card_order")
private val HIDDEN_CARDS = stringPreferencesKey("hidden_cards")
private val LAYOUT = stringPreferencesKey("home_layout")
private val NUDGE_DATE = stringPreferencesKey("nudge_date")
private val NUDGE_COUNT = intPreferencesKey("nudge_count")

/** Home cards known to the launcher, in default top-to-bottom order. */
object HomeCards {
    const val MOTIVATION = "motivation"
    const val TODOS = "todos"
    const val FOCUS = "focus"
    const val NOTES = "notes"
    const val CALENDAR = "calendar"
    val defaultOrder = listOf(MOTIVATION, TODOS, FOCUS, NOTES, CALENDAR)
}

object Tones {
    const val GENTLE = "gentle"
    const val COACH = "coach"
    const val STOIC = "stoic"
    const val FUNNY = "funny"
    val all = listOf(GENTLE, COACH, STOIC, FUNNY)
}

object Layouts {
    const val DASHBOARD = "dashboard"
    const val MINIMAL = "minimal"
}

data class HomeLayout(
    val order: List<String> = HomeCards.defaultOrder,
    val hidden: Set<String> = emptySet(),
    val layout: String = Layouts.DASHBOARD,
) {
    /** Cards to render, excluding unknown ids. Focus is pinned by callers when active. */
    fun visibleCards(): List<String> {
        val known = order.filter { it in HomeCards.defaultOrder }
        val missing = HomeCards.defaultOrder.filter { it !in known }
        return (known + missing).filter { it !in hidden }
    }
}

/** Tone, home layout, and the daily nudge counter. One DataStore file. */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val tone: Flow<String> = context.settingsStore.data.map { it[TONE] ?: Tones.GENTLE }

    suspend fun setTone(tone: String) {
        require(tone in Tones.all)
        context.settingsStore.edit { it[TONE] = tone }
    }

    val homeLayout: Flow<HomeLayout> = context.settingsStore.data.map { prefs ->
        HomeLayout(
            order = prefs[CARD_ORDER]?.split(",")?.filter { it.isNotBlank() }
                ?: HomeCards.defaultOrder,
            hidden = prefs[HIDDEN_CARDS]?.split(",")?.filter { it.isNotBlank() }?.toSet()
                ?: emptySet(),
            layout = prefs[LAYOUT] ?: Layouts.DASHBOARD,
        )
    }

    suspend fun setCardOrder(order: List<String>) {
        context.settingsStore.edit { it[CARD_ORDER] = order.joinToString(",") }
    }

    suspend fun setCardHidden(card: String, hidden: Boolean) {
        context.settingsStore.edit { prefs ->
            val current = prefs[HIDDEN_CARDS]?.split(",")?.filter { it.isNotBlank() }
                ?.toMutableSet() ?: mutableSetOf()
            if (hidden) current.add(card) else current.remove(card)
            prefs[HIDDEN_CARDS] = current.joinToString(",")
        }
    }

    suspend fun setLayout(layout: String) {
        require(layout == Layouts.DASHBOARD || layout == Layouts.MINIMAL)
        context.settingsStore.edit { it[LAYOUT] = layout }
    }

    /** Nudge servings today (for the 3/day cap). */
    suspend fun nudgesServed(dateKey: String): Int {
        var count = 0
        context.settingsStore.edit { prefs ->
            count = if (prefs[NUDGE_DATE] == dateKey) prefs[NUDGE_COUNT] ?: 0 else 0
        }
        return count
    }

    suspend fun recordNudge(dateKey: String) {
        context.settingsStore.edit { prefs ->
            if (prefs[NUDGE_DATE] == dateKey) {
                prefs[NUDGE_COUNT] = (prefs[NUDGE_COUNT] ?: 0) + 1
            } else {
                prefs[NUDGE_DATE] = dateKey
                prefs[NUDGE_COUNT] = 1
            }
        }
    }
}
