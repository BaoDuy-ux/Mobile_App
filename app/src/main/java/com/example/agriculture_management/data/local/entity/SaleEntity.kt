package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [ForeignKey(entity = FarmEntity::class, parentColumns = ["id"], childColumns = ["farmId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("farmId"), Index("soldAt")]
)
data class SaleEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val customerName: String? = null,
    val soldAt: Long,
    val status: String = "POSTED",
    val totalVnd: Long,
    val voidedAt: Long? = null,
    val createdAt: Long
)
