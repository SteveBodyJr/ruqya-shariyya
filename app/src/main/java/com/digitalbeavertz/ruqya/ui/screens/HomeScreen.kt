package com.digitalbeavertz.ruqya.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.RuqyaApp
import com.digitalbeavertz.ruqya.audio.AudioPack
import com.digitalbeavertz.ruqya.audio.DownloadState
import com.digitalbeavertz.ruqya.core.Dates
import com.digitalbeavertz.ruqya.core.streak
import com.digitalbeavertz.ruqya.data.Lang
import com.digitalbeavertz.ruqya.data.Routine
import com.digitalbeavertz.ruqya.data.Session
import com.digitalbeavertz.ruqya.data.Translation
import com.digitalbeavertz.ruqya.notify.Reminders
import com.digitalbeavertz.ruqya.ui.components.ArabicText
import com.digitalbeavertz.ruqya.ui.components.AyahArabic
import com.digitalbeavertz.ruqya.ui.components.BottomBarSpace
import com.digitalbeavertz.ruqya.ui.components.GreenButton
import com.digitalbeavertz.ruqya.ui.components.IconBadge
import com.digitalbeavertz.ruqya.ui.components.LocalContainer
import com.digitalbeavertz.ruqya.ui.components.QuietButton
import com.digitalbeavertz.ruqya.ui.components.RCard
import com.digitalbeavertz.ruqya.ui.components.ScreenPadding
import com.digitalbeavertz.ruqya.ui.components.SectionTitle
import com.digitalbeavertz.ruqya.ui.components.content
import com.digitalbeavertz.ruqya.ui.components.iconFor
import com.digitalbeavertz.ruqya.ui.components.lang
import com.digitalbeavertz.ruqya.ui.components.prefs
import com.digitalbeavertz.ruqya.ui.components.t
import com.digitalbeavertz.ruqya.ui.theme.RT
import java.time.LocalDate

/** First launch: pick the language, read what the app is (and is not). */
@Composable
fun WelcomeScreen() {
    val c = RT.colors
    val p = LocalContainer.current.prefs
    Column(
        Modifier.fillMaxSize().background(c.canvas).statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(36.dp))
        Box(Modifier.size(88.dp).clip(RoundedCornerShape(26.dp)).background(c.green), contentAlignment = Alignment.Center) {
            Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_mark), null, tint = c.onGreen, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(20.dp))
        ArabicText("الرُّقْيَةُ الشَّرْعِيَّةُ", size = 34, color = c.green, align = TextAlign.Center)
        Text("Ruqya Shariyya", style = MaterialTheme.typography.headlineLarge, color = c.ink)
        Spacer(Modifier.height(10.dp))
        Text(
            "Healing and protection from the Quran and the Sunnah\nUponyaji na kinga kutoka katika Qur'an na Sunnah",
            style = MaterialTheme.typography.bodyLarge, color = c.muted, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Text("Choose your language  /  Chagua lugha", style = MaterialTheme.typography.titleSmall, color = c.ink)
        Spacer(Modifier.height(12.dp))
        GreenButton("Kiswahili", Modifier.fillMaxWidth()) { choose(p, Lang.SW) }
        Spacer(Modifier.height(10.dp))
        QuietButton("English", Modifier.fillMaxWidth()) { choose(p, Lang.EN) }
        Spacer(Modifier.height(28.dp))
        Text(
            "Every verse is from the verified Mushaf text and every supplication is shown with its hadith reference. " +
                "Ruqya does not replace medical care.\n\n" +
                "Kila aya imetoka katika maandishi ya Msahafu yaliyothibitishwa na kila dua inaonyeshwa na rejea ya hadith yake. " +
                "Ruqyah haichukui nafasi ya matibabu.",
            style = MaterialTheme.typography.bodySmall, color = c.muted, textAlign = TextAlign.Center,
        )
    }
}

private fun choose(p: com.digitalbeavertz.ruqya.data.Prefs, l: Lang) {
    p.language = l
    RuqyaApp.applyLanguage(l)
}

/** First launch, second step: which meaning to show under the Arabic (changeable in Settings). */
@Composable
fun TranslationScreen() {
    val c = RT.colors
    val p = LocalContainer.current.prefs
    val content = content()
    val pick = { t: Translation ->
        p.translation = t
        p.translationChosen = true
    }
    // The person's language first.
    val options = if (p.lang == Lang.SW) {
        listOf(Translation.SW, Translation.EN, Translation.BOTH)
    } else {
        listOf(Translation.EN, Translation.SW, Translation.BOTH)
    }
    val sample = content.passages["ikhlas"]?.ayahs?.first()
    Column(
        Modifier.fillMaxSize().background(c.canvas).statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.translation_question), style = MaterialTheme.typography.headlineLarge, color = c.ink)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.translation_question_text), style = MaterialTheme.typography.bodyLarge, color = c.muted)
        Spacer(Modifier.height(20.dp))
        options.forEachIndexed { i, t ->
            RCard(onClick = { pick(t) }, fill = if (i == 0) c.greenSoft else c.card) {
                Text(
                    stringResource(
                        when (t) {
                            Translation.SW -> R.string.translation_sw
                            Translation.EN -> R.string.translation_en
                            else -> R.string.translation_both
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium, color = if (i == 0) c.onGreenSoft else c.ink,
                )
                if (sample != null && t != Translation.BOTH) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        sample.meaning(if (t == Translation.SW) Lang.SW else Lang.EN),
                        style = MaterialTheme.typography.bodyMedium, color = if (i == 0) c.onGreenSoft else c.muted,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.translation_change_later), style = MaterialTheme.typography.bodySmall, color = c.muted)
    }
}

