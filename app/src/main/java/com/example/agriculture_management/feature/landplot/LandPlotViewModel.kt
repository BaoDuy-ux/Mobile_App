package com.example.agriculture_management.feature.landplot

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriculture_management.data.repository.LandPlotRepository
import com.example.agriculture_management.data.repository.PlantingRepository
import com.example.agriculture_management.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class LandPlotUiState(
    val plots: List<LandPlot> = emptyList(),
    val crops: List<CropOption> = emptyList(),
    val plantings: List<PlantingSummary> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null
)

class LandPlotViewModel(
    private val repository: LandPlotRepository,
    private val plantings: PlantingRepository
) : ViewModel() {
    var uiState by mutableStateOf(LandPlotUiState())
        private set

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                repository.ensureDefaultFarm()
                val farmId = LandPlotRepository.DEFAULT_FARM_ID
                plantings.ensureDefaultCrops(farmId)
                combine(repository.observePlots(), plantings.observeCrops(farmId), plantings.observePlantings(farmId)) { plots, crops, rows ->
                    Triple(plots, crops, rows)
                }.collect { (plots, crops, rows) ->
                    uiState = uiState.copy(plots = plots, crops = crops, plantings = rows, loading = false)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { uiState = uiState.copy(loading = false, error = "Không thể tải dữ liệu. " + e.message.orEmpty()) }
        }
    }

    private fun save(operation: suspend () -> Unit) {
        if (uiState.saving || uiState.loading) return
        uiState = uiState.copy(saving = true, error = null)
        viewModelScope.launch {
            try { operation() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { uiState = uiState.copy(error = e.message ?: "Không thể lưu dữ liệu. Vui lòng thử lại.") }
            finally { uiState = uiState.copy(saving = false) }
        }
    }

    fun createPlot(name: String, areaM2: Long, location: String, note: String, photoUri: String?, onSaved: (LandPlot) -> Unit = {}) = save {
        val plot = repository.createPlot(LandPlotRepository.DEFAULT_FARM_ID, name, areaM2, location, note, photoUri).getOrThrow()
        onSaved(plot)
    }

    fun updatePlot(plot: LandPlot, onSaved: () -> Unit = {}) = save {
        repository.updatePlot(plot).getOrThrow()
        onSaved()
    }

    fun createPlanting(input: CreatePlanting, onSaved: () -> Unit) = save {
        plantings.create(LandPlotRepository.DEFAULT_FARM_ID, input).getOrThrow()
        onSaved()
    }

    fun clearError() { uiState = uiState.copy(error = null) }
}