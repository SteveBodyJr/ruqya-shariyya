package com.digitalbeavertz.ruqya

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.digitalbeavertz.ruqya.audio.Audio
import com.digitalbeavertz.ruqya.audio.AudioPack
import com.digitalbeavertz.ruqya.data.Content
import com.digitalbeavertz.ruqya.data.Lang
import com.digitalbeavertz.ruqya.data.Prefs
import com.digitalbeavertz.ruqya.notify.Reminders

/** Everything the app shares, created once. */
class AppContainer(context: Context) {
    val prefs = Prefs(context)

    /** About 120 KB of JSON, parsed on first use. */
    val content: Content by lazy {
        Content.parse(context.assets.open("ruqya.json").bufferedReader().use { it.readText() })
    }

    val audio: AudioPack by lazy {
        AudioPack(context, content.audioFiles) { prefs.audioBaseOverride ?: content.audioBase }
    }

    val player: Audio by lazy { Audio(context, audio) }
}

class RuqyaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Reminders.createChannel(this)
        Reminders.schedule(this)
    }

    companion object {
        /** Applies the app language (Android 13+ also shows it in the system's per-app language setting). */
        fun applyLanguage(lang: Lang) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang.tag))
        }
    }
}
