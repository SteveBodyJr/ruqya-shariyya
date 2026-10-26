package com.digitalbeavertz.ruqya.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.data.Step
import com.digitalbeavertz.ruqya.ui.components.Header
import com.digitalbeavertz.ruqya.ui.components.QuietButton
import com.digitalbeavertz.ruqya.ui.components.ScreenPadding
import com.digitalbeavertz.ruqya.ui.components.StarBadge
import com.digitalbeavertz.ruqya.ui.components.content
import com.digitalbeavertz.ruqya.ui.components.lang
import com.digitalbeavertz.ruqya.ui.components.prefs
import com.digitalbeavertz.ruqya.ui.components.proseFont
import com.digitalbeavertz.ruqya.ui.components.t
import com.digitalbeavertz.ruqya.ui.theme.RT
import com.digitalbeavertz.ruqya.ui.theme.arabicFamily

/** Pick which adhkar to read every day in each routine. Saved straight away. */
@Composable
fun CustomizeScreen(nav: NavHostController, startId: String) {
    val c = RT.colors
    val p = prefs()
    val l = lang()
    val routines = content().routines
    val routine = routines.firstOrNull { it.id == startId } ?: routines.first()
    val chosen = routine.chosen(p.chosenSteps(routine.id)).map { it.key }.toSet()

    fun toggle(step: Step) {
        val next = if (step.key in chosen) chosen - step.key else chosen + step.key
        p.setChosenSteps(routine.id, next)
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp)) {
        item {
            Header(stringResource(R.string.customize_title), onBack = { nav.popBackStack() }, subtitle = stringResource(R.string.customize_subtitle))
            // Routine switcher: Morning / Evening / Before sleep.
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                routines.forEach { r ->
                    val selected = r.id == routine.id
                    Text(
                        r.title.t(), style = MaterialTheme.typography.labelLarge,
                        color = if (selected) c.onGreen else c.ink,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(if (selected) c.green else c.soft)
                            .clickable { nav.navigate("customize/${r.id}") { popUpTo("customize/${routine.id}") { inclusive = true } } }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
            Text(
                stringResource(R.string.chosen_of, chosen.size, routine.steps.size),
                style = MaterialTheme.typography.labelMedium, color = c.green, modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 10.dp),
            )
        }
        itemsIndexed(routine.steps, key = { _, s -> s.key }) { i, step ->
            val on = step.key in chosen
            val shape = RoundedCornerShape(16.dp)
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(shape)
                    .background(if (on) c.greenSoft else c.card)
                    .border(1.dp, if (on) c.green.copy(alpha = 0.5f) else c.line, shape)
                    .clickable { toggle(step) }.padding(start = 10.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StarBadge("${i + 1}", 34.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(step.title[l], style = MaterialTheme.typography.titleSmall, color = c.ink, maxLines = 2)
                    val preview = when (step) {
                        is Step.Say -> step.dua.ar
                        is Step.Recite -> step.passage.ayahs.first().tanzil
                        is Step.Do -> null
                    }
                    if (preview != null) {
                        Text(
                            preview, maxLines = 1, overflow = TextOverflow.Ellipsis, color = c.green, modifier = Modifier.fillMaxWidth(),
                            style = TextStyle(fontFamily = arabicFamily(proseFont(p.arabicFont)), fontSize = 17.sp, textDirection = TextDirection.Rtl),
                        )
                    }
                    Text(
                        pluralStringResource(R.plurals.times, step.count, step.count) +
                            (step.sources.firstOrNull()?.let { "  ·  " + content().cite(it, l) } ?: "") +
                            (if (step.optional) "  ·  " + stringResource(R.string.optional_label) else ""),
                        style = MaterialTheme.typography.bodySmall, color = c.muted,
                    )
                }
                Checkbox(
                    checked = on, onCheckedChange = { toggle(step) },
                    colors = CheckboxDefaults.colors(checkedColor = c.green, uncheckedColor = c.muted, checkmarkColor = c.onGreen),
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuietButton(stringResource(R.string.select_all), Modifier.weight(1f)) {
                    p.setChosenSteps(routine.id, routine.steps.map { it.key }.toSet())
                }
                QuietButton(stringResource(R.string.reset_recommended), Modifier.weight(1f)) {
                    p.setChosenSteps(routine.id, null)
                }
            }
        }
    }
}
