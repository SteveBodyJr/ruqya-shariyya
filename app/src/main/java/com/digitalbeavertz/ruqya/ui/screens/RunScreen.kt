package com.digitalbeavertz.ruqya.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.audio.AudioPack
import com.digitalbeavertz.ruqya.core.playlist
import com.digitalbeavertz.ruqya.data.Passage
import com.digitalbeavertz.ruqya.data.Session
import com.digitalbeavertz.ruqya.data.Step
import com.digitalbeavertz.ruqya.ui.components.ArabicText
import com.digitalbeavertz.ruqya.ui.components.AyahArabic
import com.digitalbeavertz.ruqya.ui.components.Basmala
import com.digitalbeavertz.ruqya.ui.components.Chip
import com.digitalbeavertz.ruqya.ui.components.GreenButton
import com.digitalbeavertz.ruqya.ui.components.IconBadge
import com.digitalbeavertz.ruqya.ui.components.LocalContainer
import com.digitalbeavertz.ruqya.ui.components.Meanings
import com.digitalbeavertz.ruqya.ui.components.Note
import com.digitalbeavertz.ruqya.ui.components.Sources
import com.digitalbeavertz.ruqya.ui.components.content
import com.digitalbeavertz.ruqya.ui.components.lang
import com.digitalbeavertz.ruqya.ui.components.prefs
import com.digitalbeavertz.ruqya.ui.components.t
import com.digitalbeavertz.ruqya.ui.theme.RT
import kotlinx.coroutines.launch

