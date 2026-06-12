@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ultratul.lifekeeper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ultratul.lifekeeper.data.DeliveryItem
import com.ultratul.lifekeeper.data.FamilyScheduleItem
import com.ultratul.lifekeeper.data.FamilyShareProfile
import com.ultratul.lifekeeper.data.FamilyMemberItem
import com.ultratul.lifekeeper.data.FamilyActivityItem
import com.ultratul.lifekeeper.data.HomeMaintenanceItem
import com.ultratul.lifekeeper.data.ItemSyncState
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("홈", Icons.Rounded.Home),
    Shopping("장소", Icons.Rounded.LocationOn),
    Time("시간", Icons.Rounded.Schedule),
    HomeCare("여행", Icons.Rounded.FlightTakeoff),
    More("더보기", Icons.Rounded.MoreHoriz)
}

private enum class AddType { Place, Shopping, PlaceTask, TravelPlan, TravelPlace, TravelAction, TravelChecklist, TravelReservation, TravelMemo, TimeReminder, Record, Care, Family, Stored, Delivery }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeKeeperApp(
    viewModel: LifeKeeperViewModel,
    onRequestPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var query by rememberSaveable { mutableStateOf("") }
    var addType by rememberSaveable { mutableStateOf<AddType?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            LifeKeeperTopBar(
                query = query,
                onQueryChange = { query = it },
                onNotify = { viewModel.showTestNotification() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigation(tab = tab, onTab = { tab = it })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    addType = when (tab) {
                        Tab.Shopping -> AddType.Shopping
                        Tab.Time -> AddType.TimeReminder
                        Tab.HomeCare -> AddType.TravelAction
                        Tab.More -> AddType.Place
                        Tab.Home -> AddType.Shopping
                    }
                },
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("추가") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.Home -> HomeScreen(
                    state = state,
                    onGoShopping = { tab = Tab.Shopping },
                    onGoRecords = { tab = Tab.Time },
                    onGoCare = { tab = Tab.More },
                    onMarkRecord = viewModel::markRecordToday,
                    onMarkCare = viewModel::markCareToday
                )

                Tab.Shopping -> ShoppingScreen(
                    places = state.places,
                    items = state.shoppingItems,
                    tasks = state.placeTasks,
                    syncStates = state.itemSyncStates,
                    query = query,
                    onAddPlace = { addType = AddType.Place },
                    onAdd = { addType = AddType.Shopping },
                    onAddTask = { addType = AddType.PlaceTask },
                    onDone = viewModel::setShoppingDone,
                    onDelete = viewModel::deleteShopping,
                    onTaskDone = viewModel::setPlaceTaskDone,
                    onTaskDelete = viewModel::deletePlaceTask,
                    onRequestPermissions = onRequestPermissions,
                    onOpenAppSettings = onOpenAppSettings,
                    onRegisterGeofences = viewModel::registerAllGeofences,
                    onTestNotification = viewModel::showTestNotification,
                    onTestPlaceNotification = viewModel::showPlaceTestNotification,
                    onCycleAssignee = viewModel::cycleItemAssignee,
                    onToggleShared = viewModel::toggleItemShared
                )

                Tab.Time -> TimeReminderScreen(
                    items = state.timeReminders,
                    query = query,
                    onAdd = { addType = AddType.TimeReminder },
                    onDone = viewModel::setTimeReminderDone,
                    onDelete = viewModel::deleteTimeReminder,
                    onTest = viewModel::testTimeReminder,
                    onReschedule = { viewModel.scheduleActiveTimeReminders() }
                )

                Tab.HomeCare -> TravelDashboardScreen(
                    state = state,
                    syncStates = state.itemSyncStates,
                    query = query,
                    onAddTravelPlan = { addType = AddType.TravelPlan },
                    onAddTravelPlace = { addType = AddType.TravelPlace },
                    onAddTravelAction = { addType = AddType.TravelAction },
                    onAddTravelChecklist = { addType = AddType.TravelChecklist },
                    onAddTravelReservation = { addType = AddType.TravelReservation },
                    onAddTravelMemo = { addType = AddType.TravelMemo },
                    onSeedTravelEssentials = { viewModel.seedTravelEssentials() },
                    onTravelActionDone = viewModel::setTravelActionDone,
                    onTravelActionDelete = viewModel::deleteTravelAction,
                    onTravelChecklistDone = viewModel::setTravelChecklistDone,
                    onTravelChecklistDelete = viewModel::deleteTravelChecklistItem,
                    onTravelReservationDone = viewModel::setTravelReservationDone,
                    onTravelReservationDelete = viewModel::deleteTravelReservation,
                    onTravelMemoDelete = viewModel::deleteTravelMemo,
                    onCycleAssignee = viewModel::cycleItemAssignee,
                    onToggleShared = viewModel::toggleItemShared
                )

                Tab.More -> MoreScreen(
                    state = state,
                    query = query,
                    onAddPlace = { addType = AddType.Place },
                    onDeletePlace = viewModel::deletePlace,
                    onAddTask = { addType = AddType.PlaceTask },
                    onTaskDone = viewModel::setPlaceTaskDone,
                    onTaskDelete = viewModel::deletePlaceTask,
                    onAddTravelPlan = { addType = AddType.TravelPlan },
                    onAddTravelPlace = { addType = AddType.TravelPlace },
                    onAddTravelAction = { addType = AddType.TravelAction },
                    onTravelActionDone = viewModel::setTravelActionDone,
                    onTravelActionDelete = viewModel::deleteTravelAction,
                    onAddFamily = { addType = AddType.Family },
                    onAddStored = { addType = AddType.Stored },
                    onAddDelivery = { addType = AddType.Delivery },
                    onSetDeliveryDone = viewModel::setDeliveryDone,
                    onRequestPermissions = onRequestPermissions,
                    onOpenAppSettings = onOpenAppSettings,
                    onRegisterGeofences = viewModel::registerAllGeofences,
                    onTestNotification = viewModel::showTestNotification,
                    onTestPlaceNotification = viewModel::showPlaceTestNotification,
                    onCreateFamilyShare = { viewModel.createFamilyShare("우리 가족", "나") },
                    onJoinFamilyShare = { viewModel.joinFamilyShare("FAM123", "나") },
                    onLeaveFamilyShare = viewModel::leaveFamilyShare,
                    onSimulateFamilySync = viewModel::simulateFamilySync,
                    onUpdateFamilyShareOptions = viewModel::updateFamilyShareOptions,
                    onCycleAssignee = viewModel::cycleItemAssignee,
                    onToggleShared = viewModel::toggleItemShared,
                    onExportBackup = viewModel::exportBackupToClipboard,
                    onImportBackup = viewModel::importBackupFromClipboard,
                    onCopyPermissionReport = viewModel::copyPermissionReportToClipboard
                )
            }

            if (addType != null) {
                AddBottomSheet(
                    type = addType!!,
                    places = state.places,
                    travelPlans = state.travelPlans,
                    travelPlaces = state.travelPlaces,
                    onDismiss = { addType = null },
                    onRequestPermissions = onRequestPermissions,
                    onSavePlace = { name, category, radius, current, lat, lng ->
                        viewModel.addPlace(name, category, radius, current, lat, lng)
                        addType = null
                    },
                    onSaveTravelPlan = { title, destination, days, note ->
                        viewModel.addTravelPlan(title, destination, days, note)
                        addType = null
                    },
                    onSaveTravelPlace = { travelTitle, placeName, category, day, radius, current, lat, lng, note ->
                        viewModel.addTravelPlace(travelTitle, placeName, category, day, radius, current, lat, lng, note)
                        addType = null
                    },
                    onSaveTravelAction = { travelTitle, placeName, kind, title, day, note ->
                        viewModel.addTravelAction(travelTitle, placeName, kind, title, day, note)
                        addType = null
                    },
                    onSaveTravelChecklist = { travelTitle, title, category, day, note ->
                        viewModel.addTravelChecklistItem(travelTitle, title, category, day, note)
                        addType = null
                    },
                    onSaveTravelReservation = { travelTitle, placeName, title, day, reservationNo, timeText, note ->
                        viewModel.addTravelReservation(travelTitle, placeName, title, day, reservationNo, timeText, note)
                        addType = null
                    },
                    onSaveTravelMemo = { travelTitle, title, day, category, content ->
                        viewModel.addTravelMemo(travelTitle, title, day, category, content)
                        addType = null
                    },
                    onSaveTimeReminder = { title, timeText, category, repeatMode, target, note ->
                        viewModel.addTimeReminder(title, timeText, category, repeatMode, target, note)
                        addType = null
                    },
                    onSaveShopping = { name, category, place, radius, current, lat, lng ->
                        viewModel.addShopping(name, category, place, radius, current, lat, lng)
                        addType = null
                    },
                    onSavePlaceTask = { title, category, place, radius, current, lat, lng, note ->
                        viewModel.addPlaceTask(title, category, place, radius, current, lat, lng, note)
                        addType = null
                    },
                    onSaveRecord = { title, emoji, cycle ->
                        viewModel.addRecord(title, emoji, cycle)
                        addType = null
                    },
                    onSaveCare = { title, emoji, cycle ->
                        viewModel.addCare(title, emoji, cycle)
                        addType = null
                    },
                    onSaveFamily = { title, tag, days, note ->
                        viewModel.addFamilySchedule(title, tag, days, note)
                        addType = null
                    },
                    onSaveStored = { name, place, memo ->
                        viewModel.addStoredItem(name, place, memo)
                        addType = null
                    },
                    onSaveDelivery = { title, days ->
                        viewModel.addDelivery(title, days)
                        addType = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LifeKeeperTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onNotify: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.background, Color(0xFFF3FBFA))
                )
            )
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("생활비서", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("장소+시간 기반 기억 비서", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            ElevatedCard(
                onClick = onNotify,
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Notifications, contentDescription = "알림")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            placeholder = { Text("다이소, 도톤보리, 여권, 분리수거 검색") },
            singleLine = true,
            shape = RoundedCornerShape(17.dp)
        )
    }
}

@Composable
private fun BottomNavigation(tab: Tab, onTab: (Tab) -> Unit) {
    BottomAppBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Tab.entries.forEach { item ->
            NavigationBarItem(
                selected = tab == item,
                onClick = { onTab(item) },
                icon = { Icon(item.icon, null) },
                label = { Text(item.label, fontWeight = FontWeight.Black) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeScreen(
    state: LifeKeeperUiState,
    onGoShopping: () -> Unit,
    onGoRecords: () -> Unit,
    onGoCare: () -> Unit,
    onMarkRecord: (RecordItem) -> Unit,
    onMarkCare: (HomeMaintenanceItem) -> Unit
) {
    val pendingShopping = state.shoppingItems.count { !it.done }
    val pendingTasks = state.placeTasks.count { !it.done }
    val overdue = state.recordItems.count { daysLeft(it.lastDateMillis, it.cycleDays) < 0 } +
        state.careItems.count { daysLeft(it.lastDateMillis, it.cycleDays) < 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        )
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("오늘 꼭 기억할 것", color = Color.White.copy(alpha = .92f), fontWeight = FontWeight.Bold)
                        Text(
                            "장소에 도착하면\n할 일과 먹을 것을 알려줘요.",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = onGoShopping, label = { Text("📍 장소 알림") })
                            AssistChip(onClick = onGoShopping, label = { Text("🛒 살 것 $pendingShopping") })
                            AssistChip(onClick = onGoShopping, label = { Text("✅ 할 일 $pendingTasks") })
                            AssistChip(onClick = onGoRecords, label = { Text("⏰ 시간") })
                            AssistChip(onClick = onGoCare, label = { Text("✈️ 여행") })
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("사야 할 것", pendingShopping.toString(), Modifier.weight(1f), onClick = onGoShopping)
                SummaryCard("장소 할 일", pendingTasks.toString(), Modifier.weight(1f), onClick = onGoShopping)
            }
        }

        val homePlaces = state.places.take(4)
        if (homePlaces.isNotEmpty()) {
            item { SectionHeader("장소별 살 것 / 할 일", "구매로 이동", onGoShopping) }
            items(homePlaces, key = { it.id }) { place ->
                val linked = state.shoppingItems.filter { it.placeName == place.name && !it.done }
                val tasks = state.placeTasks.filter { it.placeName == place.name && !it.done }
                CompactPlaceCard(
                    place = place,
                    pendingNames = linked.map { it.name },
                    taskNames = tasks.map { it.title },
                    onClick = onGoShopping
                )
            }
        }

        item { SectionHeader("우선순위", "전체보기", onGoRecords) }

        items(state.careItems.filter { daysLeft(it.lastDateMillis, it.cycleDays) < 14 }.take(3)) { item ->
            LifeItemCard(
                emoji = item.emoji,
                title = "${item.title} 관리 필요",
                subtitle = dueText(item.lastDateMillis, item.cycleDays),
                badge = statusText(item.lastDateMillis, item.cycleDays),
                onClick = { onMarkCare(item) }
            )
        }

        item {
            val daiso = state.shoppingItems.filter { !it.done && it.category == "다이소" }
            if (daiso.isNotEmpty()) {
                LifeItemCard(
                    emoji = "📍",
                    title = "다이소 근처에서 구매",
                    subtitle = daiso.joinToString(", ") { it.name },
                    badge = "장소 알림",
                    onClick = onGoShopping
                )
            }
        }

        item {
            val placeTasks = state.placeTasks.filter { !it.done }.take(3)
            if (placeTasks.isNotEmpty()) {
                LifeItemCard(
                    emoji = "✅",
                    title = "장소에서 할 일",
                    subtitle = placeTasks.joinToString(", ") { "${it.placeName}: ${it.title}" },
                    badge = "할 일 알림",
                    onClick = onGoShopping
                )
            }
        }

        items(state.familySchedules.filter { !it.done }.take(3)) { item ->
            LifeItemCard(
                emoji = "👨‍👩‍👧‍👦",
                title = item.title,
                subtitle = "${item.familyTag} · ${dateText(item.dueDateMillis)} ${item.note}",
                badge = "일정"
            )
        }

        item { SectionHeader("최근 기록", "기록하기", onGoRecords) }

        items(state.recordItems.take(3)) { item ->
            LifeItemCard(
                emoji = item.emoji,
                title = item.title,
                subtitle = "${daysSince(item.lastDateMillis)}일 전 기록됨",
                badge = "오늘 기록",
                onClick = { onMarkRecord(item) }
            )
        }
    }
}

@Composable
private fun ShoppingScreen(
    places: List<PlaceItem>,
    items: List<ShoppingItem>,
    tasks: List<PlaceTaskItem>,
    syncStates: List<ItemSyncState>,
    query: String,
    onAddPlace: () -> Unit,
    onAdd: () -> Unit,
    onAddTask: () -> Unit,
    onDone: (ShoppingItem, Boolean) -> Unit,
    onDelete: (ShoppingItem) -> Unit,
    onTaskDone: (PlaceTaskItem, Boolean) -> Unit,
    onTaskDelete: (PlaceTaskItem) -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onRegisterGeofences: () -> Unit,
    onTestNotification: () -> Unit,
    onTestPlaceNotification: (String, List<String>, List<String>) -> Unit,
    onCycleAssignee: (String, Long) -> Unit,
    onToggleShared: (String, Long) -> Unit
) {
    var filter by rememberSaveable { mutableStateOf("전체") }
    val categories = listOf("전체", "마트", "다이소", "약국", "편의점", "온라인", "학교", "회사", "집")
    val filteredItems = items
        .filter { filter == "전체" || it.category == filter }
        .filter { query.isBlank() || "${it.name} ${it.category} ${it.placeName}".contains(query, ignoreCase = true) }

    val filteredTasks = tasks
        .filter { filter == "전체" || it.category == filter }
        .filter { query.isBlank() || "${it.title} ${it.category} ${it.placeName} ${it.note}".contains(query, ignoreCase = true) }

    val placeGroups = places
        .filter { filter == "전체" || it.category == filter }
        .filter { query.isBlank() || "${it.name} ${it.category}".contains(query, ignoreCase = true) }
        .groupBy { it.category }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader("저장된 장소", "+ 장소 추가", onAddPlace)
        }

        item { MapShareGuideCard() }

        item {
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("장소에 살 것과 할 일을 연결", fontWeight = FontWeight.Black)
                            Text(
                                "마트/다이소/약국은 구매 알림, 집/회사/학교는 할 일 알림처럼 쓸 수 있어요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onRequestPermissions, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.MyLocation, null)
                            Spacer(Modifier.width(4.dp))
                            Text("위치/알림")
                        }
                        Button(onClick = onRegisterGeofences, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Refresh, null)
                            Spacer(Modifier.width(4.dp))
                            Text("재등록")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onOpenAppSettings, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Settings, null)
                            Spacer(Modifier.width(4.dp))
                            Text("백그라운드")
                        }
                        Button(onClick = onTestNotification, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Notifications, null)
                            Spacer(Modifier.width(4.dp))
                            Text("알림 테스트")
                        }
                    }
                }
            }
        }

        item { FlowFilters(categories, filter) { filter = it } }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdd, modifier = Modifier.weight(1f)) { Text("살 것 추가") }
                Button(onClick = onAddTask, modifier = Modifier.weight(1f)) { Text("할 일 추가") }
            }
        }

        if (places.isEmpty()) {
            item { EmptyText("먼저 마트, 다이소, 약국, 집, 회사 같은 장소를 추가해보세요.") }
        } else {
            placeGroups.forEach { (category, groupedPlaces) ->
                item {
                    Text(category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
                items(groupedPlaces, key = { it.id }) { place ->
                    val linkedItems = items.filter { it.placeName == place.name && !it.done }
                    val linkedTasks = tasks.filter { it.placeName == place.name && !it.done }
                    PlaceItemCard(
                        place = place,
                        pendingCount = linkedItems.size + linkedTasks.size,
                        pendingNames = linkedItems.map { it.name },
                        taskNames = linkedTasks.map { it.title },
                        onTest = { onTestPlaceNotification(place.name, linkedItems.map { it.name }, linkedTasks.map { it.title }) }
                    )
                }
            }
        }

        item { SectionHeader("장소별 항목", "+ 살 것", onAdd) }

        val placeNames = (filteredItems.map { it.placeName } + filteredTasks.map { it.placeName }).distinct()
        placeNames.forEach { place ->
            val groupItems = filteredItems.filter { it.placeName == place }
            val groupTasks = filteredTasks.filter { it.placeName == place }
            item {
                ElevatedCard(shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📍 $place", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text("${groupItems.count { !it.done } + groupTasks.count { !it.done }}개")
                            }
                        }
                        val pendingShopping = groupItems.filter { !it.done }.map { "살 것: ${it.name}" }
                        val pendingTasks = groupTasks.filter { !it.done }.map { "할 일: ${it.title}" }
                        if (pendingShopping.isNotEmpty() || pendingTasks.isNotEmpty()) {
                            ChipFlow(values = (pendingShopping + pendingTasks).take(8))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onTestPlaceNotification(place, groupItems.filter { !it.done }.map { it.name }, groupTasks.filter { !it.done }.map { it.title }) },
                                modifier = Modifier.weight(1f)
                            ) { Text("근처 도착 테스트") }
                            Button(onClick = onAddTask, modifier = Modifier.weight(1f)) { Text("할 일 추가") }
                        }
                    }
                }
            }
            items(groupItems, key = { it.id }) { item ->
                val meta = syncStates.firstOrNull { it.entityType == "shopping" && it.localId == item.id }
                ShoppingItemCard(
                    item = item,
                    meta = meta,
                    onDone = { done -> onDone(item, done) },
                    onDelete = { onDelete(item) },
                    onCycleAssignee = { onCycleAssignee("shopping", item.id) },
                    onToggleShared = { onToggleShared("shopping", item.id) }
                )
            }
            items(groupTasks, key = { it.id }) { task ->
                val meta = syncStates.firstOrNull { it.entityType == "place_task" && it.localId == task.id }
                PlaceTaskCard(
                    item = task,
                    meta = meta,
                    onDone = { done -> onTaskDone(task, done) },
                    onDelete = { onTaskDelete(task) },
                    onCycleAssignee = { onCycleAssignee("place_task", task.id) },
                    onToggleShared = { onToggleShared("place_task", task.id) }
                )
            }
        }

        if (filteredItems.isEmpty() && filteredTasks.isEmpty()) {
            item { EmptyText("이 장소에 살 것 또는 할 일을 추가해보세요.") }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeReminderScreen(
    items: List<TimeReminderItem>,
    query: String,
    onAdd: () -> Unit,
    onDone: (TimeReminderItem, Boolean) -> Unit,
    onDelete: (TimeReminderItem) -> Unit,
    onTest: (TimeReminderItem) -> Unit,
    onReschedule: () -> Unit
) {
    var filter by rememberSaveable { mutableStateOf("전체") }
    val filters = listOf("전체", "오늘", "반복", "여행", "완료")
    val filtered = items
        .filter { query.isBlank() || "${it.title} ${it.category} ${it.repeatMode} ${it.target} ${it.note}".contains(query, ignoreCase = true) }
        .filter {
            when (filter) {
                "오늘" -> !it.done && it.category == "오늘"
                "반복" -> !it.done && (it.category == "반복" || it.repeatMode != "한 번")
                "여행" -> !it.done && it.category == "여행"
                "완료" -> it.done
                else -> !it.done
            }
        }
        .sortedBy { it.timeText }

    val next = items.filter { !it.done && it.enabled }.minByOrNull { it.reminderAtMillis }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("시간 알림", "+ 알림", onAdd) }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("장소가 아니어도 필요한 시간에 알려줘요.", fontWeight = FontWeight.Black)
                    Text(
                        "약 복용, 출발 준비, 여행 일정, 반복 루틴까지 시간 기준으로 관리합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onReschedule, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Refresh, null)
                            Spacer(Modifier.width(4.dp))
                            Text("재등록")
                        }
                        Button(
                            onClick = { next?.let(onTest) },
                            modifier = Modifier.weight(1f),
                            enabled = next != null
                        ) {
                            Icon(Icons.Rounded.Notifications, null)
                            Spacer(Modifier.width(4.dp))
                            Text("테스트")
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("오늘/여행", items.count { !it.done && (it.category == "오늘" || it.category == "여행") }.toString(), Modifier.weight(1f)) {}
                SummaryCard("반복 알림", items.count { !it.done && it.repeatMode != "한 번" }.toString(), Modifier.weight(1f)) {}
            }
        }

        item {
            FlowFilters(filters, filter) { filter = it }
        }

        if (next != null && filter == "전체") {
            item {
                LifeItemCard(
                    emoji = "⏰",
                    title = "다음 알림: ${next.title}",
                    subtitle = "${next.timeText} · ${next.repeatMode} · ${next.target} ${next.note}",
                    badge = "다음",
                    onClick = { onTest(next) }
                )
            }
        }

        items(filtered, key = { it.id }) { item ->
            TimeReminderCard(
                item = item,
                onDone = { onDone(item, !item.done) },
                onDelete = { onDelete(item) },
                onTest = { onTest(item) }
            )
        }

        if (filtered.isEmpty()) {
            item { EmptyText("${filter} 시간 알림이 없어요.") }
        }
    }
}

