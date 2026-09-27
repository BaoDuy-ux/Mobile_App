package com.example.agriculture_management.feature.landplot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriculture_management.data.repository.LandPlotRepository
import com.example.agriculture_management.domain.model.LandPlot
import kotlinx.coroutines.launch

data class LandPlotUiState(
    val plots: List<LandPlot> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class LandPlotViewModel(private val repository: LandPlotRepository) : ViewModel() {
    var uiState by mutableStateOf(LandPlotUiState())
        private set

    init {
        viewModelScope.launch {
            repository.ensureDefaultFarm()
            repository.observePlots().collect { plots ->
                uiState = LandPlotUiState(plots = plots, loading = false)
            }
        }
    }

    fun savePlot(plot: LandPlot) {
        updatePlot(plot)
    }

    fun createPlot(
        name: String,
        areaM2: Long,
        location: String,
        note: String,
        photoUri: String?,
        onSaved: (LandPlot?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createPlot(
                LandPlotRepository.DEFAULT_FARM_ID, name, areaM2, location, note, photoUri
            )
            result.onSuccess { onSaved(it) }
                .onFailure { uiState = uiState.copy(error = it.message) }
        }
    }

    fun updatePlot(plot: LandPlot, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePlot(plot)
                .onSuccess { onSaved() }
                .onFailure { uiState = uiState.copy(error = it.message) }
        }
    }

    fun clearError() { uiState = uiState.copy(error = null) }
}
