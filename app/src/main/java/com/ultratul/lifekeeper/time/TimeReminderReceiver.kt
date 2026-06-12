package com.ultratul.lifekeeper.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ultratul.lifekeeper.notification.NotificationHelper

class TimeReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "시간 알림" }
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        val repeat = intent.getStringExtra(EXTRA_REPEAT).orEmpty()
        val target = intent.getStringExtra(EXTRA_TARGET).orEmpty()
        val note = intent.getStringExtra(EXTRA_NOTE).orEmpty()

        val message = buildString {
            if (time.isNotBlank()) append("$time · ")
            if (repeat.isNotBlank()) append("$repeat · ")
            if (target.isNotBlank()) append(target)
            if (note.isNotBlank()) {
                if (isNotEmpty()) append("\n")
                append(note)
            }
        }.ifBlank { "등록한 시간 알림입니다." }

        NotificationHelper.show(
            context = context,
            title = "⏰ $title",
            message = message,
            channelId = NotificationHelper.CHANNEL_LIFE,
            actionTargets = if (id > 0) "time:$id" else null
        )
    }

    companion object {
        const val EXTRA_ID = "time_reminder_id"
        const val EXTRA_TITLE = "time_reminder_title"
        const val EXTRA_TIME = "time_reminder_time"
        const val EXTRA_REPEAT = "time_reminder_repeat"
        const val EXTRA_TARGET = "time_reminder_target"
        const val EXTRA_NOTE = "time_reminder_note"
    }
}
