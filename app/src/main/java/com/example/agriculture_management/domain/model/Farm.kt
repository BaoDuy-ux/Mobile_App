package com.example.agriculture_management.domain.model

import com.example.agriculture_management.core.EntityId

/**
 * Template model. Copy thành Cycle, Activity, CashEntry, ...
 * Định dạng tiền / hiển thị tiền thuộc domain — feature không cộng chuỗi.
 */
data class Farm(
    val id: EntityId,
    val name: String,
    val areaNote: String,
)

/**
 * Tiền theo đơn vị nhỏ nhất (đồng). Mọi quy tắc tính/hiển thị đi qua đây hoặc policy.
 */
data class Money(val minorUnits: Long, val currency: String = "VND") {
    init {
        require(currency.isNotBlank())
    }

    fun display(): String {
        val sign = if (minorUnits < 0) "-" else ""
        val abs = kotlin.math.abs(minorUnits)
        return "$sign$abs $currency"
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Khác loại tiền" }
        return Money(minorUnits + other.minorUnits, currency)
    }
}
