package com.example.agriculture_management

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.example.agriculture_management.data.local.database.FarmDatabase
import com.example.agriculture_management.data.repository.*
import com.example.agriculture_management.domain.model.LandPlot
import com.example.agriculture_management.feature.landplot.LandPlotViewModel
import com.example.agriculture_management.platform.clock.SystemAppClock
import com.example.agriculture_management.ui.*
import kotlinx.coroutines.runBlocking
import org.junit.*

class PlantingFlowTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: FarmDatabase
    private lateinit var plot: LandPlot
    private val owner = object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() }

    @Before fun setup() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(context, FarmDatabase::class.java).build()
        val repo = LandPlotRepository(database, SystemAppClock)
        repo.ensureDefaultFarm()
        plot = repo.createPlot(LandPlotRepository.DEFAULT_FARM_ID, "Thửa mới từ Room", 100, "", "", null).getOrThrow()
    }

    @After fun cleanup() {
        compose.runOnIdle { owner.viewModelStore.clear() }
        database.close()
    }

    @Test fun newRoomPlotCanReceivePlantingAndDetailUpdatesFromFlow() {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = LandPlotViewModel(
                LandPlotRepository(database, SystemAppClock), PlantingRepository(database, SystemAppClock)
            ) as T
        }
        compose.setContent {
            val vm: LandPlotViewModel = viewModel(viewModelStoreOwner=owner, factory=factory)
            val state = vm.uiState
            var saved by remember { mutableStateOf(false) }
            val land = Land(plot.id, plot.name, 100)
            val demo = remember { FarmState(context, "planting-ui-preview") }
            FarmTheme {
                if (!saved) AddPlantingScreen(listOf(land), state.crops, state.plantings, plot.id, {},
                    loading=state.loading, saving=state.saving) { input -> vm.createPlanting(input) { saved=true } }
                else LandDetailScreen(land, demo, {}, {}, {}, state.plantings.filter { it.plotId == plot.id })
            }
        }
        compose.waitUntil(10000) { compose.onAllNodesWithText("1. Thửa mới từ Room").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Ví dụ: 200 hoặc 200,50").performScrollTo().performTextInput("20,50")
        compose.onNodeWithText("Chọn ngày").performScrollTo().performClick()
        onView(withId(android.R.id.button1)).perform(click())
        compose.onNodeWithText("Lưu").performScrollTo().performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Diện tích 20,5 m²").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Diện tích 20,5 m²").performScrollTo().assertIsDisplayed()
    }
}
