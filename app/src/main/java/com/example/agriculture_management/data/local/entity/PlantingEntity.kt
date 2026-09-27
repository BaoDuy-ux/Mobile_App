package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plantings",
    foreignKeys = [
        ForeignKey(entity = LandPlotEntity::class, parentColumns = ["id"], childColumns = ["plotId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = CropEntity::class, parentColumns = ["id"], childColumns = ["cropId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("plotId"), Index("cropId"), Index(value = ["plotId", "status"])]
)
data class PlantingEntity(
    @PrimaryKey val id: String,
    val plotId: String,
    val cropId: String,
    val name: String,
    /** Hundredths of m², e.g. 120050 = 1200.50 m². */
    val areaM2x100: Long,
    val plantedOn: String,
    val expectedHarvestOn: String,
    val endedOn: String? = null,
    val status: String = "PLANNED",
    val createdAt: Long,
    val updatedAt: Long
)
