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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
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
import android.app.Activity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.digitalbeavertz.ruqya.ui.screens.CustomizeScreen
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

    // White status-bar icons over Home's green header (and in dark mode), dark ones elsewhere.
    val view = LocalView.current
    val lightIcons = !RT.colors.dark && route != "home"
    SideEffect {
        (view.context as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = lightIcons }
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
            composable("customize/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                CustomizeScreen(nav, it.arguments?.getString("id").orEmpty())
            }
            composable("article/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                ArticleScreen(nav, it.arguments?.getString("id").orEmpty())
            }
        }
        // Tabs scroll under the status bar: keep it readable with a strip of the canvas colour.
        if (route in TABS && route != "home") {
            Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(RT.colors.canvas))
        }
        AnimatedVisibility(
            visible = route in TABS,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { BottomBar(nav, route) }
    }
}

/** A plain bottom bar across the screen: icon and label for each tab, green when selected. */
@Composable
private fun BottomBar(nav: NavHostController, route: String?) {
    val c = RT.colors
    Column(Modifier.fillMaxWidth().background(c.card)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(66.dp), verticalAlignment = Alignment.CenterVertically) {
            Tab(Icons.Rounded.Home, stringResource(R.string.tab_home), route == "home") { nav.goTab("home") }
            Tab(Icons.AutoMirrored.Rounded.MenuBook, stringResource(R.string.tab_ruqya), route == "ruqya") { nav.goTab("ruqya") }
            Tab(Icons.Rounded.VolunteerActivism, stringResource(R.string.tab_duas), route == "duas") { nav.goTab("duas") }
            Tab(Icons.Rounded.AutoAwesome, stringResource(R.string.tab_names), route == "names") { nav.goTab("names") }
            Tab(Icons.Rounded.School, stringResource(R.string.tab_learn), route == "learn") { nav.goTab("learn") }
        }
    }
}

@Composable
private fun RowScope.Tab(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val c = RT.colors
    val tint = if (selected) c.green else c.muted
    Column(
        Modifier.weight(1f).fillMaxHeight().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(24.dp))
        Text(
            label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp), color = tint,
            modifier = Modifier.padding(top = 3.dp), maxLines = 1,
        )
    }
}

private fun NavHostController.goTab(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
