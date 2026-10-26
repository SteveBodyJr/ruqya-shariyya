package com.digitalbeavertz.ruqya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalbeavertz.ruqya.AppContainer
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.data.ArabicFont
import com.digitalbeavertz.ruqya.data.Ayah
import com.digitalbeavertz.ruqya.data.Content
import com.digitalbeavertz.ruqya.data.Lang
import com.digitalbeavertz.ruqya.data.Prefs
import com.digitalbeavertz.ruqya.data.Source
import com.digitalbeavertz.ruqya.data.Txt
import com.digitalbeavertz.ruqya.ui.theme.RT
import com.digitalbeavertz.ruqya.ui.theme.arabicFamily

val LocalContainer = compositionLocalOf<AppContainer> { error("No container") }

/** The settings, read so that the caller recomposes when any of them changes. */
@Composable
fun prefs(): Prefs {
    val p = LocalContainer.current.prefs
    val v by p.version.collectAsState()
    @Suppress("UNUSED_EXPRESSION") v
    return p
}

@Composable
fun content(): Content = LocalContainer.current.content

@Composable
fun lang(): Lang = prefs().lang

@Composable
fun Txt.t(): String = this[lang()]

/** 12 -> ١٢ */
fun arabicDigits(n: Int) = n.toString().map { '٠' + (it - '0') }.joinToString("")

fun iconFor(name: String): ImageVector = when (name) {
    "eye" -> Icons.Rounded.Visibility
    "book" -> Icons.AutoMirrored.Rounded.MenuBook
    "shield" -> Icons.Rounded.Shield
    "moon" -> Icons.Rounded.NightsStay
    "heart" -> Icons.Rounded.Favorite
    "calm" -> Icons.Rounded.Spa
    "family" -> Icons.Rounded.FamilyRestroom
    "sun" -> Icons.Rounded.WbSunny
    "sunset" -> Icons.Rounded.WbTwilight
    "bed" -> Icons.Rounded.Bedtime
    "hands" -> Icons.Rounded.PanTool
    "block" -> Icons.Rounded.Block
    "medical" -> Icons.Rounded.LocalHospital
    "check" -> Icons.Rounded.CheckCircle
    else -> Icons.Rounded.Info
}

/** A screen title with an optional back button and actions on the right. */
@Composable
fun Header(title: String, onBack: (() -> Unit)? = null, subtitle: String? = null, actions: @Composable RowScope.() -> Unit = {}) {
    val c = RT.colors
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(start = if (onBack == null) 20.dp else 6.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = c.ink) }
        }
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(
                title,
                style = if (onBack == null) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                color = c.ink,
            )
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.muted)
        }
        actions()
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(), style = MaterialTheme.typography.labelSmall, color = RT.colors.muted,
        modifier = modifier.padding(start = 4.dp, top = 18.dp, bottom = 8.dp),
    )
}

