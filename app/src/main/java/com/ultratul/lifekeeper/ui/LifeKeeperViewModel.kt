package com.ultratul.lifekeeper.ui

import android.app.Application
import android.content.pm.PackageManager
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.location.Location
import android.location.Geocoder
import android.os.Build
import android.Manifest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.ultratul.lifekeeper.backup.LifeKeeperBackupManager
import com.ultratul.lifekeeper.data.DeliveryItem
import com.ultratul.lifekeeper.data.FamilyScheduleItem
import com.ultratul.lifekeeper.data.FamilyShareProfile
import com.ultratul.lifekeeper.data.FamilyMemberItem
import com.ultratul.lifekeeper.data.FamilyActivityItem
import com.ultratul.lifekeeper.data.HomeMaintenanceItem
import com.ultratul.lifekeeper.data.MegaFeatureFlag
import com.ultratul.lifekeeper.data.ItemSyncState
import com.ultratul.lifekeeper.data.LifeKeeperDatabase
import com.ultratul.lifekeeper.data.LifeKeeperRepository
import com.ultratul.lifekeeper.data.PlaceItem
import com.ultratul.lifekeeper.data.PlaceTaskItem
import com.ultratul.lifekeeper.data.RecordItem
import com.ultratul.lifekeeper.data.ShoppingItem
import com.ultratul.lifekeeper.data.StoredItemLocation
import com.ultratul.lifekeeper.data.TravelActionItem
import com.ultratul.lifekeeper.data.TravelMemoItem
import com.ultratul.lifekeeper.data.TravelReservationItem
import com.ultratul.lifekeeper.data.TravelChecklistItem
import com.ultratul.lifekeeper.data.TravelPlaceItem
import com.ultratul.lifekeeper.data.TravelPlan
import com.ultratul.lifekeeper.data.TimeReminderItem
import com.ultratul.lifekeeper.geofence.GeofenceManager
import com.ultratul.lifekeeper.notification.NotificationHelper
import com.ultratul.lifekeeper.util.SharedPlaceParser
import com.ultratul.lifekeeper.time.TimeReminderScheduler
import com.ultratul.lifekeeper.family.SupabaseConfig
import com.ultratul.lifekeeper.family.SupabaseFamilyCloudSync
import com.ultratul.lifekeeper.family.FamilySyncSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class LifeKeeperUiState(
    val places: List<PlaceItem> = emptyList(),
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val placeTasks: List<PlaceTaskItem> = emptyList(),
    val recordItems: List<RecordItem> = emptyList(),
    val careItems: List<HomeMaintenanceItem> = emptyList(),
    val familySchedules: List<FamilyScheduleItem> = emptyList(),
    val storedItems: List<StoredItemLocation> = emptyList(),
    val deliveries: List<DeliveryItem> = emptyList(),
    val travelPlans: List<TravelPlan> = emptyList(),
    val travelPlaces: List<TravelPlaceItem> = emptyList(),
    val travelActions: List<TravelActionItem> = emptyList(),
    val travelChecklistItems: List<TravelChecklistItem> = emptyList(),
    val travelReservations: List<TravelReservationItem> = emptyList(),
    val travelMemos: List<TravelMemoItem> = emptyList(),
    val timeReminders: List<TimeReminderItem> = emptyList(),
    val familyShareProfile: FamilyShareProfile? = null,
    val familyMembers: List<FamilyMemberItem> = emptyList(),
    val familyActivities: List<FamilyActivityItem> = emptyList(),
    val itemSyncStates: List<ItemSyncState> = emptyList(),
    val megaFeatureFlags: List<MegaFeatureFlag> = emptyList(),
    val supabaseConfigured: Boolean = false,
    val permissionGranted: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class LifeKeeperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LifeKeeperRepository(LifeKeeperDatabase.get(application).dao())
    private val familyCloudSync = SupabaseFamilyCloudSync()

    private data class PlaceWorkState(
        val places: List<PlaceItem>,
        val shopping: List<ShoppingItem>,
        val tasks: List<PlaceTaskItem>
    )

    private data class RoutineState(
        val records: List<RecordItem>,
        val care: List<HomeMaintenanceItem>,
        val family: List<FamilyScheduleItem>
    )

    private data class TravelState(
        val plans: List<TravelPlan>,
        val places: List<TravelPlaceItem>,
        val actions: List<TravelActionItem>
    )

    private data class TravelExtraState(
        val checklist: List<TravelChecklistItem>,
        val reservations: List<TravelReservationItem>,
        val memos: List<TravelMemoItem>
    )

    private data class ExtraState(
        val stored: List<StoredItemLocation>,
        val deliveries: List<DeliveryItem>,
        val timeReminders: List<TimeReminderItem>,
        val familyShareProfile: FamilyShareProfile?,
        val familyMembers: List<FamilyMemberItem>,
        val familyActivities: List<FamilyActivityItem>,
        val itemSyncStates: List<ItemSyncState>,
        val megaFeatureFlags: List<MegaFeatureFlag>
    )

    private data class SyncMetaState(
        val itemSyncStates: List<ItemSyncState>,
        val megaFeatureFlags: List<MegaFeatureFlag>
    )

    private val placeWorkState = combine(
        repository.places,
        repository.shoppingItems,
        repository.placeTasks
    ) { places, shopping, tasks ->
        PlaceWorkState(places, shopping, tasks)
    }

    private val routineState = combine(
        repository.recordItems,
        repository.careItems,
        repository.familySchedules
    ) { records, care, family ->
        RoutineState(records, care, family)
    }

    private val familyState = combine(
        repository.familyShareProfile,
        repository.familyMembers,
        repository.familyActivities
    ) { profile, members, activities ->
        Triple(profile, members, activities)
    }

    private val syncMetaState = combine(
        repository.itemSyncStates,
        repository.megaFeatureFlags
    ) { states, flags ->
        SyncMetaState(states, flags)
    }

    private val extraState = combine(
        repository.storedItems,
        repository.deliveries,
        repository.timeReminders,
        familyState,
        syncMetaState
    ) { stored, deliveries, timeReminders, family, syncMeta ->
        ExtraState(
            stored = stored,
            deliveries = deliveries,
            timeReminders = timeReminders,
            familyShareProfile = family.first,
            familyMembers = family.second,
            familyActivities = family.third,
            itemSyncStates = syncMeta.itemSyncStates,
            megaFeatureFlags = syncMeta.megaFeatureFlags
        )
    }

    private val travelState = combine(
        repository.travelPlans,
        repository.travelPlaces,
        repository.travelActions
    ) { plans, places, actions ->
        TravelState(plans, places, actions)
    }

    private val travelExtraState = combine(
        repository.travelChecklistItems,
        repository.travelReservations,
        repository.travelMemos
    ) { checklist, reservations, memos ->
        TravelExtraState(checklist, reservations, memos)
    }

    val uiState: StateFlow<LifeKeeperUiState> = combine(
        placeWorkState,
        routineState,
        extraState,
        travelState,
        travelExtraState
    ) { placeWork, routine, extra, travel, travelExtra ->
        LifeKeeperUiState(
            places = placeWork.places,
            shoppingItems = placeWork.shopping,
            placeTasks = placeWork.tasks,
            recordItems = routine.records,
            careItems = routine.care,
            familySchedules = routine.family,
            storedItems = extra.stored,
            deliveries = extra.deliveries,
            timeReminders = extra.timeReminders,
            familyShareProfile = extra.familyShareProfile,
            familyMembers = extra.familyMembers,
            familyActivities = extra.familyActivities,
            itemSyncStates = extra.itemSyncStates,
            megaFeatureFlags = extra.megaFeatureFlags,
            supabaseConfigured = SupabaseConfig.isConfigured,
            travelPlans = travel.plans,
            travelPlaces = travel.places,
            travelActions = travel.actions,
            travelChecklistItems = travelExtra.checklist,
            travelReservations = travelExtra.reservations,
            travelMemos = travelExtra.memos,
            permissionGranted = GeofenceManager.hasLocationPermission(getApplication())
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        LifeKeeperUiState()
    )

    init {
        viewModelScope.launch {
            repository.seedIfEmpty(startOfToday())
            registerAllGeofencesFromDatabase(showNotification = false)
            scheduleActiveTimeReminders(showNotification = false)
            autoSyncFamilyDataOnStart()
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            NotificationHelper.show(getApplication<Application>(), "권한 허용 완료", "장소 기반 구매 알림을 사용할 수 있어요.")
            registerAllGeofences()
        }
    }



    fun createFamilyShare(familyName: String, myName: String) {
        viewModelScope.launch {
            val code = generateInviteCode()
            val profile = FamilyShareProfile(
                familyName = familyName.ifBlank { "우리 가족" },
                myName = myName.ifBlank { "나" },
                inviteCode = code,
                role = "owner",
                enabled = true,
                sharePlaces = true,
                shareShopping = true,
                sharePlaceTasks = true,
                shareTravel = true,
                shareTimeReminders = false,
                shareStoredItems = false
            )
            repository.upsertFamilyShareProfile(profile)
            repository.insertFamilyMember(
                FamilyMemberItem(
                    familyCode = code,
                    name = profile.myName,
                    role = "owner",
                    deviceLabel = "내 휴대폰"
                )
            )
            repository.insertFamilyActivity(
                FamilyActivityItem(
                    familyCode = code,
                    actorName = profile.myName,
                    message = "${profile.familyName} 가족 공유를 만들었어요."
                )
            )

            val cloudMessage = try {
                val cloudCode = familyCloudSync.createFamily(profile)
                if (SupabaseConfig.isConfigured) "Supabase에도 가족을 만들었어요. 초대 코드 $cloudCode"
                else "Supabase 설정 전이라 로컬 가족 공유만 만들었어요."
            } catch (e: Exception) {
                "로컬 가족 공유는 생성됐지만 Supabase 업로드는 실패했어요: ${e.message.orEmpty()}"
            }

            NotificationHelper.show(getApplication<Application>(), "가족 공유 생성", cloudMessage)
            syncFamilySnapshotIfNeeded("가족 생성 동기화", notify = false)
        }
    }

        fun joinFamilyShare(inviteCode: String, myName: String) {
        viewModelScope.launch {
            val normalized = inviteCode.trim().uppercase().ifBlank { generateInviteCode() }

            val profile = try {
                familyCloudSync.joinFamily(normalized, myName.ifBlank { "나" })
            } catch (_: Exception) {
                FamilyShareProfile(
                    familyName = "공유 가족",
                    myName = myName.ifBlank { "나" },
                    inviteCode = normalized,
                    role = "member",
                    enabled = true,
                    sharePlaces = true,
                    shareShopping = true,
                    sharePlaceTasks = true,
                    shareTravel = true,
                    shareTimeReminders = false,
                    shareStoredItems = false
                )
            }

            repository.upsertFamilyShareProfile(profile)
            repository.insertFamilyMember(
                FamilyMemberItem(
                    familyCode = normalized,
                    name = profile.myName,
                    role = profile.role,
                    deviceLabel = "내 휴대폰"
                )
            )
            repository.insertFamilyActivity(
                FamilyActivityItem(
                    familyCode = normalized,
                    actorName = profile.myName,
                    message = if (SupabaseConfig.isConfigured) "Supabase 초대 코드로 가족 공유에 참여했어요." else "초대 코드로 로컬 가족 공유에 참여했어요."
                )
            )
            NotificationHelper.show(getApplication<Application>(), "가족 공유 참여", "$normalized 가족 공유에 참여했어요.")
            syncFamilySnapshotIfNeeded("가족 참여 동기화", notify = false)
        }
    }

        fun updateFamilyShareOptions(
        profile: FamilyShareProfile,
        sharePlaces: Boolean = profile.sharePlaces,
        shareShopping: Boolean = profile.shareShopping,
        sharePlaceTasks: Boolean = profile.sharePlaceTasks,
        shareTravel: Boolean = profile.shareTravel,
        shareTimeReminders: Boolean = profile.shareTimeReminders,
        shareStoredItems: Boolean = profile.shareStoredItems
    ) {
        viewModelScope.launch {
            val updated = profile.copy(
                sharePlaces = sharePlaces,
                shareShopping = shareShopping,
                sharePlaceTasks = sharePlaceTasks,
                shareTravel = shareTravel,
                shareTimeReminders = shareTimeReminders,
                shareStoredItems = shareStoredItems,
                updatedAt = System.currentTimeMillis()
            )
            repository.upsertFamilyShareProfile(updated)
            try {
                familyCloudSync.uploadSharedData(updated)
                syncFamilySnapshotIfNeeded("공유 범위 변경", notify = false)
            } catch (_: Exception) {
            }
        }
    }

        fun leaveFamilyShare() {
        viewModelScope.launch {
            repository.clearFamilyShareLocal()
            NotificationHelper.show(getApplication<Application>(), "가족 공유 해제", "이 기기에서 가족 공유를 껐어요.")
        }
    }

    fun simulateFamilySync() {
        viewModelScope.launch {
            syncFamilySnapshotIfNeeded("수동 동기화", notify = true)
        }
    }

    private fun generateInviteCode(): String {
        val source = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { source.random() }.joinToString("")
    }


    private suspend fun markSyncState(
        entityType: String,
        localId: Long,
        status: String = "pending",
        assignee: String = "전체",
        shared: Boolean = true
    ) {
        if (localId <= 0) return
        val profile = uiState.value.familyShareProfile
        repository.upsertItemSyncState(
            ItemSyncState(
                entityType = entityType,
                localId = localId,
                familyCode = profile?.inviteCode.orEmpty(),
                shared = shared,
                assignee = assignee.ifBlank { "전체" },
                updatedAt = System.currentTimeMillis(),
                updatedBy = profile?.myName?.ifBlank { "나" } ?: "나",
                deletedAt = null,
                syncStatus = status
            )
        )
    }

    private suspend fun markSoftDeleted(entityType: String, localId: Long) {
        val profile = uiState.value.familyShareProfile
        repository.markItemSoftDeleted(
            entityType = entityType,
            localId = localId,
            updatedBy = profile?.myName?.ifBlank { "나" } ?: "나"
        )
    }

    private suspend fun autoSyncFamilyDataOnStart() {
        val profile = uiState.value.familyShareProfile ?: return
        if (!profile.enabled) return
        syncFamilySnapshotIfNeeded("앱 시작 동기화", notify = false)
    }

    private suspend fun syncFamilySnapshotIfNeeded(reason: String, notify: Boolean = false) {
        val profile = uiState.value.familyShareProfile ?: return
        if (!profile.enabled) return

        val snapshot = FamilySyncSnapshot(
            places = if (profile.sharePlaces) uiState.value.places.filter { isSharedItem("place", it.id) } else emptyList(),
            shoppingItems = if (profile.shareShopping) uiState.value.shoppingItems.filter { isSharedItem("shopping", it.id) } else emptyList(),
            placeTasks = if (profile.sharePlaceTasks) uiState.value.placeTasks.filter { isSharedItem("place_task", it.id) } else emptyList(),
            travelActions = if (profile.shareTravel) uiState.value.travelActions.filter { isSharedItem("travel_action", it.id) } else emptyList(),
            syncStates = uiState.value.itemSyncStates
        )

        val message = try {
            familyCloudSync.uploadSharedData(profile)
            familyCloudSync.uploadSnapshot(profile, snapshot)
            familyCloudSync.downloadMembers(profile.inviteCode).forEach { repository.insertFamilyMember(it.copy(id = 0)) }
            familyCloudSync.downloadActivities(profile.inviteCode).forEach { repository.insertFamilyActivity(it.copy(id = 0)) }
            mergeRemoteSnapshot(familyCloudSync.downloadSnapshot(profile))
            "$reason 완료"
        } catch (e: Exception) {
            "$reason 실패: ${e.message.orEmpty()}"
        }

        if (notify) {
            NotificationHelper.show(getApplication<Application>(), "가족 공유 동기화", message)
        }
    }


    private fun isSharedItem(entityType: String, localId: Long): Boolean {
        val meta = uiState.value.itemSyncStates.firstOrNull {
            it.entityType == entityType && it.localId == localId
        }
        return meta?.shared != false && meta?.deletedAt == null
    }

    private suspend fun mergeRemoteSnapshot(snapshot: FamilySyncSnapshot) {
        val state = uiState.value

        snapshot.places.forEach { remote ->
            val exists = state.places.any { it.name == remote.name && it.category == remote.category }
            if (!exists) repository.addPlace(remote.copy(id = 0))
        }
        snapshot.shoppingItems.forEach { remote ->
            val exists = state.shoppingItems.any { it.name == remote.name && it.placeName == remote.placeName }
            if (!exists) repository.addShoppingItem(remote.copy(id = 0))
        }
        snapshot.placeTasks.forEach { remote ->
            val exists = state.placeTasks.any { it.title == remote.title && it.placeName == remote.placeName }
            if (!exists) repository.addPlaceTask(remote.copy(id = 0))
        }
        snapshot.travelActions.forEach { remote ->
            val exists = state.travelActions.any { it.title == remote.title && it.placeName == remote.placeName && it.travelTitle == remote.travelTitle }
            if (!exists) repository.addTravelAction(remote.copy(id = 0))
        }
    }

    fun importSharedPlaceText(sharedText: String) {
        viewModelScope.launch {
            val candidate = SharedPlaceParser.parse(sharedText)
            val geocoded = if (candidate.latitude == null || candidate.longitude == null) {
                geocodeCandidate(candidate.name)
            } else {
                null
            }

            val lat = candidate.latitude ?: geocoded?.first
            val lng = candidate.longitude ?: geocoded?.second
            val name = candidate.name.ifBlank { "공유받은 장소" }

            val place = PlaceItem(
                name = name,
                category = candidate.category,
                latitude = lat,
                longitude = lng,
                radiusMeters = defaultRadiusForCategory(candidate.category)
            )

            val id = repository.addPlace(place)
            val saved = place.copy(id = id)

            val message = if (lat != null && lng != null) {
                "좌표를 자동으로 가져와 저장했어요. ${"%.5f".format(lat)}, ${"%.5f".format(lng)}"
            } else {
                "장소명은 저장했지만 좌표는 찾지 못했어요. 장소 앞에서 현재 위치로 다시 저장하거나 좌표를 보완해 주세요."
            }

            NotificationHelper.show(
                getApplication<Application>(),
                "지도 공유 장소 등록",
                "$name\\n$message"
            )

            if (lat != null && lng != null) {
                registerAllGeofencesFromDatabase(showNotification = false)
            }
        }
    }

    private suspend fun geocodeCandidate(query: String): Pair<Double, Double>? {
        if (query.isBlank() || query == "공유받은 장소") return null
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val result = Geocoder(getApplication<Application>(), Locale.KOREA)
                    .getFromLocationName(query, 1)
                    ?.firstOrNull()
                if (result != null) result.latitude to result.longitude else null
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun defaultRadiusForCategory(category: String): Int {
        return when (category) {
            "약국", "편의점" -> 100
            "마트" -> 500
            else -> 300
        }
    }


    fun addTimeReminder(
        title: String,
        timeText: String,
        category: String,
        repeatMode: String,
        target: String,
        note: String
    ) {
        viewModelScope.launch {
            val item = TimeReminderItem(
                title = title,
                timeText = timeText.ifBlank { "09:00" },
                reminderAtMillis = nextReminderMillis(timeText.ifBlank { "09:00" }),
                repeatMode = repeatMode.ifBlank { "한 번" },
                category = category.ifBlank { "오늘" },
                target = target.ifBlank { "생활" },
                note = note
            )
            val id = repository.addTimeReminder(item)
            TimeReminderScheduler.schedule(getApplication<Application>(), item.copy(id = id))
            NotificationHelper.show(
                getApplication<Application>(),
                "시간 알림 저장",
                "${item.timeText} · ${item.title} 알림을 등록했어요."
            )
        }
    }

    fun setTimeReminderDone(item: TimeReminderItem, done: Boolean) {
        viewModelScope.launch {
            repository.setTimeReminderDone(item.id, done)
            if (done) {
                TimeReminderScheduler.cancel(getApplication<Application>(), item.id)
            } else {
                TimeReminderScheduler.schedule(getApplication<Application>(), item.copy(done = false))
            }
        }
    }

    fun deleteTimeReminder(item: TimeReminderItem) {
        viewModelScope.launch {
            TimeReminderScheduler.cancel(getApplication<Application>(), item.id)
            repository.deleteTimeReminder(item.id)
        }
    }

    fun testTimeReminder(item: TimeReminderItem) {
        NotificationHelper.show(
            getApplication<Application>(),
            "⏰ ${item.title}",
            "${item.timeText} · ${item.repeatMode} · ${item.target}\n${item.note}"
        )
    }

    fun scheduleActiveTimeReminders(showNotification: Boolean = true) {
        viewModelScope.launch {
            val items = repository.activeTimeReminders()
            items.forEach { TimeReminderScheduler.schedule(getApplication<Application>(), it) }
            if (showNotification) {
                NotificationHelper.show(getApplication<Application>(), "시간 알림 재등록", "${items.size}개 시간 알림을 다시 등록했어요.")
            }
        }
    }

    private fun nextReminderMillis(timeText: String): Long {
        val parts = timeText.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
        cal.set(Calendar.MINUTE, minute.coerceIn(0, 59))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun addPlace(
        name: String,
        category: String,
        radius: Int,
        useCurrentLocation: Boolean,
        manualLat: Double?,
        manualLng: Double?
    ) {
        viewModelScope.launch {
            val loc = if (useCurrentLocation) currentLocationOrNull() else null
            val id = repository.addPlace(
                PlaceItem(
                    name = name,
                    category = category.ifBlank { "기타" },
                    latitude = loc?.latitude ?: manualLat,
                    longitude = loc?.longitude ?: manualLng,
                    radiusMeters = radius.coerceAtLeast(50)
                )
            )
            markSyncState("place", id, status = "pending")
            syncFamilySnapshotIfNeeded("장소 저장 동기화", notify = false)
        }
    }

    fun deletePlace(item: PlaceItem) {
        viewModelScope.launch {
            markSoftDeleted("place", item.id)
            repository.deletePlace(item.id)
            registerAllGeofencesFromDatabase(showNotification = false)
            syncFamilySnapshotIfNeeded("장소 삭제 동기화", notify = false)
        }
    }

    fun addShopping(
        name: String,
        category: String,
        placeName: String,
        radius: Int,
        useCurrentLocation: Boolean,
        manualLat: Double?,
        manualLng: Double?
    ) {
        viewModelScope.launch {
            val savedPlace = repository.findPlaceByName(placeName)
            val loc = if (savedPlace == null && useCurrentLocation) currentLocationOrNull() else null

            val item = ShoppingItem(
                name = name,
                category = savedPlace?.category ?: category.ifBlank { "기타" },
                placeName = savedPlace?.name ?: placeName.ifBlank { category.ifBlank { "장소 미지정" } },
                placeId = savedPlace?.id,
                latitude = savedPlace?.latitude ?: loc?.latitude ?: manualLat,
                longitude = savedPlace?.longitude ?: loc?.longitude ?: manualLng,
                radiusMeters = savedPlace?.radiusMeters ?: radius.coerceAtLeast(50)
            )

            val id = repository.addShoppingItem(item)
            val saved = item.copy(id = id)
            markSyncState("shopping", id, status = "pending")
            GeofenceManager.registerShoppingGeofence(getApplication<Application>(), saved)
            syncFamilySnapshotIfNeeded("구매 항목 저장 동기화", notify = false)
        }
    }

    fun addPlaceTask(
        title: String,
        category: String,
        placeName: String,
        radius: Int,
        useCurrentLocation: Boolean,
        manualLat: Double?,
        manualLng: Double?,
        note: String
    ) {
        viewModelScope.launch {
            val savedPlace = repository.findPlaceByName(placeName)
            val loc = if (savedPlace == null && useCurrentLocation) currentLocationOrNull() else null

            val item = PlaceTaskItem(
                title = title,
                category = savedPlace?.category ?: category.ifBlank { "기타" },
                placeName = savedPlace?.name ?: placeName.ifBlank { category.ifBlank { "장소 미지정" } },
                placeId = savedPlace?.id,
                latitude = savedPlace?.latitude ?: loc?.latitude ?: manualLat,
                longitude = savedPlace?.longitude ?: loc?.longitude ?: manualLng,
                radiusMeters = savedPlace?.radiusMeters ?: radius.coerceAtLeast(50),
                note = note
            )

            val id = repository.addPlaceTask(item)
            val saved = item.copy(id = id)
            markSyncState("place_task", id, status = "pending")
            GeofenceManager.registerTaskGeofence(getApplication<Application>(), saved)
            syncFamilySnapshotIfNeeded("장소 할 일 저장 동기화", notify = false)
        }
    }

    fun setPlaceTaskDone(item: PlaceTaskItem, done: Boolean) {
        viewModelScope.launch {
            repository.setPlaceTaskDone(item.id, done)
            markSyncState("place_task", item.id, status = if (done) "done_pending" else "pending")
            registerAllGeofencesFromDatabase(showNotification = false)
            syncFamilySnapshotIfNeeded("장소 할 일 완료 동기화", notify = false)
        }
    }

    fun deletePlaceTask(item: PlaceTaskItem) {
        viewModelScope.launch {
            markSoftDeleted("place_task", item.id)
            repository.deletePlaceTask(item.id)
            registerAllGeofencesFromDatabase(showNotification = false)
            syncFamilySnapshotIfNeeded("장소 할 일 삭제 동기화", notify = false)
        }
    }

    fun setShoppingDone(item: ShoppingItem, done: Boolean) {
        viewModelScope.launch {
            repository.setShoppingDone(item.id, done)
            markSyncState("shopping", item.id, status = if (done) "done_pending" else "pending")
            registerAllGeofencesFromDatabase(showNotification = false)
            syncFamilySnapshotIfNeeded("구매 완료 동기화", notify = false)
        }
    }

    fun deleteShopping(item: ShoppingItem) {
        viewModelScope.launch {
            markSoftDeleted("shopping", item.id)
            repository.deleteShoppingItem(item.id)
            registerAllGeofencesFromDatabase(showNotification = false)
            syncFamilySnapshotIfNeeded("구매 삭제 동기화", notify = false)
        }
    }

    fun addRecord(title: String, emoji: String, cycleDays: Int) {
        viewModelScope.launch {
            repository.addRecordItem(
                RecordItem(
                    title = title,
                    emoji = emoji.ifBlank { "⏱️" },
                    lastDateMillis = startOfToday(),
                    cycleDays = cycleDays.coerceAtLeast(1)
                )
            )
        }
    }

    fun markRecordToday(item: RecordItem) {
        viewModelScope.launch {
            repository.markRecordToday(item.id)
        }
    }

    fun addCare(title: String, emoji: String, cycleDays: Int) {
        viewModelScope.launch {
            repository.addCareItem(
                HomeMaintenanceItem(
                    title = title,
                    emoji = emoji.ifBlank { "🛠️" },
                    lastDateMillis = startOfToday(),
                    cycleDays = cycleDays.coerceAtLeast(1)
                )
            )
        }
    }

    fun markCareToday(item: HomeMaintenanceItem) {
        viewModelScope.launch {
            repository.markCareToday(item.id)
        }
    }

    fun addFamilySchedule(title: String, familyTag: String, daysFromToday: Int, note: String) {
        viewModelScope.launch {
            repository.addFamilySchedule(
                FamilyScheduleItem(
                    title = title,
                    familyTag = familyTag.ifBlank { "나" },
                    dueDateMillis = startOfToday() + TimeUnit.DAYS.toMillis(daysFromToday.toLong()),
                    note = note
                )
            )
        }
    }

    fun addStoredItem(name: String, place: String, memo: String) {
        viewModelScope.launch {
            repository.addStoredItem(StoredItemLocation(itemName = name, place = place, memo = memo))
        }
    }

    fun addDelivery(title: String, daysUntilEta: Int) {
        viewModelScope.launch {
            repository.addDelivery(
                DeliveryItem(
                    title = title,
                    expectedAt = startOfToday() + TimeUnit.DAYS.toMillis(daysUntilEta.toLong()),
                    status = "배송중"
                )
            )
        }
    }

    fun setDeliveryDone(item: DeliveryItem, done: Boolean) {
        viewModelScope.launch {
            repository.setDeliveryDone(item.id, done)
        }
    }



    private suspend fun resolveTravelPlanForWrite(travelTitle: String): TravelPlan {
        return uiState.value.travelPlans.firstOrNull { it.title == travelTitle }
            ?: uiState.value.travelPlans.firstOrNull()
            ?: TravelPlan(title = travelTitle.ifBlank { "새 여행" }, destination = "").let {
                val id = repository.addTravelPlan(it)
                it.copy(id = id)
            }
    }

    fun addTravelPlan(title: String, destination: String, days: Int, note: String) {
        viewModelScope.launch {
            val start = startOfToday()
            repository.addTravelPlan(
                TravelPlan(
                    title = title,
                    destination = destination,
                    startDateMillis = start,
                    endDateMillis = start + TimeUnit.DAYS.toMillis(days.coerceAtLeast(1).toLong()),
                    note = note
                )
            )
        }
    }

    fun deleteTravelPlan(item: TravelPlan) {
        viewModelScope.launch {
            repository.deleteTravelPlan(item.id)
            registerAllGeofencesFromDatabase(showNotification = false)
        }
    }

    fun addTravelPlace(
        travelTitle: String,
        placeName: String,
        category: String,
        dayIndex: Int,
        radius: Int,
        useCurrentLocation: Boolean,
        manualLat: Double?,
        manualLng: Double?,
        note: String
    ) {
        viewModelScope.launch {
            val plan = uiState.value.travelPlans.firstOrNull { it.title == travelTitle }
                ?: uiState.value.travelPlans.firstOrNull()
                ?: TravelPlan(title = travelTitle.ifBlank { "새 여행" }, destination = "").let {
                    val id = repository.addTravelPlan(it)
                    it.copy(id = id)
                }

            val loc = if (useCurrentLocation) currentLocationOrNull() else null

            repository.addTravelPlace(
                TravelPlaceItem(
                    travelPlanId = plan.id,
                    travelTitle = plan.title,
                    name = placeName,
                    category = category.ifBlank { "여행" },
                    dayIndex = dayIndex.coerceAtLeast(1),
                    latitude = loc?.latitude ?: manualLat,
                    longitude = loc?.longitude ?: manualLng,
                    radiusMeters = radius.coerceAtLeast(50),
                    note = note
                )
            )
        }
    }

    fun addTravelAction(
        travelTitle: String,
        placeName: String,
        kind: String,
        title: String,
        dayIndex: Int,
        note: String
    ) {
        viewModelScope.launch {
            val plan = uiState.value.travelPlans.firstOrNull { it.title == travelTitle }
                ?: uiState.value.travelPlans.firstOrNull()
                ?: TravelPlan(title = travelTitle.ifBlank { "새 여행" }, destination = "").let {
                    val id = repository.addTravelPlan(it)
                    it.copy(id = id)
                }

            val travelPlace = uiState.value.travelPlaces.firstOrNull {
                it.travelPlanId == plan.id && it.name == placeName
            } ?: uiState.value.travelPlaces.firstOrNull { it.travelPlanId == plan.id }

            val action = TravelActionItem(
                travelPlanId = plan.id,
                travelPlaceId = travelPlace?.id,
                travelTitle = plan.title,
                placeName = travelPlace?.name ?: placeName.ifBlank { "여행 장소" },
                kind = kind.ifBlank { "할 일" },
                title = title,
                latitude = travelPlace?.latitude,
                longitude = travelPlace?.longitude,
                radiusMeters = travelPlace?.radiusMeters ?: 300,
                dayIndex = travelPlace?.dayIndex ?: dayIndex.coerceAtLeast(1),
                note = note
            )

            val id = repository.addTravelAction(action)
            markSyncState("travel_action", id, status = "pending")
            GeofenceManager.registerTravelGeofence(getApplication<Application>(), action.copy(id = id))
            syncFamilySnapshotIfNeeded("여행 액션 저장 동기화", notify = false)
        }
    }

    fun setTravelActionDone(item: TravelActionItem, done: Boolean) {
        viewModelScope.launch {
            repository.setTravelActionDone(item.id, done)
            markSyncState("travel_action", item.id, status = if (done) "done_pending" else "pending")
            registerAllGeofencesFromDatabase(showNotification = false)
            syncFamilySnapshotIfNeeded("여행 액션 완료 동기화", notify = false)
        }
    }

    fun deleteTravelAction(item: TravelActionItem) {
        viewModelScope.launch {
            markSoftDeleted("travel_action", item.id)
            repository.deleteTravelAction(item.id)
            registerAllGeofencesFromDatabase(showNotification = false)
            syncFamilySnapshotIfNeeded("여행 액션 삭제 동기화", notify = false)
        }
    }


    fun cycleItemAssignee(entityType: String, localId: Long) {
        viewModelScope.launch {
            val assignees = listOf("전체", "아빠", "엄마", "아이", "나")
            val current = repository.findItemSyncState(entityType, localId)
            val currentIndex = assignees.indexOf(current?.assignee ?: "전체").takeIf { it >= 0 } ?: 0
            val next = assignees[(currentIndex + 1) % assignees.size]
            updateItemSyncState(entityType, localId, assignee = next)
        }
    }

    fun toggleItemShared(entityType: String, localId: Long) {
        viewModelScope.launch {
            val current = repository.findItemSyncState(entityType, localId)
            updateItemSyncState(entityType, localId, shared = !(current?.shared ?: true))
        }
    }

    private suspend fun updateItemSyncState(
        entityType: String,
        localId: Long,
        assignee: String? = null,
        shared: Boolean? = null
    ) {
        if (localId <= 0) return
        val profile = uiState.value.familyShareProfile
        val existing = repository.findItemSyncState(entityType, localId)
        repository.upsertItemSyncState(
            (existing ?: ItemSyncState(
                entityType = entityType,
                localId = localId,
                familyCode = profile?.inviteCode.orEmpty()
            )).copy(
                familyCode = profile?.inviteCode.orEmpty(),
                assignee = assignee ?: existing?.assignee ?: "전체",
                shared = shared ?: existing?.shared ?: true,
                updatedAt = System.currentTimeMillis(),
                updatedBy = profile?.myName?.ifBlank { "나" } ?: "나",
                syncStatus = "pending"
            )
        )
        syncFamilySnapshotIfNeeded("담당/공유 설정 동기화", notify = false)
    }


    fun addTravelChecklistItem(
        travelTitle: String,
        title: String,
        category: String,
        dayIndex: Int,
        note: String
    ) {
        viewModelScope.launch {
            val plan = resolveTravelPlanForWrite(travelTitle)
            repository.addTravelChecklistItem(
                TravelChecklistItem(
                    travelPlanId = plan.id,
                    travelTitle = plan.title,
                    title = title.ifBlank { "여행 준비물" },
                    category = category.ifBlank { "준비물" },
                    dayIndex = dayIndex.coerceAtLeast(0),
                    note = note
                )
            )
        }
    }

    fun setTravelChecklistDone(item: TravelChecklistItem, done: Boolean) {
        viewModelScope.launch {
            repository.setTravelChecklistDone(item.id, done)
        }
    }

    fun deleteTravelChecklistItem(item: TravelChecklistItem) {
        viewModelScope.launch {
            repository.deleteTravelChecklistItem(item.id)
        }
    }

    fun addTravelReservation(
        travelTitle: String,
        placeName: String,
        title: String,
        dayIndex: Int,
        reservationNo: String,
        timeText: String,
        note: String
    ) {
        viewModelScope.launch {
            val plan = resolveTravelPlanForWrite(travelTitle)
            repository.addTravelReservation(
                TravelReservationItem(
                    travelPlanId = plan.id,
                    travelTitle = plan.title,
                    placeName = placeName.ifBlank { "여행" },
                    title = title.ifBlank { "예약/티켓" },
                    dayIndex = dayIndex.coerceAtLeast(1),
                    reservationNo = reservationNo,
                    timeText = timeText,
                    ticketInfo = note,
                    note = note
                )
            )
        }
    }

    fun setTravelReservationDone(item: TravelReservationItem, done: Boolean) {
        viewModelScope.launch {
            repository.setTravelReservationDone(item.id, done)
        }
    }

    fun deleteTravelReservation(item: TravelReservationItem) {
        viewModelScope.launch {
            repository.deleteTravelReservation(item.id)
        }
    }

    fun addTravelMemo(
        travelTitle: String,
        title: String,
        dayIndex: Int,
        category: String,
        content: String
    ) {
        viewModelScope.launch {
            val plan = resolveTravelPlanForWrite(travelTitle)
            repository.addTravelMemo(
                TravelMemoItem(
                    travelPlanId = plan.id,
                    travelTitle = plan.title,
                    title = title.ifBlank { "여행 메모" },
                    dayIndex = dayIndex.coerceAtLeast(0),
                    category = category.ifBlank { "메모" },
                    content = content
                )
            )
        }
    }

    fun deleteTravelMemo(item: TravelMemoItem) {
        viewModelScope.launch {
            repository.deleteTravelMemo(item.id)
        }
    }

    fun seedTravelEssentials(travelTitle: String = "") {
        viewModelScope.launch {
            val plan = resolveTravelPlanForWrite(travelTitle)
            val existing = uiState.value.travelChecklistItems.filter { it.travelPlanId == plan.id }.map { it.title }.toSet()
            listOf(
                "여권/신분증",
                "항공권/탑승권",
                "숙소 바우처",
                "환전/해외결제 카드",
                "보조배터리/충전기",
                "유심/eSIM",
                "상비약",
                "아이 준비물",
                "여행자보험",
                "교통패스"
            ).filter { it !in existing }.forEach { title ->
                repository.addTravelChecklistItem(
                    TravelChecklistItem(
                        travelPlanId = plan.id,
                        travelTitle = plan.title,
                        title = title,
                        category = "필수",
                        dayIndex = 0,
                        note = "v19 추천 체크리스트"
                    )
                )
            }

            if (uiState.value.travelReservations.none { it.travelPlanId == plan.id && it.title == "항공/호텔 예약번호" }) {
                repository.addTravelReservation(
                    TravelReservationItem(
                        travelPlanId = plan.id,
                        travelTitle = plan.title,
                        placeName = plan.destination.ifBlank { "여행" },
                        title = "항공/호텔 예약번호",
                        reservationNo = "",
                        timeText = "",
                        note = "예약번호와 티켓 정보를 메모해두세요."
                    )
                )
            }

            NotificationHelper.show(
                getApplication<Application>(),
                "여행 준비 세트 추가",
                "${plan.title}에 필수 체크리스트와 예약 메모를 추가했어요."
            )
        }
    }


    fun exportBackupToClipboard() {
        viewModelScope.launch {
            val state = uiState.value
            val json = LifeKeeperBackupManager.exportToJson(
                profile = state.familyShareProfile,
                places = state.places,
                shoppingItems = state.shoppingItems,
                placeTasks = state.placeTasks,
                travelPlans = state.travelPlans,
                travelPlaces = state.travelPlaces,
                travelActions = state.travelActions,
                travelChecklist = state.travelChecklistItems,
                travelReservations = state.travelReservations,
                travelMemos = state.travelMemos,
                timeReminders = state.timeReminders
            )
            copyToClipboard("LifeKeeper Backup", json)
            NotificationHelper.show(
                getApplication<Application>(),
                "백업 완료",
                "생활비서 백업 JSON을 클립보드에 복사했어요. 메모장이나 카톡 나에게 보내기에 저장해두세요."
            )
        }
    }

    fun importBackupFromClipboard() {
        viewModelScope.launch {
            val text = readClipboardText()
            if (text.isBlank()) {
                NotificationHelper.show(getApplication<Application>(), "복원 실패", "클립보드에 백업 JSON이 없어요.")
                return@launch
            }

            try {
                val payload = LifeKeeperBackupManager.importFromJson(text)
                val current = uiState.value
                var importedCount = 0

                val existingPlaces = current.places.map { "${it.name}|${it.category}" }.toMutableSet()
                payload.places.forEach { item ->
                    val key = "${item.name}|${item.category}"
                    if (key !in existingPlaces) {
                        repository.addPlace(item.copy(id = 0))
                        existingPlaces += key
                        importedCount++
                    }
                }

                val existingShopping = current.shoppingItems.map { "${it.placeName}|${it.name}" }.toMutableSet()
                payload.shoppingItems.forEach { item ->
                    val key = "${item.placeName}|${item.name}"
                    if (key !in existingShopping) {
                        repository.addShoppingItem(item.copy(id = 0))
                        existingShopping += key
                        importedCount++
                    }
                }

                val existingTasks = current.placeTasks.map { "${it.placeName}|${it.title}" }.toMutableSet()
                payload.placeTasks.forEach { item ->
                    val key = "${item.placeName}|${item.title}"
                    if (key !in existingTasks) {
                        repository.addPlaceTask(item.copy(id = 0))
                        existingTasks += key
                        importedCount++
                    }
                }

                val travelIdMap = current.travelPlans.associate { it.title to it.id }.toMutableMap()
                payload.travelPlans.forEach { plan ->
                    if (plan.title !in travelIdMap) {
                        val id = repository.addTravelPlan(plan.copy(id = 0))
                        travelIdMap[plan.title] = id
                        importedCount++
                    }
                }

                val existingTravelPlaces = current.travelPlaces.map { "${it.travelTitle}|${it.name}" }.toMutableSet()
                payload.travelPlaces.forEach { place ->
                    val key = "${place.travelTitle}|${place.name}"
                    if (key !in existingTravelPlaces) {
                        val planId = travelIdMap[place.travelTitle] ?: 0L
                        repository.addTravelPlace(place.copy(id = 0, travelPlanId = planId))
                        existingTravelPlaces += key
                        importedCount++
                    }
                }

                val existingTravelActions = current.travelActions.map { "${it.travelTitle}|${it.placeName}|${it.title}" }.toMutableSet()
                payload.travelActions.forEach { action ->
                    val key = "${action.travelTitle}|${action.placeName}|${action.title}"
                    if (key !in existingTravelActions) {
                        val planId = travelIdMap[action.travelTitle] ?: 0L
                        repository.addTravelAction(action.copy(id = 0, travelPlanId = planId, travelPlaceId = null))
                        existingTravelActions += key
                        importedCount++
                    }
                }

                val existingChecklist = current.travelChecklistItems.map { "${it.travelTitle}|${it.category}|${it.title}" }.toMutableSet()
                payload.travelChecklist.forEach { item ->
                    val key = "${item.travelTitle}|${item.category}|${item.title}"
                    if (key !in existingChecklist) {
                        val planId = travelIdMap[item.travelTitle] ?: 0L
                        repository.addTravelChecklistItem(item.copy(id = 0, travelPlanId = planId))
                        existingChecklist += key
                        importedCount++
                    }
                }

                val existingReservations = current.travelReservations.map { "${it.travelTitle}|${it.placeName}|${it.title}|${it.reservationNo}" }.toMutableSet()
                payload.travelReservations.forEach { item ->
                    val key = "${item.travelTitle}|${item.placeName}|${item.title}|${item.reservationNo}"
                    if (key !in existingReservations) {
                        val planId = travelIdMap[item.travelTitle] ?: 0L
                        repository.addTravelReservation(item.copy(id = 0, travelPlanId = planId))
                        existingReservations += key
                        importedCount++
                    }
                }

                val existingMemos = current.travelMemos.map { "${it.travelTitle}|${it.title}|${it.content}" }.toMutableSet()
                payload.travelMemos.forEach { item ->
                    val key = "${item.travelTitle}|${item.title}|${item.content}"
                    if (key !in existingMemos) {
                        val planId = travelIdMap[item.travelTitle] ?: 0L
                        repository.addTravelMemo(item.copy(id = 0, travelPlanId = planId))
                        existingMemos += key
                        importedCount++
                    }
                }

                val existingTimeReminders = current.timeReminders.map { "${it.title}|${it.timeText}|${it.target}" }.toMutableSet()
                payload.timeReminders.forEach { item ->
                    val key = "${item.title}|${item.timeText}|${item.target}"
                    if (key !in existingTimeReminders) {
                        val id = repository.addTimeReminder(item.copy(id = 0))
                        TimeReminderScheduler.schedule(getApplication<Application>(), item.copy(id = id))
                        existingTimeReminders += key
                        importedCount++
                    }
                }

                registerAllGeofencesFromDatabase(showNotification = false)
                scheduleActiveTimeReminders(showNotification = false)

                NotificationHelper.show(
                    getApplication<Application>(),
                    "복원 완료",
                    "백업 JSON에서 ${importedCount}개 새 항목을 복원했어요. 중복 항목은 건너뛰었어요."
                )
            } catch (e: Exception) {
                NotificationHelper.show(
                    getApplication<Application>(),
                    "복원 실패",
                    "백업 JSON을 읽지 못했어요: ${e.message.orEmpty()}"
                )
            }
        }
    }

    fun copyPermissionReportToClipboard() {
        viewModelScope.launch {
            val report = permissionReportText()
            copyToClipboard("LifeKeeper Permission Report", report)
            NotificationHelper.show(getApplication<Application>(), "권한 진단 복사", "권한 진단 내용을 클립보드에 복사했어요.")
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun readClipboardText(): String {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip ?: return ""
        if (clip.itemCount <= 0) return ""
        return clip.getItemAt(0).coerceToText(getApplication<Application>()).toString()
    }

    private fun permissionReportText(): String {
        val app = getApplication<Application>()
        val fine = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return buildString {
            appendLine("생활비서 권한 진단")
            appendLine("알림 권한: ${if (notification) "정상" else "필요"}")
            appendLine("위치 권한: ${if (fine || coarse) "정상" else "필요"}")
            appendLine("정밀 위치: ${if (fine) "정상" else "권장"}")
            appendLine("백그라운드 위치: ${if (background) "정상" else "필요"}")
            appendLine("진동 권한: manifest 포함")
            appendLine("Supabase 설정: ${if (SupabaseConfig.isConfigured) "연결됨" else "설정 필요"}")
            appendLine("장소 수: ${uiState.value.places.size}")
            appendLine("구매목록 수: ${uiState.value.shoppingItems.size}")
            appendLine("장소 할 일 수: ${uiState.value.placeTasks.size}")
            appendLine("여행 액션 수: ${uiState.value.travelActions.size}")
            appendLine("시간 알림 수: ${uiState.value.timeReminders.size}")
        }
    }

    private fun com.ultratul.lifekeeper.backup.LifeKeeperBackupPayload.totalCount(): Int =
        places.size + shoppingItems.size + placeTasks.size + travelPlans.size + travelPlaces.size +
            travelActions.size + travelChecklist.size + travelReservations.size + travelMemos.size + timeReminders.size

    fun showTestNotification() {
        NotificationHelper.show(getApplication<Application>(), "생활비서 알림 테스트", "알림 권한이 정상 동작합니다.")
    }

    fun showPlaceTestNotification(placeName: String, shoppingNames: List<String>, taskNames: List<String> = emptyList()) {
        val parts = buildList {
            if (shoppingNames.isNotEmpty()) add("살 것: ${shoppingNames.joinToString(", ")}")
            if (taskNames.isNotEmpty()) add("할 일: ${taskNames.joinToString(", ")}")
        }
        val message = if (parts.isEmpty()) {
            "이 장소에 연결된 미완료 항목이 없어요."
        } else {
            parts.joinToString("\n")
        }
        NotificationHelper.show(
            getApplication<Application>(),
            "📍 $placeName 근처입니다",
            message,
            NotificationHelper.CHANNEL_GEOFENCE
        )
    }

    fun registerAllGeofences() {
        viewModelScope.launch {
            registerAllGeofencesFromDatabase(showNotification = true)
        }
    }

    private suspend fun registerAllGeofencesFromDatabase(showNotification: Boolean) {
        val context = getApplication<Application>()
        val shopping = repository.activeShoppingItemsWithLocation()
        val tasks = repository.activePlaceTasksWithLocation()
        val travel = repository.activeTravelActionsWithLocation()
        GeofenceManager.refreshAll(context, shopping, tasks, travel)
        if (showNotification) {
            NotificationHelper.show(context, "장소 알림 등록", "살 것 ${shopping.size}개, 할 일 ${tasks.size}개, 여행 ${travel.size}개 장소 알림을 등록했어요.")
        }
    }

    private suspend fun currentLocationOrNull(): Location? {
        if (!GeofenceManager.hasLocationPermission(getApplication<Application>())) return null
        return try {
            LocationServices.getFusedLocationProviderClient(getApplication<Application>())
                .lastLocation
                .await()
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LifeKeeperViewModel::class.java)) {
                return LifeKeeperViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
