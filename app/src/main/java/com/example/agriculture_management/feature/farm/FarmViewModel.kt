package com.example.agriculture_management.feature.farm

import com.example.agriculture_management.core.Outcome
import com.example.agriculture_management.domain.command.CreateFarm
import com.example.agriculture_management.domain.port.FarmPort

/**
 * Template ViewModel/controller feature.
 * Chỉ phụ thuộc FarmPort + model. Không import data.dao / Room.
 * Hiển thị tiền: gọi Money.display() từ domain, không `"$a $b"`.
 */
class FarmViewModel(private val farms: FarmPort) {
    var state: FarmUiState = FarmUiState()
        private set

    fun refresh() {
        state = state.copy(farms = farms.list(), message = null)
    }

    fun create(name: String, areaNote: String) {
        state = when (val result = farms.create(CreateFarm(name, areaNote))) {
            is Outcome.Ok -> FarmUiState(farms = farms.list(), message = "Đã tạo ${result.value.name}")
            is Outcome.Rejected -> state.copy(message = result.reasons.joinToString("; "))
        }
    }
}