/** A flat white card. */
@Composable
fun RCard(
    modifier: Modifier = Modifier,
    fill: Color = RT.colors.card,
    onClick: (() -> Unit)? = null,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).background(fill)
            .then(if (fill == RT.colors.card && !RT.colors.dark) Modifier.border(1.dp, RT.colors.line, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

/** A round tinted icon. */
@Composable
fun IconBadge(icon: ImageVector, size: Dp = 44.dp, fill: Color = RT.colors.greenSoft, tint: Color = RT.colors.onGreenSoft) {
    Box(Modifier.size(size).clip(CircleShape).background(fill), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/**
 * The KFGQPC Madinah font maps some ordinary characters (such as the Arabic comma) to Quranic
 * marks, so hadith and supplications use Scheherazade, the closest naskh, when it is chosen.
 */
fun proseFont(f: ArabicFont) = if (f == ArabicFont.MADINAH) ArabicFont.SCHEHERAZADE else f

/** Arabic text in the chosen font, right to left. [prose] is for hadith and supplications (not Quran). */
@Composable
fun ArabicText(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = prefs().arabicSize,
    font: ArabicFont = prefs().arabicFont,
    color: Color = RT.colors.ink,
    align: TextAlign = TextAlign.Start,
    prose: Boolean = false,
) {
    val f = if (prose) proseFont(font) else font
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text,
            modifier = modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = arabicFamily(f), fontSize = size.sp,
                // Vowel marks above and below need room.
                lineHeight = (size * if (f == ArabicFont.MADINAH) 1.9 else 1.8).sp,
                textDirection = TextDirection.Rtl, textAlign = align, color = color,
            ),
        )
    }
}

/** One verse: the Madinah font shows the King Fahd Complex text (it draws its own verse number), the others Tanzil's. */
@Composable
fun AyahArabic(ayah: Ayah, modifier: Modifier = Modifier, size: Int = prefs().arabicSize) {
    val p = prefs()
    val text = if (p.arabicFont == ArabicFont.MADINAH) ayah.hafs else "${ayah.tanzil} ۝${arabicDigits(ayah.aya)}"
    ArabicText(text, modifier, size = size)
}

/** The basmala before a surah read from its start. */
@Composable
fun Basmala() {
    val p = prefs()
    val c = content()
    ArabicText(
        if (p.arabicFont == ArabicFont.MADINAH) c.basmalaHafs else c.basmalaTanzil,
        Modifier.padding(vertical = 6.dp), size = (p.arabicSize * 0.85f).toInt(), color = RT.colors.green, align = TextAlign.Center,
    )
}

/** Transliteration and the chosen meanings under an Arabic text. */
@Composable
fun Meanings(translit: String?, meaning: (Lang) -> String) {
    val p = prefs()
    val c = RT.colors
    if (p.transliteration && !translit.isNullOrBlank()) {
        Text(translit, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = c.green)
        Spacer(Modifier.height(6.dp))
    }
    p.meanings.forEachIndexed { i, l ->
        if (i > 0) Spacer(Modifier.height(6.dp))
        Text(meaning(l), style = MaterialTheme.typography.bodyLarge, color = if (i == 0) c.ink else c.muted)
    }
}

/** "Sahih Muslim 2186 · Sahih" chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Sources(sources: List<Source>, modifier: Modifier = Modifier) {
    if (sources.isEmpty()) return
    val c = content()
    val l = lang()
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        sources.forEach { s ->
            Chip(c.cite(s, l) + (s.grade?.let { " · ${it[l]}" } ?: ""))
        }
    }
}

@Composable
fun Chip(text: String, fill: Color = RT.colors.greenSoft, color: Color = RT.colors.onGreenSoft) {
    Text(
        text, style = MaterialTheme.typography.labelMedium, color = color,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(fill).padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

/** A note with a thin green rule on the side. */
@Composable
fun Note(text: String) {
    val c = RT.colors
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.soft).padding(14.dp)) {
        Box(Modifier.width(3.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(c.green))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.ink)
    }
}

/** A big primary button in flat green. */
@Composable
fun GreenButton(
    text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, iconAfter: Boolean = false,
    enabled: Boolean = true, onClick: () -> Unit,
) {
    val c = RT.colors
    val tint = if (enabled) c.onGreen else c.muted
    Row(
        modifier.height(52.dp).clip(RoundedCornerShape(50)).background(if (enabled) c.green else c.soft)
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null && !iconAfter) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = tint)
        if (icon != null && iconAfter) {
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun QuietButton(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, onClick: () -> Unit) {
    val c = RT.colors
    Row(
        modifier.height(48.dp).clip(RoundedCornerShape(50)).background(c.soft).clickable(onClick = onClick).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = c.ink, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = c.ink)
    }
}

/** Formats minutes after midnight as 06:30. */
fun hhmm(minute: Int) = "%02d:%02d".format(minute / 60, minute % 60)

val ScreenPadding = 16.dp
val BottomBarSpace = 96.dp
