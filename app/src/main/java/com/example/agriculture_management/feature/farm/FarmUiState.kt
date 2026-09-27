package com.example.agriculture_management.feature.farm

import com.example.agriculture_management.domain.model.Farm

data class FarmUiState(
    val farms: List<Farm> = emptyList(),
    val message: String? = null,
)
