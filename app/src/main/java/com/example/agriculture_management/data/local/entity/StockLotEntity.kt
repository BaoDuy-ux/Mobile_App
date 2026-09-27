package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_lots",
    foreignKeys = [
        ForeignKey(entity = FarmEntity::class, parentColumns = ["id"], childColumns = ["farmId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = CropEntity::class, parentColumns = ["id"], childColumns = ["cropId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = HarvestEntity::class, parentColumns = ["id"], childColumns = ["harvestId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("farmId"), Index("cropId"), Index(value = ["harvestId"], unique = true), Index(value = ["farmId", "lotCode"], unique = true)]
)
data class StockLotEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val cropId: String,
    val harvestId: String? = null,
    val lotCode: String,
    val receivedAt: Long,
    val expiresOn: String? = null,
    val createdAt: Long
)
