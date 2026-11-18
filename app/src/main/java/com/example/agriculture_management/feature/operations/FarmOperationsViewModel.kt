package com.example.agriculture_management.feature.operations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agriculture_management.core.Outcome
import com.example.agriculture_management.data.local.ActivityRecordRow
import com.example.agriculture_management.data.local.CropInventoryBalanceRow
import com.example.agriculture_management.data.local.FarmOperationsRepository
import com.example.agriculture_management.data.local.FarmReportSummary
import com.example.agriculture_management.data.local.ReminderRow
import com.example.agriculture_management.data.local.entity.ExpenseEntity
import com.example.agriculture_management.data.local.entity.SaleEntity
import com.example.agriculture_management.data.repository.LandPlotRepository
import com.example.agriculture_management.ui.FarmLog
import com.example.agriculture_management.ui.FarmState
import com.example.agriculture_management.ui.Product
import com.example.agriculture_management.ui.Reminder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FarmOperationsUiState(
    val activities: List<ActivityRecordRow> = emptyList(),
    val inventory: List<CropInventoryBalanceRow> = emptyList(),
    val sales: List<SaleEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val reminders: List<ReminderRow> = emptyList(),
    val report: FarmReportSummary = FarmReportSummary(),
    val lastBackupJson: String? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class FarmOperationsViewModel(
    private val repository: FarmOperationsRepository,
    private val farmId: String = LandPlotRepository.DEFAULT_FARM_ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmOperationsUiState())
    val uiState: StateFlow<FarmOperationsUiState> = _uiState.asStateFlow()

    init {
        observeAllStreams()
    }

    private fun observeAllStreams() {
        viewModelScope.launch {
            try {
                val primaryFlow = combine(
                    repository.observeActivities(farmId),
                    repository.observeInventory(farmId),
                    repository.observeSales(farmId)
                ) { acts, inv, sales -> Triple(acts, inv, sales) }

                val secondaryFlow = combine(
                    repository.observeExpenses(farmId),
                    repository.observeReminders(farmId),
                    repository.observeReportSummary(farmId)
                ) { exps, rems, rep -> Triple(exps, rems, rep) }

                combine(primaryFlow, secondaryFlow) { (acts, inv, sales), (exps, rems, rep) ->
                    _uiState.value.copy(
                        activities = acts,
                        inventory = inv,
                        sales = sales,
                        expenses = exps,
                        reminders = rems,
                        report = rep,
                        loading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = e.message ?: "Không thể tải dữ liệu vận hành nông trại")
                }
            }
        }
    }

    fun recordCareActivity(
        plantingId: String,
        type: String,
        title: String,
        note: String,
        onSuccess: () -> Unit = {}
    ) = executeMutation(onSuccess) {
        repository.recordCareActivity(
            plantingId = plantingId,
            type = type,
            title = title,
            note = note
        )
    }

    fun recordHarvest(
        plantingId: String,
        cropId: String,
        quantityKg: Int,
        note: String,
        onSuccess: () -> Unit = {}
    ) = executeMutation(onSuccess) {
        repository.recordHarvest(
            farmId = farmId,
            plantingId = plantingId,
            cropId = cropId,
            quantityKg = quantityKg,
            note = note
        )
    }

    fun recordSale(
        cropName: String,
        quantityKg: Int,
        totalAmountVnd: Long,
        customerName: String = "Khách lẻ",
        onSuccess: () -> Unit = {}
    ) = executeMutation(onSuccess) {
        repository.recordSaleFifo(
            farmId = farmId,
            cropName = cropName,
            quantityKg = quantityKg,
            totalAmountVnd = totalAmountVnd,
            customerName = customerName
        )
    }

    fun recordExpense(
        plantingId: String? = null,
        category: String,
        description: String,
        amountVnd: Long,
        onSuccess: () -> Unit = {}
    ) = executeMutation(onSuccess) {
        repository.recordExpense(
            farmId = farmId,
            plantingId = plantingId,
            category = category,
            description = description,
            amountVnd = amountVnd
        )
    }

    fun addReminder(
        plantingId: String? = null,
        title: String,
        dueAtMillis: Long,
        onSuccess: () -> Unit = {}
    ) = executeMutation(onSuccess) {
        repository.createReminder(
            farmId = farmId,
            plantingId = plantingId,
            title = title,
            dueAtMillis = dueAtMillis
        )
    }

    fun toggleReminder(reminderId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleReminderCompleted(reminderId, completed)
        }
    }

    fun updateProfile(displayName: String, email: String, onSuccess: () -> Unit = {}) =
        executeMutation(onSuccess) {
            repository.updateUserProfile(displayName = displayName, email = email)
        }

    fun exportBackupJson(onExported: (String) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            try {
                val json = repository.exportFullBackupJson()
                _uiState.update {
                    it.copy(
                        saving = false,
                        lastBackupJson = json,
                        successMessage = "Đã sao lưu toàn bộ cơ sở dữ liệu thành công"
                    )
                }
                onExported(json)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(saving = false, error = e.message ?: "Lỗi khi xuất dữ liệu sao lưu")
                }
            }
        }
    }

    fun restoreBackupJson(rawJson: String, onSuccess: () -> Unit = {}) =
        executeMutation(onSuccess) {
            repository.restoreFromBackupJson(rawJson)
        }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    private fun <T> executeMutation(
        onSuccess: () -> Unit,
        block: suspend () -> Outcome<T>
    ) {
        if (_uiState.value.saving) return
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null, successMessage = null) }
            try {
                when (val outcome = block()) {
                    is Outcome.Ok -> {
                        _uiState.update { it.copy(saving = false) }
                        onSuccess()
                    }
                    is Outcome.Rejected -> {
                        _uiState.update {
                            it.copy(
                                saving = false,
                                error = outcome.reasons.firstOrNull() ?: "Dữ liệu không hợp lệ"
                            )
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(saving = false, error = e.message ?: "Không thể lưu dữ liệu")
                }
            }
        }
    }

    class Factory(
        private val repository: FarmOperationsRepository,
        private val farmId: String = LandPlotRepository.DEFAULT_FARM_ID
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FarmOperationsViewModel(repository, farmId) as T
        }
    }
}

