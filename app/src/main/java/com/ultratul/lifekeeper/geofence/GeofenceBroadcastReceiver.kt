package com.ultratul.lifekeeper.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.ultratul.lifekeeper.notification.NotificationHelper
import java.util.concurrent.TimeUnit

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        if (
            event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
        ) {
            val entries = event.triggeringGeofences.orEmpty().mapNotNull { geofence ->
                parseRequestId(geofence.requestId)
            }

            if (entries.isEmpty()) return

            val grouped = entries.groupBy { it.placeName }
            grouped.forEach { (placeName, items) ->
                if (!canNotifyPlace(context, placeName)) return@forEach

                val shopping = items.filter { it.type == "shopping" }.map { it.title }.distinct()
                val tasks = items.filter { it.type == "task" }.map { it.title }.distinct()
                val travelItems = items.filter { it.type == "travel" }

                val parts = buildList {
                    if (shopping.isNotEmpty()) add("살 것: ${shopping.joinToString(", ")}")
                    if (tasks.isNotEmpty()) add("할 일: ${tasks.joinToString(", ")}")
                    travelItems.groupBy { it.kind }.forEach { (kind, entries) ->
                        add("$kind: ${entries.map { it.title }.distinct().joinToString(", ")}")
                    }
                }

                val message = if (parts.isNotEmpty()) parts.joinToString("\n") else "이 장소에서 확인할 항목이 있어요."
                val actionTargets = items.mapNotNull { actionTargetFor(it) }.distinct().joinToString(",")

                markPlaceNotified(context, placeName)

                NotificationHelper.show(
                    context = context,
                    title = "📍 $placeName 근처입니다",
                    message = message,
                    channelId = NotificationHelper.CHANNEL_GEOFENCE,
                    actionTargets = actionTargets.ifBlank { null }
                )
            }
        }
    }

    private data class PlaceAlert(
        val type: String,
        val itemId: String,
        val placeName: String,
        val title: String,
        val kind: String = "할 일"
    )

    private fun parseRequestId(id: String): PlaceAlert? {
        // Format: shopping:{itemId}:{placeName}:{itemName}
        // Format: task:{itemId}:{placeName}:{taskTitle}
        // Format: travel:{itemId}:{placeName}:{kind}:{title}
        val parts = id.split(":", limit = 5)
        if (parts.size < 4) return null
        val type = parts[0]
        if (type != "shopping" && type != "task" && type != "travel") return null
        return if (type == "travel" && parts.size >= 5) {
            PlaceAlert(
                type = type,
                itemId = parts[1],
                placeName = parts[2],
                kind = parts[3],
                title = parts[4]
            )
        } else {
            PlaceAlert(
                type = type,
                itemId = parts[1],
                placeName = parts[2],
                title = parts[3]
            )
        }
    }

    private fun actionTargetFor(alert: PlaceAlert): String? {
        val id = alert.itemId.toLongOrNull() ?: return null
        val type = when (alert.type) {
            "shopping" -> "shopping"
            "task" -> "place_task"
            "travel" -> "travel_action"
            else -> return null
        }
        return "$type:$id"
    }

    private fun canNotifyPlace(context: Context, placeName: String): Boolean {
        val prefs = context.getSharedPreferences("geofence_cooldown", Context.MODE_PRIVATE)
        val key = "last_alert_$placeName"
        val last = prefs.getLong(key, 0L)
        val now = System.currentTimeMillis()
        val cooldownMillis = TimeUnit.HOURS.toMillis(24)
        return now - last >= cooldownMillis
    }

    private fun markPlaceNotified(context: Context, placeName: String) {
        context.getSharedPreferences("geofence_cooldown", Context.MODE_PRIVATE)
            .edit()
            .putLong("last_alert_$placeName", System.currentTimeMillis())
            .apply()
    }
}
