package com.ultratul.lifekeeper.time

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ultratul.lifekeeper.data.TimeReminderItem
import java.util.concurrent.TimeUnit

object TimeReminderScheduler {
    fun schedule(context: Context, item: TimeReminderItem) {
        if (!item.enabled || item.done) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context, item)

        val trigger = normalizedTrigger(item.reminderAtMillis, item.repeatMode)
        when (item.repeatMode) {
            "매일" -> alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                trigger,
                TimeUnit.DAYS.toMillis(1),
                pendingIntent
            )
            "매주" -> alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                trigger,
                TimeUnit.DAYS.toMillis(7),
                pendingIntent
            )
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
                }
            }
        }
    }

    fun cancel(context: Context, id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, id))
    }

    private fun pendingIntent(context: Context, item: TimeReminderItem): PendingIntent {
        val intent = Intent(context, TimeReminderReceiver::class.java).apply {
            putExtra(TimeReminderReceiver.EXTRA_ID, item.id)
            putExtra(TimeReminderReceiver.EXTRA_TITLE, item.title)
            putExtra(TimeReminderReceiver.EXTRA_TIME, item.timeText)
            putExtra(TimeReminderReceiver.EXTRA_REPEAT, item.repeatMode)
            putExtra(TimeReminderReceiver.EXTRA_TARGET, item.target)
            putExtra(TimeReminderReceiver.EXTRA_NOTE, item.note)
        }
        return PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun pendingIntent(context: Context, id: Long): PendingIntent {
        val intent = Intent(context, TimeReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun normalizedTrigger(baseMillis: Long, repeatMode: String): Long {
        val now = System.currentTimeMillis()
        if (baseMillis > now) return baseMillis

        val interval = when (repeatMode) {
            "매일" -> TimeUnit.DAYS.toMillis(1)
            "매주" -> TimeUnit.DAYS.toMillis(7)
            else -> 0L
        }

        if (interval == 0L) return now + TimeUnit.MINUTES.toMillis(1)

        var next = baseMillis
        while (next <= now) next += interval
        return next
    }
}
