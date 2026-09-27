package com.example.agriculture_management.core

/**
 * Validation kỹ thuật (rỗng, độ dài, khoảng). Quy tắc nghiệp vụ để ở domain/policy.
 */
class Validation private constructor() {
    private val reasons = mutableListOf<String>()

    fun requireNotBlank(field: String, value: String?) {
        if (value.isNullOrBlank()) reasons += "$field bắt buộc"
    }

    fun requireMaxLength(field: String, value: String?, max: Int) {
        if (value != null && value.length > max) reasons += "$field tối đa $max ký tự"
    }

    fun outcome(): Outcome<Unit> =
        if (reasons.isEmpty()) Outcome.Ok(Unit) else Outcome.Rejected(reasons)

    companion object {
        fun check(block: Validation.() -> Unit): Outcome<Unit> =
            Validation().apply(block).outcome()
    }
}
