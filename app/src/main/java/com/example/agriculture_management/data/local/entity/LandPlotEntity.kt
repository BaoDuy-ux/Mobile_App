package com.example.agriculture_management.data.local.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plots",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("farmId"), Index("name")]
)
data class LandPlotEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val name: String,
    val areaM2: Long,
    val location: String = "",
    val note: String = "",
    val photoUri: String? = null,
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