@Composable
private fun TimeReminderCard(
    item: TimeReminderItem,
    onDone: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox(if (item.category == "여행") "✈️" else if (item.repeatMode != "한 번") "🔁" else "⏰")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${item.timeText} · ${item.repeatMode} · ${item.target}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.note.isNotBlank()) {
                        Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) { Text(if (item.done) "완료" else item.category) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onTest, modifier = Modifier.weight(1f)) { Text("테스트") }
                OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text(if (item.done) "되돌리기" else "완료") }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "삭제") }
            }
        }
    }
}

@Composable
private fun RecordsScreen(
    items: List<RecordItem>,
    query: String,
    onAdd: () -> Unit,
    onMarkToday: (RecordItem) -> Unit
) {
    val filtered = items.filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("마지막 언제였지?", "+ 추가", onAdd) }

        item {
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("버튼 한 번으로 오늘 기록", fontWeight = FontWeight.Black)
                    Text("마지막 날짜와 경과일을 자동 계산해요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filtered.take(8).forEach { item ->
                            AssistChip(onClick = { onMarkToday(item) }, label = { Text("${item.emoji} ${item.title}") })
                        }
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { item ->
            ProgressCard(
                emoji = item.emoji,
                title = item.title,
                subtitle = "마지막 ${daysSince(item.lastDateMillis)}일 전 · 권장 주기 ${item.cycleDays}일",
                progress = progress(item.lastDateMillis, item.cycleDays),
                bottom = dueText(item.lastDateMillis, item.cycleDays),
                badge = statusText(item.lastDateMillis, item.cycleDays),
                onClick = { onMarkToday(item) }
            )
        }

        if (filtered.isEmpty()) {
            item { EmptyText("기록 항목이 없어요.") }
        }
    }
}

