package com.ultratul.lifekeeper.backup

import com.ultratul.lifekeeper.data.FamilyShareProfile
import com.ultratul.lifekeeper.data.PlaceItem
import com.ultratul.lifekeeper.data.PlaceTaskItem
import com.ultratul.lifekeeper.data.ShoppingItem
import com.ultratul.lifekeeper.data.TimeReminderItem
import com.ultratul.lifekeeper.data.TravelActionItem
import com.ultratul.lifekeeper.data.TravelChecklistItem
import com.ultratul.lifekeeper.data.TravelMemoItem
import com.ultratul.lifekeeper.data.TravelPlan
import com.ultratul.lifekeeper.data.TravelPlaceItem
import com.ultratul.lifekeeper.data.TravelReservationItem
import org.json.JSONArray
import org.json.JSONObject

/**
 * v20 JSON 백업/복원 매니저.
 *
 * 안정성을 위해 Android 파일 권한/SAF를 직접 붙이기 전 단계로,
 * 클립보드 기반 JSON 백업/복원을 지원합니다.
 */
object LifeKeeperBackupManager {
    fun exportToJson(
        profile: FamilyShareProfile?,
        places: List<PlaceItem>,
        shoppingItems: List<ShoppingItem>,
        placeTasks: List<PlaceTaskItem>,
        travelPlans: List<TravelPlan>,
        travelPlaces: List<TravelPlaceItem>,
        travelActions: List<TravelActionItem>,
        travelChecklist: List<TravelChecklistItem>,
        travelReservations: List<TravelReservationItem>,
        travelMemos: List<TravelMemoItem>,
        timeReminders: List<TimeReminderItem>
    ): String {
        return JSONObject()
            .put("version", 20)
            .put("app", "LifeKeeper")
            .put("exportedAt", System.currentTimeMillis())
            .put("familyCode", profile?.inviteCode.orEmpty())
            .put("places", JSONArray(places.map { place ->
                JSONObject()
                    .put("name", place.name)
                    .put("category", place.category)
                    .put("latitude", place.latitude)
                    .put("longitude", place.longitude)
                    .put("radiusMeters", place.radiusMeters)
                    .put("enabled", place.enabled)
            }))
            .put("shoppingItems", JSONArray(shoppingItems.map { item ->
                JSONObject()
                    .put("name", item.name)
                    .put("category", item.category)
                    .put("placeName", item.placeName)
                    .put("latitude", item.latitude)
                    .put("longitude", item.longitude)
                    .put("radiusMeters", item.radiusMeters)
                    .put("done", item.done)
            }))
            .put("placeTasks", JSONArray(placeTasks.map { item ->
                JSONObject()
                    .put("title", item.title)
                    .put("category", item.category)
                    .put("placeName", item.placeName)
                    .put("latitude", item.latitude)
                    .put("longitude", item.longitude)
                    .put("radiusMeters", item.radiusMeters)
                    .put("done", item.done)
                    .put("note", item.note)
            }))
            .put("travelPlans", JSONArray(travelPlans.map { item ->
                JSONObject()
                    .put("title", item.title)
                    .put("destination", item.destination)
                    .put("startDateMillis", item.startDateMillis)
                    .put("endDateMillis", item.endDateMillis)
                    .put("note", item.note)
            }))
            .put("travelPlaces", JSONArray(travelPlaces.map { item ->
                JSONObject()
                    .put("travelTitle", item.travelTitle)
                    .put("name", item.name)
                    .put("category", item.category)
                    .put("dayIndex", item.dayIndex)
                    .put("latitude", item.latitude)
                    .put("longitude", item.longitude)
                    .put("radiusMeters", item.radiusMeters)
                    .put("note", item.note)
            }))
            .put("travelActions", JSONArray(travelActions.map { item ->
                JSONObject()
                    .put("travelTitle", item.travelTitle)
                    .put("placeName", item.placeName)
                    .put("kind", item.kind)
                    .put("title", item.title)
                    .put("dayIndex", item.dayIndex)
                    .put("done", item.done)
                    .put("note", item.note)
            }))
            .put("travelChecklist", JSONArray(travelChecklist.map { item ->
                JSONObject()
                    .put("travelTitle", item.travelTitle)
                    .put("dayIndex", item.dayIndex)
                    .put("category", item.category)
                    .put("title", item.title)
                    .put("done", item.done)
                    .put("note", item.note)
            }))
            .put("travelReservations", JSONArray(travelReservations.map { item ->
                JSONObject()
                    .put("travelTitle", item.travelTitle)
                    .put("placeName", item.placeName)
                    .put("dayIndex", item.dayIndex)
                    .put("title", item.title)
                    .put("reservationNo", item.reservationNo)
                    .put("timeText", item.timeText)
                    .put("ticketInfo", item.ticketInfo)
                    .put("done", item.done)
                    .put("note", item.note)
            }))
            .put("travelMemos", JSONArray(travelMemos.map { item ->
                JSONObject()
                    .put("travelTitle", item.travelTitle)
                    .put("dayIndex", item.dayIndex)
                    .put("category", item.category)
                    .put("title", item.title)
                    .put("content", item.content)
            }))
            .put("timeReminders", JSONArray(timeReminders.map { item ->
                JSONObject()
                    .put("title", item.title)
                    .put("timeText", item.timeText)
                    .put("reminderAtMillis", item.reminderAtMillis)
                    .put("repeatMode", item.repeatMode)
                    .put("category", item.category)
                    .put("target", item.target)
                    .put("note", item.note)
                    .put("enabled", item.enabled)
                    .put("done", item.done)
            }))
            .toString(2)
    }

