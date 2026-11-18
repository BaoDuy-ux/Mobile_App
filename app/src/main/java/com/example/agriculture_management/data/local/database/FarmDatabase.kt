package com.example.agriculture_management.data.local.database

import com.example.agriculture_management.data.local.entity.*

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.agriculture_management.data.local.dao.FarmDao
import com.example.agriculture_management.data.local.dao.LandPlotDao
import com.example.agriculture_management.data.local.dao.UserDao
import com.example.agriculture_management.data.local.dao.CropDao
import com.example.agriculture_management.data.local.dao.PlantingDao
import com.example.agriculture_management.data.local.ActivityDao
import com.example.agriculture_management.data.local.HarvestAndInventoryDao
import com.example.agriculture_management.data.local.FinanceDao
import com.example.agriculture_management.data.local.ReminderDao
import com.example.agriculture_management.data.local.ReportDao

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
    abstract fun activityDao(): ActivityDao
    abstract fun harvestAndInventoryDao(): HarvestAndInventoryDao
    abstract fun financeDao(): FinanceDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reportDao(): ReportDao
}
