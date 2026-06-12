package com.ultratul.lifekeeper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeKeeperDao {

    @Query("SELECT * FROM item_sync_states ORDER BY updatedAt DESC")
    fun observeItemSyncStates(): Flow<List<ItemSyncState>>

    @Query("SELECT * FROM item_sync_states WHERE entityType = :entityType AND localId = :localId LIMIT 1")
    suspend fun findItemSyncState(entityType: String, localId: Long): ItemSyncState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemSyncState(item: ItemSyncState): Long

    @Query("UPDATE item_sync_states SET deletedAt = :deletedAt, syncStatus = 'deleted_pending', updatedAt = :updatedAt, updatedBy = :updatedBy WHERE entityType = :entityType AND localId = :localId")
    suspend fun markItemSoftDeleted(entityType: String, localId: Long, deletedAt: Long, updatedAt: Long, updatedBy: String)

    @Query("SELECT * FROM mega_feature_flags ORDER BY key ASC")
    fun observeMegaFeatureFlags(): Flow<List<MegaFeatureFlag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMegaFeatureFlag(flag: MegaFeatureFlag)


    @Query("SELECT * FROM family_share_profile WHERE id = 1")
    fun observeFamilyShareProfile(): Flow<FamilyShareProfile?>

    @Query("SELECT * FROM family_members ORDER BY role ASC, name ASC")
    fun observeFamilyMembers(): Flow<List<FamilyMemberItem>>

    @Query("SELECT * FROM family_activity ORDER BY createdAt DESC LIMIT 30")
    fun observeFamilyActivities(): Flow<List<FamilyActivityItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFamilyShareProfile(profile: FamilyShareProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyActivity(item: FamilyActivityItem): Long

    @Query("DELETE FROM family_members")
    suspend fun clearFamilyMembers()

    @Query("DELETE FROM family_activity")
    suspend fun clearFamilyActivity()

    @Query("SELECT COUNT(*) FROM family_share_profile")
    suspend fun familyShareProfileCount(): Int



    @Query("SELECT * FROM travel_checklist_items ORDER BY done ASC, dayIndex ASC, category ASC, createdAt DESC")
    fun observeTravelChecklistItems(): Flow<List<TravelChecklistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelChecklistItem(item: TravelChecklistItem): Long

    @Query("UPDATE travel_checklist_items SET done = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTravelChecklistDone(id: Long, done: Boolean, completedAt: Long?)

    @Query("DELETE FROM travel_checklist_items WHERE id = :id")
    suspend fun deleteTravelChecklistItem(id: Long)

    @Query("SELECT * FROM travel_reservations ORDER BY done ASC, dayIndex ASC, timeText ASC, createdAt DESC")
    fun observeTravelReservations(): Flow<List<TravelReservationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelReservation(item: TravelReservationItem): Long

    @Query("UPDATE travel_reservations SET done = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTravelReservationDone(id: Long, done: Boolean, completedAt: Long?)

    @Query("DELETE FROM travel_reservations WHERE id = :id")
    suspend fun deleteTravelReservation(id: Long)

    @Query("SELECT * FROM travel_memos ORDER BY dayIndex ASC, updatedAt DESC")
    fun observeTravelMemos(): Flow<List<TravelMemoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelMemo(item: TravelMemoItem): Long

    @Query("DELETE FROM travel_memos WHERE id = :id")
    suspend fun deleteTravelMemo(id: Long)

    @Query("SELECT * FROM time_reminders ORDER BY done ASC, reminderAtMillis ASC, createdAt DESC")
    fun observeTimeReminders(): Flow<List<TimeReminderItem>>

    @Query("SELECT * FROM time_reminders WHERE enabled = 1 AND done = 0")
    suspend fun getActiveTimeReminders(): List<TimeReminderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeReminder(item: TimeReminderItem): Long

    @Query("UPDATE time_reminders SET done = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTimeReminderDone(id: Long, done: Boolean, completedAt: Long?)

    @Query("UPDATE time_reminders SET enabled = :enabled WHERE id = :id")
    suspend fun updateTimeReminderEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM time_reminders WHERE id = :id")
    suspend fun deleteTimeReminder(id: Long)

    @Query("SELECT COUNT(*) FROM time_reminders")
    suspend fun timeReminderCount(): Int


    @Query("SELECT * FROM travel_plans ORDER BY startDateMillis DESC, createdAt DESC")
    fun observeTravelPlans(): Flow<List<TravelPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelPlan(item: TravelPlan): Long

    @Query("DELETE FROM travel_plans WHERE id = :id")
    suspend fun deleteTravelPlan(id: Long)

    @Query("SELECT * FROM travel_places ORDER BY travelPlanId ASC, dayIndex ASC, createdAt ASC")
    fun observeTravelPlaces(): Flow<List<TravelPlaceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelPlace(item: TravelPlaceItem): Long

    @Query("DELETE FROM travel_places WHERE id = :id")
    suspend fun deleteTravelPlace(id: Long)

    @Query("SELECT * FROM travel_actions ORDER BY done ASC, dayIndex ASC, createdAt DESC")
    fun observeTravelActions(): Flow<List<TravelActionItem>>

    @Query("SELECT * FROM travel_actions WHERE done = 0 AND latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getActiveTravelActionsWithLocation(): List<TravelActionItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelAction(item: TravelActionItem): Long

    @Query("UPDATE travel_actions SET done = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTravelActionDone(id: Long, done: Boolean, completedAt: Long?)

    @Query("DELETE FROM travel_actions WHERE id = :id")
    suspend fun deleteTravelAction(id: Long)

    @Query("SELECT COUNT(*) FROM travel_plans")
    suspend fun travelPlanCount(): Int

    @Query("SELECT * FROM places ORDER BY category ASC, name ASC")
    fun observePlaces(): Flow<List<PlaceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(item: PlaceItem): Long

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlace(id: Long)

    @Query("SELECT * FROM places WHERE name = :name LIMIT 1")
    suspend fun findPlaceByName(name: String): PlaceItem?

    @Query("SELECT * FROM places WHERE enabled = 1 AND latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getPlacesWithLocation(): List<PlaceItem>

    @Query("SELECT * FROM shopping_items ORDER BY done ASC, createdAt DESC")
    fun observeShoppingItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE done = 0 AND latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getActiveShoppingItemsWithLocation(): List<ShoppingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem): Long

    @Query("UPDATE shopping_items SET done = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun updateShoppingDone(id: Long, done: Boolean, completedAt: Long?)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteShoppingItem(id: Long)

    @Query("SELECT * FROM place_tasks ORDER BY done ASC, createdAt DESC")
    fun observePlaceTasks(): Flow<List<PlaceTaskItem>>

    @Query("SELECT * FROM place_tasks WHERE done = 0 AND latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getActivePlaceTasksWithLocation(): List<PlaceTaskItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaceTask(item: PlaceTaskItem): Long

    @Query("UPDATE place_tasks SET done = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun updatePlaceTaskDone(id: Long, done: Boolean, completedAt: Long?)

    @Query("DELETE FROM place_tasks WHERE id = :id")
    suspend fun deletePlaceTask(id: Long)

    @Query("SELECT * FROM record_items ORDER BY lastDateMillis DESC")
    fun observeRecordItems(): Flow<List<RecordItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordItem(item: RecordItem): Long

    @Query("UPDATE record_items SET lastDateMillis = :today WHERE id = :id")
    suspend fun markRecordToday(id: Long, today: Long)

    @Query("DELETE FROM record_items WHERE id = :id")
    suspend fun deleteRecordItem(id: Long)

    @Query("SELECT * FROM home_maintenance_items ORDER BY lastDateMillis ASC")
    fun observeHomeMaintenanceItems(): Flow<List<HomeMaintenanceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeMaintenanceItem(item: HomeMaintenanceItem): Long

    @Query("UPDATE home_maintenance_items SET lastDateMillis = :today WHERE id = :id")
    suspend fun markHomeMaintenanceToday(id: Long, today: Long)

    @Query("DELETE FROM home_maintenance_items WHERE id = :id")
    suspend fun deleteHomeMaintenanceItem(id: Long)

    @Query("SELECT * FROM family_schedule_items ORDER BY dueDateMillis ASC")
    fun observeFamilySchedules(): Flow<List<FamilyScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilySchedule(item: FamilyScheduleItem): Long

    @Query("UPDATE family_schedule_items SET done = :done WHERE id = :id")
    suspend fun updateFamilyScheduleDone(id: Long, done: Boolean)

    @Query("SELECT * FROM stored_item_locations ORDER BY updatedAt DESC")
    fun observeStoredItems(): Flow<List<StoredItemLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoredItem(item: StoredItemLocation): Long

    @Query("SELECT * FROM delivery_items ORDER BY done ASC, orderedAt DESC")
    fun observeDeliveryItems(): Flow<List<DeliveryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryItem(item: DeliveryItem): Long

    @Query("UPDATE delivery_items SET done = :done, status = :status WHERE id = :id")
    suspend fun updateDeliveryDone(id: Long, done: Boolean, status: String)

    @Query("SELECT COUNT(*) FROM places")
    suspend fun placeCount(): Int

    @Query("SELECT COUNT(*) FROM shopping_items")
    suspend fun shoppingCount(): Int

    @Query("SELECT COUNT(*) FROM place_tasks")
    suspend fun placeTaskCount(): Int

    @Query("SELECT COUNT(*) FROM record_items")
    suspend fun recordCount(): Int

    @Query("SELECT COUNT(*) FROM home_maintenance_items")
    suspend fun careCount(): Int
}