/** A session or routine, one step per page, with a counter, recitation and a finish page. */
@Composable
fun RunScreen(nav: NavHostController, id: String) {
    val session = content().session(id)
    if (session == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    val c = RT.colors
    val container = LocalContainer.current
    val p = prefs()
    val context = LocalContext.current
    val player = container.player
    val play by player.state.collectAsState()
    val steps = session.steps
    val pager = rememberPagerState { steps.size + 1 }
    val scope = rememberCoroutineScope()
    val counts = remember { mutableStateMapOf<Int, Int>() }
    var askDownload by remember { mutableStateOf(false) }

    val view = LocalView.current
    DisposableEffect(p.keepScreenOn) {
        view.keepScreenOn = p.keepScreenOn
        onDispose { view.keepScreenOn = false }
    }
    DisposableEffect(Unit) { onDispose { player.stop() } }

    // The ta'awwudh is recited once, before the first recitation of the session.
    val firstRecital = steps.indexOfFirst { it.passages().isNotEmpty() }
    fun files(i: Int) = steps.getOrNull(i)?.let { playlist(it.passages(), it.count, i == firstRecital) }.orEmpty()
    fun keyOf(i: Int) = "$id/$i"
    fun start(i: Int) {
        val f = files(i)
        if (f.isEmpty()) return
        if (!f.all(container.audio::has)) {
            askDownload = true
            return
        }
        player.play(keyOf(i), steps[i].title[p.lang], f)
    }

    val page = pager.currentPage
    LaunchedEffect(page) {
        if (play.key != null && play.key != keyOf(page)) player.stop()
        if (p.autoPlay && page < steps.size && files(page).all(container.audio::has)) start(page)
        if (page == steps.size) container.prefs.markDone(session.id)
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        // Top: close, title, progress
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 4.dp, end = 16.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Rounded.Close, stringResource(R.string.close), tint = c.ink) }
            Column(Modifier.weight(1f)) {
                Text(session.title.t(), style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(
                    if (page < steps.size) stringResource(R.string.step_of, page + 1, steps.size) else stringResource(R.string.finished),
                    style = MaterialTheme.typography.bodySmall, color = c.muted,
                )
            }
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).height(4.dp).clip(RoundedCornerShape(50)).background(c.soft)) {
            Box(Modifier.fillMaxWidth(((page + 1f) / (steps.size + 1f)).coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(c.green))
        }

        HorizontalPager(pager, Modifier.weight(1f), beyondViewportPageCount = 1) { i ->
            if (i < steps.size) StepPage(steps[i]) else FinishPage(session) { nav.popBackStack() }
        }

        if (page < steps.size) {
            val step = steps[page]
            val done = counts[page] ?: 0
            val haptics = LocalHapticFeedback.current
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.previous), enabled = page > 0) {
                    scope.launch { pager.animateScrollToPage(page - 1) }
                }
                Spacer(Modifier.width(10.dp))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    if (files(page).isNotEmpty()) {
                        val playing = play.key == keyOf(page) && play.playing
                        RoundButton(
                            if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            stringResource(if (playing) R.string.pause else R.string.listen),
                            filled = true,
                        ) {
                            when {
                                playing -> player.pause()
                                play.key == keyOf(page) -> player.resume()
                                else -> start(page)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    // Every step shows its count, even a single reading, so each one can be ticked off.
                    Counter(done, step.count) {
                        if (done < step.count) {
                            counts[page] = done + 1
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            counts[page] = 0
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                GreenButton(
                    stringResource(if (page == steps.size - 1) R.string.finish else R.string.next),
                    icon = Icons.AutoMirrored.Rounded.ArrowForward, iconAfter = true,
                ) { scope.launch { pager.animateScrollToPage(page + 1) } }
            }
        }
    }

    if (askDownload) {
        AlertDialog(
            onDismissRequest = { askDownload = false },
            icon = { Icon(Icons.Rounded.Headphones, null, tint = c.green) },
            title = { Text(stringResource(R.string.audio_title)) },
            text = { Text(stringResource(R.string.audio_ask)) },
            confirmButton = {
                TextButton(onClick = { askDownload = false; AudioPack.start(context) }) { Text(stringResource(R.string.audio_download)) }
            },
            dismissButton = { TextButton(onClick = { askDownload = false }) { Text(stringResource(R.string.not_now)) } },
        )
    }
}

/** The verses a step recites aloud (the "blow and wipe" step recites the three Quls). */
private fun Step.passages(): List<Passage> = when (this) {
    is Step.Recite -> listOf(passage)
    is Step.Do -> action.passages
    is Step.Say -> emptyList()
}

@Composable
private fun StepPage(step: Step) {
    val c = RT.colors
    val l = lang()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(
                stringResource(
                    when (step) {
                        is Step.Recite -> R.string.kind_quran
                        is Step.Say -> R.string.kind_dua
                        is Step.Do -> R.string.kind_action
                    },
                ),
            )
            Chip(
                pluralStringResource(
                    when (step) {
                        is Step.Recite -> R.plurals.read_times
                        is Step.Say -> R.plurals.say_times
                        is Step.Do -> R.plurals.do_times
                    },
                    step.count, step.count,
                ),
                fill = c.soft, color = c.ink,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(step.title.t(), style = MaterialTheme.typography.headlineSmall, color = c.ink)
        if (step is Step.Recite) {
            Text(stringResource(R.string.quran_ref, step.passage.reference), style = MaterialTheme.typography.bodySmall, color = c.muted)
        }
        step.note?.let {
            Spacer(Modifier.height(12.dp))
            Note(it[l])
        }
        Spacer(Modifier.height(16.dp))
        when (step) {
            is Step.Recite -> PassageText(step.passage)
            is Step.Say -> {
                ArabicText(step.dua.ar, prose = true)
                Spacer(Modifier.height(12.dp))
                Meanings(step.dua.translit) { step.dua.meaning[it] }
            }
            is Step.Do -> {
                Text(step.action.body[l], style = MaterialTheme.typography.bodyLarge, color = c.ink)
                step.action.passages.forEach { ps ->
                    Spacer(Modifier.height(18.dp))
                    Text(ps.name[l], style = MaterialTheme.typography.titleSmall, color = c.green)
                    Spacer(Modifier.height(4.dp))
                    PassageText(ps, compact = true)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Sources(step.sources)
        Spacer(Modifier.height(24.dp))
    }
}

/** Verses with the basmala where a surah starts, each followed by its transliteration and meaning. */
@Composable
fun PassageText(passage: Passage, compact: Boolean = false) {
    val c = RT.colors
    passage.ayahs.forEachIndexed { i, a ->
        if (a.startsSura) Basmala()
        AyahArabic(a)
        if (!compact) {
            Spacer(Modifier.height(8.dp))
            Meanings(a.translit) { a.meaning(it) }
            if (i < passage.ayahs.lastIndex) HorizontalDivider(Modifier.padding(vertical = 14.dp), color = c.line)
        } else {
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun FinishPage(session: Session, onDone: () -> Unit) {
    val c = RT.colors
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(40.dp))
        IconBadge(Icons.Rounded.Check, 84.dp)
        Spacer(Modifier.height(20.dp))
        ArabicText("الْحَمْدُ لِلَّهِ", size = 34, color = c.green, align = TextAlign.Center)
        Text(stringResource(R.string.finish_title), style = MaterialTheme.typography.headlineSmall, color = c.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.finish_text, session.title.t()),
            style = MaterialTheme.typography.bodyLarge, color = c.muted, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        GreenButton(stringResource(R.string.done), Modifier.fillMaxWidth().navigationBarsPadding(), onClick = onDone)
    }
}

@Composable
private fun RoundButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector, label: String,
    enabled: Boolean = true, filled: Boolean = false, onClick: () -> Unit,
) {
    val c = RT.colors
    Box(
        Modifier.size(52.dp).clip(CircleShape).background(if (filled) c.green else c.soft)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, tint = when {
            filled -> c.onGreen
            enabled -> c.ink
            else -> c.muted.copy(alpha = 0.4f)
        })
    }
}

/** Tap to count repetitions: "2 / 3"; a check when complete (tap again to restart). */
@Composable
private fun Counter(done: Int, total: Int, onTap: () -> Unit) {
    val c = RT.colors
    val complete = done >= total
    Row(
        Modifier.height(52.dp).clip(RoundedCornerShape(50)).background(if (complete) c.greenSoft else c.card)
            .clickable(onClick = onTap).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (complete) {
            Icon(Icons.Rounded.Check, null, tint = c.onGreenSoft, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text("$done / $total", style = MaterialTheme.typography.titleMedium, color = if (complete) c.onGreenSoft else c.ink)
    }
}
