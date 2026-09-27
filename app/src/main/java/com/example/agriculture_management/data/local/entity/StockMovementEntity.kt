package com.example.agriculture_management.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(entity = StockLotEntity::class, parentColumns = ["id"], childColumns = ["lotId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SaleItemEntity::class, parentColumns = ["id"], childColumns = ["saleItemId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("lotId"), Index("occurredAt"), Index(value = ["saleItemId", "type"], unique = true)]
)
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val lotId: String,
    val saleItemId: String? = null,
    val type: String,
    /** Positive for IN, negative for OUT. */
    val quantityDeltaGrams: Long,
    val occurredAt: Long,
    val reason: String? = null
)
