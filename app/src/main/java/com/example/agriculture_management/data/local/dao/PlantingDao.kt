package com.example.agriculture_management.data.local.dao

import androidx.room.*
import com.example.agriculture_management.data.local.entity.PlantingEntity
import com.example.agriculture_management.domain.model.PlantingSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantingDao {
    @Query("""
        SELECT v.id, v.plotId, v.cropId, c.name AS cropName, v.areaM2x100,
               v.plantedOn, v.expectedHarvestOn, v.status
        FROM plantings v JOIN crops c ON c.id = v.cropId
        JOIN plots p ON p.id = v.plotId
        WHERE p.farmId = :farmId AND p.archived = 0
          AND v.status IN ('ACTIVE', 'PLANNED') AND v.endedOn IS NULL
        ORDER BY v.plantedOn DESC, v.createdAt DESC, v.id
    """)
    fun observeCurrent(farmId: String): Flow<List<PlantingSummary>>

    // Both current and planned plantings reserve space until explicitly ended.
    @Query("""SELECT COALESCE(SUM(areaM2x100), 0) FROM plantings
        WHERE plotId = :plotId AND status IN ('ACTIVE', 'PLANNED') AND endedOn IS NULL""")
    suspend fun occupiedArea(plotId: String): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(planting: PlantingEntity)
}
