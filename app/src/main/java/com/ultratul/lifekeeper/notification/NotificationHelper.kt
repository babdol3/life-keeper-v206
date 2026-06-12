package com.ultratul.lifekeeper.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ultratul.lifekeeper.R

object NotificationHelper {
    /*
     * v17: 알림 액션 버튼을 실제 항목 ID와 연결합니다.
     * Android 8.0+에서는 한 번 생성된 NotificationChannel의 진동/소리 설정이 앱 업데이트만으로
     * 잘 바뀌지 않을 수 있어 기존 v10 채널 ID는 유지합니다.
     */
    const val CHANNEL_LIFE = "life_keeper_alerts_v10"
    const val CHANNEL_GEOFENCE = "life_keeper_geofence_v10"

    const val EXTRA_ACTION_TARGETS = "life_keeper_action_targets"
    const val EXTRA_NOTIFICATION_TITLE = "life_keeper_notification_title"
    const val EXTRA_NOTIFICATION_MESSAGE = "life_keeper_notification_message"
    const val EXTRA_CHANNEL_ID = "life_keeper_channel_id"

    private val TIME_VIBRATION_PATTERN = longArrayOf(0, 450, 160, 450)
    private val PLACE_VIBRATION_PATTERN = longArrayOf(0, 180, 100, 180, 100, 280)

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val life = NotificationChannel(
            CHANNEL_LIFE,
            "생활비서 시간/일정 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "시간 알림, 일정 알림, 생활 관리 알림"
            enableVibration(true)
            vibrationPattern = TIME_VIBRATION_PATTERN
        }

        val geofence = NotificationChannel(
            CHANNEL_GEOFENCE,
            "장소 기반 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "지정 장소 근처에 도착했을 때 살 것과 할 일을 알려주는 알림"
            enableVibration(true)
            vibrationPattern = PLACE_VIBRATION_PATTERN
        }

        manager.createNotificationChannel(life)
        manager.createNotificationChannel(geofence)
    }

    fun show(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_LIFE,
        actionTargets: String? = null
    ) {
        createChannels(context)

        val vibrationPattern = if (channelId == CHANNEL_GEOFENCE) {
            PLACE_VIBRATION_PATTERN
        } else {
            TIME_VIBRATION_PATTERN
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DONE
            putExtra(EXTRA_ACTION_TARGETS, actionTargets.orEmpty())
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_MESSAGE, message)
            putExtra(EXTRA_CHANNEL_ID, channelId)
        }

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(EXTRA_ACTION_TARGETS, actionTargets.orEmpty())
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_MESSAGE, message)
            putExtra(EXTRA_CHANNEL_ID, channelId)
        }

        val openIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(vibrationPattern)
            .setDefaults(0)
            .setContentIntent(openIntent?.let { PendingIntent.getActivity(context, 100, it, flags) })
            .addAction(
                R.drawable.ic_launcher,
                "완료",
                PendingIntent.getBroadcast(context, uniqueRequestCode(101, actionTargets), doneIntent, flags)
            )
            .addAction(
                R.drawable.ic_launcher,
                "10분 뒤",
                PendingIntent.getBroadcast(context, uniqueRequestCode(102, actionTargets), snoozeIntent, flags)
            )
            .apply {
                if (openIntent != null) {
                    addAction(
                        R.drawable.ic_launcher,
                        "앱 열기",
                        PendingIntent.getActivity(context, uniqueRequestCode(103, actionTargets), openIntent, flags)
                    )
                }
            }
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(uniqueRequestCode(900, "$title$message${System.currentTimeMillis()}"), notification)
        } catch (_: SecurityException) {
            // Android 13+ notification permission이 없으면 알림 표시가 제한됩니다.
        }
    }

    private fun uniqueRequestCode(seed: Int, value: String?): Int {
        val hash = value.orEmpty().hashCode()
        return (seed * 100_000 + (hash and 0x7fffffff) % 100_000)
    }
}
