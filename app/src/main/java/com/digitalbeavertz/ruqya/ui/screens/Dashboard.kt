package com.digitalbeavertz.ruqya.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.core.DailyReminder
import com.digitalbeavertz.ruqya.core.Dates
import com.digitalbeavertz.ruqya.core.nextReminder
import com.digitalbeavertz.ruqya.core.streak
import com.digitalbeavertz.ruqya.data.Routine
import com.digitalbeavertz.ruqya.data.Session
import com.digitalbeavertz.ruqya.notify.Reminders
import com.digitalbeavertz.ruqya.ui.components.ArchHeader
import com.digitalbeavertz.ruqya.ui.components.AyahArabic
import com.digitalbeavertz.ruqya.ui.components.BottomBarSpace
import com.digitalbeavertz.ruqya.ui.components.OutlinedTile
import com.digitalbeavertz.ruqya.ui.components.RCard
import com.digitalbeavertz.ruqya.ui.components.ScreenPadding
import com.digitalbeavertz.ruqya.ui.components.content
import com.digitalbeavertz.ruqya.ui.components.hhmm
import com.digitalbeavertz.ruqya.ui.components.iconFor
import com.digitalbeavertz.ruqya.ui.components.lang
import com.digitalbeavertz.ruqya.ui.components.prefs
import com.digitalbeavertz.ruqya.ui.components.t
import com.digitalbeavertz.ruqya.ui.theme.RT
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(nav: NavHostController) {
    val c = RT.colors
    val p = prefs()
    val content = content()
    val l = lang()
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000 - System.currentTimeMillis() % 1000)
        }
    }
    val today = now.toLocalDate()
    val inRow = streak(p.activeDays, today)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = BottomBarSpace)) {
        item { HomeHeader(nav, now) }

        item {
            Column(Modifier.padding(horizontal = ScreenPadding).padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val eye = content.session("eye")
                TileRow(
                    Triple(eye?.title?.t() ?: "", Icons.Rounded.Visibility) { nav.navigate("run/eye") },
                    Triple(stringResource(R.string.tab_ruqya), Icons.AutoMirrored.Rounded.MenuBook) { nav.goTabFromHome("ruqya") },
                )
                TileRow(
                    Triple(stringResource(R.string.tile_morning), Icons.Rounded.WbSunny) { nav.navigate("run/morning") },
                    Triple(stringResource(R.string.tile_evening), Icons.Rounded.WbTwilight) { nav.navigate("run/evening") },
                )
                TileRow(
                    Triple(stringResource(R.string.tab_names), Icons.Rounded.AutoAwesome) { nav.goTabFromHome("names") },
                    Triple(stringResource(R.string.tab_duas), Icons.Rounded.VolunteerActivism) { nav.goTabFromHome("duas") },
                )
            }
        }

        item {
            Row(Modifier.padding(start = ScreenPadding + 4.dp, end = ScreenPadding, top = 26.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.schedule_today), style = MaterialTheme.typography.titleMedium, color = c.ink)
                    if (inRow > 0) {
                        Text(pluralStringResource(R.plurals.days_in_a_row, inRow, inRow), style = MaterialTheme.typography.labelMedium, color = c.green)
                    }
                }
                Row(
                    Modifier.clip(RoundedCornerShape(50)).clickable { nav.navigate("customize/morning") }.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Tune, null, tint = c.green, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.choose_adhkar), style = MaterialTheme.typography.labelLarge, color = c.green)
                }
            }
        }
        item { ScheduleList(content.routines, nav, today) }

        item {
            Column(Modifier.padding(horizontal = ScreenPadding).padding(top = 18.dp)) { AudioPackCard(compact = true) }
        }

        item {
            Row(Modifier.padding(start = ScreenPadding + 4.dp, end = ScreenPadding, top = 18.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.guidance), style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.weight(1f))
                Row(
                    Modifier.clip(RoundedCornerShape(50)).clickable { nav.goTabFromHome("learn") }.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.see_all), style = MaterialTheme.typography.labelLarge, color = c.green)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = c.green, modifier = Modifier.size(16.dp))
                }
            }
            GuidanceGrid(nav)
        }

        content.passages["isra_82"]?.let { verse ->
            item {
                Column(Modifier.padding(horizontal = ScreenPadding).padding(top = 18.dp)) {
                    RCard(fill = c.greenSoft) {
                        verse.ayahs.forEach { a ->
                            AyahArabic(a, size = (p.arabicSize * 0.85f).toInt())
                            Spacer(Modifier.height(8.dp))
                            Text(a.meaning(l), style = MaterialTheme.typography.bodyMedium, color = c.onGreenSoft)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(verse.name.t() + " · " + verse.reference, style = MaterialTheme.typography.labelMedium, color = c.green)
                    }
                }
            }
        }
    }
}

