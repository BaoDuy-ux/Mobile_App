package com.example.agriculture_management.domain.policy

import com.example.agriculture_management.core.Outcome
import com.example.agriculture_management.core.Validation
import com.example.agriculture_management.domain.command.CreateFarm
import com.example.agriculture_management.domain.model.Money

/**
 * Template policy. Quy tắc tính tiền / hợp lệ nghiệp vụ nằm ở đây, không nằm feature hay data.
 */
object FarmPolicy {
    fun validateCreate(command: CreateFarm): Outcome<Unit> =
        Validation.check {
            requireNotBlank("name", command.name)
            requireMaxLength("name", command.name, 80)
            requireMaxLength("areaNote", command.areaNote, 200)
        }

    fun canRecord(amount: Money): Boolean = amount.minorUnits != 0L
}
