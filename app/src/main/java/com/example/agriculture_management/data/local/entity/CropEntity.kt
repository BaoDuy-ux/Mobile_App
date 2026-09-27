package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crops",
    foreignKeys = [ForeignKey(entity = FarmEntity::class, parentColumns = ["id"], childColumns = ["farmId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("farmId"), Index(value = ["farmId", "name"], unique = true)]
)
data class CropEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val name: String,
    val variety: String? = null,
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
