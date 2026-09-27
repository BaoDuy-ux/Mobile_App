package com.example.agriculture_management.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.agriculture_management.data.local.entity.FarmEntity

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): FarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(farm: FarmEntity)
}