/** Green arch header: today's dates, a live clock and the time left until the next routine. */
@Composable
private fun HomeHeader(nav: NavHostController, now: LocalDateTime) {
    val c = RT.colors
    val p = prefs()
    val content = content()
    val l = lang()
    val zone = ZoneId.systemDefault()
    val next = nextReminder(Routine.entries.map { DailyReminder(it.id, p.reminderMinute(it)) }, now.atZone(zone))
    ArchHeader(height = 300.dp) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium, color = Color.White,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { nav.navigate("settings") }) {
                Icon(Icons.Rounded.Settings, stringResource(R.string.settings), tint = Color.White)
            }
        }
        Column(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(Dates.hijri(now.toLocalDate(), l), style = MaterialTheme.typography.titleSmall, color = c.gold)
            Text(
                now.format(DateTimeFormatter.ofPattern("HH:mm")), color = Color.White,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 46.sp, lineHeight = 52.sp, fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum"),
            )
            if (next != null) {
                val routine = content.session(next.first.id)
                val left = Duration.between(now.atZone(zone), next.second).coerceAtLeast(Duration.ZERO)
                Text(
                    stringResource(
                        R.string.next_in, routine?.title?.t().orEmpty(),
                        "%02d:%02d:%02d".format(left.toHours(), left.toMinutesPart(), left.toSecondsPart()),
                    ),
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center,
                )
            }
            Text(Dates.gregorian(now.toLocalDate(), l), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f))
        }
    }
}

@Composable
private fun TileRow(a: Triple<String, ImageVector, () -> Unit>, b: Triple<String, ImageVector, () -> Unit>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTile(a.first, a.second, Modifier.weight(1f), a.third)
        OutlinedTile(b.first, b.second, Modifier.weight(1f), b.third)
    }
}

/** Today's routines like a prayer-times list: time, done tick and the reminder switch. */
@Composable
private fun ScheduleList(routines: List<Session>, nav: NavHostController, today: java.time.LocalDate) {
    val c = RT.colors
    val p = prefs()
    val context = LocalContext.current
    var pending by remember { mutableStateOf<Routine?>(null) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pending?.let { if (granted) { p.setReminderOn(it, true); Reminders.schedule(context) } }
        pending = null
    }
    Column(Modifier.padding(horizontal = ScreenPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        routines.forEach { r ->
            val routine = Routine.of(r.id) ?: return@forEach
            val done = p.lastDone(r.id) == today
            val times = p.timesDone(r.id)
            val chosen = r.chosen(p.chosenSteps(r.id)).size
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (done) c.greenSoft else c.soft)
                    .clickable { nav.navigate("run/${r.id}") }.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(iconFor(r.icon), null, tint = c.green, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(r.title.t(), style = MaterialTheme.typography.titleSmall, color = c.ink)
                    Text(
                        hhmm(p.reminderMinute(routine)) + "  ·  " + pluralStringResource(R.plurals.adhkar_count, chosen, chosen) +
                            (if (times > 0) "  ·  " + pluralStringResource(R.plurals.times_done, times, times) else ""),
                        style = MaterialTheme.typography.bodySmall, color = c.muted,
                    )
                }
                if (done) {
                    Icon(Icons.Rounded.CheckCircle, stringResource(R.string.done_today), tint = c.green, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Switch(
                    checked = p.reminderOn(routine),
                    onCheckedChange = { on ->
                        if (on && Build.VERSION.SDK_INT >= 33) {
                            pending = routine
                            ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            p.setReminderOn(routine, on)
                            Reminders.schedule(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = c.onGreen, checkedTrackColor = c.green,
                        uncheckedTrackColor = c.card, uncheckedBorderColor = c.line, uncheckedThumbColor = c.muted,
                    ),
                )
            }
        }
    }
}

/** The first Learn articles as flat pastel tiles, like the reference's guidance grid. */
@Composable
private fun GuidanceGrid(nav: NavHostController) {
    val c = RT.colors
    val articles = content().articles.take(4)
    val fills = if (c.dark) List(4) { c.soft } else listOf(Color(0xFFE3F3E8), Color(0xFFE5EFFA), Color(0xFFFFF3D8), Color(0xFFFBE8E6))
    val tints = if (c.dark) List(4) { c.green } else listOf(Color(0xFF1E7A3C), Color(0xFF2F6DB5), Color(0xFFB07A0E), Color(0xFFB4473B))
    Column(Modifier.padding(horizontal = ScreenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        articles.chunked(2).forEachIndexed { row, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEachIndexed { i, a ->
                    val k = row * 2 + i
                    Column(
                        Modifier.weight(1f).height(108.dp).clip(RoundedCornerShape(16.dp)).background(fills[k])
                            .clickable { nav.navigate("article/${a.id}") }.padding(14.dp),
                    ) {
                        Icon(iconFor(a.icon), null, tint = tints[k], modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(a.title.t(), style = MaterialTheme.typography.titleSmall, color = if (c.dark) c.ink else tints[k], maxLines = 2)
                    }
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

/** Opens a tab from Home the same way the bottom bar does. */
private fun NavHostController.goTabFromHome(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
