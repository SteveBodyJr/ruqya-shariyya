package com.digitalbeavertz.ruqya.core

import com.digitalbeavertz.ruqya.data.Lang
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale

/** Today's dates for the Home header: "4 Jumada al-Ula 1448" and "Monday, 26 October". */
object Dates {
    private val monthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Akhir", "Jumada al-Ula", "Jumada al-Akhirah",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhul-Qa'dah", "Dhul-Hijjah",
    )
    private val monthsSw = listOf(
        "Muharram", "Safar", "Rabiul Awwal", "Rabiul Akhir", "Jumadal Ula", "Jumadal Akhira",
        "Rajab", "Shaaban", "Ramadhani", "Shawwal", "Dhul Qaada", "Dhul Hijja",
    )
    private val daysSw = listOf("Jumatatu", "Jumanne", "Jumatano", "Alhamisi", "Ijumaa", "Jumamosi", "Jumapili")
    private val monthsGregSw = listOf(
        "Januari", "Februari", "Machi", "Aprili", "Mei", "Juni", "Julai", "Agosti", "Septemba", "Oktoba", "Novemba", "Desemba",
    )

    /** Umm al-Qura calendar (java.time); local moon sighting can differ by a day. */
    fun hijri(date: LocalDate, lang: Lang): String = runCatching {
        val h = HijrahDate.from(date)
        val m = h.get(ChronoField.MONTH_OF_YEAR) - 1
        val name = (if (lang == Lang.SW) monthsSw else monthsEn)[m]
        "${h.get(ChronoField.DAY_OF_MONTH)} $name ${h.get(ChronoField.YEAR)}"
    }.getOrDefault("")

    fun gregorian(date: LocalDate, lang: Lang): String = if (lang == Lang.SW) {
        "${daysSw[date.dayOfWeek.value - 1]}, ${date.dayOfMonth} ${monthsGregSw[date.monthValue - 1]}"
    } else {
        "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}, ${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"
    }
}
