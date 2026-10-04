package com.example.agriculture_management.data.repository

import androidx.room.withTransaction
import com.example.agriculture_management.data.local.database.FarmDatabase
import com.example.agriculture_management.data.local.entity.CropEntity
import com.example.agriculture_management.data.local.entity.PlantingEntity
import com.example.agriculture_management.domain.model.CreatePlanting
import com.example.agriculture_management.platform.clock.AppClock
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.util.UUID

class PlantingRepository(private val database: FarmDatabase, private val clock: AppClock) {
    fun observeCrops(farmId: String) = database.cropDao().observeCrops(farmId)
    fun observePlantings(farmId: String) = database.plantingDao().observeCurrent(farmId)

    suspend fun ensureDefaultCrops(farmId: String) {
        database.withTransaction {
            requireNotNull(database.farmDao().findById(farmId)) { "Không tìm thấy nông trại." }
            val now = clock.nowEpochMs()
            database.cropDao().insertDefaults(listOf("Rau cải", "Rau muống", "Cà chua", "Ớt", "Khác").mapIndexed { index, name ->
                CropEntity("$farmId-crop-$index", farmId, name, createdAt = now, updatedAt = now)
            })
        }
    }

    suspend fun create(farmId: String, input: CreatePlanting): Result<String> = try {
        require(input.areaM2x100 > 0) { "Diện tích phải lớn hơn 0." }
        val planted = parseDate(input.plantedOn)
        val harvest = parseDate(input.expectedHarvestOn)
        require(!harvest.isBefore(planted)) { "Ngày thu hoạch phải từ ngày trồng trở đi." }
        val id = database.withTransaction {
            val plot = requireNotNull(database.plotDao().findById(input.plotId)) { "Thửa đất không tồn tại hoặc đã được ẩn." }
            val crop = requireNotNull(database.cropDao().findById(input.cropId)) { "Cây trồng không tồn tại hoặc đã được ẩn." }
            require(plot.farmId == farmId && crop.farmId == farmId) { "Thửa đất và cây trồng phải thuộc cùng nông trại." }
            val capacity = Math.multiplyExact(plot.areaM2, 100L)
            val occupied = database.plantingDao().occupiedArea(plot.id)
            require(input.areaM2x100 <= capacity - occupied) { "Diện tích vượt phần còn lại của thửa đất." }
            val now = clock.nowEpochMs()
            val plantingId = UUID.randomUUID().toString()
            database.plantingDao().insert(PlantingEntity(
                id = plantingId, plotId = plot.id, cropId = crop.id,
                name = "${crop.name} - $planted", areaM2x100 = input.areaM2x100,
                plantedOn = planted.toString(), expectedHarvestOn = harvest.toString(),
                status = "ACTIVE", createdAt = now, updatedAt = now
            ))
            plantingId
        }
        Result.success(id)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun parseDate(value: String): LocalDate = try {
        LocalDate.parse(value)
    } catch (e: java.time.format.DateTimeParseException) {
        throw IllegalArgumentException("Ngày không hợp lệ. Vui lòng chọn lại ngày.", e)
    }
}
