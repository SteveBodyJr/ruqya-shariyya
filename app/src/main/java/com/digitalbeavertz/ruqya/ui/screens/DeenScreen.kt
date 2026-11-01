package com.digitalbeavertz.ruqya.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.ui.components.Chip
import com.digitalbeavertz.ruqya.ui.components.GreenButton
import com.digitalbeavertz.ruqya.ui.components.Header
import com.digitalbeavertz.ruqya.ui.components.RCard
import com.digitalbeavertz.ruqya.ui.components.ScreenPadding
import com.digitalbeavertz.ruqya.ui.components.SectionTitle
import com.digitalbeavertz.ruqya.ui.theme.RT

/** The Deen family: Digital Beaver's Islamic apps. The same list is in Sahih Al Bukhari. */
data class DeenApp(val id: String, val pkg: String, val name: String, val tagline: Int, val icon: Int)

val DEEN_APPS = listOf(
    DeenApp("ruqya", "com.digitalbeavertz.ruqya", "Ruqya Shariyya", R.string.deen_ruqya, R.drawable.deen_ruqya),
    DeenApp("bukhari", "com.digitalbeavertz.bukhari", "Sahih Al Bukhari", R.string.deen_bukhari, R.drawable.deen_bukhari),
)

/** The Deen page on the Digital Beaver Apps site: every Islamic app, new ones as they come. */
const val DEEN_URL = "https://stevebodyjr.github.io/apps/deen/"

private fun open(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

@Composable
fun DeenScreen(nav: NavHostController) {
    val c = RT.colors
    val context = LocalContext.current
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Header(stringResource(R.string.deen_title), onBack = { nav.popBackStack() }, subtitle = stringResource(R.string.deen_sub)) }
        item { Text(stringResource(R.string.deen_intro), style = MaterialTheme.typography.bodyMedium, color = c.muted, modifier = Modifier.padding(horizontal = 4.dp)) }
        item { SectionTitle(stringResource(R.string.deen_apps)) }
        items(DEEN_APPS, key = { it.id }) { app ->
            val here = app.pkg == context.packageName
            val launch = if (here) null else context.packageManager.getLaunchIntentForPackage(app.pkg)
            RCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(app.icon), null, Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.name, style = MaterialTheme.typography.titleMedium, color = c.ink)
                        Spacer(Modifier.height(2.dp))
                        Text(stringResource(app.tagline), style = MaterialTheme.typography.bodySmall, color = c.muted)
                    }
                }
                Spacer(Modifier.height(12.dp))
                when {
                    here -> Chip(stringResource(R.string.deen_here))
                    launch != null -> GreenButton(
                        stringResource(R.string.deen_open), Modifier.fillMaxWidth(), Icons.AutoMirrored.Rounded.ArrowForward, iconAfter = true,
                    ) { context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    else -> GreenButton(
                        stringResource(R.string.deen_get), Modifier.fillMaxWidth(), Icons.Rounded.Download, iconAfter = true,
                    ) { open(context, DEEN_URL + "#" + app.id) }
                }
            }
        }
        item {
            RCard(fill = c.greenSoft, onClick = { open(context, DEEN_URL) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.deen_site), style = MaterialTheme.typography.titleSmall, color = c.onGreenSoft)
                        Text(stringResource(R.string.deen_site_sub), style = MaterialTheme.typography.bodySmall, color = c.onGreenSoft)
                    }
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = c.onGreenSoft)
                }
            }
        }
    }
}
