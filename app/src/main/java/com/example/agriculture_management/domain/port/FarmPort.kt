package com.example.agriculture_management.domain.port

import com.example.agriculture_management.core.EntityId
import com.example.agriculture_management.core.Outcome
import com.example.agriculture_management.domain.command.CreateFarm
import com.example.agriculture_management.domain.model.Farm

/**
 * Template port. Feature và domain gọi port; data/recovery implement.
 * Không để Room/DAO lọt vào chữ ký.
 */
interface FarmPort {
    fun create(command: CreateFarm): Outcome<Farm>
    fun get(id: EntityId): Farm?
    fun list(): List<Farm>
}
