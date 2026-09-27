package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(entity = FarmEntity::class, parentColumns = ["id"], childColumns = ["farmId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PlantingEntity::class, parentColumns = ["id"], childColumns = ["plantingId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("farmId"), Index("plantingId"), Index(value = ["farmId", "status", "dueAt"])]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val plantingId: String? = null,
    val title: String,
    val dueAt: Long,
    val completedAt: Long? = null,
    val status: String = "PENDING",
    val createdAt: Long,
    val updatedAt: Long
)
