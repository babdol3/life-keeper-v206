package com.ultratul.lifekeeper.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ultratul.lifekeeper.data.ItemSyncState
import com.ultratul.lifekeeper.data.LifeKeeperDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * v17 알림 액션 처리기.
 *
 * 알림 버튼:
 * - 완료: time/shopping/place_task/travel_action DB 완료 처리
 * - 10분 뒤: 같은 알림 내용을 10분 뒤 다시 표시
 * - 앱 열기: NotificationHelper에서 launcher intent로 처리
 */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action.orEmpty()) {
            ACTION_DONE -> handleDone(context, intent)
            ACTION_SNOOZE -> handleSnooze(context, intent)
            ACTION_SHOW_SNOOZED -> showSnoozed(context, intent)
        }
    }

    private fun handleDone(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val targets = parseTargets(intent.getStringExtra(NotificationHelper.EXTRA_ACTION_TARGETS).orEmpty())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (targets.isEmpty()) {
                    NotificationHelper.show(context, "완료 처리", "완료할 항목 정보가 없는 알림이에요.")
                    return@launch
                }

                val dao = LifeKeeperDatabase.get(context).dao()
                val now = System.currentTimeMillis()
                var doneCount = 0

                targets.forEach { target ->
                    when (target.type) {
                        "time" -> {
                            dao.updateTimeReminderDone(target.id, true, now)
                            markDonePending(dao, "time", target.id, now)
                            doneCount++
                        }
                        "shopping" -> {
                            dao.updateShoppingDone(target.id, true, now)
                            markDonePending(dao, "shopping", target.id, now)
                            doneCount++
                        }
                        "place_task", "task" -> {
                            dao.updatePlaceTaskDone(target.id, true, now)
                            markDonePending(dao, "place_task", target.id, now)
                            doneCount++
                        }
                        "travel", "travel_action" -> {
                            dao.updateTravelActionDone(target.id, true, now)
                            markDonePending(dao, "travel_action", target.id, now)
                            doneCount++
                        }
                    }
                }

                NotificationHelper.show(
                    context = context,
                    title = "완료 처리",
                    message = "${doneCount}개 항목을 완료 처리했어요."
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun markDonePending(
        dao: com.ultratul.lifekeeper.data.LifeKeeperDao,
        entityType: String,
        localId: Long,
        now: Long
    ) {
        dao.upsertItemSyncState(
            ItemSyncState(
                entityType = entityType,
                localId = localId,
                shared = true,
                updatedAt = now,
                updatedBy = "알림",
                syncStatus = "done_pending"
            )
        )
    }

    private fun handleSnooze(context: Context, intent: Intent) {
        val title = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TITLE).orEmpty().ifBlank { "생활비서" }
        val message = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_MESSAGE).orEmpty()
        val channelId = intent.getStringExtra(NotificationHelper.EXTRA_CHANNEL_ID).orEmpty()
            .ifBlank { NotificationHelper.CHANNEL_LIFE }
        val targets = intent.getStringExtra(NotificationHelper.EXTRA_ACTION_TARGETS).orEmpty()

        val showIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SHOW_SNOOZED
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_TITLE, title)
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_MESSAGE, message)
            putExtra(NotificationHelper.EXTRA_CHANNEL_ID, channelId)
            putExtra(NotificationHelper.EXTRA_ACTION_TARGETS, targets)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val requestCode = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, showIntent, flags)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }

        NotificationHelper.show(
            context = context,
            title = "10분 뒤 다시 알림",
            message = "10분 뒤 다시 알려줄게요."
        )
    }

    private fun showSnoozed(context: Context, intent: Intent) {
        NotificationHelper.show(
            context = context,
            title = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TITLE).orEmpty().ifBlank { "생활비서" },
            message = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_MESSAGE).orEmpty().ifBlank { "다시 알려드려요." },
            channelId = intent.getStringExtra(NotificationHelper.EXTRA_CHANNEL_ID).orEmpty().ifBlank { NotificationHelper.CHANNEL_LIFE },
            actionTargets = intent.getStringExtra(NotificationHelper.EXTRA_ACTION_TARGETS)
        )
    }

    private data class ActionTarget(
        val type: String,
        val id: Long
    )

    private fun parseTargets(raw: String): List<ActionTarget> {
        if (raw.isBlank()) return emptyList()

        return raw.split(",")
            .mapNotNull { token ->
                val parts = token.trim().split(":", limit = 2)
                if (parts.size != 2) return@mapNotNull null
                val id = parts[1].toLongOrNull() ?: return@mapNotNull null
                ActionTarget(parts[0], id)
            }
            .distinct()
    }

    companion object {
        const val ACTION_DONE = "com.ultratul.lifekeeper.notification.ACTION_DONE"
        const val ACTION_SNOOZE = "com.ultratul.lifekeeper.notification.ACTION_SNOOZE"
        const val ACTION_SHOW_SNOOZED = "com.ultratul.lifekeeper.notification.ACTION_SHOW_SNOOZED"
    }
}
