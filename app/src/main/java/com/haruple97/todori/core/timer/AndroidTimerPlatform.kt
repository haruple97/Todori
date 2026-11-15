package com.haruple97.todori.core.timer

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.haruple97.todori.R
import com.haruple97.todori.core.model.TimerPhase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidTimerPlatform @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: TimeSource
) : TimerPlatform {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val notifications = NotificationManagerCompat.from(context)

    override fun scheduleAlarm(triggerElapsedMillis: Long) {
        val intent = alarmIntent()
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerElapsedMillis, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerElapsedMillis, intent)
        }
    }

    override fun cancelAlarm() = alarmManager.cancel(alarmIntent())

    override fun showRunning(phase: TimerPhase, endElapsedMillis: Long) {
        val endWall = clock.wallClockMillis() + (endElapsedMillis - clock.elapsedRealtime())
        val notification = NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                context.getString(
                    when (phase) {
                        TimerPhase.Focus -> R.string.notif_focus_running
                        TimerPhase.ShortBreak -> R.string.notif_short_break_running
                        TimerPhase.LongBreak -> R.string.notif_long_break_running
                    }
                )
            )
            .setWhen(endWall)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(openAppIntent())
            .build()
        notify(ID_RUNNING, notification, ensureChannels = true)
    }

    override fun clearRunning() = notifications.cancel(ID_RUNNING)

    override fun showDone(phase: TimerPhase) {
        val breakEnded = phase != TimerPhase.Focus
        val notification = NotificationCompat.Builder(context, CHANNEL_DONE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(if (breakEnded) R.string.notif_break_done_title else R.string.notif_done_title))
            .setContentText(context.getString(if (breakEnded) R.string.notif_break_done_text else R.string.notif_done_text))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()
        notify(ID_DONE, notification, ensureChannels = true)
    }

    private fun notify(id: Int, notification: android.app.Notification, ensureChannels: Boolean) {
        if (ensureChannels) createChannels()
        if (!notifications.areNotificationsEnabled()) return
        @Suppress("MissingPermission")
        notifications.notify(id, notification)
    }

    private fun createChannels() {
        val progress = NotificationChannel(
            CHANNEL_PROGRESS,
            context.getString(R.string.notif_channel_progress),
            NotificationManager.IMPORTANCE_LOW
        )
        val done = NotificationChannel(
            CHANNEL_DONE,
            context.getString(R.string.notif_channel_done),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
        }
        notifications.createNotificationChannel(progress)
        notifications.createNotificationChannel(done)
    }

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_ALARM,
        Intent(context, TimerAlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun openAppIntent(): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        return PendingIntent.getActivity(context, REQUEST_OPEN, launch, PendingIntent.FLAG_IMMUTABLE)
    }

    private companion object {
        const val CHANNEL_PROGRESS = "timer_progress"
        const val CHANNEL_DONE = "timer_done"
        const val ID_RUNNING = 1
        const val ID_DONE = 2
        const val REQUEST_ALARM = 100
        const val REQUEST_OPEN = 101
    }
}
