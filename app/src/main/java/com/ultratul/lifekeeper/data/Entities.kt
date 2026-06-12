package com.ultratul.lifekeeper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val latitude: Double?,
    val longitude: Double?,
    val radiusMeters: Int = 300,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val placeName: String,
    val placeId: Long? = null,
    val latitude: Double?,
    val longitude: Double?,
    val radiusMeters: Int = 300,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "place_tasks")
data class PlaceTaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val placeName: String,
    val placeId: Long? = null,
    val latitude: Double?,
    val longitude: Double?,
    val radiusMeters: Int = 300,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val note: String = ""
)

@Entity(tableName = "record_items")
data class RecordItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val emoji: String = "⏱️",
    val lastDateMillis: Long,
    val cycleDays: Int = 30,
    val note: String = ""
)

@Entity(tableName = "home_maintenance_items")
data class HomeMaintenanceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val emoji: String = "🛠️",
    val lastDateMillis: Long,
    val cycleDays: Int = 180,
    val note: String = ""
)

@Entity(tableName = "family_schedule_items")
data class FamilyScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val familyTag: String = "나",
    val dueDateMillis: Long,
    val note: String = "",
    val done: Boolean = false
)

@Entity(tableName = "stored_item_locations")
data class StoredItemLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemName: String,
    val place: String,
    val memo: String = "",
    val photoUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_items")
data class DeliveryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val orderedAt: Long = System.currentTimeMillis(),
    val expectedAt: Long? = null,
    val status: String = "주문",
    val done: Boolean = false
)


@Entity(tableName = "travel_plans")
data class TravelPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val destination: String = "",
    val startDateMillis: Long = System.currentTimeMillis(),
    val endDateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "travel_places")
data class TravelPlaceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val travelPlanId: Long,
    val travelTitle: String,
    val name: String,
    val category: String = "여행",
    val dayIndex: Int = 1,
    val latitude: Double?,
    val longitude: Double?,
    val radiusMeters: Int = 300,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "travel_actions")
data class TravelActionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val travelPlanId: Long,
    val travelPlaceId: Long?,
    val travelTitle: String,
    val placeName: String,
    val kind: String = "할 일",
    val title: String,
    val latitude: Double?,
    val longitude: Double?,
    val radiusMeters: Int = 300,
    val dayIndex: Int = 1,
    val done: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)


@Entity(tableName = "travel_checklist_items")
data class TravelChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val travelPlanId: Long,
    val travelTitle: String,
    val dayIndex: Int = 0,
    val category: String = "준비물",
    val title: String,
    val done: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "travel_reservations")
data class TravelReservationItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val travelPlanId: Long,
    val travelTitle: String,
    val placeName: String = "",
    val dayIndex: Int = 1,
    val title: String,
    val reservationNo: String = "",
    val timeText: String = "",
    val ticketInfo: String = "",
    val done: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "travel_memos")
data class TravelMemoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val travelPlanId: Long,
    val travelTitle: String,
    val dayIndex: Int = 0,
    val category: String = "메모",
    val title: String,
    val content: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "time_reminders")
data class TimeReminderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timeText: String = "09:00",
    val reminderAtMillis: Long,
    val repeatMode: String = "한 번",
    val category: String = "오늘",
    val target: String = "생활",
    val note: String = "",
    val enabled: Boolean = true,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)


@Entity(tableName = "family_share_profile")
data class FamilyShareProfile(
    @PrimaryKey val id: Long = 1,
    val familyName: String = "",
    val myName: String = "",
    val inviteCode: String = "",
    val role: String = "owner",
    val enabled: Boolean = false,
    val sharePlaces: Boolean = true,
    val shareShopping: Boolean = true,
    val sharePlaceTasks: Boolean = true,
    val shareTravel: Boolean = true,
    val shareTimeReminders: Boolean = false,
    val shareStoredItems: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_members")
data class FamilyMemberItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyCode: String,
    val name: String,
    val role: String = "member",
    val deviceLabel: String = "Android",
    val lastSeenAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_activity")
data class FamilyActivityItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyCode: String,
    val actorName: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "item_sync_states")
data class ItemSyncState(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val localId: Long,
    val familyCode: String = "",
    val shared: Boolean = true,
    val assignee: String = "전체",
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "나",
    val deletedAt: Long? = null,
    val syncStatus: String = "local"
)

@Entity(tableName = "mega_feature_flags")
data class MegaFeatureFlag(
    @PrimaryKey val key: String,
    val enabled: Boolean = false,
    val label: String = "",
    val note: String = ""
)