@Composable
private fun HomeCareScreen(
    items: List<HomeMaintenanceItem>,
    query: String,
    onAdd: () -> Unit,
    onMarkToday: (HomeMaintenanceItem) -> Unit
) {
    val filtered = items.filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("집관리", "+ 추가", onAdd) }

        items(filtered, key = { it.id }) { item ->
            ProgressCard(
                emoji = item.emoji,
                title = item.title,
                subtitle = "마지막 ${daysSince(item.lastDateMillis)}일 전 · 권장 주기 ${item.cycleDays}일",
                progress = progress(item.lastDateMillis, item.cycleDays),
                bottom = dueText(item.lastDateMillis, item.cycleDays),
                badge = statusText(item.lastDateMillis, item.cycleDays),
                onClick = { onMarkToday(item) }
            )
        }

        if (filtered.isEmpty()) {
            item { EmptyText("집관리 항목이 없어요.") }
        }
    }
}

@Composable
private fun TravelDashboardScreen(
    state: LifeKeeperUiState,
    syncStates: List<ItemSyncState>,
    query: String,
    onAddTravelPlan: () -> Unit,
    onAddTravelPlace: () -> Unit,
    onAddTravelAction: () -> Unit,
    onAddTravelChecklist: () -> Unit,
    onAddTravelReservation: () -> Unit,
    onAddTravelMemo: () -> Unit,
    onSeedTravelEssentials: () -> Unit,
    onTravelActionDone: (TravelActionItem, Boolean) -> Unit,
    onTravelActionDelete: (TravelActionItem) -> Unit,
    onTravelChecklistDone: (TravelChecklistItem, Boolean) -> Unit,
    onTravelChecklistDelete: (TravelChecklistItem) -> Unit,
    onTravelReservationDone: (TravelReservationItem, Boolean) -> Unit,
    onTravelReservationDelete: (TravelReservationItem) -> Unit,
    onTravelMemoDelete: (TravelMemoItem) -> Unit,
    onCycleAssignee: (String, Long) -> Unit,
    onToggleShared: (String, Long) -> Unit
) {
    val travelPlans = state.travelPlans.filter { query.isBlank() || "${it.title} ${it.destination} ${it.note}".contains(query, ignoreCase = true) }
    val travelPlaces = state.travelPlaces.filter { query.isBlank() || "${it.travelTitle} ${it.name} ${it.note}".contains(query, ignoreCase = true) }
    val travelActions = state.travelActions.filter { query.isBlank() || "${it.travelTitle} ${it.placeName} ${it.kind} ${it.title} ${it.note}".contains(query, ignoreCase = true) }
    val travelChecklist = state.travelChecklistItems.filter { query.isBlank() || "${it.travelTitle} ${it.category} ${it.title} ${it.note}".contains(query, ignoreCase = true) }
    val travelReservations = state.travelReservations.filter { query.isBlank() || "${it.travelTitle} ${it.placeName} ${it.title} ${it.reservationNo} ${it.note}".contains(query, ignoreCase = true) }
    val travelMemos = state.travelMemos.filter { query.isBlank() || "${it.travelTitle} ${it.category} ${it.title} ${it.content}".contains(query, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("여행 플래너", "+ 여행", onAddTravelPlan) }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("여행지에 도착하면 기억할 것을 알려줘요.", color = Color.White, fontWeight = FontWeight.Black)
                    Text(
                        "먹을 것, 할 일, 사진 포인트, 예약 확인을 여행 장소별로 정리합니다.",
                        color = Color.White.copy(alpha = .9f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onAddTravelPlace, modifier = Modifier.weight(1f)) { Text("여행 장소") }
                        Button(onClick = onAddTravelAction, modifier = Modifier.weight(1f)) { Text("액션 추가") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onAddTravelChecklist, modifier = Modifier.weight(1f)) { Text("체크리스트") }
                        OutlinedButton(onClick = onAddTravelReservation, modifier = Modifier.weight(1f)) { Text("예약/티켓") }
                        OutlinedButton(onClick = onAddTravelMemo, modifier = Modifier.weight(1f)) { Text("메모") }
                    }
                    Button(onClick = onSeedTravelEssentials, modifier = Modifier.fillMaxWidth()) {
                        Text("여권·환전·보조배터리 필수 세트 추가")
                    }
                }
            }
        }

        item {
            TravelDayTimelinePanel(
                actions = travelActions,
                checklist = travelChecklist,
                reservations = travelReservations,
                memos = travelMemos
            )
        }

        if (travelPlans.isEmpty()) {
            item { EmptyText("여행을 추가해보세요. 예: 오사카 3박 4일") }
        } else {
            items(travelPlans, key = { it.id }) { plan ->
                val placeCount = state.travelPlaces.count { it.travelPlanId == plan.id }
                val actionCount = state.travelActions.count { it.travelPlanId == plan.id && !it.done }
                TravelPlanCard(plan, placeCount, actionCount, onAddPlace = onAddTravelPlace, onAddAction = onAddTravelAction)
            }
        }

        item { SectionHeader("여행 장소", "+ 장소", onAddTravelPlace) }
        items(travelPlaces.take(12), key = { it.id }) { place ->
            val actions = state.travelActions.filter { it.travelPlaceId == place.id && !it.done }
            TravelPlaceCard(place, actions)
        }


        item { SectionHeader("여권 / 환전 / 준비물 체크리스트", "+ 체크", onAddTravelChecklist) }
        items(travelChecklist.take(20), key = { it.id }) { item ->
            TravelChecklistCard(
                item = item,
                onDone = { done -> onTravelChecklistDone(item, done) },
                onDelete = { onTravelChecklistDelete(item) }
            )
        }

        item { SectionHeader("예약 / 티켓 / 번호", "+ 예약", onAddTravelReservation) }
        items(travelReservations.take(16), key = { it.id }) { item ->
            TravelReservationCard(
                item = item,
                onDone = { done -> onTravelReservationDone(item, done) },
                onDelete = { onTravelReservationDelete(item) }
            )
        }

        item { SectionHeader("오프라인 여행 메모", "+ 메모", onAddTravelMemo) }
        items(travelMemos.take(12), key = { it.id }) { memo ->
            TravelMemoCard(memo = memo, onDelete = { onTravelMemoDelete(memo) })
        }

        item { SectionHeader("먹을 것 / 할 일 / 사진", "+ 액션", onAddTravelAction) }
        items(travelActions.take(16), key = { it.id }) { action ->
            val meta = syncStates.firstOrNull { it.entityType == "travel_action" && it.localId == action.id }
            TravelActionCard(
                item = action,
                meta = meta,
                onDone = { done -> onTravelActionDone(action, done) },
                onDelete = { onTravelActionDelete(action) },
                onCycleAssignee = { onCycleAssignee("travel_action", action.id) },
                onToggleShared = { onToggleShared("travel_action", action.id) }
            )
        }
    }
}