@Composable
fun BindFarmStateToRoom(
    state: FarmState,
    operationsViewModel: FarmOperationsViewModel
) {
    val opsState by operationsViewModel.uiState.collectAsState()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))

    LaunchedEffect(opsState.inventory) {
        if (opsState.inventory.isNotEmpty()) {
            state.products.clear()
            state.products.addAll(
                opsState.inventory.map { item ->
                    Product(name = item.cropName, quantity = item.totalKg)
                }
            )
        }
    }

    LaunchedEffect(opsState.activities, opsState.sales, opsState.expenses) {
        val mergedLogs = mutableListOf<Pair<Long, FarmLog>>()

        opsState.activities.forEach { act ->
            val kind = when (act.type.uppercase()) {
                "WATER", "IRRIGATION" -> 0
                "FERTILIZE", "CARE" -> 1
                "HARVEST" -> 2
                else -> 0
            }
            mergedLogs += act.performedAt to FarmLog(
                title = act.title,
                detail = "${act.plotName} • ${act.cropName}" +
                    if (act.note.isNotBlank()) " — ${act.note}" else "",
                date = dateFormatter.format(Date(act.performedAt)),
                kind = kind,
                amount = 0
            )
        }

        opsState.sales.filter { it.status == "COMPLETED" }.forEach { sale ->
            mergedLogs += sale.soldAt to FarmLog(
                title = "Bán nông sản (${sale.customerName})",
                detail = "Đơn hàng #${sale.id.takeLast(5).uppercase()}",
                date = dateFormatter.format(Date(sale.soldAt)),
                kind = 3,
                amount = sale.totalVnd.toInt()
            )
        }

        opsState.expenses.filter { it.status == "PAID" }.forEach { exp ->
            mergedLogs += exp.paidAt to FarmLog(
                title = exp.description,
                detail = "Danh mục: ${exp.category}",
                date = dateFormatter.format(Date(exp.paidAt)),
                kind = 4,
                amount = exp.amountVnd.toInt()
            )
        }

        if (mergedLogs.isNotEmpty()) {
            val sorted = mergedLogs.sortedByDescending { it.first }.map { it.second }
            state.logs.clear()
            state.logs.addAll(sorted)
        }
    }

    LaunchedEffect(opsState.reminders) {
        if (opsState.reminders.isNotEmpty()) {
            state.reminders.clear()
            state.reminders.addAll(
                opsState.reminders.map { rem ->
                    Reminder(
                        title = rem.title,
                        land = rem.plotName ?: "Toàn nông trại",
                        date = dateFormatter.format(Date(rem.dueAt)),
                        done = rem.status == "COMPLETED"
                    )
                }
            )
        }
    }
}
