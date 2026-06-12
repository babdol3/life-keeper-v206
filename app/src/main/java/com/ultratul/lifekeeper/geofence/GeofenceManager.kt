package com.ultratul.lifekeeper.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.ultratul.lifekeeper.data.PlaceTaskItem
import com.ultratul.lifekeeper.data.ShoppingItem
import com.ultratul.lifekeeper.data.TravelActionItem
import com.ultratul.lifekeeper.notification.NotificationHelper

object GeofenceManager {
    fun ensureNotificationChannel(context: Context) {
        NotificationHelper.createChannels(context)
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            9104,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun buildGeofence(
        requestId: String,
        latitude: Double?,
        longitude: Double?,
        radiusMeters: Int
    ): Geofence? {
        val lat = latitude ?: return null
        val lng = longitude ?: return null

        return Geofence.Builder()
            .setRequestId(requestId)
            .setCircularRegion(lat, lng, radiusMeters.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setLoiteringDelay(30_000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .build()
    }

    fun registerShoppingGeofence(context: Context, item: ShoppingItem) {
        val geofence = buildGeofence(
            requestId = "shopping:${item.id}:${item.placeName}:${item.name}",
            latitude = item.latitude,
            longitude = item.longitude,
            radiusMeters = item.radiusMeters
        ) ?: return

        addGeofence(context, geofence)
    }

    fun registerTaskGeofence(context: Context, item: PlaceTaskItem) {
        val geofence = buildGeofence(
            requestId = "task:${item.id}:${item.placeName}:${item.title}",
            latitude = item.latitude,
            longitude = item.longitude,
            radiusMeters = item.radiusMeters
        ) ?: return

        addGeofence(context, geofence)
    }


    fun registerTravelGeofence(context: Context, item: TravelActionItem) {
        val geofence = buildGeofence(
            requestId = "travel:${item.id}:${item.placeName}:${item.kind}:${item.title}",
            latitude = item.latitude,
            longitude = item.longitude,
            radiusMeters = item.radiusMeters
        ) ?: return

        addGeofence(context, geofence)
    }

    private fun addGeofence(context: Context, geofence: Geofence) {
        if (!hasLocationPermission(context)) return

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        try {
            LocationServices.getGeofencingClient(context)
                .addGeofences(request, pendingIntent(context))
        } catch (_: SecurityException) {
        }
    }

    fun registerAll(
        context: Context,
        shoppingItems: List<ShoppingItem>,
        taskItems: List<PlaceTaskItem>,
        travelActions: List<TravelActionItem> = emptyList()
    ) {
        if (!hasLocationPermission(context)) return
        shoppingItems.forEach { registerShoppingGeofence(context, it) }
        taskItems.forEach { registerTaskGeofence(context, it) }
        travelActions.forEach { registerTravelGeofence(context, it) }
    }

    fun refreshAll(
        context: Context,
        shoppingItems: List<ShoppingItem>,
        taskItems: List<PlaceTaskItem>,
        travelActions: List<TravelActionItem> = emptyList()
    ) {
        if (!hasLocationPermission(context)) return

        try {
            LocationServices.getGeofencingClient(context)
                .removeGeofences(pendingIntent(context))
                .addOnCompleteListener {
                    registerAll(context, shoppingItems, taskItems, travelActions)
                }
        } catch (_: SecurityException) {
        } catch (_: Exception) {
            registerAll(context, shoppingItems, taskItems, travelActions)
        }
    }

    fun refreshShoppingGeofences(context: Context, items: List<ShoppingItem>) {
        refreshAll(context, items, emptyList())
    }

    fun removeAll(context: Context) {
        try {
            LocationServices.getGeofencingClient(context)
                .removeGeofences(pendingIntent(context))
        } catch (_: Exception) {
        }
    }
}
