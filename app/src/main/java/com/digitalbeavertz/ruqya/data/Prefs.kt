package com.digitalbeavertz.ruqya.data

import android.content.Context
import androidx.core.content.edit
import com.digitalbeavertz.ruqya.core.DailyReminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.time.LocalDate

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Which meaning to show under the Arabic. AUTO follows the app language. */
enum class Translation { AUTO, EN, SW, BOTH, OFF }

/** Arabic fonts. Madinah (KFGQPC Uthmanic Hafs) shows the King Fahd Complex text; the others show Tanzil's. */
enum class ArabicFont { MADINAH, AMIRI, SCHEHERAZADE, NASKH }

/** The three daily routines that can remind. */
enum class Routine(val id: String, val defaultMinute: Int) {
    MORNING("morning", 6 * 60 + 30),
    EVENING("evening", 17 * 60 + 30),
    BEDTIME("bedtime", 21 * 60 + 30),
    ;

    companion object {
        fun of(id: String) = entries.firstOrNull { it.id == id }
    }
}

/** Settings and progress. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("ruqya", Context.MODE_PRIVATE)

    /** Changes whenever a setting changes, so Compose screens can react. */
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()
    private fun bump() { _version.value++ }

    /** Null until the person picks a language on the welcome screen. */
    var language: Lang?
        get() = sp.getString("language", null)?.let { t -> Lang.entries.firstOrNull { it.tag == t } }
        set(v) { sp.edit { putString("language", v?.tag) }; bump() }

    val lang: Lang get() = language ?: Lang.EN

    var translation: Translation
        get() = enumOr("translation", Translation.AUTO)
        set(v) { sp.edit { putString("translation", v.name) }; bump() }

    /** False until the person picks a translation on the second welcome screen. */
    var translationChosen: Boolean
        get() = sp.getBoolean("translationChosen", false)
        set(v) { sp.edit { putBoolean("translationChosen", v) }; bump() }

    /** The meanings to show, in order. */
    val meanings: List<Lang>
        get() = when (translation) {
            Translation.AUTO -> listOf(lang)
            Translation.EN -> listOf(Lang.EN)
            Translation.SW -> listOf(Lang.SW)
            Translation.BOTH -> listOf(lang, if (lang == Lang.EN) Lang.SW else Lang.EN)
            Translation.OFF -> emptyList()
        }

    var transliteration: Boolean
        get() = sp.getBoolean("transliteration", true)
        set(v) { sp.edit { putBoolean("transliteration", v) }; bump() }

    var arabicFont: ArabicFont
        get() = enumOr("arabicFont", ArabicFont.MADINAH)
        set(v) { sp.edit { putString("arabicFont", v.name) }; bump() }

    /** Arabic text size in sp. */
    var arabicSize: Int
        get() = sp.getInt("arabicSize", 28)
        set(v) { sp.edit { putInt("arabicSize", v.coerceIn(MIN_ARABIC, MAX_ARABIC)) }; bump() }

    var theme: ThemeMode
        get() = enumOr("theme", ThemeMode.SYSTEM)
        set(v) { sp.edit { putString("theme", v.name) }; bump() }

    var keepScreenOn: Boolean
        get() = sp.getBoolean("keepScreenOn", true)
        set(v) { sp.edit { putBoolean("keepScreenOn", v) }; bump() }

    /** Play the recitation automatically when a Quran step opens. */
    var autoPlay: Boolean
        get() = sp.getBoolean("autoPlay", false)
        set(v) { sp.edit { putBoolean("autoPlay", v) }; bump() }

    // ---- Reminders
    fun reminderOn(r: Routine) = sp.getBoolean("remind_${r.id}", false)
    fun setReminderOn(r: Routine, on: Boolean) { sp.edit { putBoolean("remind_${r.id}", on) }; bump() }
    fun reminderMinute(r: Routine) = sp.getInt("remindAt_${r.id}", r.defaultMinute)
    fun setReminderMinute(r: Routine, minute: Int) { sp.edit { putInt("remindAt_${r.id}", minute) }; bump() }
    val reminders: List<DailyReminder>
        get() = Routine.entries.filter { reminderOn(it) }.map { DailyReminder(it.id, reminderMinute(it)) }

    var askedNotificationPermission: Boolean
        get() = sp.getBoolean("askedNotif", false)
        set(v) { sp.edit { putBoolean("askedNotif", v) } }

    var dismissedReminderCard: Boolean
        get() = sp.getBoolean("dismissedReminderCard", false)
        set(v) { sp.edit { putBoolean("dismissedReminderCard", v) }; bump() }

    // ---- Progress
    /** The last day each routine or session was finished (ISO date). */
    fun lastDone(id: String): LocalDate? = sp.getString("done_$id", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /** Days with anything finished, newest last (kept to about a year). */
    val activeDays: Set<LocalDate>
        get() = runCatching {
            val a = JSONArray(sp.getString("days", "[]"))
            (0 until a.length()).map { LocalDate.parse(a.getString(it)) }.toSet()
        }.getOrDefault(emptySet())

    fun markDone(id: String, today: LocalDate = LocalDate.now()) {
        val days = (activeDays + today).sorted().takeLast(400)
        sp.edit {
            putString("done_$id", today.toString())
            putInt("times_$id", sp.getInt("times_$id", 0) + 1)
            putString("days", JSONArray(days.map { it.toString() }).toString())
            putInt("finished", sp.getInt("finished", 0) + 1)
        }
        bump()
    }

    val finishedCount: Int get() = sp.getInt("finished", 0)

    /** How many times a session or routine has been read to the end. */
    fun timesDone(id: String) = sp.getInt("times_$id", 0)

    // ---- Audio
    /** Debug builds only: a local server for testing the audio download on the emulator. */
    var audioBaseOverride: String?
        get() = sp.getString("audioBase", null)
        set(v) { sp.edit { putString("audioBase", v) } }

    private inline fun <reified E : Enum<E>> enumOr(key: String, default: E): E =
        runCatching { enumValueOf<E>(sp.getString(key, default.name)!!) }.getOrDefault(default)

    companion object {
        const val MIN_ARABIC = 20
        const val MAX_ARABIC = 44
    }
}
