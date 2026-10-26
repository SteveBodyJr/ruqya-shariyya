package com.digitalbeavertz.ruqya.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.RuqyaApp
import com.digitalbeavertz.ruqya.data.ArabicFont
import com.digitalbeavertz.ruqya.data.Lang
import com.digitalbeavertz.ruqya.data.Prefs
import com.digitalbeavertz.ruqya.data.Routine
import com.digitalbeavertz.ruqya.data.ThemeMode
import com.digitalbeavertz.ruqya.data.Translation
import com.digitalbeavertz.ruqya.notify.Reminders
import com.digitalbeavertz.ruqya.ui.components.ArabicText
import com.digitalbeavertz.ruqya.ui.components.AyahArabic
import com.digitalbeavertz.ruqya.ui.components.Header
import com.digitalbeavertz.ruqya.ui.components.RCard
import com.digitalbeavertz.ruqya.ui.components.ScreenPadding
import com.digitalbeavertz.ruqya.ui.components.SectionTitle
import com.digitalbeavertz.ruqya.ui.components.content
import com.digitalbeavertz.ruqya.ui.components.hhmm
import com.digitalbeavertz.ruqya.ui.components.prefs
import com.digitalbeavertz.ruqya.ui.theme.RT

private val FONT_NAMES = mapOf(
    ArabicFont.MADINAH to R.string.font_madinah,
    ArabicFont.AMIRI to R.string.font_amiri,
    ArabicFont.SCHEHERAZADE to R.string.font_scheherazade,
    ArabicFont.NASKH to R.string.font_naskh,
)

@Composable
fun SettingsScreen(nav: NavHostController) {
    val c = RT.colors
    val p = prefs()
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<String?>(null) }
    var pendingReminder by remember { mutableStateOf<Routine?>(null) }
    val askNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingReminder?.let { if (granted) { p.setReminderOn(it, true); Reminders.schedule(context) } }
        pendingReminder = null
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 40.dp)) {
        item { Header(stringResource(R.string.settings), onBack = { nav.popBackStack() }) }

        item {
            SectionTitle(stringResource(R.string.reading))
            RCard(padding = 0.dp) {
                Row2(stringResource(R.string.language), if (p.lang == Lang.SW) "Kiswahili" else "English") { dialog = "language" }
                Divider()
                Row2(stringResource(R.string.translation), stringResource(translationLabel(p.translation))) { dialog = "translation" }
                Divider()
                SwitchRow(stringResource(R.string.transliteration), stringResource(R.string.transliteration_hint), p.transliteration) { p.transliteration = it }
                Divider()
                Row2(stringResource(R.string.arabic_font), stringResource(FONT_NAMES.getValue(p.arabicFont))) { dialog = "font" }
            }
        }

        item {
            Spacer(Modifier.height(10.dp))
            RCard {
                Text(stringResource(R.string.arabic_size), style = MaterialTheme.typography.titleSmall, color = c.ink)
                content().passages["ikhlas"]?.ayahs?.first()?.let { AyahArabic(it, Modifier.padding(vertical = 8.dp)) }
                Slider(
                    value = p.arabicSize.toFloat(), onValueChange = { p.arabicSize = it.toInt() },
                    valueRange = Prefs.MIN_ARABIC.toFloat()..Prefs.MAX_ARABIC.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = c.green, activeTrackColor = c.green, inactiveTrackColor = c.soft),
                )
            }
        }

        item {
            SectionTitle(stringResource(R.string.recitation))
            AudioPackCard(compact = false)
            RCard(padding = 0.dp) {
                SwitchRow(stringResource(R.string.auto_play), stringResource(R.string.auto_play_hint), p.autoPlay) { p.autoPlay = it }
            }
        }

        item {
            SectionTitle(stringResource(R.string.reminders))
            RCard(padding = 0.dp) {
                Routine.entries.forEachIndexed { i, r ->
                    if (i > 0) Divider()
                    val title = stringResource(
                        when (r) {
                            Routine.MORNING -> R.string.remind_morning_title
                            Routine.EVENING -> R.string.remind_evening_title
                            Routine.BEDTIME -> R.string.remind_bedtime_title
                        },
                    )
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier.weight(1f).clickable {
                                val m = p.reminderMinute(r)
                                TimePickerDialog(context, { _, h, min ->
                                    p.setReminderMinute(r, h * 60 + min)
                                    Reminders.schedule(context)
                                }, m / 60, m % 60, true).show()
                            }.padding(vertical = 8.dp),
                        ) {
                            Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
                            Text(stringResource(R.string.at_time, hhmm(p.reminderMinute(r))), style = MaterialTheme.typography.bodySmall, color = c.green)
                        }
                        Switch(
                            checked = p.reminderOn(r),
                            onCheckedChange = { on ->
                                if (on && Build.VERSION.SDK_INT >= 33) {
                                    pendingReminder = r
                                    askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    p.setReminderOn(r, on)
                                    Reminders.schedule(context)
                                }
                            },
                            colors = switchColors(),
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.display))
            RCard(padding = 0.dp) {
                Row2(stringResource(R.string.theme), stringResource(themeLabel(p.theme))) { dialog = "theme" }
                Divider()
                SwitchRow(stringResource(R.string.keep_screen_on), stringResource(R.string.keep_screen_on_hint), p.keepScreenOn) { p.keepScreenOn = it }
            }
            Spacer(Modifier.height(16.dp))
            RCard(padding = 0.dp) {
                Row2(stringResource(R.string.about_sources), stringResource(R.string.about_sources_short)) { nav.navigate("about") }
            }
        }
    }

    when (dialog) {
        "language" -> Choice(
            stringResource(R.string.language), listOf(Lang.SW to "Kiswahili", Lang.EN to "English"), p.lang,
            onDismiss = { dialog = null },
        ) {
            p.language = it
            RuqyaApp.applyLanguage(it)
        }
        "translation" -> Choice(
            stringResource(R.string.translation),
            Translation.entries.map { it to stringResource(translationLabel(it)) }, p.translation,
            onDismiss = { dialog = null },
        ) { p.translation = it }
        "theme" -> Choice(
            stringResource(R.string.theme), ThemeMode.entries.map { it to stringResource(themeLabel(it)) }, p.theme,
            onDismiss = { dialog = null },
        ) { p.theme = it }
        "font" -> FontDialog(p.arabicFont, onDismiss = { dialog = null }) { p.arabicFont = it }
    }
}

