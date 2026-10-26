package com.digitalbeavertz.ruqya.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.digitalbeavertz.ruqya.MainActivity
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.RuqyaApp
import com.digitalbeavertz.ruqya.core.DailyReminder
import com.digitalbeavertz.ruqya.core.nextReminder
import com.digitalbeavertz.ruqya.data.Routine
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Home-screen widget: the next adhkar (morning, evening or before sleep) with a live countdown,
 * and today's three routines. The countdown is a Chronometer, so it ticks on the home screen
 * without waking the app; an alarm redraws the widget when the time comes.
 */
class NextAdhkarWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        render(context, manager, ids)
        scheduleRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) update(context)
    }

    override fun onDisabled(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(refreshIntent(context))
    }

    companion object {
        private const val ACTION_REFRESH = "com.digitalbeavertz.ruqya.widget.REFRESH"

        private val pills = listOf(
            Triple(Routine.MORNING, R.id.s_morning, R.string.tile_morning),
            Triple(Routine.EVENING, R.id.s_evening, R.string.tile_evening),
            Triple(Routine.BEDTIME, R.id.s_bedtime, R.string.tile_bedtime),
        )

        /** Redraws every placed widget, e.g. after a routine is finished or a time is changed. */
        fun update(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, NextAdhkarWidget::class.java))
            if (ids.isEmpty()) return
            render(context, manager, ids)
            scheduleRefresh(context)
        }

        private fun next(context: Context, now: ZonedDateTime): Pair<DailyReminder, ZonedDateTime>? {
            val prefs = (context.applicationContext as RuqyaApp).container.prefs
            return nextReminder(Routine.entries.map { DailyReminder(it.id, prefs.reminderMinute(it)) }, now)
        }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val container = (context.applicationContext as RuqyaApp).container
            val prefs = container.prefs
            val lc = RuqyaApp.localized(context)
            val now = ZonedDateTime.now()
            val (reminder, at) = next(context, now) ?: return
            val routine = Routine.of(reminder.id) ?: return
            val session = container.content.session(routine.id)
            val chosen = session?.chosen(prefs.chosenSteps(routine.id))?.size ?: 0
            val left = Duration.between(now, at).toMillis().coerceAtLeast(0)
            val today = LocalDate.now()

            ids.forEach { id ->
                val v = RemoteViews(context.packageName, R.layout.widget_next)
                v.setTextViewText(R.id.label, lc.getString(R.string.widget_next))
                v.setImageViewResource(
                    R.id.icon,
                    when (routine) {
                        Routine.MORNING -> R.drawable.ic_widget_sun
                        Routine.EVENING -> R.drawable.ic_widget_sunset
                        Routine.BEDTIME -> R.drawable.ic_widget_moon
                    },
                )
                v.setTextViewText(R.id.title, session?.title?.get(prefs.lang).orEmpty())
                v.setChronometer(R.id.countdown, SystemClock.elapsedRealtime() + left, null, true)
                v.setChronometerCountDown(R.id.countdown, true)
                v.setTextViewText(
                    R.id.detail,
                    "%02d:%02d".format(reminder.minute / 60, reminder.minute % 60) + "  ·  " +
                        lc.resources.getQuantityString(R.plurals.adhkar_count, chosen, chosen),
                )
                pills.forEachIndexed { i, (r, viewId, label) ->
                    val done = prefs.lastDone(r.id) == today
                    v.setTextViewText(viewId, lc.getString(label))
                    v.setInt(viewId, "setBackgroundResource", if (done) R.drawable.widget_pill_done else R.drawable.widget_pill_todo)
                    v.setTextColor(viewId, if (done) 0xFF14261A.toInt() else 0xFFFFFFFF.toInt())
                    v.setOnClickPendingIntent(viewId, open(context, "run/${r.id}", 20 + i))
                }
                v.setOnClickPendingIntent(R.id.root, open(context, "run/${routine.id}", 10))
                manager.updateAppWidget(id, v)
            }
        }

        private fun open(context: Context, route: String, request: Int) = PendingIntent.getActivity(
            context, request,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, route)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        private fun refreshIntent(context: Context) = PendingIntent.getBroadcast(
            context, 2, Intent(context, NextAdhkarWidget::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        /** Redraw just after the countdown reaches its time, to move on to the next adhkar. */
        private fun scheduleRefresh(context: Context) {
            val at = next(context, ZonedDateTime.now())?.second ?: return
            context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
                AlarmManager.RTC, at.toInstant().toEpochMilli() + 1_000, refreshIntent(context),
            )
        }
    }
}
