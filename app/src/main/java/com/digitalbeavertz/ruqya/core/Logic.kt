package com.digitalbeavertz.ruqya.core

import com.digitalbeavertz.ruqya.data.Passage
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/** Days in a row with at least one completed routine or session, counting today or (if not yet) yesterday. */
fun streak(days: Set<LocalDate>, today: LocalDate): Int {
    var d = if (today in days) today else today.minusDays(1)
    var n = 0
    while (d in days) {
        n++
        d = d.minusDays(1)
    }
    return n
}

/** A daily reminder at [minute] minutes after midnight. */
data class DailyReminder(val id: String, val minute: Int)

/** The next time any of [reminders] rings after [now] (strictly later), or null when there are none. */
fun nextReminder(reminders: List<DailyReminder>, now: ZonedDateTime): Pair<DailyReminder, ZonedDateTime>? =
    reminders.map { r ->
        val today = now.with(LocalTime.of(r.minute / 60, r.minute % 60)).withSecond(0).withNano(0)
        r to if (today.isAfter(now)) today else today.plusDays(1)
    }.minByOrNull { it.second.toInstant() }

/**
 * The audio files (everyayah.com names, without .mp3) for reciting [passages] [count] times.
 * The ta'awwudh comes once at the start when [taawwudh]; each surah read from its first verse
 * starts with the basmala (not Al-Fatihah, where the basmala is verse 1, nor At-Tawbah).
 */
fun playlist(passages: List<Passage>, count: Int, taawwudh: Boolean): List<String> = buildList {
    if (taawwudh) add(AUDHUBILLAH)
    repeat(count.coerceAtLeast(1)) {
        passages.forEach { p ->
            p.ayahs.forEach { a ->
                if (a.startsSura) add(BISMILLAH)
                add(a.audio)
            }
        }
    }
}

const val AUDHUBILLAH = "audhubillah"
const val BISMILLAH = "bismillah"
