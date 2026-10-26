package com.digitalbeavertz.ruqya.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.digitalbeavertz.ruqya.ui.components.proseFont
import com.digitalbeavertz.ruqya.ui.theme.arabicFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.digitalbeavertz.ruqya.BuildConfig
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.data.Content
import com.digitalbeavertz.ruqya.data.Dua
import com.digitalbeavertz.ruqya.data.Evidence
import com.digitalbeavertz.ruqya.data.Lang
import com.digitalbeavertz.ruqya.ui.components.ArabicText
import com.digitalbeavertz.ruqya.ui.components.BottomBarSpace
import com.digitalbeavertz.ruqya.ui.components.Chip
import com.digitalbeavertz.ruqya.ui.components.Header
import com.digitalbeavertz.ruqya.ui.components.IconBadge
import com.digitalbeavertz.ruqya.ui.components.Meanings
import com.digitalbeavertz.ruqya.ui.components.Note
import com.digitalbeavertz.ruqya.ui.components.QuietButton
import com.digitalbeavertz.ruqya.ui.components.RCard
import com.digitalbeavertz.ruqya.ui.components.ScreenPadding
import com.digitalbeavertz.ruqya.ui.components.SectionTitle
import com.digitalbeavertz.ruqya.ui.components.StarBadge
import com.digitalbeavertz.ruqya.ui.components.Sources
import com.digitalbeavertz.ruqya.ui.components.content
import com.digitalbeavertz.ruqya.ui.components.iconFor
import com.digitalbeavertz.ruqya.ui.components.lang
import com.digitalbeavertz.ruqya.ui.components.prefs
import com.digitalbeavertz.ruqya.ui.components.t
import com.digitalbeavertz.ruqya.ui.theme.RT

