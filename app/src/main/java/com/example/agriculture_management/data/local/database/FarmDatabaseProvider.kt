package com.example.agriculture_management.data.local.database

import android.content.Context
import androidx.room.Room

object FarmDatabaseProvider {
    @Volatile
    private var instance: FarmDatabase? = null

    fun get(context: Context): FarmDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                FarmDatabase::class.java,
                "nong_san_xanh.db"
            )
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                .also { instance = it }
        }
}
