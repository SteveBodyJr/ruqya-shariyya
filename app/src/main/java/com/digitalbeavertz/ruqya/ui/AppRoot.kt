package com.digitalbeavertz.ruqya.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.ui.components.prefs
import com.digitalbeavertz.ruqya.ui.screens.AboutScreen
import com.digitalbeavertz.ruqya.ui.screens.ArticleScreen
import com.digitalbeavertz.ruqya.ui.screens.DuaScreen
import com.digitalbeavertz.ruqya.ui.screens.DuasScreen
import com.digitalbeavertz.ruqya.ui.screens.HomeScreen
import com.digitalbeavertz.ruqya.ui.screens.LearnScreen
import com.digitalbeavertz.ruqya.ui.screens.NamesScreen
import com.digitalbeavertz.ruqya.ui.screens.RunScreen
import com.digitalbeavertz.ruqya.ui.screens.SessionsScreen
import com.digitalbeavertz.ruqya.ui.screens.SettingsScreen
import com.digitalbeavertz.ruqya.ui.screens.TranslationScreen
import com.digitalbeavertz.ruqya.ui.screens.WelcomeScreen
import com.digitalbeavertz.ruqya.ui.theme.RT

private val TABS = listOf("home", "ruqya", "duas", "names", "learn")

@Composable
fun AppRoot(startRoute: String?, onStartRouteUsed: () -> Unit) {
    val p = prefs()
    if (p.language == null) {
        WelcomeScreen()
        return
    }
    if (!p.translationChosen) {
        TranslationScreen()
        return
    }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    // Deep links from reminder notifications.
    LaunchedEffect(startRoute) {
        if (startRoute != null) {
            nav.navigate(startRoute) { launchSingleTop = true }
            onStartRouteUsed()
        }
    }

    Box(Modifier.fillMaxSize().background(RT.colors.canvas)) {
        NavHost(nav, startDestination = "home", modifier = Modifier.fillMaxSize()) {
            composable("home") { HomeScreen(nav) }
            composable("ruqya") { SessionsScreen(nav) }
            composable("duas") { DuasScreen(nav) }
            composable("names") { NamesScreen() }
            composable("learn") { LearnScreen(nav) }
            composable("settings") { SettingsScreen(nav) }
            composable("about") { AboutScreen(nav) }
            composable("run/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                RunScreen(nav, it.arguments?.getString("id").orEmpty())
            }
            composable("dua/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                DuaScreen(nav, it.arguments?.getString("id").orEmpty())
            }
            composable("article/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                ArticleScreen(nav, it.arguments?.getString("id").orEmpty())
            }
        }
        // Tabs scroll under the status bar: keep it readable with a strip of the canvas colour.
        if (route in TABS) {
            Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(RT.colors.canvas))
        }
        AnimatedVisibility(
            visible = route in TABS,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { BottomBar(nav, route) }
    }
}

/** A floating pill with the five tabs; the selected one shows its label. */
@Composable
private fun BottomBar(nav: NavHostController, route: String?) {
    val c = RT.colors
    Row(
        Modifier.navigationBarsPadding().padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
            .shadow(16.dp, RoundedCornerShape(50), ambientColor = Color.Black.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(50)).background(c.card).padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Tab(Icons.Rounded.Home, stringResource(R.string.tab_home), route == "home") { nav.goTab("home") }
        Tab(Icons.AutoMirrored.Rounded.MenuBook, stringResource(R.string.tab_ruqya), route == "ruqya") { nav.goTab("ruqya") }
        Tab(Icons.Rounded.VolunteerActivism, stringResource(R.string.tab_duas), route == "duas") { nav.goTab("duas") }
        Tab(Icons.Rounded.AutoAwesome, stringResource(R.string.tab_names), route == "names") { nav.goTab("names") }
        Tab(Icons.Rounded.School, stringResource(R.string.tab_learn), route == "learn") { nav.goTab("learn") }
    }
}

@Composable
private fun Tab(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val c = RT.colors
    Column(
        Modifier.clip(RoundedCornerShape(50)).background(if (selected) c.green else Color.Transparent)
            .clickable(onClick = onClick).height(50.dp).padding(horizontal = if (selected) 14.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, label, tint = if (selected) c.onGreen else c.muted, modifier = Modifier.size(22.dp))
            if (selected) {
                Text(
                    label, style = MaterialTheme.typography.labelMedium, color = c.onGreen,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

private fun NavHostController.goTab(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
