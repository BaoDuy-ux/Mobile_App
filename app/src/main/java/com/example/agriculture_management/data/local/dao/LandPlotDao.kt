package com.example.agriculture_management.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.agriculture_management.data.local.entity.LandPlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LandPlotDao {

    @Query("""
        SELECT * FROM plots
        WHERE farmId = :farmId
        AND archived = 0
        ORDER BY createdAt DESC
    """)
    fun observePlots(farmId: String): Flow<List<LandPlotEntity>>

    @Query("""
        SELECT * FROM plots
        WHERE id = :plotId
        AND archived = 0
        LIMIT 1
    """)
    suspend fun findById(plotId: String): LandPlotEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(plot: LandPlotEntity)

    @Update
    suspend fun update(plot: LandPlotEntity)

    @Query("""
        UPDATE plots
        SET archived = 1, updatedAt = :updatedAt
        WHERE id = :plotId
    """)
    suspend fun archive(plotId: String, updatedAt: Long)
}