@Composable
fun HomeScreen(nav: NavHostController) {
    val c = RT.colors
    val p = prefs()
    val content = content()
    val l = lang()
    val today = LocalDate.now()
    val days = remember(p.version.collectAsState().value) { p.activeDays }
    val inRow = streak(days, today)
    val routines = content.routines

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = BottomBarSpace),
    ) {
        item {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 12.dp, start = 4.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(Dates.hijri(today, l), style = MaterialTheme.typography.titleSmall, color = c.green)
                    Text(Dates.gregorian(today, l), style = MaterialTheme.typography.bodySmall, color = c.muted)
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, color = c.ink)
                }
                IconButton(onClick = { nav.navigate("settings") }) {
                    Icon(Icons.Rounded.Settings, stringResource(R.string.settings), tint = c.ink)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // The main action: the evil-eye self-ruqya from the user's guide.
        content.session("eye")?.let { eye ->
            item {
                RCard(fill = c.green, padding = 20.dp, onClick = { nav.navigate("run/eye") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Rounded.Visibility, 44.dp, fill = c.onGreen.copy(alpha = 0.16f), tint = c.onGreen)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(eye.title.t(), style = MaterialTheme.typography.titleLarge, color = c.onGreen)
                            Text(
                                pluralStringResource(R.plurals.steps, eye.steps.size, eye.steps.size),
                                style = MaterialTheme.typography.bodySmall, color = c.onGreen.copy(alpha = 0.8f),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(eye.subtitle.t(), style = MaterialTheme.typography.bodyMedium, color = c.onGreen.copy(alpha = 0.9f))
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).background(c.onGreen).padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = c.green, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.start_ruqya), style = MaterialTheme.typography.labelLarge, color = c.green)
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.Bottom) {
                SectionTitle(stringResource(R.string.today), Modifier.weight(1f))
                if (inRow > 0) {
                    Text(
                        pluralStringResource(R.plurals.days_in_a_row, inRow, inRow),
                        style = MaterialTheme.typography.labelMedium, color = c.green, modifier = Modifier.padding(bottom = 8.dp, end = 4.dp),
                    )
                }
            }
        }
        routines.forEach { r ->
            item(key = r.id) {
                RoutineRow(r, p.lastDone(r.id) == today, p.timesDone(r.id)) { nav.navigate("run/${r.id}") }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (p.reminders.isEmpty() && !p.dismissedReminderCard) {
            item { ReminderCard() }
        }

        item { AudioPackCard(compact = true) }

        content.passages["isra_82"]?.let { verse ->
            item {
                SectionTitle(stringResource(R.string.reminder_verse))
                RCard {
                    verse.ayahs.forEach { a ->
                        AyahArabic(a, size = (p.arabicSize * 0.9f).toInt())
                        Spacer(Modifier.height(8.dp))
                        Text(a.meaning(l), style = MaterialTheme.typography.bodyMedium, color = c.muted)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(verse.name.t() + " · " + verse.reference, style = MaterialTheme.typography.labelMedium, color = c.green)
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.explore))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExploreTile(stringResource(R.string.tab_names), stringResource(R.string.names_count), Modifier.weight(1f)) { nav.navigate("names") }
                ExploreTile(stringResource(R.string.tab_learn), stringResource(R.string.learn_short), Modifier.weight(1f)) { nav.navigate("learn") }
            }
        }
    }
}

@Composable
private fun ExploreTile(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    val c = RT.colors
    RCard(modifier, onClick = onClick) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.muted)
        Spacer(Modifier.height(8.dp))
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = c.green, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun RoutineRow(r: Session, done: Boolean, times: Int, onClick: () -> Unit) {
    val c = RT.colors
    RCard(onClick = onClick, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(iconFor(r.icon), 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(r.title.t(), style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(r.subtitle.t(), style = MaterialTheme.typography.bodySmall, color = c.muted)
                if (times > 0) {
                    Text(pluralStringResource(R.plurals.times_done, times, times), style = MaterialTheme.typography.labelMedium, color = c.green)
                }
            }
            if (done) {
                Icon(Icons.Rounded.CheckCircle, stringResource(R.string.done_today), tint = c.green, modifier = Modifier.size(26.dp))
            } else {
                Box(Modifier.size(24.dp).clip(CircleShape).background(c.soft))
            }
        }
    }
}

