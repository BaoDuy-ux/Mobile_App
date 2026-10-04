package com.example.agriculture_management.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agriculture_management.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val plantingDateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
fun plantingDate(iso: String): String = runCatching { LocalDate.parse(iso).format(plantingDateFormat) }.getOrDefault(iso)
fun plantingArea(hundredths: Long): String = BigDecimal.valueOf(hundredths, 2).stripTrailingZeros().toPlainString().replace('.', ',')

@Composable
fun AddPlantingScreen(
    lands: List<Land>, crops: List<CropOption>, plantings: List<PlantingSummary>,
    selectedId: String, back: () -> Unit,
    loading: Boolean = false, saving: Boolean = false, addLand: () -> Unit = {},
    save: (CreatePlanting) -> Unit
) {
    var landId by rememberSaveable { mutableStateOf(selectedId) }
    var cropId by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf("") }
    var planted by rememberSaveable { mutableStateOf(today()) }
    var harvest by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(lands.map { it.id }) {
        if (lands.none { it.id == landId }) landId = lands.firstOrNull()?.id.orEmpty()
    }
    LaunchedEffect(crops.map { it.id }) {
        if (crops.none { it.id == cropId }) cropId = crops.firstOrNull()?.id.orEmpty()
    }
    Page("Thêm vụ trồng", { if (!saving) back() }) {
        if (loading) {
            CircularProgressIndicator()
            Text("Đang tải thửa đất và cây trồng…")
        } else if (lands.isEmpty()) {
            Text("Hãy thêm thửa đất trước khi tạo vụ trồng.")
            PrimaryButton("Thêm thửa đất", onClick = addLand)
        } else {
            val land = lands.find { it.id == landId }
            val crop = crops.find { it.id == cropId }
            val labels = lands.mapIndexed { index, item -> "${index + 1}. ${item.name}" }
            val index = lands.indexOfFirst { it.id == landId }
            ChooseField("Chọn thửa đất", labels, labels.getOrNull(index).orEmpty()) { choice ->
                landId = lands[labels.indexOf(choice)].id
            }
            ChooseField("Chọn cây trồng", crops.map { it.name }, crop?.name.orEmpty()) { choice ->
                cropId = crops.first { it.name == choice }.id
            }
            val used = plantings.filter { it.plotId == landId }.sumOf { it.areaM2x100 }
            val remaining = (land?.area?.toLong() ?: 0L) * 100L - used
            Text("Diện tích còn lại: ${plantingArea(remaining.coerceAtLeast(0))} m²", color = Forest)
            Spacer(Modifier.height(16.dp))
            Field("Diện tích (m²)", area, { area=it }, "Ví dụ: 200 hoặc 200,50", keyboardType=KeyboardType.Decimal)
            DateField("Ngày trồng", planted) { planted=it }
            DateField("Dự kiến thu hoạch", harvest) { harvest=it }
            Text("Mỗi lần lưu tạo một vụ riêng. Các vụ chưa kết thúc cùng sử dụng diện tích thửa đất.", color=Muted)
            Spacer(Modifier.height(12.dp))
            if (error.isNotBlank()) Text(error, color=Coral)
            Button(onClick = {
                val amount = runCatching { BigDecimal(area.trim().replace(',', '.')).movePointRight(2).longValueExact() }.getOrNull()
                val start = runCatching { LocalDate.parse(planted, plantingDateFormat) }.getOrNull()
                val end = runCatching { LocalDate.parse(harvest, plantingDateFormat) }.getOrNull()
                error = when {
                    land == null || crop == null -> "Vui lòng chọn thửa đất và cây trồng."
                    amount == null || amount <= 0 -> "Nhập diện tích dương, tối đa 2 chữ số thập phân."
                    amount > remaining -> "Diện tích vượt phần còn lại của thửa đất."
                    start == null || end == null -> "Vui lòng chọn ngày trồng và ngày thu hoạch hợp lệ."
                    end.isBefore(start) -> "Ngày thu hoạch phải từ ngày trồng trở đi."
                    else -> ""
                }
                if (error.isEmpty()) save(CreatePlanting(landId, cropId, amount!!, start.toString(), end.toString()))
            }, enabled = !saving && crop != null && land != null, modifier=Modifier.fillMaxWidth()) {
                Text(if (saving) "Đang lưu…" else "Lưu")
            }
        }
    }
}
