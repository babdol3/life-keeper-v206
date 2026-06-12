package com.ultratul.lifekeeper.family

import com.ultratul.lifekeeper.data.FamilyActivityItem
import com.ultratul.lifekeeper.data.FamilyMemberItem
import com.ultratul.lifekeeper.data.FamilyShareProfile
import com.ultratul.lifekeeper.data.ItemSyncState
import com.ultratul.lifekeeper.data.PlaceItem
import com.ultratul.lifekeeper.data.PlaceTaskItem
import com.ultratul.lifekeeper.data.ShoppingItem
import com.ultratul.lifekeeper.data.TravelActionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Supabase REST API 기반 가족 공유 동기화 구현체.
 *
 * 추가 SDK 없이 HttpURLConnection + org.json만 사용합니다.
 */
class SupabaseFamilyCloudSync : FamilyCloudSyncContract {

    override suspend fun createFamily(profile: FamilyShareProfile): String = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) return@withContext profile.inviteCode.ifBlank { "LOCAL" }

        val code = profile.inviteCode.ifBlank { "LOCAL" }
        upsert(
            table = "families",
            body = JSONObject()
                .put("family_code", code)
                .put("family_name", profile.familyName.ifBlank { "우리 가족" })
                .put("owner_name", profile.myName.ifBlank { "나" })
                .put("updated_at", System.currentTimeMillis())
        )
        upsertMember(code, profile.myName.ifBlank { "나" }, "owner")
        insertActivity(code, profile.myName.ifBlank { "나" }, "${profile.familyName.ifBlank { "우리 가족" }} 가족 공유를 만들었어요.")
        code
    }

    override suspend fun joinFamily(inviteCode: String, memberName: String): FamilyShareProfile = withContext(Dispatchers.IO) {
        val code = inviteCode.trim().uppercase().ifBlank { "LOCAL" }
        if (SupabaseConfig.isConfigured) {
            upsertMember(code, memberName.ifBlank { "나" }, "member")
            insertActivity(code, memberName.ifBlank { "나" }, "초대 코드로 가족 공유에 참여했어요.")
        }
        FamilyShareProfile(
            familyName = "공유 가족",
            myName = memberName.ifBlank { "나" },
            inviteCode = code,
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

    override suspend fun uploadSharedData(profile: FamilyShareProfile) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured || !profile.enabled) return@withContext
        upsert(
            table = "family_share_profiles",
            body = JSONObject()
                .put("family_code", profile.inviteCode)
                .put("family_name", profile.familyName)
                .put("my_name", profile.myName)
                .put("role", profile.role)
                .put("share_places", profile.sharePlaces)
                .put("share_shopping", profile.shareShopping)
                .put("share_place_tasks", profile.sharePlaceTasks)
                .put("share_travel", profile.shareTravel)
                .put("share_time_reminders", profile.shareTimeReminders)
                .put("share_stored_items", profile.shareStoredItems)
                .put("updated_at", profile.updatedAt)
        )
    }

    override suspend fun downloadMembers(familyCode: String): List<FamilyMemberItem> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured || familyCode.isBlank()) return@withContext emptyList()
        val json = get("family_members", "family_code=eq.${encode(familyCode)}&order=role.asc,name.asc")
        val arr = JSONArray(json)
        List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            FamilyMemberItem(
                familyCode = obj.optString("family_code"),
                name = obj.optString("name"),
                role = obj.optString("role", "member"),
                deviceLabel = obj.optString("device_label", "Android"),
                lastSeenAt = obj.optLong("last_seen_at", System.currentTimeMillis())
            )
        }
    }

    override suspend fun downloadActivities(familyCode: String): List<FamilyActivityItem> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured || familyCode.isBlank()) return@withContext emptyList()
        val json = get("family_activity", "family_code=eq.${encode(familyCode)}&order=created_at.desc&limit=30")
        val arr = JSONArray(json)
        List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            FamilyActivityItem(
                familyCode = obj.optString("family_code"),
                actorName = obj.optString("actor_name", "가족"),
                message = obj.optString("message"),
                createdAt = obj.optLong("created_at", System.currentTimeMillis())
            )
        }
    }

    override suspend fun uploadSnapshot(profile: FamilyShareProfile, snapshot: FamilySyncSnapshot) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured || !profile.enabled) return@withContext
        val code = profile.inviteCode
        if (profile.sharePlaces) snapshot.places.forEach { item ->
            upsertSharedPlace(code, item, snapshot.findMeta("place", item.id))
        }
        if (profile.shareShopping) snapshot.shoppingItems.forEach { item ->
            upsertSharedShopping(code, item, snapshot.findMeta("shopping", item.id))
        }
        if (profile.sharePlaceTasks) snapshot.placeTasks.forEach { item ->
            upsertSharedTask(code, item, snapshot.findMeta("place_task", item.id))
        }
        if (profile.shareTravel) snapshot.travelActions.forEach { item ->
            upsertSharedTravelAction(code, item, snapshot.findMeta("travel_action", item.id))
        }
    }

    override suspend fun downloadSnapshot(profile: FamilyShareProfile): FamilySyncSnapshot = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured || !profile.enabled || profile.inviteCode.isBlank()) return@withContext FamilySyncSnapshot()
        val code = profile.inviteCode
        FamilySyncSnapshot(
            places = if (profile.sharePlaces) downloadPlaces(code) else emptyList(),
            shoppingItems = if (profile.shareShopping) downloadShoppingItems(code) else emptyList(),
            placeTasks = if (profile.sharePlaceTasks) downloadPlaceTasks(code) else emptyList(),
            travelActions = if (profile.shareTravel) downloadTravelActions(code) else emptyList()
        )
    }

    suspend fun insertActivity(familyCode: String, actorName: String, message: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured || familyCode.isBlank()) return@withContext
        post(
            table = "family_activity",
            body = JSONObject()
                .put("family_code", familyCode)
                .put("actor_name", actorName.ifBlank { "가족" })
                .put("message", message)
                .put("created_at", System.currentTimeMillis())
        )
    }

    private fun downloadPlaces(familyCode: String): List<PlaceItem> {
        val json = get("shared_places", "family_code=eq.${encode(familyCode)}&order=updated_at.desc")
        val arr = JSONArray(json)
        return List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            PlaceItem(
                name = obj.optString("name"),
                category = obj.optString("category", "기타"),
                latitude = obj.optDoubleOrNull("latitude"),
                longitude = obj.optDoubleOrNull("longitude"),
                radiusMeters = obj.optInt("radius_meters", 300),
                enabled = true,
                createdAt = obj.optLong("updated_at", System.currentTimeMillis())
            )
        }
    }

    private fun downloadShoppingItems(familyCode: String): List<ShoppingItem> {
        val json = get("shared_shopping_items", "family_code=eq.${encode(familyCode)}&order=updated_at.desc")
        val arr = JSONArray(json)
        return List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            ShoppingItem(
                name = obj.optString("title"),
                category = obj.optString("category", "기타"),
                placeName = obj.optString("place_name"),
                placeId = null,
                latitude = obj.optDoubleOrNull("latitude"),
                longitude = obj.optDoubleOrNull("longitude"),
                radiusMeters = obj.optInt("radius_meters", 300),
                done = obj.optBoolean("done", false),
                createdAt = obj.optLong("updated_at", System.currentTimeMillis()),
                completedAt = obj.optLongOrNull("completed_at")
            )
        }
    }

    private fun downloadPlaceTasks(familyCode: String): List<PlaceTaskItem> {
        val json = get("shared_place_tasks", "family_code=eq.${encode(familyCode)}&order=updated_at.desc")
        val arr = JSONArray(json)
        return List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            PlaceTaskItem(
                title = obj.optString("title"),
                category = obj.optString("category", "기타"),
                placeName = obj.optString("place_name"),
                placeId = null,
                latitude = obj.optDoubleOrNull("latitude"),
                longitude = obj.optDoubleOrNull("longitude"),
                radiusMeters = obj.optInt("radius_meters", 300),
                done = obj.optBoolean("done", false),
                createdAt = obj.optLong("updated_at", System.currentTimeMillis()),
                completedAt = obj.optLongOrNull("completed_at"),
                note = obj.optString("note", "")
            )
        }
    }

    private fun downloadTravelActions(familyCode: String): List<TravelActionItem> {
        val json = get("shared_travel_actions", "family_code=eq.${encode(familyCode)}&order=updated_at.desc")
        val arr = JSONArray(json)
        return List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            TravelActionItem(
                travelPlanId = 0,
                travelPlaceId = null,
                travelTitle = obj.optString("travel_title"),
                placeName = obj.optString("place_name"),
                kind = obj.optString("kind", "할 일"),
                title = obj.optString("title"),
                latitude = obj.optDoubleOrNull("latitude"),
                longitude = obj.optDoubleOrNull("longitude"),
                radiusMeters = obj.optInt("radius_meters", 300),
                dayIndex = obj.optInt("day_index", 1),
                done = obj.optBoolean("done", false),
                note = obj.optString("note", ""),
                createdAt = obj.optLong("updated_at", System.currentTimeMillis()),
                completedAt = obj.optLongOrNull("completed_at")
            )
        }
    }

    private fun upsertSharedPlace(familyCode: String, item: PlaceItem, meta: ItemSyncState?) {
        upsert(
            table = "shared_places",
            onConflict = "family_code,name,category",
            body = JSONObject()
                .put("family_code", familyCode)
                .put("local_id", item.id)
                .put("name", item.name)
                .put("category", item.category)
                .put("latitude", item.latitude)
                .put("longitude", item.longitude)
                .put("radius_meters", item.radiusMeters)
                .put("updated_at", meta?.updatedAt ?: System.currentTimeMillis())
                .put("updated_by", meta?.updatedBy ?: "나")
                .put("deleted_at", meta?.deletedAt)
                .put("sync_status", meta?.syncStatus ?: "synced")
                .put("assignee", meta?.assignee ?: "전체")
                .put("shared", meta?.shared ?: true)
        )
    }

    private fun upsertSharedShopping(familyCode: String, item: ShoppingItem, meta: ItemSyncState?) {
        upsert(
            table = "shared_shopping_items",
            onConflict = "family_code,place_name,title",
            body = JSONObject()
                .put("family_code", familyCode)
                .put("local_id", item.id)
                .put("place_name", item.placeName)
                .put("title", item.name)
                .put("category", item.category)
                .put("latitude", item.latitude)
                .put("longitude", item.longitude)
                .put("radius_meters", item.radiusMeters)
                .put("done", item.done)
                .put("completed_at", item.completedAt)
                .put("updated_at", meta?.updatedAt ?: System.currentTimeMillis())
                .put("updated_by", meta?.updatedBy ?: "나")
                .put("deleted_at", meta?.deletedAt)
                .put("sync_status", meta?.syncStatus ?: "synced")
                .put("assignee", meta?.assignee ?: "전체")
                .put("shared", meta?.shared ?: true)
        )
    }

    private fun upsertSharedTask(familyCode: String, item: PlaceTaskItem, meta: ItemSyncState?) {
        upsert(
            table = "shared_place_tasks",
            onConflict = "family_code,place_name,title",
            body = JSONObject()
                .put("family_code", familyCode)
                .put("local_id", item.id)
                .put("place_name", item.placeName)
                .put("title", item.title)
                .put("category", item.category)
                .put("latitude", item.latitude)
                .put("longitude", item.longitude)
                .put("radius_meters", item.radiusMeters)
                .put("done", item.done)
                .put("completed_at", item.completedAt)
                .put("note", item.note)
                .put("updated_at", meta?.updatedAt ?: System.currentTimeMillis())
                .put("updated_by", meta?.updatedBy ?: "나")
                .put("deleted_at", meta?.deletedAt)
                .put("sync_status", meta?.syncStatus ?: "synced")
                .put("assignee", meta?.assignee ?: "전체")
                .put("shared", meta?.shared ?: true)
        )
    }

    private fun upsertSharedTravelAction(familyCode: String, item: TravelActionItem, meta: ItemSyncState?) {
        upsert(
            table = "shared_travel_actions",
            onConflict = "family_code,travel_title,place_name,title",
            body = JSONObject()
                .put("family_code", familyCode)
                .put("local_id", item.id)
                .put("travel_title", item.travelTitle)
                .put("place_name", item.placeName)
                .put("kind", item.kind)
                .put("title", item.title)
                .put("latitude", item.latitude)
                .put("longitude", item.longitude)
                .put("radius_meters", item.radiusMeters)
                .put("day_index", item.dayIndex)
                .put("done", item.done)
                .put("completed_at", item.completedAt)
                .put("note", item.note)
                .put("updated_at", meta?.updatedAt ?: System.currentTimeMillis())
                .put("updated_by", meta?.updatedBy ?: "나")
                .put("deleted_at", meta?.deletedAt)
                .put("sync_status", meta?.syncStatus ?: "synced")
                .put("assignee", meta?.assignee ?: "전체")
                .put("shared", meta?.shared ?: true)
        )
    }

    private fun upsertMember(familyCode: String, name: String, role: String) {
        upsert(
            table = "family_members",
            onConflict = "family_code,name",
            body = JSONObject()
                .put("family_code", familyCode)
                .put("name", name.ifBlank { "나" })
                .put("role", role)
                .put("device_label", "Android")
                .put("last_seen_at", System.currentTimeMillis())
        )
    }

    private fun get(table: String, query: String): String {
        val url = "${baseRestUrl()}/$table?$query"
        val conn = openConnection(url, "GET")
        return readResponse(conn)
    }

    private fun post(table: String, body: JSONObject): String {
        val conn = openConnection("${baseRestUrl()}/$table", "POST")
        conn.setRequestProperty("Prefer", "return=minimal")
        writeBody(conn, body)
        return readResponse(conn)
    }

    private fun upsert(table: String, body: JSONObject, onConflict: String? = null): String {
        val suffix = onConflict?.let { "?on_conflict=$it" } ?: ""
        val conn = openConnection("${baseRestUrl()}/$table$suffix", "POST")
        conn.setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
        writeBody(conn, body)
        return readResponse(conn)
    }

    private fun openConnection(url: String, method: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
            setRequestProperty("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doInput = true
            if (method == "POST" || method == "PATCH") doOutput = true
        }
    }

    private fun writeBody(conn: HttpURLConnection, body: JSONObject) {
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            throw IllegalStateException("Supabase REST error $code: $text")
        }
        return text
    }

    private fun baseRestUrl(): String = SupabaseConfig.SUPABASE_URL.trimEnd('/') + "/rest/v1"
    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}

private fun FamilySyncSnapshot.findMeta(entityType: String, localId: Long): ItemSyncState? =
    syncStates.firstOrNull { it.entityType == entityType && it.localId == localId }

private fun JSONObject.optDoubleOrNull(key: String): Double? =
    if (isNull(key)) null else optDouble(key)

private fun JSONObject.optLongOrNull(key: String): Long? =
    if (isNull(key)) null else optLong(key)
