package com.example.agriculture_management.data.repository

import com.example.agriculture_management.data.local.dao.LandPlotDao
import com.example.agriculture_management.data.local.database.FarmDatabase
import com.example.agriculture_management.data.local.entity.FarmEntity
import com.example.agriculture_management.data.local.entity.LandPlotEntity
import com.example.agriculture_management.data.local.entity.UserEntity
import com.example.agriculture_management.domain.model.LandPlot
import com.example.agriculture_management.platform.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import java.util.UUID

class LandPlotRepository(
    private val database: FarmDatabase,
    private val clock: AppClock
) {
    private val dao: LandPlotDao get() = database.plotDao()

    suspend fun ensureDefaultFarm() {
        database.withTransaction {
            if (database.userDao().findById(DEFAULT_USER_ID) == null) {
                database.userDao().insert(UserEntity(DEFAULT_USER_ID, "local@nong-san-xanh.app", "Nguyễn Văn An", createdAt = clock.nowEpochMs(), updatedAt = clock.nowEpochMs()))
            }
            if (database.farmDao().findById(DEFAULT_FARM_ID) == null) {
                val now = clock.nowEpochMs()
                database.farmDao().insert(FarmEntity(DEFAULT_FARM_ID, DEFAULT_USER_ID, "Nông trại của tôi", "", "Asia/Bangkok", now, now))
                database.plotDao().insert(LandPlotEntity("plot-1", DEFAULT_FARM_ID, "Vườn sau nhà", 1200, "Đà Lạt, Lâm Đồng", "Đất tơi xốp, thoát nước tốt. Bón phân hữu cơ định kỳ.", createdAt = now, updatedAt = now))
                database.plotDao().insert(LandPlotEntity("plot-2", DEFAULT_FARM_ID, "Thửa bờ Đông", 850, "Đà Lạt, Lâm Đồng", "Tưới nước vào sáng sớm.", createdAt = now, updatedAt = now))
                database.plotDao().insert(LandPlotEntity("plot-3", DEFAULT_FARM_ID, "Thửa bờ Tây", 600, "Đà Lạt, Lâm Đồng", createdAt = now, updatedAt = now))
            }
        }
    }

    fun observePlots(): Flow<List<LandPlot>> =
        dao.observePlots(DEFAULT_FARM_ID).map { rows -> rows.map { it.toDomain() } }
    fun observePlots(farmId: String): Flow<List<LandPlot>> =
        dao.observePlots(farmId).map { rows ->
            rows.map { it.toDomain() }
        }

    suspend fun createPlot(
        farmId: String,
        name: String,
        areaM2: Long,
        location: String,
        note: String,
        photoUri: String?
    ): Result<LandPlot> {
        if (name.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Tên thửa đất không được rỗng")
            )
        }

        if (areaM2 <= 0) {
            return Result.failure(
                IllegalArgumentException("Diện tích phải lớn hơn 0")
            )
        }

        val now = clock.nowEpochMs()

        val plot = LandPlot(
            id = UUID.randomUUID().toString(),
            farmId = farmId,
            name = name.trim(),
            areaM2 = areaM2,
            location = location.trim(),
            note = note.trim(),
            photoUri = photoUri,
            archived = false,
            createdAt = now,
            updatedAt = now
        )

        dao.insert(plot.toEntity())
        return Result.success(plot)
    }

    suspend fun updatePlot(plot: LandPlot): Result<Unit> {
        if (plot.name.isBlank()) return Result.failure(IllegalArgumentException("Tên thửa đất không được rỗng"))
        if (plot.areaM2 <= 0) return Result.failure(IllegalArgumentException("Diện tích phải lớn hơn 0"))
        database.withTransaction {
            val current = requireNotNull(dao.findById(plot.id)) { "Thửa đất không tồn tại hoặc đã được ẩn." }
            require(current.farmId == plot.farmId) { "Không thể chuyển thửa đất sang nông trại khác." }
            require(Math.multiplyExact(plot.areaM2, 100L) >= database.plantingDao().occupiedArea(plot.id)) {
                "Diện tích không thể nhỏ hơn phần đã dành cho các vụ trồng."
            }
        database.withTransaction {
            val current = requireNotNull(dao.findById(plot.id)) { "Thửa đất không tồn tại hoặc đã được ẩn." }
            require(current.farmId == plot.farmId) { "Không thể chuyển thửa đất sang nông trại khác." }
            require(Math.multiplyExact(plot.areaM2, 100L) >= database.plantingDao().occupiedArea(plot.id)) {
                "Diện tích không thể nhỏ hơn phần đã dành cho các vụ trồng."
            }
            dao.update(plot.toEntity().copy(updatedAt = clock.nowEpochMs()))
        }
        }
        return Result.success(Unit)
    }

    suspend fun archivePlot(plotId: String) {
        dao.archive(plotId, clock.nowEpochMs())
    }

    companion object {
        const val DEFAULT_USER_ID = "local-user"
        const val DEFAULT_FARM_ID = "default-farm"
    }
}

private fun LandPlotEntity.toDomain(): LandPlot = LandPlot(
    id = id,
    farmId = farmId,
    name = name,
    areaM2 = areaM2,
    location = location,
    note = note,
    photoUri = photoUri,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun LandPlot.toEntity(): LandPlotEntity = LandPlotEntity(
    id = id,
    farmId = farmId,
    name = name,
    areaM2 = areaM2,
    location = location,
    note = note,
    photoUri = photoUri,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt
)
