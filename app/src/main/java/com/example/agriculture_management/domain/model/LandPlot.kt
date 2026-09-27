package com.example.agriculture_management.domain.model

data class LandPlot(
    val id: String,
    val farmId: String,
    val name: String,
    val areaM2: Long,
    val location: String,
    val note: String,
    val photoUri: String?,
    val archived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
