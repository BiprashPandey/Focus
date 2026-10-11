package com.example.focuslauncher.data.motivation

import android.content.Context
import com.example.focuslauncher.data.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class DailyMessage(val quote: Quote?, val quietCapped: Boolean)

/** Intention CRUD, tone setting, and the capped daily quote. */
@Singleton
class MotivationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentions: IntentionDao,
    private val settings: SettingsRepository,
) {
    val tone: Flow<String> = settings.tone

    suspend fun setTone(tone: String) = settings.setTone(tone)

    fun todayKey(date: LocalDate = LocalDate.now()): String = date.toString()

    suspend fun intention(dateKey: String): Intention? = withContext(Dispatchers.IO) {
        intentions.get(dateKey)
    }

    suspend fun saveIntention(dateKey: String, text: String) = withContext(Dispatchers.IO) {
        val current = intentions.get(dateKey)
        intentions.upsert(Intention(dateKey, text, current?.completed == true))
    }

    suspend fun setIntentionDone(dateKey: String, done: Boolean) = withContext(Dispatchers.IO) {
        intentions.get(dateKey)?.let { intentions.upsert(it.copy(completed = done)) }
    }

    @Volatile
    private var cachedQuotes: List<Quote>? = null

    /**
     * Quote of the day in the user's tone. Serves at most 3/day and stays
     * silent during quiet hours (then only the intention shows).
     */
    suspend fun dailyMessage(
        date: LocalDate = LocalDate.now(),
        hour: Int = java.time.LocalTime.now().hour,
    ): DailyMessage = withContext(Dispatchers.IO) {
        val key = todayKey(date)
        val served = settings.nudgesServed(key)
        if (!NudgePolicy.shouldShow(served, hour)) {
            return@withContext DailyMessage(null, quietCapped = true)
        }
        val quotes = cachedQuotes ?: loadQuotes().also { cachedQuotes = it }
        val tone = settings.tone.first()
        val quote = QuotePicker.pick(quotes, tone, date.toEpochDay())
        if (quote != null) settings.recordNudge(key)
        DailyMessage(quote, quietCapped = false)
    }

    private fun loadQuotes(): List<Quote> {
        return try {
            context.assets.open("quotes.json").bufferedReader().use { QuotePicker.parse(it.readText()) }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
