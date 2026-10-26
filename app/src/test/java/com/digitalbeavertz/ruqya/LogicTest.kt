package com.digitalbeavertz.ruqya

import com.digitalbeavertz.ruqya.core.AUDHUBILLAH
import com.digitalbeavertz.ruqya.core.BISMILLAH
import com.digitalbeavertz.ruqya.core.DailyReminder
import com.digitalbeavertz.ruqya.core.nextReminder
import com.digitalbeavertz.ruqya.core.playlist
import com.digitalbeavertz.ruqya.core.streak
import com.digitalbeavertz.ruqya.data.Ayah
import com.digitalbeavertz.ruqya.data.Passage
import com.digitalbeavertz.ruqya.data.Txt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class LogicTest {
    private val today = LocalDate.of(2026, 10, 26)

    @Test fun streakCountsBackFromTodayOrYesterday() {
        assertEquals(0, streak(emptySet(), today))
        assertEquals(3, streak(setOf(today, today.minusDays(1), today.minusDays(2), today.minusDays(4)), today))
        // Not done yet today: yesterday's run still counts.
        assertEquals(2, streak(setOf(today.minusDays(1), today.minusDays(2)), today))
        assertEquals(0, streak(setOf(today.minusDays(2)), today))
    }

    @Test fun nextReminderIsTheSoonestLaterOne() {
        val zone = ZoneId.of("Africa/Dar_es_Salaam")
        val now = ZonedDateTime.of(2026, 10, 26, 12, 0, 0, 0, zone)
        val list = listOf(DailyReminder("morning", 6 * 60 + 30), DailyReminder("evening", 17 * 60 + 30))
        val (r, at) = nextReminder(list, now)!!
        assertEquals("evening", r.id)
        assertEquals(ZonedDateTime.of(2026, 10, 26, 17, 30, 0, 0, zone), at)
        // After the evening one, the next is tomorrow morning.
        val (r2, at2) = nextReminder(list, now.withHour(18))!!
        assertEquals("morning", r2.id)
        assertEquals(ZonedDateTime.of(2026, 10, 27, 6, 30, 0, 0, zone), at2)
        // Exactly at the time: it has rung, so the next one.
        assertEquals("morning", nextReminder(list, now.withHour(17).withMinute(30))!!.first.id)
        assertNull(nextReminder(emptyList(), now))
    }

    private fun ayah(s: Int, a: Int) = Ayah(s, a, "", "", "", "", "")
    private fun passage(vararg a: Ayah) = Passage("p", Txt("", ""), a.toList())

    @Test fun playlistAddsBasmalaAtSurahStartsAndRepeats() {
        val ikhlas = passage(ayah(112, 1), ayah(112, 2), ayah(112, 3), ayah(112, 4))
        assertEquals(
            listOf(AUDHUBILLAH, BISMILLAH, "112001", "112002", "112003", "112004", BISMILLAH, "112001", "112002", "112003", "112004"),
            playlist(listOf(ikhlas), 2, taawwudh = true),
        )
        // Al-Fatihah's basmala is its first verse; mid-surah passages have none.
        assertEquals(listOf("001001", "001002"), playlist(listOf(passage(ayah(1, 1), ayah(1, 2))), 1, false))
        assertEquals(listOf("002255"), playlist(listOf(passage(ayah(2, 255))), 1, false))
    }
}