@Composable
fun DuasScreen(nav: NavHostController) {
    val c = RT.colors
    val p = prefs()
    val content = content()
    val l = lang()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = BottomBarSpace)) {
        item { Header(stringResource(R.string.tab_duas), subtitle = stringResource(R.string.duas_subtitle)) }
        content.categories.forEach { cat ->
            val duas = content.duas.filter { it.category == cat.id }
            if (duas.isEmpty()) return@forEach
            item(key = "cat-${cat.id}") { SectionTitle(cat.title[l]) }
            duas.forEachIndexed { n, d ->
                item(key = d.id) {
                    RCard(onClick = { nav.navigate("dua/${d.id}") }, padding = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarBadge("${n + 1}", 34.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(d.title[l], style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.weight(1f))
                            Chip(pluralStringResource(R.plurals.times, d.count, d.count), fill = c.soft, color = c.ink)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            d.ar, maxLines = 1, overflow = TextOverflow.Ellipsis, color = c.green,
                            style = TextStyle(fontFamily = arabicFamily(proseFont(p.arabicFont)), fontSize = 20.sp, textDirection = TextDirection.Rtl),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(content.cite(d.sources.first(), l), style = MaterialTheme.typography.labelMedium, color = c.muted)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DuaScreen(nav: NavHostController, id: String) {
    val c = RT.colors
    val content = content()
    val d = content.dua(id) ?: return
    val l = lang()
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp)) {
        item { Header(d.title[l], onBack = { nav.popBackStack() }) }
        item {
            RCard(padding = 20.dp) {
                Chip(pluralStringResource(R.plurals.say_times, d.count, d.count))
                Spacer(Modifier.height(12.dp))
                ArabicText(d.ar, prose = true)
                Spacer(Modifier.height(14.dp))
                Meanings(d.translit) { d.meaning[it] }
            }
            d.note?.let {
                Spacer(Modifier.height(12.dp))
                Note(it[l])
            }
            Spacer(Modifier.height(14.dp))
            Sources(d.sources)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuietButton(stringResource(R.string.copy), Modifier.weight(1f), Icons.Rounded.ContentCopy) {
                    copy(context, shareText(d, content, l))
                }
                QuietButton(stringResource(R.string.share), Modifier.weight(1f), Icons.Rounded.Share) {
                    share(context, shareText(d, content, l))
                }
            }
        }
    }
}

private fun shareText(d: Dua, content: Content, l: Lang) = buildString {
    appendLine(d.title[l]).appendLine()
    appendLine(d.ar).appendLine()
    appendLine(d.translit).appendLine()
    appendLine(d.meaning[l]).appendLine()
    append(d.sources.joinToString(", ") { content.cite(it, l) })
}

fun copy(context: Context, text: String) {
    context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("dua", text))
    Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
}

fun share(context: Context, text: String) {
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
}

@Composable
fun LearnScreen(nav: NavHostController) {
    val c = RT.colors
    val content = content()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = BottomBarSpace)) {
        item {
            Header(stringResource(R.string.tab_learn), subtitle = stringResource(R.string.learn_subtitle))
            Spacer(Modifier.height(8.dp))
        }
        content.articles.forEach { a ->
            item(key = a.id) {
                RCard(onClick = { nav.navigate("article/${a.id}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(iconFor(a.icon), 42.dp)
                        Spacer(Modifier.width(14.dp))
                        Text(a.title.t(), style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = c.muted)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            RCard(onClick = { nav.navigate("about") }, fill = c.soft) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Rounded.Verified, 42.dp, fill = c.card)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.about_sources), style = MaterialTheme.typography.titleMedium, color = c.ink)
                        Text(stringResource(R.string.about_sources_short), style = MaterialTheme.typography.bodySmall, color = c.muted)
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleScreen(nav: NavHostController, id: String) {
    val c = RT.colors
    val a = content().article(id) ?: return
    val l = lang()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp)) {
        item { Header(a.title[l], onBack = { nav.popBackStack() }) }
        item {
            RCard(padding = 20.dp) {
                a.body(l).forEachIndexed { i, para ->
                    if (i > 0) Spacer(Modifier.height(12.dp))
                    Text(para, style = MaterialTheme.typography.bodyLarge, color = c.ink)
                }
            }
        }
        if (a.evidence.isNotEmpty()) item { SectionTitle(stringResource(R.string.evidence)) }
        a.evidence.forEach { e -> item { EvidenceCard(e); Spacer(Modifier.height(8.dp)) } }
    }
}

@Composable
fun EvidenceCard(e: Evidence) {
    val c = RT.colors
    val l = lang()
    val p = prefs()
    RCard {
        when (e) {
            is Evidence.Quran -> {
                PassageText(e.passage, compact = true)
                Spacer(Modifier.height(8.dp))
                e.passage.ayahs.forEach { a ->
                    Text(a.meaning(l), style = MaterialTheme.typography.bodyMedium, color = c.ink)
                }
                Spacer(Modifier.height(8.dp))
                Chip(stringResource(R.string.quran_ref, e.passage.reference))
            }
            is Evidence.Hadith -> {
                ArabicText(e.ar, size = (p.arabicSize * 0.8f).toInt(), prose = true)
                Spacer(Modifier.height(8.dp))
                Text(e.meaning[l], style = MaterialTheme.typography.bodyMedium, color = c.ink)
                Spacer(Modifier.height(10.dp))
                Sources(listOf(e.source))
            }
        }
    }
}

/** Where everything comes from, how it was checked, and the licences. */
@Composable
fun AboutScreen(nav: NavHostController) {
    val c = RT.colors
    val context = LocalContext.current
    val open = { url: String -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp)) {
        item { Header(stringResource(R.string.about_sources), onBack = { nav.popBackStack() }) }
        item {
            RCard(fill = c.greenSoft) {
                Text(stringResource(R.string.about_checked_title), style = MaterialTheme.typography.titleMedium, color = c.onGreenSoft)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.about_checked), style = MaterialTheme.typography.bodyMedium, color = c.onGreenSoft)
            }
            SectionTitle(stringResource(R.string.about_sources))
        }
        val sources = listOf(
            Triple(R.string.src_tanzil_title, R.string.src_tanzil, "https://tanzil.net"),
            Triple(R.string.src_kfgqpc_title, R.string.src_kfgqpc, "https://qurancomplex.gov.sa"),
            Triple(R.string.src_translations_title, R.string.src_translations, "https://tanzil.net/trans/"),
            Triple(R.string.src_translit_title, R.string.src_translit, "https://tanzil.net"),
            Triple(R.string.src_hadith_title, R.string.src_hadith, "https://sunnah.com"),
            Triple(R.string.src_names_title, R.string.src_names, "https://shamela.ws/book/8874/19"),
            Triple(R.string.src_audio_title, R.string.src_audio, "https://everyayah.com"),
            Triple(R.string.src_guide_title, R.string.src_guide, ""),
            Triple(R.string.src_fonts_title, R.string.src_fonts, "https://fonts.google.com"),
        )
        sources.forEach { (title, text, url) ->
            item {
                RCard(onClick = if (url.isNotEmpty()) ({ open(url) }) else null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(title), style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.weight(1f))
                        if (url.isNotEmpty()) Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = c.muted)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(text), style = MaterialTheme.typography.bodySmall, color = c.muted)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        item {
            SectionTitle(stringResource(R.string.about_disclaimer_title))
            Text(stringResource(R.string.about_disclaimer), style = MaterialTheme.typography.bodyMedium, color = c.ink)
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.version, BuildConfig.VERSION_NAME) + "  ·  Digital Beaver Apps",
                style = MaterialTheme.typography.bodySmall, color = c.muted, modifier = Modifier.navigationBarsPadding(),
            )
        }
    }
}
