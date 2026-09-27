package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "harvests",
    foreignKeys = [ForeignKey(entity = PlantingEntity::class, parentColumns = ["id"], childColumns = ["plantingId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("plantingId"), Index("harvestedAt")]
)
data class HarvestEntity(
    @PrimaryKey val id: String,
    val plantingId: String,
    /** Whole grams; 50.125 kg is stored as 50125. */
    val quantityGrams: Long,
    val harvestedAt: Long,
    val note: String = "",
    val createdAt: Long
)
