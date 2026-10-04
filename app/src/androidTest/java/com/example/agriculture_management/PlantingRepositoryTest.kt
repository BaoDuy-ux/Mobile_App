package com.example.agriculture_management

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.agriculture_management.data.local.database.FarmDatabase
import com.example.agriculture_management.data.local.entity.FarmEntity
import com.example.agriculture_management.data.repository.*
import com.example.agriculture_management.domain.model.*
import com.example.agriculture_management.platform.clock.SystemAppClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import java.util.UUID

class PlantingRepositoryTest {
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val farmId = LandPlotRepository.DEFAULT_FARM_ID
    private lateinit var db: FarmDatabase
    private lateinit var repository: PlantingRepository
    private lateinit var plots: LandPlotRepository
    private lateinit var plot: LandPlot
    private lateinit var crop: CropOption
    private lateinit var filename: String

    private fun open() {
        db = Room.databaseBuilder(context, FarmDatabase::class.java, filename).build()
        repository = PlantingRepository(db, SystemAppClock)
        plots = LandPlotRepository(db, SystemAppClock)
    }

    @Before fun setup() = runBlocking {
        filename = "planting-test-${UUID.randomUUID()}.db"
        open()
        plots.ensureDefaultFarm()
        repository.ensureDefaultCrops(farmId)
        crop = repository.observeCrops(farmId).first().first()
        plot = plots.createPlot(farmId, "Vườn kiểm thử", 100, "", "", null).getOrThrow()
    }

    @After fun cleanup() { db.close(); context.deleteDatabase(filename) }

    private fun input(area: Long = 2500) = CreatePlanting(plot.id, crop.id, area, "2026-10-04", "2026-11-04")

    @Test fun savesAgainstNewPlotAndSurvivesDatabaseReopen() = runBlocking {
        val id = repository.create(farmId, input(2050)).getOrThrow()
        val row = withTimeout(5000) { repository.observePlantings(farmId).first { it.any { row -> row.id == id } } }.single()
        assertEquals(plot.id, row.plotId)
        assertEquals(crop.name, row.cropName)
        assertEquals(2050L, row.areaM2x100)
        assertEquals("2026-10-04", row.plantedOn)
        db.close()
        open()
        assertEquals(id, repository.observePlantings(farmId).first().single().id)
    }

    @Test fun rejectsBadDatesAndNonPositiveAreaWithoutWriting() = runBlocking {
        listOf(input(0), input(-1), input().copy(plantedOn="2026-02-30"),
            input().copy(expectedHarvestOn=""), input().copy(expectedHarvestOn="2026-10-03")).forEach {
            assertTrue(repository.create(farmId, it).isFailure)
        }
        assertTrue(repository.observePlantings(farmId).first().isEmpty())
    }

    @Test fun repeatedCropAddsIndependentPlantingsAndCountsAllArea() = runBlocking {
        repository.create(farmId, input(6000)).getOrThrow()
        assertTrue(repository.create(farmId, input(4001)).isFailure)
        repository.create(farmId, input(4000)).getOrThrow()
        assertEquals(2, repository.observePlantings(farmId).first().size)
        assertEquals(10000L, db.plantingDao().occupiedArea(plot.id))
    }

    @Test fun concurrentSavesCannotOverbookPlot() = runBlocking {
        val outcomes = coroutineScope {
            listOf(async(Dispatchers.IO) { repository.create(farmId, input(6000)) },
                async(Dispatchers.IO) { repository.create(farmId, input(6000)) }).awaitAll()
        }
        assertEquals(1, outcomes.count { it.isSuccess })
        assertEquals(6000L, db.plantingDao().occupiedArea(plot.id))
    }

    @Test fun rejectsMissingArchivedAndCrossFarmReferences() = runBlocking {
        assertTrue(repository.create(farmId, input().copy(plotId="missing")).isFailure)
        assertTrue(repository.create(farmId, input().copy(cropId="missing")).isFailure)
        db.farmDao().insert(FarmEntity("other-farm", LandPlotRepository.DEFAULT_USER_ID, "Khác", createdAt=0, updatedAt=0))
        repository.ensureDefaultCrops("other-farm")
        val foreignCrop = repository.observeCrops("other-farm").first().first()
        assertTrue(repository.create(farmId, input().copy(cropId=foreignCrop.id)).isFailure)
        assertTrue(repository.create("other-farm", input()).isFailure)
        plots.archivePlot(plot.id)
        assertTrue(repository.create(farmId, input()).isFailure)
        assertEquals(0L, db.plantingDao().occupiedArea(plot.id))
    }

    @Test fun plotCannotShrinkBelowReservedArea() = runBlocking {
        repository.create(farmId, input(6050)).getOrThrow()
        val result = runCatching { plots.updatePlot(plot.copy(areaM2=60)).getOrThrow() }
        assertTrue(result.isFailure)
        assertEquals(100L, db.plotDao().findById(plot.id)!!.areaM2)
        plots.updatePlot(plot.copy(areaM2=61)).getOrThrow()
        assertEquals(61L, db.plotDao().findById(plot.id)!!.areaM2)
    }

    @Test fun cropDefaultsAreIdempotent() = runBlocking {
        repository.ensureDefaultCrops(farmId)
        repository.ensureDefaultCrops(farmId)
        val crops = repository.observeCrops(farmId).first()
        assertEquals(5, crops.size)
        assertEquals(5, crops.map { it.id }.distinct().size)
    }
}
