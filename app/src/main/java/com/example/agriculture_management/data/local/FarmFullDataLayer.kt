package com.example.agriculture_management.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.example.agriculture_management.core.EntityId
import com.example.agriculture_management.core.Outcome
import com.example.agriculture_management.data.local.dao.CropDao
import com.example.agriculture_management.data.local.dao.FarmDao
import com.example.agriculture_management.data.local.dao.LandPlotDao
import com.example.agriculture_management.data.local.dao.PlantingDao
import com.example.agriculture_management.data.local.dao.UserDao
import com.example.agriculture_management.data.local.entity.ActivityEntity
import com.example.agriculture_management.data.local.entity.CropEntity
import com.example.agriculture_management.data.local.entity.ExpenseEntity
import com.example.agriculture_management.data.local.entity.FarmEntity
import com.example.agriculture_management.data.local.entity.HarvestEntity
import com.example.agriculture_management.data.local.entity.LandPlotEntity
import com.example.agriculture_management.data.local.entity.PlantingEntity
import com.example.agriculture_management.data.local.entity.ReminderEntity
import com.example.agriculture_management.data.local.entity.SaleEntity
import com.example.agriculture_management.data.local.entity.SaleItemEntity
import com.example.agriculture_management.data.local.entity.StockLotEntity
import com.example.agriculture_management.data.local.entity.StockMovementEntity
import com.example.agriculture_management.data.local.entity.UserEntity
import com.example.agriculture_management.data.repository.LandPlotRepository
import com.example.agriculture_management.platform.clock.AppClock
import com.example.agriculture_management.platform.clock.SystemAppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActivityRecordRow(
    val id: String,
    val plantingId: String,
    val plantingName: String,
    val plotName: String,
    val cropName: String,
    val type: String,
    val title: String,
    val performedAt: Long,
    val note: String
)

data class CropInventoryBalanceRow(
    val cropId: String,
    val cropName: String,
    val totalGrams: Long
) {
    val totalKg: Int get() = (totalGrams / 1000L).toInt()
}

data class AvailableLotRow(
    val lotId: String,
    val lotCode: String,
    val cropId: String,
    val cropName: String,
    val receivedAt: Long,
    val availableGrams: Long
)

data class ReminderRow(
    val id: String,
    val farmId: String,
    val plantingId: String?,
    val plotName: String?,
    val title: String,
    val dueAt: Long,
    val completedAt: Long?,
    val status: String
)

data class CategoryExpenseTotal(
    val category: String,
    val totalVnd: Long
)

data class CropHarvestTotal(
    val cropName: String,
    val totalGrams: Long
)

data class MonthlyFinanceRow(
    val yearMonth: String,
    val totalVnd: Long
)

@Dao
interface ActivityDao {
    @Query(
        """
        SELECT a.id AS id,
               a.plantingId AS plantingId,
               p.name AS plantingName,
               pl.name AS plotName,
               c.name AS cropName,
               a.type AS type,
               a.title AS title,
               a.performedAt AS performedAt,
               a.note AS note
        FROM activities a
        INNER JOIN plantings p ON p.id = a.plantingId
        INNER JOIN plots pl ON pl.id = p.plotId
        INNER JOIN crops c ON c.id = p.cropId
        WHERE pl.farmId = :farmId
        ORDER BY a.performedAt DESC
        """
    )
    fun observeActivities(farmId: String): Flow<List<ActivityRecordRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteById(activityId: String)

    @Query("SELECT * FROM activities ORDER BY performedAt DESC")
    suspend fun listAll(): List<ActivityEntity>
}

