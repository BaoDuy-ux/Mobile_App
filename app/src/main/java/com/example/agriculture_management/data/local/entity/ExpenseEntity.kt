package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(entity = FarmEntity::class, parentColumns = ["id"], childColumns = ["farmId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = PlantingEntity::class, parentColumns = ["id"], childColumns = ["plantingId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("farmId"), Index("plantingId"), Index("paidAt")]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val plantingId: String? = null,
    val category: String,
    val description: String,
    val amountVnd: Long,
    val paidAt: Long,
    val status: String = "POSTED",
    val createdAt: Long
)