@Composable
private fun FamilySharePanel(
    profile: FamilyShareProfile?,
    members: List<FamilyMemberItem>,
    activities: List<FamilyActivityItem>,
    supabaseConfigured: Boolean,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onSync: () -> Unit,
    onUpdateOptions: (FamilyShareProfile, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    val current = profile ?: FamilyShareProfile()
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox("👨‍👩‍👧‍👦")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("가족 공유", fontWeight = FontWeight.Black)
                    Text(
                        if (current.enabled) "${current.familyName.ifBlank { "공유 가족" }} · 초대코드 ${current.inviteCode.ifBlank { "미생성" }}"
                        else "가족과 마트 구매목록, 장소 할 일을 같이 볼 수 있어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Badge(containerColor = if (current.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                    Text(if (current.enabled) "공유중" else "꺼짐")
                }
            }

            Badge(
                containerColor = if (supabaseConfigured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(if (supabaseConfigured) "Supabase 연결됨" else "Supabase 설정 필요")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCreate, modifier = Modifier.weight(1f)) { Text("가족 만들기") }
                OutlinedButton(onClick = onJoin, modifier = Modifier.weight(1f)) { Text("초대 참여") }
            }

            if (current.enabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onSync, modifier = Modifier.weight(1f)) { Text("동기화 테스트") }
                    OutlinedButton(onClick = onLeave, modifier = Modifier.weight(1f)) { Text("공유 끄기") }
                }

                Text("공유 범위", fontWeight = FontWeight.Black)
                FamilyShareSwitchRow("장소", current.sharePlaces) {
                    onUpdateOptions(current, it, current.shareShopping, current.sharePlaceTasks, current.shareTravel, current.shareTimeReminders, current.shareStoredItems)
                }
                FamilyShareSwitchRow("마트/다이소 구매 목록", current.shareShopping) {
                    onUpdateOptions(current, current.sharePlaces, it, current.sharePlaceTasks, current.shareTravel, current.shareTimeReminders, current.shareStoredItems)
                }
                FamilyShareSwitchRow("장소에서 할 일", current.sharePlaceTasks) {
                    onUpdateOptions(current, current.sharePlaces, current.shareShopping, it, current.shareTravel, current.shareTimeReminders, current.shareStoredItems)
                }
                FamilyShareSwitchRow("여행 플래너", current.shareTravel) {
                    onUpdateOptions(current, current.sharePlaces, current.shareShopping, current.sharePlaceTasks, it, current.shareTimeReminders, current.shareStoredItems)
                }
                FamilyShareSwitchRow("개인 시간 알림", current.shareTimeReminders) {
                    onUpdateOptions(current, current.sharePlaces, current.shareShopping, current.sharePlaceTasks, current.shareTravel, it, current.shareStoredItems)
                }
                FamilyShareSwitchRow("물건 위치", current.shareStoredItems) {
                    onUpdateOptions(current, current.sharePlaces, current.shareShopping, current.sharePlaceTasks, current.shareTravel, current.shareTimeReminders, it)
                }

                if (members.isNotEmpty()) {
                    Text("가족 구성원", fontWeight = FontWeight.Black)
                    ChipFlow(members.take(6).map { "${if (it.role == "owner") "👑" else "👤"} ${it.name}" })
                }

                if (activities.isNotEmpty()) {
                    Text("최근 공유 활동", fontWeight = FontWeight.Black)
                    activities.take(3).forEach { activity ->
                        LifeItemCard(
                            emoji = "🔄",
                            title = activity.actorName,
                            subtitle = activity.message,
                            badge = "공유"
                        )
                    }
                }
            } else {
                Text(
                    "Supabase URL/Anon Key를 설정하면 가족 간 자동 동기화가 켜집니다. 설정 전에는 로컬 시뮬레이션으로 동작합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FamilyShareSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MegaStatusPanel(
    state: LifeKeeperUiState,
    onOpenSettings: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    val pendingSync = state.itemSyncStates.count { it.deletedAt == null && it.syncStatus.contains("pending", ignoreCase = true) }
    val deletedPending = state.itemSyncStates.count { it.deletedAt != null }
    val assigned = state.itemSyncStates.count { it.assignee != "전체" }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox("🧩")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("v20 백업/권한 진단", fontWeight = FontWeight.Black)
                    Text(
                        "백업/복원과 권한 진단 리포트를 실사용형으로 강화했어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) { Text("v20") }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("🔄 동기화 대기 $pendingSync") })
                AssistChip(onClick = {}, label = { Text("🗑 삭제 대기 $deletedPending") })
                AssistChip(onClick = {}, label = { Text("👤 담당 지정 $assigned") })
                AssistChip(onClick = {}, label = { Text(if (state.supabaseConfigured) "☁️ Supabase 연결" else "☁️ Supabase 설정 필요") })
                AssistChip(onClick = {}, label = { Text("🔔 알림 액션 ON") })
            }

            Text("권한 진단", fontWeight = FontWeight.Black)
            LifeItemCard(
                emoji = if (state.permissionGranted) "✅" else "⚠️",
                title = "위치 권한",
                subtitle = if (state.permissionGranted) "장소 기반 알림을 사용할 수 있어요." else "장소 알림을 위해 위치 권한이 필요해요.",
                badge = if (state.permissionGranted) "정상" else "확인"
            )
            LifeItemCard(
                emoji = "📳",
                title = "진동/알림",
                subtitle = "v10부터 시간/장소 알림 진동 패턴을 분리했어요. 안드로이드 알림 채널 설정이 우선 적용됩니다.",
                badge = "진동"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRequestPermissions, modifier = Modifier.weight(1f)) { Text("권한 요청") }
                Button(onClick = onOpenSettings, modifier = Modifier.weight(1f)) { Text("앱 설정") }
            }

            Text("백업/복원", fontWeight = FontWeight.Black)
            Text(
                "JSON 백업/복원 구조가 추가됐어요. 다음 단계에서 파일 선택 UI를 붙이면 가족 데이터 전체를 내보내고 복원할 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TravelDayTimelinePanel(
    actions: List<TravelActionItem>,
    checklist: List<TravelChecklistItem>,
    reservations: List<TravelReservationItem>,
    memos: List<TravelMemoItem>
) {
    val dayKeys = (
        actions.filter { !it.done }.map { it.dayIndex.coerceAtLeast(1) } +
            reservations.filter { !it.done }.map { it.dayIndex.coerceAtLeast(1) } +
            memos.map { it.dayIndex.coerceAtLeast(0) }.filter { it > 0 }
        ).distinct().sorted()

    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox("🗓️")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Day별 여행 타임라인", fontWeight = FontWeight.Black)
                    Text(
                        "먹을 것, 할 일, 사진, 예약, 메모를 일차별로 모아서 봅니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val readyCount = checklist.count { !it.done }
            if (readyCount > 0) {
                LifeItemCard(
                    emoji = "🎒",
                    title = "출발 전 준비물",
                    subtitle = "미완료 체크리스트 ${readyCount}개",
                    badge = "준비"
                )
            }

            if (dayKeys.isEmpty() && readyCount == 0) {
                Text("아직 남은 여행 일정이 없어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                dayKeys.take(5).forEach { day ->
                    Text("Day $day", fontWeight = FontWeight.Black)

                    reservations.filter { !it.done && it.dayIndex == day }.take(3).forEach { item ->
                        LifeItemCard(
                            emoji = "🎫",
                            title = item.title,
                            subtitle = listOf(item.timeText, item.placeName, item.reservationNo).filter { it.isNotBlank() }.joinToString(" · "),
                            badge = "예약"
                        )
                    }

                    actions.filter { !it.done && it.dayIndex == day }.take(4).forEach { action ->
                        LifeItemCard(
                            emoji = when (action.kind) {
                                "먹을 것" -> "🍜"
                                "사진" -> "📸"
                                "예약" -> "🎫"
                                "주의" -> "⚠️"
                                else -> "✅"
                            },
                            title = action.title,
                            subtitle = "${action.placeName} · ${action.kind}" + if (action.note.isNotBlank()) " · ${action.note}" else "",
                            badge = action.kind
                        )
                    }

                    memos.filter { it.dayIndex == day }.take(2).forEach { memo ->
                        LifeItemCard(
                            emoji = "📝",
                            title = memo.title,
                            subtitle = memo.content.ifBlank { memo.category },
                            badge = memo.category
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelChecklistCard(
    item: TravelChecklistItem,
    onDone: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.done, onCheckedChange = onDone)
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val day = if (item.dayIndex <= 0) "출발 전" else "Day ${item.dayIndex}"
                Text("$day · ${item.category}" + if (item.note.isNotBlank()) " · ${item.note}" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "삭제")
            }
        }
    }
}

@Composable
private fun TravelReservationCard(
    item: TravelReservationItem,
    onDone: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.done, onCheckedChange = onDone)
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOf("Day ${item.dayIndex}", item.timeText, item.placeName, item.reservationNo).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.note.isNotBlank()) {
                    Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "삭제")
            }
        }
    }
}

@Composable
private fun TravelMemoCard(
    memo: TravelMemoItem,
    onDelete: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiBox("📝")
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(memo.title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val day = if (memo.dayIndex <= 0) "전체" else "Day ${memo.dayIndex}"
                Text("$day · ${memo.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (memo.content.isNotBlank()) {
                    Text(memo.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "삭제")
            }
        }
    }
}

@Composable
private fun BackupRestorePanel(
    state: LifeKeeperUiState,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onCopyPermissionReport: () -> Unit
) {
    val totalItems = state.places.size +
        state.shoppingItems.size +
        state.placeTasks.size +
        state.travelPlans.size +
        state.travelPlaces.size +
        state.travelActions.size +
        state.travelChecklistItems.size +
        state.travelReservations.size +
        state.travelMemos.size +
        state.timeReminders.size

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox("💾")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("v20 백업/복원", fontWeight = FontWeight.Black)
                    Text(
                        "장소, 구매목록, 여행, 시간 알림을 JSON으로 백업하고 복원합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) { Text("${totalItems}개") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExportBackup, modifier = Modifier.weight(1f)) { Text("백업 복사") }
                OutlinedButton(onClick = onImportBackup, modifier = Modifier.weight(1f)) { Text("클립보드 복원") }
            }

            Text(
                "백업 JSON은 클립보드에 복사됩니다. 메모장, 카카오톡 나에게 보내기, 구글 Keep 등에 저장해두면 새 폰에서 복원할 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("권한 진단 리포트", fontWeight = FontWeight.Black)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(if (state.permissionGranted) "📍 위치 정상" else "📍 위치 필요") })
                AssistChip(onClick = {}, label = { Text("📳 진동 manifest 포함") })
                AssistChip(onClick = {}, label = { Text(if (state.supabaseConfigured) "☁️ Supabase 연결" else "☁️ Supabase 설정 필요") })
            }

            OutlinedButton(onClick = onCopyPermissionReport, modifier = Modifier.fillMaxWidth()) {
                Text("권한 진단 내용 복사")
            }
        }
    }
}