@Dao
interface HarvestAndInventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: HarvestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockLot(lot: StockLotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity)

    @Query(
        """
        SELECT c.id AS cropId,
               c.name AS cropName,
               COALESCE(SUM(sm.quantityDeltaGrams), 0) AS totalGrams
        FROM crops c
        LEFT JOIN stock_lots sl ON sl.cropId = c.id AND sl.farmId = :farmId
        LEFT JOIN stock_movements sm ON sm.lotId = sl.id
        WHERE c.farmId = :farmId AND c.archived = 0
        GROUP BY c.id, c.name
        ORDER BY c.name ASC
        """
    )
    fun observeInventoryByCrop(farmId: String): Flow<List<CropInventoryBalanceRow>>

    @Query(
        """
        SELECT sl.id AS lotId,
               sl.lotCode AS lotCode,
               sl.cropId AS cropId,
               c.name AS cropName,
               sl.receivedAt AS receivedAt,
               COALESCE(SUM(sm.quantityDeltaGrams), 0) AS availableGrams
        FROM stock_lots sl
        INNER JOIN crops c ON c.id = sl.cropId
        LEFT JOIN stock_movements sm ON sm.lotId = sl.id
        WHERE sl.farmId = :farmId AND c.name = :cropName
        GROUP BY sl.id, sl.lotCode, sl.cropId, c.name, sl.receivedAt
        HAVING COALESCE(SUM(sm.quantityDeltaGrams), 0) > 0
        ORDER BY sl.receivedAt ASC
        """
    )
    suspend fun findAvailableLotsFifo(farmId: String, cropName: String): List<AvailableLotRow>

    @Query("SELECT * FROM harvests ORDER BY harvestedAt DESC")
    suspend fun listAllHarvests(): List<HarvestEntity>

    @Query("SELECT * FROM stock_lots ORDER BY receivedAt DESC")
    suspend fun listAllStockLots(): List<StockLotEntity>

    @Query("SELECT * FROM stock_movements ORDER BY occurredAt DESC")
    suspend fun listAllStockMovements(): List<StockMovementEntity>
}

