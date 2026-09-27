package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(entity = SaleEntity::class, parentColumns = ["id"], childColumns = ["saleId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = StockLotEntity::class, parentColumns = ["id"], childColumns = ["lotId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("saleId"), Index("lotId")]
)
data class SaleItemEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val lotId: String,
    val quantityGrams: Long,
    val unitPriceVndPerKg: Long,
    val lineTotalVnd: Long
)
