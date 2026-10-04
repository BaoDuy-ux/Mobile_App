package com.example.agriculture_management.data.local.dao

import androidx.room.*
import com.example.agriculture_management.data.local.entity.CropEntity
import com.example.agriculture_management.domain.model.CropOption
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT id, farmId, name FROM crops WHERE farmId = :farmId AND archived = 0 ORDER BY name")
    fun observeCrops(farmId: String): Flow<List<CropOption>>

    @Query("SELECT * FROM crops WHERE id = :id AND archived = 0 LIMIT 1")
    suspend fun findById(id: String): CropEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(crops: List<CropEntity>)
}