@Composable
private fun MoreScreen(
    state: LifeKeeperUiState,
    query: String,
    onAddPlace: () -> Unit,
    onDeletePlace: (PlaceItem) -> Unit,
    onAddTask: () -> Unit,
    onTaskDone: (PlaceTaskItem, Boolean) -> Unit,
    onTaskDelete: (PlaceTaskItem) -> Unit,
    onAddTravelPlan: () -> Unit,
    onAddTravelPlace: () -> Unit,
    onAddTravelAction: () -> Unit,
    onTravelActionDone: (TravelActionItem, Boolean) -> Unit,
    onTravelActionDelete: (TravelActionItem) -> Unit,
    onAddFamily: () -> Unit,
    onAddStored: () -> Unit,
    onAddDelivery: () -> Unit,
    onSetDeliveryDone: (DeliveryItem, Boolean) -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onRegisterGeofences: () -> Unit,
    onTestNotification: () -> Unit,
    onTestPlaceNotification: (String, List<String>, List<String>) -> Unit,
    onCreateFamilyShare: () -> Unit,
    onJoinFamilyShare: () -> Unit,
    onLeaveFamilyShare: () -> Unit,
    onSimulateFamilySync: () -> Unit,
    onUpdateFamilyShareOptions: (FamilyShareProfile, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onCycleAssignee: (String, Long) -> Unit,
    onToggleShared: (String, Long) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onCopyPermissionReport: () -> Unit
) {
    val stored = state.storedItems.filter { query.isBlank() || "${it.itemName} ${it.place} ${it.memo}".contains(query, ignoreCase = true) }
    val deliveries = state.deliveries.filter { query.isBlank() || "${it.title} ${it.status}".contains(query, ignoreCase = true) }
    val family = state.familySchedules.filter { query.isBlank() || "${it.title} ${it.familyTag} ${it.note}".contains(query, ignoreCase = true) }
    val places = state.places.filter { query.isBlank() || "${it.name} ${it.category}".contains(query, ignoreCase = true) }
    val placeTasks = state.placeTasks.filter { query.isBlank() || "${it.title} ${it.placeName} ${it.note}".contains(query, ignoreCase = true) }
    val travelPlans = state.travelPlans.filter { query.isBlank() || "${it.title} ${it.destination} ${it.note}".contains(query, ignoreCase = true) }
    val travelPlaces = state.travelPlaces.filter { query.isBlank() || "${it.travelTitle} ${it.name} ${it.note}".contains(query, ignoreCase = true) }
    val travelActions = state.travelActions.filter { query.isBlank() || "${it.travelTitle} ${it.placeName} ${it.kind} ${it.title} ${it.note}".contains(query, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("더보기", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }

        item {
            FamilySharePanel(
                profile = state.familyShareProfile,
                members = state.familyMembers,
                activities = state.familyActivities,
                supabaseConfigured = state.supabaseConfigured,
                onCreate = onCreateFamilyShare,
                onJoin = onJoinFamilyShare,
                onLeave = onLeaveFamilyShare,
                onSync = onSimulateFamilySync,
                onUpdateOptions = onUpdateFamilyShareOptions
            )
        }

        item {
            MegaStatusPanel(state = state, onOpenSettings = onOpenAppSettings, onRequestPermissions = onRequestPermissions)
        }

        item {
            BackupRestorePanel(
                state = state,
                onExportBackup = onExportBackup,
                onImportBackup = onImportBackup,
                onCopyPermissionReport = onCopyPermissionReport
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoreTile("🧳", "여행 플래너", "장소별 먹을 것/할 일", Modifier.weight(1f), onAddTravelPlan)
                MoreTile("📍", "장소 관리", "마트, 다이소, 약국", Modifier.weight(1f), onAddPlace)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoreTile("✅", "장소 할 일", "집, 회사, 학교 알림", Modifier.weight(1f), onAddTask)
                MoreTile("📦", "물건 위치", "여권, 체온계 찾기", Modifier.weight(1f), onAddStored)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoreTile("🚚", "택배 기억", "주문한 물건 관리", Modifier.weight(1f), onAddDelivery)
                MoreTile("👨‍👩‍👧‍👦", "가족 일정", "아이 일정 메모", Modifier.weight(1f), onAddFamily)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoreTile("🛠️", "집관리", "필터, 에어컨, 정수기", Modifier.weight(1f)) {}
                MoreTile("⏱️", "마지막 언제였지", "치과, 운동, 엔진오일", Modifier.weight(1f)) {}
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("여행 모드", fontWeight = FontWeight.Black)
                    Text(
                        "여행지를 미리 등록하고, 그 장소 근처에 가면 먹을 것·할 일·사진 포인트·예약 확인을 알려줘요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onAddTravelPlace, modifier = Modifier.weight(1f)) { Text("여행 장소") }
                        Button(onClick = onAddTravelAction, modifier = Modifier.weight(1f)) { Text("먹을 것/할 일") }
                    }
                }
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("알림/위치 권한", fontWeight = FontWeight.Black)
                    Text(
                        "근처 구매/할 일/여행 알림을 안정적으로 받으려면 위치 권한과 알림 권한을 확인하세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onRequestPermissions, modifier = Modifier.weight(1f)) { Text("권한 요청") }
                        Button(onClick = onOpenAppSettings, modifier = Modifier.weight(1f)) { Text("앱 설정") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onRegisterGeofences, modifier = Modifier.weight(1f)) { Text("장소 재등록") }
                        Button(onClick = onTestNotification, modifier = Modifier.weight(1f)) { Text("알림 테스트") }
                    }
                }
            }
        }

        item { MapShareGuideCard() }

        item { SectionHeader("여행 플래너", "+ 여행 추가", onAddTravelPlan) }
        if (travelPlans.isEmpty()) {
            item { EmptyText("여행을 추가해보세요. 예: 오사카 3박 4일") }
        } else {
            items(travelPlans, key = { it.id }) { plan ->
                val placeCount = state.travelPlaces.count { it.travelPlanId == plan.id }
                val actionCount = state.travelActions.count { it.travelPlanId == plan.id && !it.done }
                TravelPlanCard(plan, placeCount, actionCount, onAddPlace = onAddTravelPlace, onAddAction = onAddTravelAction)
            }
        }

        item { SectionHeader("여행 장소", "+ 장소 추가", onAddTravelPlace) }
        items(travelPlaces.take(12), key = { it.id }) { place ->
            val actions = state.travelActions.filter { it.travelPlaceId == place.id && !it.done }
            TravelPlaceCard(place, actions)
        }

        item { SectionHeader("여행 먹을 것/할 일", "+ 액션 추가", onAddTravelAction) }
        items(travelActions.take(16), key = { it.id }) { action ->
            val meta = state.itemSyncStates.firstOrNull { it.entityType == "travel_action" && it.localId == action.id }
            TravelActionCard(
                item = action,
                meta = meta,
                onDone = { done -> onTravelActionDone(action, done) },
                onDelete = { onTravelActionDelete(action) },
                onCycleAssignee = { onCycleAssignee("travel_action", action.id) },
                onToggleShared = { onToggleShared("travel_action", action.id) }
            )
        }

        item { SectionHeader("장소 관리", "+ 장소 추가", onAddPlace) }
        if (places.isEmpty()) {
            item { EmptyText("저장된 장소가 없어요. 마트, 다이소, 약국, 집, 회사, 학교를 먼저 추가해보세요.") }
        } else {
            items(places.take(8), key = { it.id }) { place ->
                val linkedShopping = state.shoppingItems.filter { it.placeName == place.name && !it.done }
                val linkedTasks = state.placeTasks.filter { it.placeName == place.name && !it.done }
                PlaceItemCard(
                    place = place,
                    pendingCount = linkedShopping.size + linkedTasks.size,
                    pendingNames = linkedShopping.map { it.name },
                    taskNames = linkedTasks.map { it.title },
                    onTest = { onTestPlaceNotification(place.name, linkedShopping.map { it.name }, linkedTasks.map { it.title }) },
                    onDelete = { onDeletePlace(place) }
                )
            }
        }

        item { SectionHeader("장소 할 일", "+ 할 일 추가", onAddTask) }
        items(placeTasks.take(10), key = { it.id }) { task ->
            val meta = state.itemSyncStates.firstOrNull { it.entityType == "place_task" && it.localId == task.id }
            PlaceTaskCard(
                item = task,
                meta = meta,
                onDone = { done -> onTaskDone(task, done) },
                onDelete = { onTaskDelete(task) },
                onCycleAssignee = { onCycleAssignee("place_task", task.id) },
                onToggleShared = { onToggleShared("place_task", task.id) }
            )
        }

        item { SectionHeader("가족 일정", "+ 추가", onAddFamily) }
        items(family.take(5)) {
            LifeItemCard("👨‍👩‍👧‍👦", it.title, "${it.familyTag} · ${dateText(it.dueDateMillis)} ${it.note}", "일정")
        }

        item { SectionHeader("물건 위치", "+ 추가", onAddStored) }
        items(stored.take(8)) {
            LifeItemCard("📦", it.itemName, it.place + if (it.memo.isNotBlank()) " · ${it.memo}" else "", "위치")
        }

        item { SectionHeader("택배", "+ 추가", onAddDelivery) }
        items(deliveries.take(8)) { item ->
            LifeItemCard(
                emoji = "🚚",
                title = item.title,
                subtitle = "${item.status} · ${item.expectedAt?.let { dateText(it) } ?: "예정일 없음"}",
                badge = if (item.done) "완료" else "진행중",
                onClick = { onSetDeliveryDone(item, !item.done) }
            )
        }
    }
}

