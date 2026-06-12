package com.ultratul.lifekeeper.family

import com.ultratul.lifekeeper.data.FamilyActivityItem
import com.ultratul.lifekeeper.data.FamilyMemberItem
import com.ultratul.lifekeeper.data.FamilyShareProfile
import com.ultratul.lifekeeper.data.ItemSyncState
import com.ultratul.lifekeeper.data.PlaceItem
import com.ultratul.lifekeeper.data.PlaceTaskItem
import com.ultratul.lifekeeper.data.ShoppingItem
import com.ultratul.lifekeeper.data.TravelActionItem

/**
 * v14 가족 공유용 클라우드 동기화 계약.
 *
 * Supabase URL/Anon Key를 설정하면 가족 프로필, 멤버, 활동뿐 아니라
 * 장소 / 구매목록 / 장소 할 일 / 여행 액션까지 같이 올리고 받을 수 있습니다.
 */
data class FamilySyncSnapshot(
    val places: List<PlaceItem> = emptyList(),
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val placeTasks: List<PlaceTaskItem> = emptyList(),
    val travelActions: List<TravelActionItem> = emptyList(),
    val syncStates: List<ItemSyncState> = emptyList(),
)

interface FamilyCloudSyncContract {
    suspend fun createFamily(profile: FamilyShareProfile): String
    suspend fun joinFamily(inviteCode: String, memberName: String): FamilyShareProfile
    suspend fun uploadSharedData(profile: FamilyShareProfile)
    suspend fun downloadMembers(familyCode: String): List<FamilyMemberItem>
    suspend fun downloadActivities(familyCode: String): List<FamilyActivityItem>
    suspend fun uploadSnapshot(profile: FamilyShareProfile, snapshot: FamilySyncSnapshot)
    suspend fun downloadSnapshot(profile: FamilyShareProfile): FamilySyncSnapshot
}

/**
 * Supabase 미설정 상태에서도 앱이 안전하게 동작하도록 하는 로컬 전용 구현체.
 */
class LocalOnlyFamilyCloudSync : FamilyCloudSyncContract {
    override suspend fun createFamily(profile: FamilyShareProfile): String = profile.inviteCode.ifBlank { "LOCAL" }

    override suspend fun joinFamily(inviteCode: String, memberName: String): FamilyShareProfile {
        return FamilyShareProfile(
            familyName = "공유 가족",
            myName = memberName.ifBlank { "나" },
            inviteCode = inviteCode.ifBlank { "LOCAL" },
            role = "member",
            enabled = true
        )
    }

    override suspend fun uploadSharedData(profile: FamilyShareProfile) = Unit
    override suspend fun downloadMembers(familyCode: String): List<FamilyMemberItem> = emptyList()
    override suspend fun downloadActivities(familyCode: String): List<FamilyActivityItem> = emptyList()
    override suspend fun uploadSnapshot(profile: FamilyShareProfile, snapshot: FamilySyncSnapshot) = Unit
    override suspend fun downloadSnapshot(profile: FamilyShareProfile): FamilySyncSnapshot = FamilySyncSnapshot()
}
