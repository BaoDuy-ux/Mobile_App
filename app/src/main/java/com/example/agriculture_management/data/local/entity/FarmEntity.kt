package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "farms",
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["ownerId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("ownerId")]
)
data class FarmEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val address: String = "",
    val timezone: String = "Asia/Bangkok",
    val createdAt: Long,
    val updatedAt: Long
)