@Composable
private fun TravelPlanCard(
    plan: TravelPlan,
    placeCount: Int,
    actionCount: Int,
    onAddPlace: () -> Unit,
    onAddAction: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox("🧳")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(plan.title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOf(plan.destination, "${placeCount}개 장소", "${actionCount}개 남음").filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) { Text("여행") }
            }
            if (plan.note.isNotBlank()) {
                Text(plan.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAddPlace, modifier = Modifier.weight(1f)) { Text("장소 추가") }
                Button(onClick = onAddAction, modifier = Modifier.weight(1f)) { Text("액션 추가") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TravelPlaceCard(place: TravelPlaceItem, actions: List<TravelActionItem>) {
    ElevatedCard(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox("📍")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(place.name, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${place.travelTitle} · Day ${place.dayIndex} · 반경 ${place.radiusMeters}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) { Text(place.category) }
            }
            if (actions.isNotEmpty()) {
                ChipFlow(actions.map { "${it.kind}: ${it.title}" }.take(6))
            } else {
                Text("아직 연결된 먹을 것/할 일이 없어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TravelActionCard(
    item: TravelActionItem,
    meta: ItemSyncState?,
    onDone: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onCycleAssignee: () -> Unit,
    onToggleShared: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.done, onCheckedChange = onDone)
                Column(Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${item.kind} · ${item.travelTitle} · ${item.placeName} · Day ${item.dayIndex}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.note.isNotBlank()) {
                        Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "삭제")
                }
            }
            AssignmentShareRow(meta = meta, onCycleAssignee = onCycleAssignee, onToggleShared = onToggleShared)
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier, danger: Boolean = false, onClick: () -> Unit) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = if (danger) Color(0xFFF15B5B) else MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.weight(1f))
                Badge(containerColor = if (danger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer) {
                    Text(if (danger) "확인" else "등록됨")
                }
            }
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, fontWeight = FontWeight.Black) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowFilters(values: List<String>, selected: String, onSelected: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text(value) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlaceItemCard(
    place: PlaceItem,
    pendingCount: Int,
    pendingNames: List<String>,
    taskNames: List<String> = emptyList(),
    onTest: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox(categoryEmoji(place.category))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(place.name, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val coordinateText = if (place.latitude != null && place.longitude != null) {
                        "반경 ${place.radiusMeters}m · 하루 1회 알림"
                    } else {
                        "좌표 없음 · 장소는 계속 유지"
                    }
                    Text(
                        "${place.category} · $coordinateText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("${pendingCount}개")
                }
            }

            val chips = pendingNames.map { "살 것: $it" } + taskNames.map { "할 일: $it" }
            if (chips.isNotEmpty()) {
                ChipFlow(values = chips.take(8))
            } else {
                Text(
                    "아직 연결된 물건이나 할 일이 없어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTest, modifier = Modifier.weight(1f)) {
                    Text("근처 도착 테스트")
                }
                if (onDelete != null) {
                    OutlinedButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "장소 삭제")
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactPlaceCard(
    place: PlaceItem,
    pendingNames: List<String>,
    taskNames: List<String> = emptyList(),
    onClick: () -> Unit
) {
    val total = pendingNames.size + taskNames.size
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox(categoryEmoji(place.category))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(place.name, fontWeight = FontWeight.Black)
                    Text(
                        "${place.category} · 반경 ${place.radiusMeters}m · ${total}개 미완료",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            val chips = pendingNames.map { "살 것: $it" } + taskNames.map { "할 일: $it" }
            if (chips.isNotEmpty()) ChipFlow(values = chips.take(5))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(values: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEach { value ->
            AssistChip(
                onClick = {},
                label = { Text(value, fontWeight = FontWeight.Bold) }
            )
        }
    }
}

private fun categoryEmoji(category: String): String = when (category) {
    "다이소" -> "🛍️"
    "마트" -> "🛒"
    "약국" -> "💊"
    "학교" -> "🏫"
    "회사" -> "🏢"
    "집" -> "🏠"
    "편의점" -> "🏪"
    "온라인" -> "📦"
    else -> "📍"
}

@Composable
private fun MapShareGuideCard() {
    ElevatedCard(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox("🗺️")
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("지도 앱에서 공유로 장소 추가", fontWeight = FontWeight.Black)
                    Text(
                        "네이버지도/카카오맵/구글지도에서 장소를 검색한 뒤 공유 → 생활비서를 선택하면 장소명과 좌표를 최대한 자동으로 가져와요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            ChipFlow(listOf("무료 방식", "API 과금 없음", "좌표 자동 추출", "Geocoder 보조"))
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
    meta: ItemSyncState?,
    onDone: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onCycleAssignee: () -> Unit,
    onToggleShared: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.done, onCheckedChange = onDone)
                Column(Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${item.category} · ${item.placeName} · 반경 ${item.radiusMeters}m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "삭제")
                }
            }
            AssignmentShareRow(meta = meta, onCycleAssignee = onCycleAssignee, onToggleShared = onToggleShared)
        }
    }
}

@Composable
private fun PlaceTaskCard(
    item: PlaceTaskItem,
    meta: ItemSyncState?,
    onDone: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onCycleAssignee: () -> Unit,
    onToggleShared: () -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.done, onCheckedChange = onDone)
                EmojiBox("✅")
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val noteText = if (item.note.isNotBlank()) " · ${item.note}" else ""
                    Text("${item.category} · ${item.placeName} · 반경 ${item.radiusMeters}m$noteText", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "삭제")
                }
            }
            AssignmentShareRow(meta = meta, onCycleAssignee = onCycleAssignee, onToggleShared = onToggleShared)
        }
    }
}

@Composable
private fun AssignmentShareRow(
    meta: ItemSyncState?,
    onCycleAssignee: () -> Unit,
    onToggleShared: () -> Unit
) {
    val assignee = meta?.assignee ?: "전체"
    val shared = meta?.shared ?: true
    val syncStatus = meta?.syncStatus ?: "local"
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AssistChip(onClick = onCycleAssignee, label = { Text("👤 $assignee") })
        AssistChip(onClick = onToggleShared, label = { Text(if (shared) "👨‍👩‍👧‍👦 가족 공유" else "🔒 나만 보기") })
        if (syncStatus.contains("pending", ignoreCase = true)) {
            AssistChip(onClick = {}, label = { Text("🔄 동기화 대기") })
        }
        if (meta?.deletedAt != null) {
            AssistChip(onClick = {}, label = { Text("🗑 삭제 대기") })
        }
    }
}

