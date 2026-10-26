package com.digitalbeavertz.ruqya.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.digitalbeavertz.ruqya.MainActivity
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.RuqyaApp
import com.digitalbeavertz.ruqya.core.nextReminder
import com.digitalbeavertz.ruqya.data.Routine
import com.digitalbeavertz.ruqya.widget.NextAdhkarWidget
import java.time.ZonedDateTime

/**
 * Daily reminders for the morning, evening and bedtime routines. One alarm at a time: it rings
 * for the next reminder, posts it and sets itself for the one after. No exact-alarm permission
 * is needed; Android may deliver it a few minutes late while the phone dozes.
 */
object Reminders {
    const val CHANNEL = "reminders"
    private const val EXTRA_ID = "routine"

    fun createChannel(context: Context) {
        val c = localized(context)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, c.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = c.getString(R.string.channel_reminders_desc)
            },
        )
    }

    /** Sets the alarm for the next enabled reminder, or clears it when none are on. */
    fun schedule(context: Context) {
        val prefs = (context.applicationContext as RuqyaApp).container.prefs
        val am = context.getSystemService(AlarmManager::class.java)
        val next = nextReminder(prefs.reminders, ZonedDateTime.now())
        if (next == null) {
            am.cancel(alarmIntent(context, null))
            return
        }
        val (r, at) = next
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toInstant().toEpochMilli(), alarmIntent(context, r.id))
    }

    private fun alarmIntent(context: Context, id: String?) = PendingIntent.getBroadcast(
        context, 1, Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_ID, id),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    fun post(context: Context, routine: Routine) {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val c = localized(context)
        val (title, text) = when (routine) {
            Routine.MORNING -> R.string.remind_morning_title to R.string.remind_morning_text
            Routine.EVENING -> R.string.remind_evening_title to R.string.remind_evening_text
            Routine.BEDTIME -> R.string.remind_bedtime_title to R.string.remind_bedtime_text
        }
        val open = PendingIntent.getActivity(
            context, routine.ordinal + 10,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, "run/${routine.id}")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(c.getString(title))
            .setContentText(c.getString(text))
            .setStyle(NotificationCompat.BigTextStyle().bigText(c.getString(text)))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(routine.ordinal + 1, n)
    }

    /** Notifications are built outside any activity, so apply the app language by hand. */
    private fun localized(context: Context): Context = RuqyaApp.localized(context)
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = (context.applicationContext as RuqyaApp).container.prefs
        Routine.of(intent.getStringExtra("routine").orEmpty())?.let { r ->
            // Skip it if the routine was already done today, or switched off meanwhile.
            if (prefs.reminderOn(r) && prefs.lastDone(r.id) != java.time.LocalDate.now()) Reminders.post(context, r)
        }
        Reminders.schedule(context)
        NextAdhkarWidget.update(context)
    }
}

/** Alarms are cleared when the phone restarts or the clock changes; set them again. */
class SystemEventsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.schedule(context)
        NextAdhkarWidget.update(context)
    }
}