    fun importFromJson(json: String): LifeKeeperBackupPayload {
        val root = JSONObject(json)
        return LifeKeeperBackupPayload(
            places = root.optJSONArray("places").toPlaceList(),
            shoppingItems = root.optJSONArray("shoppingItems").toShoppingList(),
            placeTasks = root.optJSONArray("placeTasks").toPlaceTaskList(),
            travelPlans = root.optJSONArray("travelPlans").toTravelPlanList(),
            travelPlaces = root.optJSONArray("travelPlaces").toTravelPlaceList(),
            travelActions = root.optJSONArray("travelActions").toTravelActionList(),
            travelChecklist = root.optJSONArray("travelChecklist").toTravelChecklistList(),
            travelReservations = root.optJSONArray("travelReservations").toTravelReservationList(),
            travelMemos = root.optJSONArray("travelMemos").toTravelMemoList(),
            timeReminders = root.optJSONArray("timeReminders").toTimeReminderList()
        )
    }
}

data class LifeKeeperBackupPayload(
    val places: List<PlaceItem> = emptyList(),
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val placeTasks: List<PlaceTaskItem> = emptyList(),
    val travelPlans: List<TravelPlan> = emptyList(),
    val travelPlaces: List<TravelPlaceItem> = emptyList(),
    val travelActions: List<TravelActionItem> = emptyList(),
    val travelChecklist: List<TravelChecklistItem> = emptyList(),
    val travelReservations: List<TravelReservationItem> = emptyList(),
    val travelMemos: List<TravelMemoItem> = emptyList(),
    val timeReminders: List<TimeReminderItem> = emptyList()
)

private fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else List(length()) { getJSONObject(it) }

private fun JSONObject.optDoubleOrNull(key: String): Double? =
    if (isNull(key)) null else optDouble(key)

private fun JSONArray?.toPlaceList(): List<PlaceItem> = objects().map {
    PlaceItem(
        name = it.optString("name"),
        category = it.optString("category", "기타"),
        latitude = it.optDoubleOrNull("latitude"),
        longitude = it.optDoubleOrNull("longitude"),
        radiusMeters = it.optInt("radiusMeters", 300),
        enabled = it.optBoolean("enabled", true)
    )
}

private fun JSONArray?.toShoppingList(): List<ShoppingItem> = objects().map {
    ShoppingItem(
        name = it.optString("name"),
        category = it.optString("category", "기타"),
        placeName = it.optString("placeName", "장소 미지정"),
        latitude = it.optDoubleOrNull("latitude"),
        longitude = it.optDoubleOrNull("longitude"),
        radiusMeters = it.optInt("radiusMeters", 300),
        done = it.optBoolean("done", false)
    )
}

