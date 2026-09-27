package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    foreignKeys = [ForeignKey(entity = PlantingEntity::class, parentColumns = ["id"], childColumns = ["plantingId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("plantingId"), Index("performedAt")]
)
data class ActivityEntity(
    @PrimaryKey val id: String,
    val plantingId: String,
    val type: String,
    val title: String,
    val performedAt: Long,
    val note: String = "",
    val createdAt: Long
)