private fun translationLabel(t: Translation) = when (t) {
    Translation.AUTO -> R.string.translation_auto
    Translation.EN -> R.string.translation_en
    Translation.SW -> R.string.translation_sw
    Translation.BOTH -> R.string.translation_both
    Translation.OFF -> R.string.translation_off
}

private fun themeLabel(t: ThemeMode) = when (t) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

@Composable
private fun Divider() = HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = RT.colors.line)

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = RT.colors.onGreen, checkedTrackColor = RT.colors.green,
    uncheckedTrackColor = RT.colors.soft, uncheckedBorderColor = RT.colors.line,
)

@Composable
private fun Row2(title: String, value: String, onClick: () -> Unit) {
    val c = RT.colors
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
        Text(value, style = MaterialTheme.typography.bodySmall, color = c.green)
    }
}

@Composable
private fun SwitchRow(title: String, hint: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = RT.colors
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = c.muted)
        }
        Switch(checked = checked, onCheckedChange = onChange, colors = switchColors())
    }
}

@Composable
private fun <T> Choice(title: String, options: List<Pair<T, String>>, selected: T, onDismiss: () -> Unit, onPick: (T) -> Unit) {
    val c = RT.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onPick(value); onDismiss() }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = value == selected, onClick = { onPick(value); onDismiss() }, colors = RadioButtonDefaults.colors(selectedColor = c.green))
                        Text(label, style = MaterialTheme.typography.bodyLarge, color = c.ink)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = c.green) } },
    )
}

/** Each font previews the basmala in its own text (King Fahd Complex for Madinah, Tanzil for the rest). */
@Composable
private fun FontDialog(selected: ArabicFont, onDismiss: () -> Unit, onPick: (ArabicFont) -> Unit) {
    val c = RT.colors
    val content = content()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.arabic_font)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ArabicFont.entries.forEach { f ->
                    Column(Modifier.fillMaxWidth().clickable { onPick(f); onDismiss() }.padding(vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = f == selected, onClick = { onPick(f); onDismiss() }, colors = RadioButtonDefaults.colors(selectedColor = c.green))
                            Text(stringResource(FONT_NAMES.getValue(f)), style = MaterialTheme.typography.titleSmall, color = c.ink)
                        }
                        ArabicText(
                            if (f == ArabicFont.MADINAH) content.basmalaHafs else content.basmalaTanzil,
                            size = 26, font = f, color = c.green,
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = c.green) } },
    )
}
