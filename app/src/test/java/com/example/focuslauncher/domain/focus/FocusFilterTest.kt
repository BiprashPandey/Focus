package com.example.focuslauncher.domain.focus

import com.example.focuslauncher.domain.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class FocusFilterTest {

    private fun app(pkg: String) = AppInfo(pkg, pkg, "$pkg/.Main", "0")

    @Test
    fun visible_keepsAllowedAndProtected() {
        val apps = listOf(app("work"), app("game"), app("phone"))
        val out = FocusFilter.visible(apps, setOf("work"), setOf("phone"))
        assertEquals(listOf(app("work"), app("phone")), out)
    }

    @Test
    fun blocked_excludesAllowedAndProtected() {
        val apps = listOf(app("work"), app("game"), app("phone"))
        assertEquals(
            setOf("game"),
            FocusFilter.blockedPackages(apps, setOf("work"), setOf("phone")),
        )
    }

    @Test
    fun parseAllowed_roundTrips() {
        val pkgs = setOf("com.a", "com.b")
        assertEquals(pkgs, FocusFilter.parseAllowed(FocusFilter.encodeAllowed(pkgs)))
        assertEquals(setOf("com.a"), FocusFilter.parseAllowed("""[ "com.a" ]"""))
        assertEquals(emptySet<String>(), FocusFilter.parseAllowed("garbage"))
        assertEquals(emptySet<String>(), FocusFilter.parseAllowed(""))
    }

    @Test
    fun schedule_activeInsideWindow() {
        val s = FocusSchedule(setOf(1, 2, 3, 4, 5), 540, 720)
        val monday10 = LocalDateTime.of(2026, 10, 12, 10, 0) // a Monday
        assertEquals(DayOfWeek.MONDAY, monday10.dayOfWeek)
        assertEquals(true, s.isActive(monday10))
        assertEquals(false, s.isActive(monday10.withHour(8)))
        assertEquals(false, s.isActive(monday10.withHour(13).withDayOfMonth(17))) // Saturday
    }

    @Test
    fun schedule_overnightSpansMidnight() {
        val s = FocusSchedule(setOf(5), 22 * 60, 6 * 60) // Friday night
        val fri23 = LocalDateTime.of(2026, 10, 16, 23, 0)
        val sat02 = LocalDateTime.of(2026, 10, 17, 2, 0)
        val sat10 = LocalDateTime.of(2026, 10, 17, 10, 0)
        assertEquals(true, s.isActive(fri23))
        assertEquals(true, s.isActive(sat02))
        assertEquals(false, s.isActive(sat10))
    }

    @Test
    fun schedule_emptyDaysNeverActive() {
        val s = FocusSchedule(emptySet(), 0, 1439)
        assertEquals(false, s.isActive(LocalDateTime.of(2026, 10, 12, 10, 0)))
    }

    @Test
    fun schedule_jsonRoundTrips() {
        val s = FocusSchedule(setOf(1, 5), 540, 720)
        assertEquals(s, FocusSchedule.fromJson(s.toJson()))
        assertEquals(null, FocusSchedule.fromJson(null))
        assertEquals(null, FocusSchedule.fromJson("{oops"))
    }

    @Test
    fun schedule_timeFormatAndParse() {
        assertEquals("09:30", FocusSchedule.formatTime(570))
        assertEquals(570, FocusSchedule.parseTime("09:30"))
        assertEquals(null, FocusSchedule.parseTime("25:00"))
        assertEquals(null, FocusSchedule.parseTime("nine"))
    }
}