@Dao
interface FinanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(item: SaleItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM sales WHERE farmId = :farmId ORDER BY soldAt DESC")
    fun observeSales(farmId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM expenses WHERE farmId = :farmId ORDER BY paidAt DESC")
    fun observeExpenses(farmId: String): Flow<List<ExpenseEntity>>

    @Query("UPDATE sales SET status = 'VOIDED', voidedAt = :voidedAt WHERE id = :saleId")
    suspend fun voidSale(saleId: String, voidedAt: Long)

    @Query("SELECT * FROM sales ORDER BY soldAt DESC")
    suspend fun listAllSales(): List<SaleEntity>

    @Query("SELECT * FROM sale_items")
    suspend fun listAllSaleItems(): List<SaleItemEntity>

    @Query("SELECT * FROM expenses ORDER BY paidAt DESC")
    suspend fun listAllExpenses(): List<ExpenseEntity>
}

@Dao
interface ReminderDao {
    @Query(
        """
        SELECT r.id AS id,
               r.farmId AS farmId,
               r.plantingId AS plantingId,
               pl.name AS plotName,
               r.title AS title,
               r.dueAt AS dueAt,
               r.completedAt AS completedAt,
               r.status AS status
        FROM reminders r
        LEFT JOIN plantings p ON p.id = r.plantingId
        LEFT JOIN plots pl ON pl.id = p.plotId
        WHERE r.farmId = :farmId
        ORDER BY CASE WHEN r.status = 'COMPLETED' THEN 1 ELSE 0 END ASC, r.dueAt ASC
        """
    )
    fun observeReminders(farmId: String): Flow<List<ReminderRow>>

    @Query(
        """
        SELECT * FROM reminders
        WHERE status != 'COMPLETED' AND dueAt <= :deadlineMillis
        ORDER BY dueAt ASC
        """
    )
    suspend fun findDueReminders(deadlineMillis: Long): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Query(
        """
        UPDATE reminders
        SET status = CASE WHEN :completed THEN 'COMPLETED' ELSE 'PENDING' END,
            completedAt = :completedAt,
            updatedAt = :updatedAt
        WHERE id = :reminderId
        """
    )
    suspend fun setCompleted(
        reminderId: String,
        completed: Boolean,
        completedAt: Long?,
        updatedAt: Long
    )

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteById(reminderId: String)

    @Query("SELECT * FROM reminders ORDER BY dueAt ASC")
    suspend fun listAll(): List<ReminderEntity>
}

@Dao
interface ReportDao {
    @Query("SELECT COALESCE(SUM(totalVnd), 0) FROM sales WHERE farmId = :farmId AND status = 'COMPLETED'")
    fun observeTotalRevenue(farmId: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountVnd), 0) FROM expenses WHERE farmId = :farmId AND status = 'PAID'")
    fun observeTotalExpense(farmId: String): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(h.quantityGrams), 0)
        FROM harvests h
        INNER JOIN plantings p ON p.id = h.plantingId
        INNER JOIN plots pl ON pl.id = p.plotId
        WHERE pl.farmId = :farmId
        """
    )
    fun observeTotalHarvestGrams(farmId: String): Flow<Long>

    @Query(
        """
        SELECT category, COALESCE(SUM(amountVnd), 0) AS totalVnd
        FROM expenses
        WHERE farmId = :farmId AND status = 'PAID'
        GROUP BY category
        ORDER BY totalVnd DESC
        """
    )
    fun observeExpenseByCategory(farmId: String): Flow<List<CategoryExpenseTotal>>

    @Query(
        """
        SELECT c.name AS cropName, COALESCE(SUM(h.quantityGrams), 0) AS totalGrams
        FROM harvests h
        INNER JOIN plantings p ON p.id = h.plantingId
        INNER JOIN plots pl ON pl.id = p.plotId
        INNER JOIN crops c ON c.id = p.cropId
        WHERE pl.farmId = :farmId
        GROUP BY c.id, c.name
        ORDER BY totalGrams DESC
        """
    )
    fun observeHarvestByCrop(farmId: String): Flow<List<CropHarvestTotal>>

    @Query(
        """
        SELECT strftime('%Y-%m', soldAt / 1000, 'unixepoch', 'localtime') AS yearMonth,
               COALESCE(SUM(totalVnd), 0) AS totalVnd
        FROM sales
        WHERE farmId = :farmId AND status = 'COMPLETED'
        GROUP BY yearMonth
        ORDER BY yearMonth DESC
        LIMIT 6
        """
    )
    fun observeMonthlyRevenue(farmId: String): Flow<List<MonthlyFinanceRow>>

    @Query(
        """
        SELECT strftime('%Y-%m', paidAt / 1000, 'unixepoch', 'localtime') AS yearMonth,
               COALESCE(SUM(amountVnd), 0) AS totalVnd
        FROM expenses
        WHERE farmId = :farmId AND status = 'PAID'
        GROUP BY yearMonth
        ORDER BY yearMonth DESC
        LIMIT 6
        """
    )
    fun observeMonthlyExpense(farmId: String): Flow<List<MonthlyFinanceRow>>
}

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
    version = 3,
    exportSchema = true
)
abstract class UpgradedFarmDatabase : RoomDatabase() {
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

data class FarmReportSummary(
    val totalRevenueVnd: Long = 0L,
    val totalExpenseVnd: Long = 0L,
    val totalHarvestGrams: Long = 0L,
    val expenseByCategory: List<CategoryExpenseTotal> = emptyList(),
    val harvestByCrop: List<CropHarvestTotal> = emptyList(),
    val monthlyRevenue: List<MonthlyFinanceRow> = emptyList(),
    val monthlyExpense: List<MonthlyFinanceRow> = emptyList()
) {
    val netProfitVnd: Long get() = totalRevenueVnd - totalExpenseVnd
    val totalHarvestKg: Double get() = totalHarvestGrams / 1000.0
}

class FarmOperationsRepository(
    private val database: UpgradedFarmDatabase,
    private val clock: AppClock = SystemAppClock
) {
    private val activityDao = database.activityDao()
    private val inventoryDao = database.harvestAndInventoryDao()
    private val financeDao = database.financeDao()
    private val reminderDao = database.reminderDao()
    private val reportDao = database.reportDao()
    private val userDao = database.userDao()

    fun observeActivities(farmId: String = LandPlotRepository.DEFAULT_FARM_ID): Flow<List<ActivityRecordRow>> =
        activityDao.observeActivities(farmId)

    fun observeInventory(farmId: String = LandPlotRepository.DEFAULT_FARM_ID): Flow<List<CropInventoryBalanceRow>> =
        inventoryDao.observeInventoryByCrop(farmId)

    fun observeSales(farmId: String = LandPlotRepository.DEFAULT_FARM_ID): Flow<List<SaleEntity>> =
        financeDao.observeSales(farmId)

    fun observeExpenses(farmId: String = LandPlotRepository.DEFAULT_FARM_ID): Flow<List<ExpenseEntity>> =
        financeDao.observeExpenses(farmId)

    fun observeReminders(farmId: String = LandPlotRepository.DEFAULT_FARM_ID): Flow<List<ReminderRow>> =
        reminderDao.observeReminders(farmId)

    fun observeReportSummary(farmId: String = LandPlotRepository.DEFAULT_FARM_ID): Flow<FarmReportSummary> {
        val kpiFlow = combine(
            reportDao.observeTotalRevenue(farmId),
            reportDao.observeTotalExpense(farmId),
            reportDao.observeTotalHarvestGrams(farmId)
        ) { rev, exp, harv -> Triple(rev, exp, harv) }

        val breakdownFlow = combine(
            reportDao.observeExpenseByCategory(farmId),
            reportDao.observeHarvestByCrop(farmId),
            reportDao.observeMonthlyRevenue(farmId),
            reportDao.observeMonthlyExpense(farmId)
        ) { expCat, harvCrop, mRev, mExp ->
            arrayOf(expCat, harvCrop, mRev, mExp)
        }

        return combine(kpiFlow, breakdownFlow) { (rev, exp, harv), breakdowns ->
            @Suppress("UNCHECKED_CAST")
            FarmReportSummary(
                totalRevenueVnd = rev,
                totalExpenseVnd = exp,
                totalHarvestGrams = harv,
                expenseByCategory = breakdowns[0] as List<CategoryExpenseTotal>,
                harvestByCrop = breakdowns[1] as List<CropHarvestTotal>,
                monthlyRevenue = breakdowns[2] as List<MonthlyFinanceRow>,
                monthlyExpense = breakdowns[3] as List<MonthlyFinanceRow>
            )
        }
    }

    suspend fun recordCareActivity(
        plantingId: String,
        type: String,
        title: String,
        note: String,
        performedAt: Long = clock.nowMillis()
    ): Outcome<ActivityEntity> {
        if (title.isBlank()) {
            return Outcome.Rejected(listOf("Tiêu đề công việc không được để trống"))
        }
        val now = clock.nowMillis()
        val entity = ActivityEntity(
            id = EntityId.new("act").value,
            plantingId = plantingId,
            type = type.ifBlank { "CARE" },
            title = title.trim(),
            performedAt = performedAt,
            note = note.trim(),
            createdAt = now
        )
        activityDao.insert(entity)
        return Outcome.Ok(entity)
    }

    suspend fun recordHarvest(
        farmId: String = LandPlotRepository.DEFAULT_FARM_ID,
        plantingId: String,
        cropId: String,
        quantityKg: Int,
        note: String = ""
    ): Outcome<HarvestEntity> {
        if (quantityKg <= 0) {
            return Outcome.Rejected(listOf("Sản lượng thu hoạch phải lớn hơn 0 kg"))
        }
        val now = clock.nowMillis()
        val quantityGrams = quantityKg.toLong() * 1000L
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(now))
        val harvest = HarvestEntity(
            id = EntityId.new("harv").value,
            plantingId = plantingId,
            quantityGrams = quantityGrams,
            harvestedAt = now,
            note = note.trim(),
            createdAt = now
        )
        val lot = StockLotEntity(
            id = EntityId.new("lot").value,
            farmId = farmId,
            cropId = cropId,
            harvestId = harvest.id,
            lotCode = "LOT-$dateCode-${harvest.id.takeLast(4).uppercase()}",
            receivedAt = now,
            expiresOn = null,
            createdAt = now
        )
        val movement = StockMovementEntity(
            id = EntityId.new("mov").value,
            lotId = lot.id,
            saleItemId = null,
            type = "HARVEST_IN",
            quantityDeltaGrams = quantityGrams,
            occurredAt = now,
            reason = note.ifBlank { "Nhập kho từ thu hoạch" }
        )
        val activity = ActivityEntity(
            id = EntityId.new("act").value,
            plantingId = plantingId,
            type = "HARVEST",
            title = "Thu hoạch ${quantityKg}kg",
            performedAt = now,
            note = note.trim(),
            createdAt = now
        )

        database.withTransaction {
            inventoryDao.insertHarvest(harvest)
            inventoryDao.insertStockLot(lot)
            inventoryDao.insertStockMovement(movement)
            activityDao.insert(activity)
        }
        return Outcome.Ok(harvest)
    }

    suspend fun recordSaleFifo(
        farmId: String = LandPlotRepository.DEFAULT_FARM_ID,
        cropName: String,
        quantityKg: Int,
        totalAmountVnd: Long,
        customerName: String = "Khách lẻ"
    ): Outcome<SaleEntity> {
        if (quantityKg <= 0) {
            return Outcome.Rejected(listOf("Số lượng xuất bán phải lớn hơn 0 kg"))
        }
        if (totalAmountVnd < 0L) {
            return Outcome.Rejected(listOf("Số tiền bán không hợp lệ"))
        }
        val requiredGrams = quantityKg.toLong() * 1000L
        val now = clock.nowMillis()

        return database.withTransaction {
            val availableLots = inventoryDao.findAvailableLotsFifo(farmId, cropName)
            val totalAvailableGrams = availableLots.sumOf { it.availableGrams }
            if (totalAvailableGrams < requiredGrams) {
                val availableKg = totalAvailableGrams / 1000L
                return@withTransaction Outcome.Rejected(
                    listOf("Tồn kho $cropName chỉ còn ${availableKg}kg, không đủ xuất bán ${quantityKg}kg")
                )
            }

            val sale = SaleEntity(
                id = EntityId.new("sale").value,
                farmId = farmId,
                customerName = customerName.trim().ifBlank { "Khách lẻ" },
                soldAt = now,
                status = "COMPLETED",
                totalVnd = totalAmountVnd,
                voidedAt = null,
                createdAt = now
            )
            financeDao.insertSale(sale)

            val unitPriceVndPerKg = if (quantityKg > 0) totalAmountVnd / quantityKg else 0L
            var remainingGramsToDeduct = requiredGrams

            for (lot in availableLots) {
                if (remainingGramsToDeduct <= 0L) break
                val takeGrams = minOf(lot.availableGrams, remainingGramsToDeduct)
                val lineTotal = (takeGrams * totalAmountVnd) / requiredGrams

                val saleItem = SaleItemEntity(
                    id = EntityId.new("sitem").value,
                    saleId = sale.id,
                    lotId = lot.lotId,
                    quantityGrams = takeGrams,
                    unitPriceVndPerKg = unitPriceVndPerKg,
                    lineTotalVnd = lineTotal
                )
                financeDao.insertSaleItem(saleItem)

                val movement = StockMovementEntity(
                    id = EntityId.new("mov").value,
                    lotId = lot.lotId,
                    saleItemId = saleItem.id,
                    type = "SALE_OUT",
                    quantityDeltaGrams = -takeGrams,
                    occurredAt = now,
                    reason = "Xuất bán $cropName cho ${sale.customerName}"
                )
                inventoryDao.insertStockMovement(movement)
                remainingGramsToDeduct -= takeGrams
            }

            Outcome.Ok(sale)
        }
    }

    suspend fun recordExpense(
        farmId: String = LandPlotRepository.DEFAULT_FARM_ID,
        plantingId: String? = null,
        category: String,
        description: String,
        amountVnd: Long
    ): Outcome<ExpenseEntity> {
        if (description.isBlank()) {
            return Outcome.Rejected(listOf("Nội dung chi phí không được để trống"))
        }
        if (amountVnd <= 0L) {
            return Outcome.Rejected(listOf("Số tiền chi phí phải lớn hơn 0 đ"))
        }
        val now = clock.nowMillis()
        val expense = ExpenseEntity(
            id = EntityId.new("exp").value,
            farmId = farmId,
            plantingId = plantingId,
            category = category.ifBlank { "Vật tư" },
            description = description.trim(),
            amountVnd = amountVnd,
            paidAt = now,
            status = "PAID",
            createdAt = now
        )
        financeDao.insertExpense(expense)
        return Outcome.Ok(expense)
    }

    suspend fun createReminder(
        farmId: String = LandPlotRepository.DEFAULT_FARM_ID,
        plantingId: String? = null,
        title: String,
        dueAtMillis: Long
    ): Outcome<ReminderEntity> {
        if (title.isBlank()) {
            return Outcome.Rejected(listOf("Nội dung nhắc việc không được để trống"))
        }
        val now = clock.nowMillis()
        val entity = ReminderEntity(
            id = EntityId.new("rem").value,
            farmId = farmId,
            plantingId = plantingId,
            title = title.trim(),
            dueAt = dueAtMillis,
            completedAt = null,
            status = "PENDING",
            createdAt = now,
            updatedAt = now
        )
        reminderDao.insert(entity)
        return Outcome.Ok(entity)
    }

    suspend fun toggleReminderCompleted(reminderId: String, completed: Boolean) {
        val now = clock.nowMillis()
        reminderDao.setCompleted(
            reminderId = reminderId,
            completed = completed,
            completedAt = if (completed) now else null,
            updatedAt = now
        )
    }

    suspend fun updateUserProfile(
        userId: String = LandPlotRepository.DEFAULT_USER_ID,
        displayName: String,
        email: String
    ): Outcome<UserEntity> {
        if (displayName.isBlank()) {
            return Outcome.Rejected(listOf("Họ và tên không được để trống"))
        }
        val now = clock.nowMillis()
        val existing = userDao.findById(userId)
        val updated = UserEntity(
            id = userId,
            email = email.trim().ifBlank { existing?.email ?: "local@nong-san-xanh.app" },
            displayName = displayName.trim(),
            status = existing?.status ?: "ACTIVE",
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        userDao.insert(updated)
        return Outcome.Ok(updated)
    }

    suspend fun exportFullBackupJson(): String {
        val root = JSONObject()
        root.put("version", 3)
        root.put("exportedAt", clock.nowMillis())

        val activitiesArray = JSONArray()
        for (a in activityDao.listAll()) {
            activitiesArray.put(
                JSONObject()
                    .put("id", a.id)
                    .put("plantingId", a.plantingId)
                    .put("type", a.type)
                    .put("title", a.title)
                    .put("performedAt", a.performedAt)
                    .put("note", a.note)
                    .put("createdAt", a.createdAt)
            )
        }
        root.put("activities", activitiesArray)

        val expensesArray = JSONArray()
        for (e in financeDao.listAllExpenses()) {
            expensesArray.put(
                JSONObject()
                    .put("id", e.id)
                    .put("farmId", e.farmId)
                    .put("plantingId", e.plantingId)
                    .put("category", e.category)
                    .put("description", e.description)
                    .put("amountVnd", e.amountVnd)
                    .put("paidAt", e.paidAt)
                    .put("status", e.status)
                    .put("createdAt", e.createdAt)
            )
        }
        root.put("expenses", expensesArray)

        val salesArray = JSONArray()
        for (s in financeDao.listAllSales()) {
            salesArray.put(
                JSONObject()
                    .put("id", s.id)
                    .put("farmId", s.farmId)
                    .put("customerName", s.customerName)
                    .put("soldAt", s.soldAt)
                    .put("status", s.status)
                    .put("totalVnd", s.totalVnd)
                    .put("voidedAt", s.voidedAt)
                    .put("createdAt", s.createdAt)
            )
        }
        root.put("sales", salesArray)

        val remindersArray = JSONArray()
        for (r in reminderDao.listAll()) {
            remindersArray.put(
                JSONObject()
                    .put("id", r.id)
                    .put("farmId", r.farmId)
                    .put("plantingId", r.plantingId)
                    .put("title", r.title)
                    .put("dueAt", r.dueAt)
                    .put("completedAt", r.completedAt)
                    .put("status", r.status)
                    .put("createdAt", r.createdAt)
                    .put("updatedAt", r.updatedAt)
            )
        }
        root.put("reminders", remindersArray)

        return root.toString(2)
    }

    suspend fun restoreFromBackupJson(rawJson: String): Outcome<Unit> {
        return try {
            val root = JSONObject(rawJson)
            database.withTransaction {
                root.optJSONArray("activities")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        activityDao.insert(
                            ActivityEntity(
                                id = o.getString("id"),
                                plantingId = o.getString("plantingId"),
                                type = o.optString("type", "CARE"),
                                title = o.getString("title"),
                                performedAt = o.getLong("performedAt"),
                                note = o.optString("note", ""),
                                createdAt = o.optLong("createdAt", clock.nowMillis())
                            )
                        )
                    }
                }
                root.optJSONArray("expenses")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        financeDao.insertExpense(
                            ExpenseEntity(
                                id = o.getString("id"),
                                farmId = o.optString("farmId", LandPlotRepository.DEFAULT_FARM_ID),
                                plantingId = o.optString("plantingId").takeIf { it.isNotEmpty() },
                                category = o.optString("category", "Vật tư"),
                                description = o.getString("description"),
                                amountVnd = o.getLong("amountVnd"),
                                paidAt = o.getLong("paidAt"),
                                status = o.optString("status", "PAID"),
                                createdAt = o.optLong("createdAt", clock.nowMillis())
                            )
                        )
                    }
                }
                root.optJSONArray("reminders")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        reminderDao.insert(
                            ReminderEntity(
                                id = o.getString("id"),
                                farmId = o.optString("farmId", LandPlotRepository.DEFAULT_FARM_ID),
                                plantingId = o.optString("plantingId").takeIf { it.isNotEmpty() },
                                title = o.getString("title"),
                                dueAt = o.getLong("dueAt"),
                                completedAt = if (o.isNull("completedAt")) null else o.optLong("completedAt"),
                                status = o.optString("status", "PENDING"),
                                createdAt = o.optLong("createdAt", clock.nowMillis()),
                                updatedAt = o.optLong("updatedAt", clock.nowMillis())
                            )
                        )
                    }
                }
            }
            Outcome.Ok(Unit)
        } catch (e: Exception) {
            Outcome.Rejected(listOf("Tệp sao lưu không hợp lệ: ${e.message ?: "Lỗi định dạng JSON"}"))
        }
    }
}
