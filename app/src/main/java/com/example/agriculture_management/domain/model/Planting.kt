package com.example.agriculture_management.domain.model

data class CropOption(val id: String, val farmId: String, val name: String)

/** Room stores dates as ISO yyyy-MM-dd and area in hundredths of a square metre. */
data class PlantingSummary(
    val id: String,
    val plotId: String,
    val cropId: String,
    val cropName: String,
    val areaM2x100: Long,
    val plantedOn: String,
    val expectedHarvestOn: String,
    val status: String
)

data class CreatePlanting(
    val plotId: String,
    val cropId: String,
    val areaM2x100: Long,
    val plantedOn: String,
    val expectedHarvestOn: String
)