/** Offers the three daily reminders once; asks for the notification permission when switched on. */
@Composable
private fun ReminderCard() {
    val c = RT.colors
    val context = LocalContext.current
    val p = LocalContainer.current.prefs
    val enableAll = {
        Routine.entries.forEach { p.setReminderOn(it, true) }
        Reminders.schedule(context)
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) enableAll()
    }
    RCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.NotificationsActive, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.reminders_card_title), style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(stringResource(R.string.reminders_card_text), style = MaterialTheme.typography.bodySmall, color = c.muted)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GreenButton(stringResource(R.string.turn_on), Modifier.weight(1f)) {
                p.askedNotificationPermission = true
                if (Build.VERSION.SDK_INT >= 33) ask.launch(Manifest.permission.POST_NOTIFICATIONS) else enableAll()
            }
            QuietButton(stringResource(R.string.not_now), Modifier.weight(1f)) { p.dismissedReminderCard = true }
        }
    }
    Spacer(Modifier.height(8.dp))
}

/** Download / progress / ready state of the recitation pack. On Home it hides once the pack is ready. */
@Composable
fun AudioPackCard(compact: Boolean) {
    val c = RT.colors
    val context = LocalContext.current
    val container = LocalContainer.current
    val pack = container.audio
    val state by remember { AudioPack.progress(context) }.collectAsState(initial = null)
    val running = state as? DownloadState.Running
    // Re-read the files whenever the download moves on, or after deleting.
    var deleted by remember { mutableIntStateOf(0) }
    val complete = remember(running?.done, state, deleted) { pack.complete }
    if (compact && complete) return
    RCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.Headphones, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.audio_title), style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(
                    when {
                        complete -> stringResource(R.string.audio_ready, pack.bytesOnDisk / 1_000_000.0)
                        running != null && running.waiting -> stringResource(R.string.audio_waiting)
                        running != null -> stringResource(R.string.audio_progress, running.done, running.total)
                        else -> stringResource(R.string.audio_text)
                    },
                    style = MaterialTheme.typography.bodySmall, color = c.muted,
                )
            }
        }
        if (running != null && running.total > 0) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { running.done / running.total.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                color = c.green, trackColor = c.soft, strokeCap = StrokeCap.Round, drawStopIndicator = {},
            )
        }
        (state as? DownloadState.Failed)?.let {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.audio_failed), style = MaterialTheme.typography.bodySmall, color = c.warn)
        }
        if (!complete && running == null) {
            Spacer(Modifier.height(12.dp))
            GreenButton(stringResource(R.string.audio_download), Modifier.fillMaxWidth(), icon = Icons.Rounded.Download) {
                AudioPack.start(context)
            }
        } else if (running != null) {
            Spacer(Modifier.height(10.dp))
            QuietButton(stringResource(R.string.cancel), Modifier.fillMaxWidth()) { AudioPack.cancel(context) }
        } else if (!compact) {
            Spacer(Modifier.height(12.dp))
            QuietButton(stringResource(R.string.audio_delete), Modifier.fillMaxWidth()) {
                container.player.stop()
                pack.delete()
                deleted++
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

/** Ruqya tab: the sessions, then the daily routines. */
@Composable
fun SessionsScreen(nav: NavHostController) {
    val c = RT.colors
    val content = content()
    val p = prefs()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = BottomBarSpace),
    ) {
        item {
            com.digitalbeavertz.ruqya.ui.components.Header(stringResource(R.string.tab_ruqya), subtitle = stringResource(R.string.ruqya_subtitle))
            Spacer(Modifier.height(8.dp))
        }
        content.sessions.forEach { s -> item(key = s.id) { SessionCard(s, p.lastDone(s.id), p.timesDone(s.id)) { nav.navigate("run/${s.id}") } } }
        item { SectionTitle(stringResource(R.string.daily_protection)) }
        content.routines.forEach { s -> item(key = s.id) { SessionCard(s, p.lastDone(s.id), p.timesDone(s.id)) { nav.navigate("run/${s.id}") } } }
    }
}

@Composable
private fun SessionCard(s: Session, lastDone: LocalDate?, times: Int, onClick: () -> Unit) {
    val c = RT.colors
    RCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(iconFor(s.icon))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(s.title.t(), style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(s.subtitle.t(), style = MaterialTheme.typography.bodySmall, color = c.muted)
                Spacer(Modifier.height(6.dp))
                Text(
                    pluralStringResource(R.plurals.steps, s.steps.size, s.steps.size) +
                        (if (times > 0) "  ·  " + pluralStringResource(R.plurals.times_done, times, times) else ""),
                    style = MaterialTheme.typography.labelMedium, color = c.green,
                )
                lastDone?.let {
                    Text(stringResource(R.string.last_done, it.toString()), style = MaterialTheme.typography.bodySmall, color = c.muted)
                }
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = c.muted, modifier = Modifier.size(20.dp))
        }
    }
    Spacer(Modifier.height(10.dp))
}
