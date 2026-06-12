package com.ultratul.lifekeeper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlaceItem::class,
        ShoppingItem::class,
        PlaceTaskItem::class,
        RecordItem::class,
        HomeMaintenanceItem::class,
        FamilyScheduleItem::class,
        StoredItemLocation::class,
        DeliveryItem::class,
        TravelActionItem::class,
        TravelPlaceItem::class,
        TravelPlan::class,
        TravelChecklistItem::class,
        TravelReservationItem::class,
        TravelMemoItem::class,
        TimeReminderItem::class,
        FamilyShareProfile::class,
        FamilyMemberItem::class,
        FamilyActivityItem::class,
        ItemSyncState::class,
        MegaFeatureFlag::class
    ],
    version = 19,
    exportSchema = false
)
abstract class LifeKeeperDatabase : RoomDatabase() {
    abstract fun dao(): LifeKeeperDao

    companion object {
        @Volatile
        private var INSTANCE: LifeKeeperDatabase? = null

        private val MIGRATION_12_15 = object : Migration(12, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `item_sync_states` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `entityType` TEXT NOT NULL,
                        `localId` INTEGER NOT NULL,
                        `familyCode` TEXT NOT NULL,
                        `shared` INTEGER NOT NULL,
                        `assignee` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `updatedBy` TEXT NOT NULL,
                        `deletedAt` INTEGER,
                        `syncStatus` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `mega_feature_flags` (
                        `key` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        `label` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        PRIMARY KEY(`key`)
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_15_19 = object : Migration(15, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `travel_checklist_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `travelPlanId` INTEGER NOT NULL,
                        `travelTitle` TEXT NOT NULL,
                        `dayIndex` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `done` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `completedAt` INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `travel_reservations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `travelPlanId` INTEGER NOT NULL,
                        `travelTitle` TEXT NOT NULL,
                        `placeName` TEXT NOT NULL,
                        `dayIndex` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `reservationNo` TEXT NOT NULL,
                        `timeText` TEXT NOT NULL,
                        `ticketInfo` TEXT NOT NULL,
                        `done` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `completedAt` INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `travel_memos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `travelPlanId` INTEGER NOT NULL,
                        `travelTitle` TEXT NOT NULL,
                        `dayIndex` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun get(context: Context): LifeKeeperDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LifeKeeperDatabase::class.java,
                    "life_keeper.db"
                )
                    .addMigrations(MIGRATION_12_15, MIGRATION_15_19)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
