package com.digitalbeavertz.ruqya.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.data.Evidence
import com.digitalbeavertz.ruqya.data.Name
import com.digitalbeavertz.ruqya.data.Passage
import com.digitalbeavertz.ruqya.ui.components.ArabicText
import com.digitalbeavertz.ruqya.ui.components.BottomBarSpace
import com.digitalbeavertz.ruqya.ui.components.Header
import com.digitalbeavertz.ruqya.ui.components.Note
import com.digitalbeavertz.ruqya.ui.components.RCard
import com.digitalbeavertz.ruqya.ui.components.ScreenPadding
import com.digitalbeavertz.ruqya.ui.components.Sources
import com.digitalbeavertz.ruqya.ui.components.arabicDigits
import com.digitalbeavertz.ruqya.ui.components.content
import com.digitalbeavertz.ruqya.ui.components.lang
import com.digitalbeavertz.ruqya.ui.components.t
import com.digitalbeavertz.ruqya.ui.theme.RT

/** The 99 names (Ibn Uthaymeen, al-Qawa'id al-Muthla), each with the verse or hadith it comes from. */
@Composable
fun NamesScreen() {
    val c = RT.colors
    val names = content().names
    val l = lang()
    var query by rememberSaveable { mutableStateOf("") }
    var open by remember { mutableStateOf<Name?>(null) }
    var introOpen by rememberSaveable { mutableStateOf(false) }
    val shown = remember(query, l) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) names.list
        else names.list.filter { n ->
            n.translit.lowercase().contains(q) || n.meaning[l].lowercase().contains(q) || n.ar.contains(q) || n.number.toString() == q
        }
    }

    LazyVerticalGrid(
        GridCells.Fixed(2), Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = BottomBarSpace),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Header(stringResource(R.string.names_title), subtitle = stringResource(R.string.names_subtitle))
                RCard(fill = c.greenSoft, onClick = { introOpen = !introOpen }) {
                    val intro = names.intro(l)
                    Text(intro.first(), style = MaterialTheme.typography.bodyMedium, color = c.onGreenSoft)
                    if (introOpen) {
                        intro.drop(1).forEach {
                            Spacer(Modifier.height(10.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = c.onGreenSoft)
                        }
                        Spacer(Modifier.height(12.dp))
                        Sources(listOf(names.hadith.source))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.names_weak, content().cite(names.weakList, l), names.weakList.grade?.get(l).orEmpty()),
                            style = MaterialTheme.typography.bodySmall, color = c.onGreenSoft,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(if (introOpen) R.string.show_less else R.string.read_more),
                        style = MaterialTheme.typography.labelLarge, color = c.green,
                    )
                }
                Spacer(Modifier.height(10.dp))
                SearchField(query) { query = it }
                Spacer(Modifier.height(4.dp))
            }
        }
        items(shown, key = { it.number }) { n -> NameCard(n) { open = n } }
    }

    open?.let { n -> NameSheet(n) { open = null } }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val c = RT.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(c.card).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = c.muted)
        androidx.compose.foundation.layout.Box(Modifier.weight(1f).padding(start = 10.dp)) {
            if (value.isEmpty()) Text(stringResource(R.string.names_search), style = MaterialTheme.typography.bodyLarge, color = c.muted)
            BasicTextField(
                value, onChange, singleLine = true, modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink), cursorBrush = SolidColor(c.green),
            )
        }
    }
}

@Composable
private fun NameCard(n: Name, onClick: () -> Unit) {
    val c = RT.colors
    RCard(onClick = onClick, padding = 14.dp) {
        Text(arabicDigits(n.number) + "  ·  " + n.number, style = MaterialTheme.typography.labelSmall, color = c.gold)
        ArabicText(n.ar, size = 30, color = c.green, align = TextAlign.Center, modifier = Modifier.padding(vertical = 2.dp))
        Text(n.translit, style = MaterialTheme.typography.titleSmall, color = c.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(
            n.meaning.t(), style = MaterialTheme.typography.bodySmall, color = c.muted, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth().height(40.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NameSheet(n: Name, onDismiss: () -> Unit) {
    val c = RT.colors
    val l = lang()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.canvas) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 20.dp)) {
            Text(stringResource(R.string.name_number, n.number), style = MaterialTheme.typography.labelSmall, color = c.gold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            ArabicText(n.ar, size = 46, color = c.green, align = TextAlign.Center)
            Text(n.translit, style = MaterialTheme.typography.headlineSmall, color = c.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Text(n.meaning[l], style = MaterialTheme.typography.bodyLarge, color = c.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.name_source), style = MaterialTheme.typography.labelSmall, color = c.muted)
            Spacer(Modifier.height(8.dp))
            when {
                n.ayah != null -> EvidenceCard(Evidence.Quran(Passage("name-${n.number}", n.meaning, listOf(n.ayah))))
                n.hadith != null -> RCard {
                    Text(stringResource(R.string.name_from_sunnah), style = MaterialTheme.typography.bodyMedium, color = c.ink)
                    Spacer(Modifier.height(10.dp))
                    Sources(listOf(n.hadith))
                }
                n.cite != null -> RCard {
                    Text(n.cite[l], style = MaterialTheme.typography.bodyMedium, color = c.ink)
                }
            }
            n.note?.let {
                Spacer(Modifier.height(10.dp))
                Note(it[l])
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.close), color = c.green)
            }
        }
    }
}
