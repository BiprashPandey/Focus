package com.example.focuslauncher.data.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CalEvent(
    val id: Long,
    val title: String,
    val start: Long,
    val end: Long,
)

/** Next events via CalendarContract. Empty when permission is missing. */
@Singleton
class CalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CALENDAR,
    ) == PackageManager.PERMISSION_GRANTED

    suspend fun nextEvents(limit: Int = 3): List<CalEvent> = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext emptyList()
        val now = System.currentTimeMillis()
        val end = now + 7L * 24 * 60 * 60 * 1000
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString()).appendPath(end.toString()).build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
        )
        val out = ArrayList<CalEvent>()
        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                CalendarContract.Instances.BEGIN + " ASC",
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val titleCol = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
                val beginCol = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endCol = cursor.getColumnIndex(CalendarContract.Instances.END)
                while (cursor.moveToNext() && out.size < limit) {
                    out.add(
                        CalEvent(
                            id = if (idCol >= 0) cursor.getLong(idCol) else -1,
                            title = if (titleCol >= 0) cursor.getString(titleCol) ?: "" else "",
                            start = if (beginCol >= 0) cursor.getLong(beginCol) else 0,
                            end = if (endCol >= 0) cursor.getLong(endCol) else 0,
                        ),
                    )
                }
            }
        } catch (_: SecurityException) {
            return@withContext emptyList()
        }
        out
    }
}
