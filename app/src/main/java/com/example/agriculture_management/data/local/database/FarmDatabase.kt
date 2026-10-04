package com.example.agriculture_management.data.local.database

import com.example.agriculture_management.data.local.entity.*

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.agriculture_management.data.local.dao.FarmDao
import com.example.agriculture_management.data.local.dao.LandPlotDao
import com.example.agriculture_management.data.local.dao.UserDao
import com.example.agriculture_management.data.local.dao.CropDao
import com.example.agriculture_management.data.local.dao.PlantingDao

@Database(
    entities = [
        UserEntity::class,
        FarmEntity::class,
        LandPlotEntity::class,
        CropEntity::class,
        PlantingEntity::class,
        ActivityEntity::class,
        HarvestEntity::class,
        StockLotEntity::class,
        StockMovementEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        ExpenseEntity::class,
        ReminderEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun farmDao(): FarmDao
    abstract fun plotDao(): LandPlotDao
    abstract fun cropDao(): CropDao
    abstract fun plantingDao(): PlantingDao
}
