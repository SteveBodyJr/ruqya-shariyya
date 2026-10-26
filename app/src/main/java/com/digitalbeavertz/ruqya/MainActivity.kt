package com.digitalbeavertz.ruqya

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.digitalbeavertz.ruqya.ui.AppRoot
import com.digitalbeavertz.ruqya.ui.components.LocalContainer
import com.digitalbeavertz.ruqya.ui.theme.RuqyaTheme
import com.digitalbeavertz.ruqya.widget.NextAdhkarWidget

/** AppCompat only for the per-app language (English / Kiswahili); everything on screen is Compose. */
class MainActivity : AppCompatActivity() {
    private val container get() = (application as RuqyaApp).container
    private var startRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            startRoute = intent?.getStringExtra(EXTRA_ROUTE)
            debugOverrides(intent)
        }
        setContent {
            val version by container.prefs.version.collectAsState()
            @Suppress("UNUSED_EXPRESSION") version
            RuqyaTheme(container.prefs.theme) {
                CompositionLocalProvider(LocalContainer provides container) {
                    AppRoot(startRoute) { startRoute = null }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        NextAdhkarWidget.update(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(EXTRA_ROUTE)?.let { startRoute = it }
    }

    /** Debug builds: `--es audioBase http://localhost:8765/` points the audio download at a local server. */
    private fun debugOverrides(intent: Intent?) {
        if (!BuildConfig.DEBUG) return
        intent?.getStringExtra("audioBase")?.let { container.prefs.audioBaseOverride = it.ifEmpty { null } }
    }

    companion object {
        const val EXTRA_ROUTE = "route"
    }
}
