package com.digitalbeavertz.ruqya.audio

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.digitalbeavertz.ruqya.RuqyaApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * The recitation pack: one MP3 per verse (Mishary Rashid Alafasy, 64 kbps, from everyayah.com),
 * plus the ta'awwudh and the basmala. Downloaded once into app storage, then played offline.
 */
class AudioPack(context: Context, private val files: List<String>, private val base: () -> String) {
    private val dir = File(context.filesDir, "audio/alafasy64").apply { mkdirs() }

    fun file(name: String) = File(dir, "$name.mp3")
    fun has(name: String) = file(name).length() > 0
    fun missing() = files.filterNot(::has)
    val complete get() = missing().isEmpty()
    val bytesOnDisk get() = dir.listFiles()?.sumOf { it.length() } ?: 0L
    val total get() = files.size

    fun delete() {
        dir.listFiles()?.forEach { it.delete() }
    }

    /** Downloads every missing file, reporting (done, total) after each. Throws when one keeps failing. */
    suspend fun download(onProgress: suspend (Int, Int) -> Unit) = withContext(Dispatchers.IO) {
        val todo = missing()
        var done = total - todo.size
        onProgress(done, total)
        for (name in todo) {
            fetch(name)
            done++
            onProgress(done, total)
        }
    }

    private fun fetch(name: String) {
        val target = file(name)
        val part = File(dir, "$name.part")
        var last: Exception? = null
        repeat(3) {
            try {
                val conn = URL(base() + "$name.mp3").openConnection() as HttpURLConnection
                conn.connectTimeout = 20_000
                conn.readTimeout = 30_000
                try {
                    if (conn.responseCode != 200) throw IOException("HTTP ${conn.responseCode} for $name")
                    conn.inputStream.use { input -> part.outputStream().use { input.copyTo(it) } }
                } finally {
                    conn.disconnect()
                }
                // A real verse is never this small: an error page or a cut connection.
                if (part.length() < 2_000) throw IOException("$name is too small (${part.length()} bytes)")
                if (!part.renameTo(target)) throw IOException("could not save $name")
                return
            } catch (e: Exception) {
                last = e
                part.delete()
            }
        }
        throw last ?: IOException("download failed")
    }

    companion object {
        private const val WORK = "audio-pack"

        fun start(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK, ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build(),
            )
        }

        fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(WORK)

        /** Null when nothing is running. */
        fun progress(context: Context): Flow<DownloadState?> =
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(WORK).map { infos ->
                val w = infos.lastOrNull() ?: return@map null
                when (w.state) {
                    WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED ->
                        DownloadState.Running(w.progress.getInt("done", 0), w.progress.getInt("total", 0), w.state == WorkInfo.State.ENQUEUED)
                    WorkInfo.State.FAILED -> DownloadState.Failed(w.outputData.getString("error").orEmpty())
                    else -> null
                }
            }
    }
}

sealed class DownloadState {
    /** [waiting] while there is no connection yet. */
    data class Running(val done: Int, val total: Int, val waiting: Boolean) : DownloadState()
    data class Failed(val reason: String) : DownloadState()
}

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pack = (applicationContext as RuqyaApp).container.audio
        return try {
            pack.download { done, total -> setProgress(workDataOf("done" to done, "total" to total)) }
            Result.success()
        } catch (e: Exception) {
            Result.failure(workDataOf("error" to (e.message ?: e.javaClass.simpleName)))
        }
    }
}
