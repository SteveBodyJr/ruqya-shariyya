package com.digitalbeavertz.ruqya.audio

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.digitalbeavertz.ruqya.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Plays the recitation with a media notification, so it continues with the screen off. */
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, player).setSessionActivity(open).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = session?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}

/** What the session screen needs to know about playback. [key] names the step that is playing. */
data class PlayState(val key: String? = null, val playing: Boolean = false, val index: Int = 0, val total: Int = 0)

/** Connects to [PlaybackService] and plays lists of verse files. */
class Audio(private val context: Context, private val pack: AudioPack) {
    private var controller: MediaController? = null
    private var connecting = false
    private var pending: (() -> Unit)? = null
    private val _state = MutableStateFlow(PlayState())
    val state: StateFlow<PlayState> = _state.asStateFlow()
    private var key: String? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish(player)
    }

    private fun publish(p: Player) {
        val ended = p.playbackState == Player.STATE_ENDED || p.mediaItemCount == 0
        _state.value = PlayState(
            key = if (ended) null else key,
            playing = p.isPlaying || (p.playWhenReady && p.playbackState == Player.STATE_BUFFERING),
            index = p.currentMediaItemIndex, total = p.mediaItemCount,
        )
    }

    private fun withController(block: (MediaController) -> Unit) {
        controller?.let { return block(it) }
        pending = { controller?.let(block) }
        if (connecting) return
        connecting = true
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            connecting = false
            controller = runCatching { future.get() }.getOrNull()?.also { it.addListener(listener) }
            pending?.invoke()
            pending = null
        }, ContextCompat.getMainExecutor(context))
    }

    /** Plays [files] (verse file names) for the step [stepKey], titled [title]. Files not downloaded are skipped. */
    fun play(stepKey: String, title: String, files: List<String>) {
        val items = files.filter(pack::has).mapIndexed { i, name ->
            MediaItem.Builder()
                .setMediaId("$stepKey#$i")
                .setUri(Uri.fromFile(pack.file(name)))
                .setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist("Mishary Rashid Alafasy").build())
                .build()
        }
        if (items.isEmpty()) return
        key = stepKey
        withController { c ->
            c.setMediaItems(items)
            c.prepare()
            c.play()
        }
    }

    fun pause() = withController { it.pause() }
    fun resume() = withController { it.play() }

    fun stop() {
        key = null
        controller?.run {
            stop()
            clearMediaItems()
        }
        _state.value = PlayState()
    }
}