@Composable
private fun LifeItemCard(
    emoji: String,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiBox(emoji)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (badge != null) {
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(badge)
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(
    emoji: String,
    title: String,
    subtitle: String,
    progress: Float,
    bottom: String,
    badge: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBox(emoji)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Black)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) { Text(badge) }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(bottom, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MoreTile(emoji: String, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedCard(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp).heightIn(min = 90.dp)) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Text(title, fontWeight = FontWeight.Black)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmojiBox(emoji: String) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun EmptyText(text: String) {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddBottomSheet(
    type: AddType,
    places: List<PlaceItem>,
    travelPlans: List<TravelPlan>,
    travelPlaces: List<TravelPlaceItem>,
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit,
    onSavePlace: (String, String, Int, Boolean, Double?, Double?) -> Unit,
    onSaveTravelPlan: (String, String, Int, String) -> Unit,
    onSaveTravelPlace: (String, String, String, Int, Int, Boolean, Double?, Double?, String) -> Unit,
    onSaveTravelAction: (String, String, String, String, Int, String) -> Unit,
    onSaveTravelChecklist: (String, String, String, Int, String) -> Unit,
    onSaveTravelReservation: (String, String, String, Int, String, String, String) -> Unit,
    onSaveTravelMemo: (String, String, Int, String, String) -> Unit,
    onSaveTimeReminder: (String, String, String, String, String, String) -> Unit,
    onSaveShopping: (String, String, String, Int, Boolean, Double?, Double?) -> Unit,
    onSavePlaceTask: (String, String, String, Int, Boolean, Double?, Double?, String) -> Unit,
    onSaveRecord: (String, String, Int) -> Unit,
    onSaveCare: (String, String, Int) -> Unit,
    onSaveFamily: (String, String, Int, String) -> Unit,
    onSaveStored: (String, String, String) -> Unit,
    onSaveDelivery: (String, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        var title by rememberSaveable { mutableStateOf("") }
        var emoji by rememberSaveable { mutableStateOf("") }
        var cycle by rememberSaveable { mutableStateOf("30") }
        var category by rememberSaveable { mutableStateOf(places.firstOrNull()?.category ?: "마트") }
        var place by rememberSaveable { mutableStateOf(places.firstOrNull()?.name ?: "") }
        var radius by rememberSaveable { mutableStateOf((places.firstOrNull()?.radiusMeters ?: 300).toString()) }
        var coordinateMode by rememberSaveable { mutableStateOf("current") }
        var lat by rememberSaveable { mutableStateOf("") }
        var lng by rememberSaveable { mutableStateOf("") }
        var note by rememberSaveable { mutableStateOf("") }
        var familyTag by rememberSaveable { mutableStateOf("나") }
        var days by rememberSaveable { mutableStateOf("1") }
        var travelTitle by rememberSaveable { mutableStateOf(travelPlans.firstOrNull()?.title ?: "") }
        var travelKind by rememberSaveable { mutableStateOf("먹을 것") }
        var reminderTime by rememberSaveable { mutableStateOf("21:00") }
        var reservationNo by rememberSaveable { mutableStateOf("") }
        var repeatMode by rememberSaveable { mutableStateOf("한 번") }
        var reminderTarget by rememberSaveable { mutableStateOf("생활") }

        fun selectPlace(savedPlace: PlaceItem) {
            place = savedPlace.name
            category = savedPlace.category
            radius = savedPlace.radiusMeters.toString()
        }

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                when (type) {
                    AddType.Place -> "장소 추가"
                    AddType.Shopping -> "살 것 추가"
                    AddType.PlaceTask -> "장소 할 일 추가"
                    AddType.TravelPlan -> "여행 추가"
                    AddType.TravelPlace -> "여행 장소 추가"
                    AddType.TravelAction -> "여행 먹을 것/할 일 추가"
                    AddType.TravelChecklist -> "여행 체크리스트 추가"
                    AddType.TravelReservation -> "예약/티켓 추가"
                    AddType.TravelMemo -> "오프라인 여행 메모 추가"
                    AddType.TimeReminder -> "시간 알림 추가"
                    AddType.Record -> "기록 항목 추가"
                    AddType.Care -> "집관리 항목 추가"
                    AddType.Family -> "가족 일정 추가"
                    AddType.Stored -> "물건 위치 추가"
                    AddType.Delivery -> "택배 추가"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        when (type) {
                            AddType.Place -> "장소 이름 예: 다이소 고덕점"
                            AddType.Shopping -> "살 것 예: 건전지"
                            AddType.PlaceTask -> "할 일 예: 우산 챙기기"
                            AddType.TravelPlan -> "여행 이름 예: 오사카 3박 4일"
                            AddType.TravelPlace -> "여행 장소 예: 도톤보리"
                            AddType.TravelAction -> "예: 타코야키 먹기, 글리코상 사진"
                            AddType.TravelChecklist -> "예: 여권, 보조배터리, 환전"
                            AddType.TravelReservation -> "예: 호텔 예약, USJ 입장권"
                            AddType.TravelMemo -> "예: 오프라인 메모 제목"
                            AddType.TimeReminder -> "예: 약 복용, 공항 출발 준비"
                            AddType.Stored -> "물건 이름"
                            else -> "이름"
                        }
                    )
                },
                singleLine = true
            )

            when (type) {
                AddType.Place -> {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("장소 종류: 마트/다이소/약국/집/학교/회사/편의점") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = radius,
                        onValueChange = { radius = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("알림 반경 m: 100 / 300 / 500") },
                        singleLine = true
                    )

                    CoordinateModeSelector(
                        coordinateMode = coordinateMode,
                        onMode = { coordinateMode = it },
                        onRequestPermissions = onRequestPermissions,
                        lat = lat,
                        lng = lng,
                        onLat = { lat = it },
                        onLng = { lng = it },
                        currentDescription = "저장 버튼을 누르는 순간의 현재 위치를 이 장소 좌표로 저장합니다."
                    )
                }

                AddType.Shopping, AddType.PlaceTask -> {
                    if (places.isEmpty()) {
                        ElevatedCard(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("먼저 장소를 추가하는 것을 추천해요.", fontWeight = FontWeight.Black)
                                Text(
                                    "마트, 다이소, 약국, 집, 회사, 학교 같은 장소를 먼저 저장하면 다음부터 항목만 연결하면 됩니다.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    } else {
                        Text("저장된 장소 선택", fontWeight = FontWeight.Black)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            places.forEach { savedPlace ->
                                FilterChip(
                                    selected = place == savedPlace.name,
                                    onClick = { selectPlace(savedPlace) },
                                    label = { Text("${savedPlace.category} · ${savedPlace.name}") },
                                    leadingIcon = {
                                        if (place == savedPlace.name) Icon(Icons.Rounded.Check, null)
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = place,
                        onValueChange = { place = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("연결할 장소 이름") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("카테고리") },
                        singleLine = true
                    )

                    val suggestions = if (type == AddType.Shopping) {
                        defaultShoppingTemplates(category)
                    } else {
                        defaultTaskTemplates(category)
                    }
                    if (suggestions.isNotEmpty()) {
                        Text(if (type == AddType.Shopping) "추천 구매템" else "추천 할 일", fontWeight = FontWeight.Black)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            suggestions.forEach { suggestion ->
                                AssistChip(
                                    onClick = { title = suggestion },
                                    label = { Text(suggestion) }
                                )
                            }
                        }
                    }

                    if (type == AddType.PlaceTask) {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("메모 선택: 집 도착하면, 회사 떠나기 전 등") }
                        )
                    }

                    Text(
                        if (type == AddType.Shopping) {
                            "저장된 장소 이름과 같으면 그 장소의 좌표/반경을 자동 사용합니다."
                        } else {
                            "집, 회사, 학교처럼 구매가 아닌 할 일도 장소 알림으로 받을 수 있어요."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AddType.TravelPlan -> {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("여행지 예: 오사카, 제주도, 부산") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("여행 기간 일수") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("메모 예: 가족여행, 3박 4일") }
                    )
                }

                AddType.TravelPlace -> {
                    Text("여행 선택", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        travelPlans.forEach { plan ->
                            FilterChip(
                                selected = travelTitle == plan.title,
                                onClick = { travelTitle = plan.title },
                                label = { Text(plan.title) },
                                leadingIcon = { if (travelTitle == plan.title) Icon(Icons.Rounded.Check, null) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = travelTitle,
                        onValueChange = { travelTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("여행 이름") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Day 번호") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("장소 종류: 관광/식당/공항/호텔/쇼핑") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = radius,
                        onValueChange = { radius = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("알림 반경 m") },
                        singleLine = true
                    )
                    CoordinateModeSelector(
                        coordinateMode = coordinateMode,
                        onMode = { coordinateMode = it },
                        onRequestPermissions = onRequestPermissions,
                        lat = lat,
                        lng = lng,
                        onLat = { lat = it },
                        onLng = { lng = it },
                        currentDescription = "저장 버튼을 누르는 순간의 현재 위치를 이 여행 장소 좌표로 저장합니다."
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("메모 예: 저녁 방문, 사진 포인트") }
                    )
                }

                AddType.TravelAction -> {
                    Text("여행 선택", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        travelPlans.forEach { plan ->
                            FilterChip(
                                selected = travelTitle == plan.title,
                                onClick = { travelTitle = plan.title },
                                label = { Text(plan.title) },
                                leadingIcon = { if (travelTitle == plan.title) Icon(Icons.Rounded.Check, null) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = travelTitle,
                        onValueChange = { travelTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("여행 이름") },
                        singleLine = true
                    )
                    Text("여행 장소 선택", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        travelPlaces.filter { travelTitle.isBlank() || it.travelTitle == travelTitle }.forEach { tPlace ->
                            FilterChip(
                                selected = place == tPlace.name,
                                onClick = {
                                    place = tPlace.name
                                    travelTitle = tPlace.travelTitle
                                    days = tPlace.dayIndex.toString()
                                },
                                label = { Text("Day ${tPlace.dayIndex} · ${tPlace.name}") },
                                leadingIcon = { if (place == tPlace.name) Icon(Icons.Rounded.Check, null) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = place,
                        onValueChange = { place = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("여행 장소 이름") },
                        singleLine = true
                    )
                    Text("종류", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("먹을 것", "할 일", "사진", "살 것", "예약", "주의").forEach { kind ->
                            FilterChip(
                                selected = travelKind == kind,
                                onClick = { travelKind = kind },
                                label = { Text(kind) },
                                leadingIcon = { if (travelKind == kind) Icon(Icons.Rounded.Check, null) }
                            )
                        }
                    }
                    val suggestions = defaultTravelTemplates(travelKind)
                    if (suggestions.isNotEmpty()) {
                        Text("추천 항목", fontWeight = FontWeight.Black)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            suggestions.forEach { suggestion ->
                                AssistChip(onClick = { title = suggestion }, label = { Text(suggestion) })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Day 번호") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("메모 예: 아이들이 좋아함, 예약 시간") }
                    )
                }


                AddType.TravelChecklist -> {
                    Text("여행 선택", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        travelPlans.forEach { plan ->
                            FilterChip(
                                selected = travelTitle == plan.title,
                                onClick = { travelTitle = plan.title },
                                label = { Text(plan.title) },
                                leadingIcon = { if (travelTitle == plan.title) Icon(Icons.Rounded.Check, null) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = travelTitle,
                        onValueChange = { travelTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("여행 이름") },
                        singleLine = true
                    )
                    Text("체크 종류", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("필수", "여권", "환전", "전자기기", "아이", "숙소", "교통").forEach { value ->
                            FilterChip(
                                selected = category == value,
                                onClick = { category = value },
                                label = { Text(value) },
                                leadingIcon = { if (category == value) Icon(Icons.Rounded.Check, null) }
                            )
                        }
                    }
                    Text("추천 준비물", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("여권/신분증", "환전/카드", "보조배터리", "유심/eSIM", "상비약", "숙소 바우처").forEach { value ->
                            AssistChip(onClick = { title = value }, label = { Text(value) })
                        }
                    }
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Day 번호, 출발 전이면 0") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("메모") }
                    )
                }

                AddType.TravelReservation -> {
                    Text("여행 선택", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        travelPlans.forEach { plan ->
                            FilterChip(
                                selected = travelTitle == plan.title,
                                onClick = { travelTitle = plan.title },
                                label = { Text(plan.title) },
                                leadingIcon = { if (travelTitle == plan.title) Icon(Icons.Rounded.Check, null) }
                            )
                        }
                    }
                    OutlinedTextField(value = travelTitle, onValueChange = { travelTitle = it }, modifier = Modifier.fillMaxWidth(), label = { Text("여행 이름") }, singleLine = true)
                    OutlinedTextField(value = place, onValueChange = { place = it }, modifier = Modifier.fillMaxWidth(), label = { Text("장소/예약처 예: 호텔, 항공, USJ") }, singleLine = true)
                    OutlinedTextField(value = days, onValueChange = { days = it.filter(Char::isDigit) }, modifier = Modifier.fillMaxWidth(), label = { Text("Day 번호") }, singleLine = true)
                    OutlinedTextField(value = reminderTime, onValueChange = { reminderTime = it }, modifier = Modifier.fillMaxWidth(), label = { Text("시간 예: 09:30") }, singleLine = true)
                    OutlinedTextField(value = reservationNo, onValueChange = { reservationNo = it }, modifier = Modifier.fillMaxWidth(), label = { Text("예약번호 / 티켓번호") }, singleLine = true)
                    OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("티켓/주의사항 메모") })
                }

                AddType.TravelMemo -> {
                    Text("여행 선택", fontWeight = FontWeight.Black)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        travelPlans.forEach { plan ->
                            FilterChip(
                                selected = travelTitle == plan.title,
                                onClick = { travelTitle = plan.title },
                                label = { Text(plan.title) },
                                leadingIcon = { if (travelTitle == plan.title) Icon(Icons.Rounded.Check, null) }
                            )
                        }
                    }
                    OutlinedTextField(value = travelTitle, onValueChange = { travelTitle = it }, modifier = Modifier.fillMaxWidth(), label = { Text("여행 이름") }, singleLine = true)
                    OutlinedTextField(value = days, onValueChange = { days = it.filter(Char::isDigit) }, modifier = Modifier.fillMaxWidth(), label = { Text("Day 번호, 전체 메모면 0") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, modifier = Modifier.fillMaxWidth(), label = { Text("메모 종류: 팁/주의/오프라인/일정") }, singleLine = true)
                    OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("오프라인에서도 볼 메모 내용") })
                }

                AddType.TimeReminder -> {
                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("알림 시간 예: 21:30") },
                        singleLine = true
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("오늘", "반복", "여행").forEach { value ->
                            FilterChip(
                                selected = category == value,
                                onClick = { category = value },
                                label = { Text(value) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("한 번", "매일", "매주").forEach { value ->
                            FilterChip(
                                selected = repeatMode == value,
                                onClick = { repeatMode = value },
                                label = { Text(value) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = reminderTarget,
                        onValueChange = { reminderTarget = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("대상 예: 생활 / 집 / 오사카 Day 1") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("메모 예: 여권, 충전기 확인") }
                    )
                }

                AddType.Record, AddType.Care -> {
                    OutlinedTextField(value = emoji, onValueChange = { emoji = it }, modifier = Modifier.fillMaxWidth(), label = { Text("아이콘 예: 🦷") }, singleLine = true)
                    OutlinedTextField(value = cycle, onValueChange = { cycle = it.filter(Char::isDigit) }, modifier = Modifier.fillMaxWidth(), label = { Text("권장 주기 일") }, singleLine = true)
                }

                AddType.Family -> {
                    OutlinedTextField(value = familyTag, onValueChange = { familyTag = it }, modifier = Modifier.fillMaxWidth(), label = { Text("가족 태그") }, singleLine = true)
                    OutlinedTextField(value = days, onValueChange = { days = it.filter(Char::isDigit) }, modifier = Modifier.fillMaxWidth(), label = { Text("며칠 뒤") }, singleLine = true)
                    OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("메모") })
                }

                AddType.Stored -> {
                    OutlinedTextField(value = place, onValueChange = { place = it }, modifier = Modifier.fillMaxWidth(), label = { Text("보관 위치") }, singleLine = true)
                    OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("메모") })
                }

                AddType.Delivery -> {
                    OutlinedTextField(value = days, onValueChange = { days = it.filter(Char::isDigit) }, modifier = Modifier.fillMaxWidth(), label = { Text("며칠 뒤 도착 예정") }, singleLine = true)
                }
            }

            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    when (type) {
                        AddType.Place -> onSavePlace(
                            title,
                            category.ifBlank { "기타" },
                            radius.toIntOrNull() ?: 300,
                            coordinateMode == "current",
                            if (coordinateMode == "manual") lat.toDoubleOrNull() else null,
                            if (coordinateMode == "manual") lng.toDoubleOrNull() else null
                        )
                        AddType.Shopping -> onSaveShopping(
                            title,
                            category.ifBlank { "기타" },
                            place.ifBlank { places.firstOrNull()?.name ?: "장소 미지정" },
                            radius.toIntOrNull() ?: 300,
                            false,
                            null,
                            null
                        )
                        AddType.PlaceTask -> onSavePlaceTask(
                            title,
                            category.ifBlank { "기타" },
                            place.ifBlank { places.firstOrNull()?.name ?: "장소 미지정" },
                            radius.toIntOrNull() ?: 300,
                            false,
                            null,
                            null,
                            note
                        )
                        AddType.TravelPlan -> onSaveTravelPlan(
                            title,
                            category,
                            days.toIntOrNull() ?: 3,
                            note
                        )
                        AddType.TravelPlace -> onSaveTravelPlace(
                            travelTitle.ifBlank { travelPlans.firstOrNull()?.title ?: "새 여행" },
                            title,
                            category.ifBlank { "여행" },
                            days.toIntOrNull() ?: 1,
                            radius.toIntOrNull() ?: 300,
                            coordinateMode == "current",
                            if (coordinateMode == "manual") lat.toDoubleOrNull() else null,
                            if (coordinateMode == "manual") lng.toDoubleOrNull() else null,
                            note
                        )
                        AddType.TravelAction -> onSaveTravelAction(
                            travelTitle.ifBlank { travelPlans.firstOrNull()?.title ?: "새 여행" },
                            place.ifBlank { travelPlaces.firstOrNull()?.name ?: "여행 장소" },
                            travelKind,
                            title,
                            days.toIntOrNull() ?: 1,
                            note
                        )
                        AddType.TravelChecklist -> onSaveTravelChecklist(
                            travelTitle.ifBlank { travelPlans.firstOrNull()?.title ?: "새 여행" },
                            title,
                            category.ifBlank { "필수" },
                            days.toIntOrNull() ?: 0,
                            note
                        )
                        AddType.TravelReservation -> onSaveTravelReservation(
                            travelTitle.ifBlank { travelPlans.firstOrNull()?.title ?: "새 여행" },
                            place.ifBlank { travelPlaces.firstOrNull()?.name ?: "여행" },
                            title,
                            days.toIntOrNull() ?: 1,
                            reservationNo,
                            reminderTime,
                            note
                        )
                        AddType.TravelMemo -> onSaveTravelMemo(
                            travelTitle.ifBlank { travelPlans.firstOrNull()?.title ?: "새 여행" },
                            title,
                            days.toIntOrNull() ?: 0,
                            category.ifBlank { "메모" },
                            note
                        )
                        AddType.TimeReminder -> onSaveTimeReminder(
                            title,
                            reminderTime.ifBlank { "09:00" },
                            repeatMode,
                            category.ifBlank { "오늘" },
                            reminderTarget.ifBlank { "생활" },
                            note
                        )
                        AddType.Record -> onSaveRecord(title, emoji, cycle.toIntOrNull() ?: 30)
                        AddType.Care -> onSaveCare(title, emoji, cycle.toIntOrNull() ?: 180)
                        AddType.Family -> onSaveFamily(title, familyTag, days.toIntOrNull() ?: 1, note)
                        AddType.Stored -> onSaveStored(title, place.ifBlank { "위치 미지정" }, note)
                        AddType.Delivery -> onSaveDelivery(title, days.toIntOrNull() ?: 1)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoordinateModeSelector(
    coordinateMode: String,
    onMode: (String) -> Unit,
    onRequestPermissions: () -> Unit,
    lat: String,
    lng: String,
    onLat: (String) -> Unit,
    onLng: (String) -> Unit,
    currentDescription: String
) {
    Text("좌표 입력 방식", fontWeight = FontWeight.Black)

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = coordinateMode == "current",
            onClick = { onMode("current") },
            label = { Text("현재 위치로 입력") },
            leadingIcon = {
                if (coordinateMode == "current") Icon(Icons.Rounded.Check, null)
            }
        )
        FilterChip(
            selected = coordinateMode == "manual",
            onClick = { onMode("manual") },
            label = { Text("좌표 직접 입력") },
            leadingIcon = {
                if (coordinateMode == "manual") Icon(Icons.Rounded.Check, null)
            }
        )
    }

    if (coordinateMode == "current") {
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(currentDescription, fontWeight = FontWeight.Bold)
                Text(
                    "매장 앞에서 저장하면 가장 정확해요. 위치 권한이 필요합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onRequestPermissions) {
                    Icon(Icons.Rounded.MyLocation, null)
                    Spacer(Modifier.width(6.dp))
                    Text("위치 권한 허용")
                }
            }
        }
    } else {
        Text(
            "네이버지도/구글지도에서 복사한 좌표를 입력하세요. 예: 37.557123, 127.154321",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = lat,
                onValueChange = { value -> onLat(value.filter { ch -> ch.isDigit() || ch == '.' || ch == '-' }) },
                modifier = Modifier.weight(1f),
                label = { Text("위도") },
                singleLine = true
            )
            OutlinedTextField(
                value = lng,
                onValueChange = { value -> onLng(value.filter { ch -> ch.isDigit() || ch == '.' || ch == '-' }) },
                modifier = Modifier.weight(1f),
                label = { Text("경도") },
                singleLine = true
            )
        }
    }
}


private fun defaultShoppingTemplates(category: String): List<String> = when (category) {
    "다이소" -> listOf("건전지", "멀티탭", "테이프", "지퍼백", "수납함", "문구류")
    "마트" -> listOf("우유", "계란", "휴지", "생수", "라면", "세제")
    "약국" -> listOf("감기약", "밴드", "소화제", "마스크", "파스")
    "편의점" -> listOf("생수", "우유", "간식", "택배봉투")
    "온라인" -> listOf("세제", "생수", "휴지", "아이 준비물")
    else -> emptyList()
}

private fun defaultTaskTemplates(category: String): List<String> = when (category) {
    "집" -> listOf("분리수거하기", "택배 챙기기", "충전기 챙기기", "냉동실 고기 꺼내기")
    "회사" -> listOf("노트북 충전기 챙기기", "보안카드 챙기기", "퇴근 전 메일 확인", "USB 가져가기")
    "학교" -> listOf("준비물 확인", "알림장 확인", "체육복 챙기기", "아이 픽업")
    "약국" -> listOf("처방전 확인", "약 복용법 물어보기")
    "마트" -> listOf("주차 위치 사진 찍기", "장바구니 챙기기")
    else -> listOf("도착하면 확인하기", "나가기 전에 챙기기")
}

private fun defaultTravelTemplates(kind: String): List<String> = when (kind) {
    "먹을 것" -> listOf("타코야키 먹기", "라멘 먹기", "시장 간식 먹기", "현지 디저트 먹기")
    "할 일" -> listOf("입장권 확인", "운영시간 확인", "화장실 위치 확인", "아이들 쉬는 곳 확인")
    "사진" -> listOf("가족사진 찍기", "랜드마크 앞 사진", "야경 사진", "아이들 독사진")
    "살 것" -> listOf("기념품 사기", "간식 사기", "선물 사기", "마그넷 사기")
    "예약" -> listOf("QR 티켓 확인", "예약 시간 확인", "익스프레스 패스 확인", "체크인 정보 확인")
    "주의" -> listOf("혼잡 시간 피하기", "유모차 동선 확인", "현금 필요 여부 확인", "휴무일 확인")
    else -> listOf("도착하면 확인하기")
}


private fun daysSince(dateMillis: Long): Long {
    val now = startOfToday()
    val then = startOfDay(dateMillis)
    return TimeUnit.MILLISECONDS.toDays(now - then).coerceAtLeast(0)
}

private fun daysLeft(lastDateMillis: Long, cycleDays: Int): Long {
    return cycleDays.toLong() - daysSince(lastDateMillis)
}

private fun dueText(lastDateMillis: Long, cycleDays: Int): String {
    val left = daysLeft(lastDateMillis, cycleDays)
    return if (left < 0) "${kotlin.math.abs(left)}일 지남" else "다음 예정까지 ${left}일"
}

private fun statusText(lastDateMillis: Long, cycleDays: Int): String {
    val left = daysLeft(lastDateMillis, cycleDays)
    return when {
        left < 0 -> "지남"
        left < 14 -> "곧 필요"
        else -> "정상"
    }
}

private fun progress(lastDateMillis: Long, cycleDays: Int): Float {
    if (cycleDays <= 0) return 1f
    return (daysSince(lastDateMillis).toFloat() / cycleDays.toFloat()).coerceIn(0f, 1f)
}

private fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

private fun startOfDay(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun dateText(millis: Long): String {
    return SimpleDateFormat("M월 d일", Locale.KOREA).format(Date(millis))
}