private fun JSONArray?.toPlaceTaskList(): List<PlaceTaskItem> = objects().map {
    PlaceTaskItem(
        title = it.optString("title"),
        category = it.optString("category", "기타"),
        placeName = it.optString("placeName", "장소 미지정"),
        latitude = it.optDoubleOrNull("latitude"),
        longitude = it.optDoubleOrNull("longitude"),
        radiusMeters = it.optInt("radiusMeters", 300),
        done = it.optBoolean("done", false),
        note = it.optString("note")
    )
}

private fun JSONArray?.toTravelPlanList(): List<TravelPlan> = objects().map {
    TravelPlan(
        title = it.optString("title"),
        destination = it.optString("destination"),
        startDateMillis = it.optLong("startDateMillis", System.currentTimeMillis()),
        endDateMillis = it.optLong("endDateMillis", System.currentTimeMillis()),
        note = it.optString("note")
    )
}

private fun JSONArray?.toTravelPlaceList(): List<TravelPlaceItem> = objects().map {
    TravelPlaceItem(
        travelPlanId = 0,
        travelTitle = it.optString("travelTitle"),
        name = it.optString("name"),
        category = it.optString("category", "여행"),
        dayIndex = it.optInt("dayIndex", 1),
        latitude = it.optDoubleOrNull("latitude"),
        longitude = it.optDoubleOrNull("longitude"),
        radiusMeters = it.optInt("radiusMeters", 300),
        note = it.optString("note")
    )
}

private fun JSONArray?.toTravelActionList(): List<TravelActionItem> = objects().map {
    TravelActionItem(
        travelPlanId = 0,
        travelPlaceId = null,
        travelTitle = it.optString("travelTitle"),
        placeName = it.optString("placeName", "여행 장소"),
        kind = it.optString("kind", "할 일"),
        title = it.optString("title"),
        latitude = null,
        longitude = null,
        dayIndex = it.optInt("dayIndex", 1),
        done = it.optBoolean("done", false),
        note = it.optString("note")
    )
}

private fun JSONArray?.toTravelChecklistList(): List<TravelChecklistItem> = objects().map {
    TravelChecklistItem(
        travelPlanId = 0,
        travelTitle = it.optString("travelTitle"),
        dayIndex = it.optInt("dayIndex", 0),
        category = it.optString("category", "준비물"),
        title = it.optString("title"),
        done = it.optBoolean("done", false),
        note = it.optString("note")
    )
}

private fun JSONArray?.toTravelReservationList(): List<TravelReservationItem> = objects().map {
    TravelReservationItem(
        travelPlanId = 0,
        travelTitle = it.optString("travelTitle"),
        placeName = it.optString("placeName"),
        dayIndex = it.optInt("dayIndex", 1),
        title = it.optString("title"),
        reservationNo = it.optString("reservationNo"),
        timeText = it.optString("timeText"),
        ticketInfo = it.optString("ticketInfo"),
        done = it.optBoolean("done", false),
        note = it.optString("note")
    )
}

private fun JSONArray?.toTravelMemoList(): List<TravelMemoItem> = objects().map {
    TravelMemoItem(
        travelPlanId = 0,
        travelTitle = it.optString("travelTitle"),
        dayIndex = it.optInt("dayIndex", 0),
        category = it.optString("category", "메모"),
        title = it.optString("title"),
        content = it.optString("content")
    )
}

private fun JSONArray?.toTimeReminderList(): List<TimeReminderItem> = objects().map {
    TimeReminderItem(
        title = it.optString("title"),
        timeText = it.optString("timeText", "09:00"),
        reminderAtMillis = it.optLong("reminderAtMillis", System.currentTimeMillis()),
        repeatMode = it.optString("repeatMode", "한 번"),
        category = it.optString("category", "오늘"),
        target = it.optString("target", "생활"),
        note = it.optString("note"),
        enabled = it.optBoolean("enabled", true),
        done = it.optBoolean("done", false)
    )
}
