package com.ultratul.lifekeeper.data

import kotlinx.coroutines.flow.Flow

class LifeKeeperRepository(
    private val dao: LifeKeeperDao
) {
    val places: Flow<List<PlaceItem>> = dao.observePlaces()
    val shoppingItems: Flow<List<ShoppingItem>> = dao.observeShoppingItems()
    val placeTasks: Flow<List<PlaceTaskItem>> = dao.observePlaceTasks()
    val recordItems: Flow<List<RecordItem>> = dao.observeRecordItems()
    val careItems: Flow<List<HomeMaintenanceItem>> = dao.observeHomeMaintenanceItems()
    val familySchedules: Flow<List<FamilyScheduleItem>> = dao.observeFamilySchedules()
    val storedItems: Flow<List<StoredItemLocation>> = dao.observeStoredItems()
    val deliveries: Flow<List<DeliveryItem>> = dao.observeDeliveryItems()
    val travelPlans: Flow<List<TravelPlan>> = dao.observeTravelPlans()
    val travelPlaces: Flow<List<TravelPlaceItem>> = dao.observeTravelPlaces()
    val travelActions: Flow<List<TravelActionItem>> = dao.observeTravelActions()
    val travelChecklistItems: Flow<List<TravelChecklistItem>> = dao.observeTravelChecklistItems()
    val travelReservations: Flow<List<TravelReservationItem>> = dao.observeTravelReservations()
    val travelMemos: Flow<List<TravelMemoItem>> = dao.observeTravelMemos()
    val timeReminders: Flow<List<TimeReminderItem>> = dao.observeTimeReminders()
    val familyShareProfile: Flow<FamilyShareProfile?> = dao.observeFamilyShareProfile()
    val familyMembers: Flow<List<FamilyMemberItem>> = dao.observeFamilyMembers()
    val familyActivities: Flow<List<FamilyActivityItem>> = dao.observeFamilyActivities()
    val itemSyncStates: Flow<List<ItemSyncState>> = dao.observeItemSyncStates()
    val megaFeatureFlags: Flow<List<MegaFeatureFlag>> = dao.observeMegaFeatureFlags()

    
    suspend fun addTravelPlan(item: TravelPlan): Long = dao.insertTravelPlan(item)

    suspend fun addTimeReminder(item: TimeReminderItem): Long = dao.insertTimeReminder(item)
    suspend fun activeTimeReminders(): List<TimeReminderItem> = dao.getActiveTimeReminders()
    suspend fun setTimeReminderDone(id: Long, done: Boolean) =
        dao.updateTimeReminderDone(id, done, if (done) System.currentTimeMillis() else null)
    suspend fun setTimeReminderEnabled(id: Long, enabled: Boolean) =
        dao.updateTimeReminderEnabled(id, enabled)
    suspend fun deleteTimeReminder(id: Long) = dao.deleteTimeReminder(id)

    suspend fun upsertFamilyShareProfile(profile: FamilyShareProfile) = dao.upsertFamilyShareProfile(profile)
    suspend fun insertFamilyMember(member: FamilyMemberItem): Long = dao.insertFamilyMember(member)
    suspend fun insertFamilyActivity(item: FamilyActivityItem): Long = dao.insertFamilyActivity(item)
    suspend fun upsertItemSyncState(item: ItemSyncState): Long = dao.upsertItemSyncState(item)
    suspend fun findItemSyncState(entityType: String, localId: Long): ItemSyncState? =
        dao.findItemSyncState(entityType, localId)
    suspend fun markItemSoftDeleted(entityType: String, localId: Long, updatedBy: String) {
        val now = System.currentTimeMillis()
        val existing = dao.findItemSyncState(entityType, localId)
        if (existing == null) {
            dao.upsertItemSyncState(
                ItemSyncState(
                    entityType = entityType,
                    localId = localId,
                    updatedAt = now,
                    updatedBy = updatedBy,
                    deletedAt = now,
                    syncStatus = "deleted_pending"
                )
            )
        } else {
            dao.markItemSoftDeleted(entityType, localId, now, now, updatedBy)
        }
    }
    suspend fun upsertMegaFeatureFlag(flag: MegaFeatureFlag) = dao.upsertMegaFeatureFlag(flag)

    suspend fun clearFamilyShareLocal() {
        dao.clearFamilyMembers()
        dao.clearFamilyActivity()
        dao.upsertFamilyShareProfile(FamilyShareProfile(enabled = false))
    }
    suspend fun deleteTravelPlan(id: Long) = dao.deleteTravelPlan(id)

    suspend fun addTravelPlace(item: TravelPlaceItem): Long = dao.insertTravelPlace(item)
    suspend fun deleteTravelPlace(id: Long) = dao.deleteTravelPlace(id)

    suspend fun addTravelAction(item: TravelActionItem): Long = dao.insertTravelAction(item)

    suspend fun addTravelChecklistItem(item: TravelChecklistItem): Long = dao.insertTravelChecklistItem(item)
    suspend fun setTravelChecklistDone(id: Long, done: Boolean) =
        dao.updateTravelChecklistDone(id, done, if (done) System.currentTimeMillis() else null)
    suspend fun deleteTravelChecklistItem(id: Long) = dao.deleteTravelChecklistItem(id)

    suspend fun addTravelReservation(item: TravelReservationItem): Long = dao.insertTravelReservation(item)
    suspend fun setTravelReservationDone(id: Long, done: Boolean) =
        dao.updateTravelReservationDone(id, done, if (done) System.currentTimeMillis() else null)
    suspend fun deleteTravelReservation(id: Long) = dao.deleteTravelReservation(id)

    suspend fun addTravelMemo(item: TravelMemoItem): Long = dao.insertTravelMemo(item)
    suspend fun deleteTravelMemo(id: Long) = dao.deleteTravelMemo(id)

    suspend fun activeTravelActionsWithLocation(): List<TravelActionItem> =
        dao.getActiveTravelActionsWithLocation()

    suspend fun setTravelActionDone(id: Long, done: Boolean) {
        dao.updateTravelActionDone(id, done, if (done) System.currentTimeMillis() else null)
    }

    suspend fun deleteTravelAction(id: Long) = dao.deleteTravelAction(id)

    suspend fun addPlace(item: PlaceItem): Long = dao.insertPlace(item)
    suspend fun deletePlace(id: Long) = dao.deletePlace(id)
    suspend fun findPlaceByName(name: String): PlaceItem? = dao.findPlaceByName(name)

    suspend fun addShoppingItem(item: ShoppingItem): Long = dao.insertShoppingItem(item)

    suspend fun addPlaceTask(item: PlaceTaskItem): Long = dao.insertPlaceTask(item)

    suspend fun activePlaceTasksWithLocation(): List<PlaceTaskItem> =
        dao.getActivePlaceTasksWithLocation()

    suspend fun setPlaceTaskDone(id: Long, done: Boolean) {
        dao.updatePlaceTaskDone(id, done, if (done) System.currentTimeMillis() else null)
    }

    suspend fun deletePlaceTask(id: Long) = dao.deletePlaceTask(id)

    suspend fun activeShoppingItemsWithLocation(): List<ShoppingItem> =
        dao.getActiveShoppingItemsWithLocation()
    suspend fun setShoppingDone(id: Long, done: Boolean) {
        dao.updateShoppingDone(id, done, if (done) System.currentTimeMillis() else null)
    }
    suspend fun deleteShoppingItem(id: Long) = dao.deleteShoppingItem(id)

    suspend fun addRecordItem(item: RecordItem): Long = dao.insertRecordItem(item)
    suspend fun markRecordToday(id: Long) = dao.markRecordToday(id, System.currentTimeMillis())
    suspend fun deleteRecordItem(id: Long) = dao.deleteRecordItem(id)

    suspend fun addCareItem(item: HomeMaintenanceItem): Long = dao.insertHomeMaintenanceItem(item)
    suspend fun markCareToday(id: Long) = dao.markHomeMaintenanceToday(id, System.currentTimeMillis())
    suspend fun deleteCareItem(id: Long) = dao.deleteHomeMaintenanceItem(id)

    suspend fun addFamilySchedule(item: FamilyScheduleItem): Long = dao.insertFamilySchedule(item)
    suspend fun setFamilyDone(id: Long, done: Boolean) = dao.updateFamilyScheduleDone(id, done)

    suspend fun addStoredItem(item: StoredItemLocation): Long = dao.insertStoredItem(item)

    suspend fun addDelivery(item: DeliveryItem): Long = dao.insertDeliveryItem(item)
    suspend fun setDeliveryDone(id: Long, done: Boolean) {
        dao.updateDeliveryDone(id, done, if (done) "도착 완료" else "배송중")
    }

    suspend fun seedIfEmpty(today: Long) {
        if (dao.placeCount() + dao.shoppingCount() + dao.placeTaskCount() + dao.recordCount() + dao.careCount() > 0) return

        val homeId = dao.insertPlace(
            PlaceItem(
                name = "집",
                category = "집",
                latitude = null,
                longitude = null,
                radiusMeters = 300
            )
        )
        val daisoId = dao.insertPlace(
            PlaceItem(
                name = "다이소 고덕점",
                category = "다이소",
                latitude = 37.557,
                longitude = 127.154,
                radiusMeters = 300
            )
        )
        val martId = dao.insertPlace(
            PlaceItem(
                name = "이마트",
                category = "마트",
                latitude = 37.556,
                longitude = 127.153,
                radiusMeters = 500
            )
        )
        val pharmacyId = dao.insertPlace(
            PlaceItem(
                name = "동네 약국",
                category = "약국",
                latitude = 37.558,
                longitude = 127.155,
                radiusMeters = 100
            )
        )
        dao.insertPlace(
            PlaceItem(
                name = "회사",
                category = "회사",
                latitude = null,
                longitude = null,
                radiusMeters = 300
            )
        )
        dao.insertPlace(
            PlaceItem(
                name = "학교",
                category = "학교",
                latitude = null,
                longitude = null,
                radiusMeters = 300
            )
        )
        dao.insertPlace(
            PlaceItem(
                name = "편의점",
                category = "편의점",
                latitude = null,
                longitude = null,
                radiusMeters = 100
            )
        )

        dao.insertPlaceTask(
            PlaceTaskItem(
                title = "분리수거하기",
                category = "집",
                placeName = "집",
                placeId = homeId,
                latitude = null,
                longitude = null,
                radiusMeters = 300,
                note = "집 도착하면 확인"
            )
        )
        dao.insertPlaceTask(
            PlaceTaskItem(
                title = "충전기 챙기기",
                category = "회사",
                placeName = "회사",
                placeId = null,
                latitude = null,
                longitude = null,
                radiusMeters = 300,
                note = "퇴근 전 확인"
            )
        )
        dao.insertPlaceTask(
            PlaceTaskItem(
                title = "아이 준비물 확인",
                category = "학교",
                placeName = "학교",
                placeId = null,
                latitude = null,
                longitude = null,
                radiusMeters = 300,
                note = "학교 근처에서 확인"
            )
        )

        dao.insertShoppingItem(
            ShoppingItem(
                name = "건전지",
                category = "다이소",
                placeName = "다이소 고덕점",
                placeId = daisoId,
                latitude = 37.557,
                longitude = 127.154,
                radiusMeters = 300
            )
        )
        dao.insertShoppingItem(
            ShoppingItem(
                name = "멀티탭",
                category = "다이소",
                placeName = "다이소 고덕점",
                placeId = daisoId,
                latitude = 37.557,
                longitude = 127.154,
                radiusMeters = 300
            )
        )
        dao.insertShoppingItem(
            ShoppingItem(
                name = "우유",
                category = "마트",
                placeName = "이마트",
                placeId = martId,
                latitude = 37.556,
                longitude = 127.153,
                radiusMeters = 500
            )
        )
        dao.insertShoppingItem(
            ShoppingItem(
                name = "감기약",
                category = "약국",
                placeName = "동네 약국",
                placeId = pharmacyId,
                latitude = 37.558,
                longitude = 127.155,
                radiusMeters = 100
            )
        )


        val osakaPlanId = dao.insertTravelPlan(
            TravelPlan(
                title = "오사카 가족여행",
                destination = "오사카",
                startDateMillis = today + 30L * 24 * 60 * 60 * 1000,
                endDateMillis = today + 33L * 24 * 60 * 60 * 1000,
                note = "3박 4일 여행 샘플"
            )
        )
        val dotonboriId = dao.insertTravelPlace(
            TravelPlaceItem(
                travelPlanId = osakaPlanId,
                travelTitle = "오사카 가족여행",
                name = "도톤보리",
                category = "여행",
                dayIndex = 1,
                latitude = 34.6687,
                longitude = 135.5013,
                radiusMeters = 300,
                note = "저녁 산책"
            )
        )
        dao.insertTravelAction(
            TravelActionItem(
                travelPlanId = osakaPlanId,
                travelPlaceId = dotonboriId,
                travelTitle = "오사카 가족여행",
                placeName = "도톤보리",
                kind = "먹을 것",
                title = "타코야키 먹기",
                latitude = 34.6687,
                longitude = 135.5013,
                radiusMeters = 300,
                dayIndex = 1
            )
        )
        dao.insertTravelAction(
            TravelActionItem(
                travelPlanId = osakaPlanId,
                travelPlaceId = dotonboriId,
                travelTitle = "오사카 가족여행",
                placeName = "도톤보리",
                kind = "사진",
                title = "글리코상 앞 가족사진",
                latitude = 34.6687,
                longitude = 135.5013,
                radiusMeters = 300,
                dayIndex = 1
            )
        )

        dao.insertRecordItem(
            RecordItem(
                title = "치과 방문",
                emoji = "🦷",
                lastDateMillis = today - 81L * 24 * 60 * 60 * 1000,
                cycleDays = 180
            )
        )
        dao.insertRecordItem(
            RecordItem(
                title = "운동",
                emoji = "🏃",
                lastDateMillis = today - 2L * 24 * 60 * 60 * 1000,
                cycleDays = 3
            )
        )
        dao.insertRecordItem(
            RecordItem(
                title = "자동차 엔진오일",
                emoji = "🚗",
                lastDateMillis = today - 153L * 24 * 60 * 60 * 1000,
                cycleDays = 180
            )
        )

        dao.insertHomeMaintenanceItem(
            HomeMaintenanceItem(
                title = "정수기 필터",
                emoji = "🧊",
                lastDateMillis = today - 194L * 24 * 60 * 60 * 1000,
                cycleDays = 180
            )
        )
        dao.insertHomeMaintenanceItem(
            HomeMaintenanceItem(
                title = "공기청정기 필터",
                emoji = "🌬️",
                lastDateMillis = today - 131L * 24 * 60 * 60 * 1000,
                cycleDays = 120
            )
        )
        dao.insertHomeMaintenanceItem(
            HomeMaintenanceItem(
                title = "에어컨 청소",
                emoji = "❄️",
                lastDateMillis = today - 300L * 24 * 60 * 60 * 1000,
                cycleDays = 365
            )
        )

        dao.insertFamilySchedule(
            FamilyScheduleItem(
                title = "첫째 치과",
                familyTag = "첫째",
                dueDateMillis = today + 1L * 24 * 60 * 60 * 1000,
                note = "오후 4시 30분"
            )
        )

        dao.insertStoredItem(
            StoredItemLocation(
                itemName = "여권",
                place = "안방 서랍",
                memo = "가족 여권 보관"
            )
        )
        dao.insertStoredItem(
            StoredItemLocation(
                itemName = "체온계",
                place = "거실 TV장",
                memo = "약통 옆"
            )
        )

        dao.insertDeliveryItem(
            DeliveryItem(
                title = "USB 케이블",
                expectedAt = today + 1L * 24 * 60 * 60 * 1000,
                status = "배송중"
            )
        )
    }
}
