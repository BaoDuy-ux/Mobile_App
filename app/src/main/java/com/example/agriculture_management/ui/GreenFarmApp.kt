package com.example.agriculture_management.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.agriculture_management.data.repository.LandPlotRepository
import com.example.agriculture_management.data.local.database.FarmDatabaseProvider
import com.example.agriculture_management.domain.model.LandPlot
import com.example.agriculture_management.feature.landplot.LandPlotViewModel
import com.example.agriculture_management.platform.clock.SystemAppClock
import com.example.agriculture_management.data.repository.PlantingRepository
import com.example.agriculture_management.domain.model.PlantingSummary
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agriculture_management.data.local.FarmOperationsRepository
import com.example.agriculture_management.feature.operations.BindFarmStateToRoom
import com.example.agriculture_management.feature.operations.FarmOperationsViewModel
import com.example.agriculture_management.platform.reminder.ReminderNotificationWorker

enum class Screen { Splash, Onboarding, Login, Home, Lands, LandDetail, AddLand, AddPlanting, QuickAction,
    Logs, Report, Inventory, Reminders, Settings, Backup, Profile, More, Empty, Record }

@Composable
fun GreenFarmApp(store: FarmState? = null) {
    val context = LocalContext.current
    val state = remember(store) { store ?: FarmState(context.applicationContext) }
    val database = remember { FarmDatabaseProvider.get(context.applicationContext) }
    val plotViewModel: LandPlotViewModel = viewModel(factory = remember(database) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                LandPlotViewModel(LandPlotRepository(database, SystemAppClock), PlantingRepository(database, SystemAppClock)) as T
        }
    })
    val plotState = plotViewModel.uiState
    val roomLands = plotState.plots.map { plot -> plot.toLand(plotState.plantings.filter { it.plotId == plot.id }) }
    val operationsRepository = remember(database) { FarmOperationsRepository(database) }
    val operationsViewModel: FarmOperationsViewModel = viewModel(factory = remember(operationsRepository) { object : ViewModelProvider.Factory { @Suppress("UNCHECKED_CAST") override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = FarmOperationsViewModel(operationsRepository) as T } })
    val opsState = operationsViewModel.uiState
    BindFarmStateToRoom(state = state, operationsViewModel = operationsViewModel)
    LaunchedEffect(opsState.error, opsState.successMessage) { (opsState.error ?: opsState.successMessage)?.let { message(it); operationsViewModel.clearMessages() } }
    LaunchedEffect(Unit) { ReminderNotificationWorker.schedulePeriodicChecks(context.applicationContext) }
    var route by rememberSaveable { mutableStateOf(Screen.Splash.name) }
    var history by rememberSaveable { mutableStateOf(listOf<String>()) }
    var selectedLand by rememberSaveable { mutableStateOf("plot-1") }
    var editing by rememberSaveable { mutableStateOf(false) }
    var recordKind by rememberSaveable { mutableStateOf(0) }
    fun go(screen: Screen) { history = history + route; route = screen.name }
    fun back() { route = history.lastOrNull() ?: Screen.Home.name; history = history.dropLast(1) }
    fun home() { history = emptyList(); route = Screen.Home.name }
    fun message(text: String) { Toast.makeText(context,text,Toast.LENGTH_SHORT).show() }
    LaunchedEffect(plotState.error) {
        plotState.error?.let { message(it); plotViewModel.clearError() }
    }
    val screen = Screen.valueOf(route)
    val land = roomLands.find { it.id == selectedLand }
    val mainTabs = listOf(Screen.Home,Screen.Lands,Screen.Logs,Screen.Report)
    BackHandler(history.isNotEmpty()) { back() }
    FarmTheme {
        Scaffold(containerColor = Cream, contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if(screen in mainTabs) {
                    NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White, tonalElevation = 0.dp) {
                        val titles = listOf("Trang chủ","Thửa đất","Nhật ký","Báo cáo")
                        val icons = listOf(Icons.Outlined.Home,Icons.Outlined.Landscape,Icons.Outlined.EditNote,Icons.Outlined.BarChart)
                        mainTabs.forEachIndexed { index, target ->
                            NavigationBarItem(selected = screen == target, onClick = { history=emptyList();route=target.name },
                                icon = { Icon(icons[index],titles[index]) }, label = { Text(titles[index]) },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = Pale,selectedIconColor = Forest,selectedTextColor = Forest))
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).imePadding()) {
                key(route) {
                    when(screen) {
                        Screen.Splash -> SplashScreen { go(Screen.Onboarding) }
                        Screen.Onboarding -> OnboardingScreen { go(Screen.Login) }
                        Screen.Login -> LoginScreen { home() }
                        Screen.Home -> HomeScreen(state,::go, { selectedLand=it;go(Screen.LandDetail) }, roomLands)
                        Screen.Lands -> LandListScreen(state,::back,{ editing=false;go(Screen.AddLand) }, { selectedLand=it;go(Screen.LandDetail) }, roomLands)
                        Screen.LandDetail -> if(land != null) LandDetailScreen(land,state,::back,
                            { editing=true;go(Screen.AddLand) }, { go(Screen.AddPlanting) },
                            plotState.plantings.filter { it.plotId == land.id }) else EmptyScreen(::back) { editing=false;go(Screen.AddLand) }
                        Screen.AddLand -> AddLandScreen(if(editing) land else null,::back) { updated ->
                            val existing = plotState.plots.firstOrNull { it.id == updated.id }
                            if (existing == null) {
                                plotViewModel.createPlot(
                                    name = updated.name,
                                    areaM2 = updated.area.toLong(),
                                    location = updated.location,
                                    note = updated.note,
                                    photoUri = updated.photo.ifBlank { null }
                                ) { created ->
                                    created?.let { selectedLand = it.id }
                                    editing = false
                                    back()
                                    message("Đã lưu thửa đất")
                                }
                            } else {
                                plotViewModel.updatePlot(existing.copy(
                                    name = updated.name,
                                    areaM2 = updated.area.toLong(),
                                    location = updated.location,
                                    note = updated.note,
                                    photoUri = updated.photo.ifBlank { null }
                                )) {
                                    selectedLand = updated.id
                                    editing = false
                                    back()
                                    message("Đã cập nhật thửa đất")
                                }
                            }
                        }
                        Screen.AddPlanting -> AddPlantingScreen(
                            lands = roomLands, crops = plotState.crops, plantings = plotState.plantings,
                            selectedId = selectedLand, back = ::back, loading = plotState.loading,
                            saving = plotState.saving, addLand = { editing=false;go(Screen.AddLand) }
                        ) { input -> plotViewModel.createPlanting(input) {
                            selectedLand = input.plotId
                            back()
                            message("Đã lưu vụ trồng")
                        } }
                        Screen.QuickAction -> QuickActionScreen(::back,{ target -> if(target==Screen.AddLand) editing=false;go(target) }) { recordKind=it;go(Screen.Record) }
                        Screen.Logs -> LogsScreen(state,::back) { go(Screen.QuickAction) }
                        Screen.Report -> ReportScreen(::back)
                        Screen.Inventory -> InventoryScreen(state,::back)
                        Screen.Reminders -> RemindersScreen(state,::back)
                        Screen.Settings -> SettingsScreen(state,::back,::go,::message)
                        Screen.Backup -> BackupScreen(state,::back,::message)
                        Screen.Profile -> ProfileScreen(state,::back) { history=emptyList();route=Screen.Login.name }
                        Screen.More -> MoreScreen(state,::back,::go,::message)
                        Screen.Empty -> EmptyScreen(::back) { editing=false;go(Screen.AddLand) }
                        Screen.Record -> RecordScreen(recordKind,state,::back) { state.save();back();message("Đã lưu hoạt động") }
                    }
                }
            }
        }
    }
}

private fun LandPlot.toLand(plantings: List<PlantingSummary>): Land = Land(
    id = id,
    name = name,
    area = areaM2.toInt(),
    location = location,
    note = note,
    photo = photoUri.orEmpty(),
    crop = plantings.map { it.cropName }.distinct().joinToString(", "),
    cropAreas = plantings.groupBy { it.cropName }.mapValues { (_, rows) ->
        ((rows.sumOf { it.areaM2x100 } + 99) / 100).toInt()
    }
)
