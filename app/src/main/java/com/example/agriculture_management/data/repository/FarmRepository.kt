package com.example.agriculture_management.data.repository

import com.example.agriculture_management.core.EntityId
import com.example.agriculture_management.core.Outcome
import com.example.agriculture_management.domain.command.CreateFarm
import com.example.agriculture_management.domain.model.Farm
import com.example.agriculture_management.domain.policy.FarmPolicy
import com.example.agriculture_management.domain.port.FarmPort
import com.example.agriculture_management.data.local.demo.DemoFarmDao
import com.example.agriculture_management.data.local.demo.DemoFarmRecord

/**
 * Template repository: implement domain port, map Record <-> Model.
 * Không chứa Context UI. Không side-effect nền tảng trong lúc ghi DB.
 */
class FarmRepository(
    private val dao: DemoFarmDao,
    private val newId: () -> EntityId = { EntityId.new("farm") },
) : FarmPort {

    override fun create(command: CreateFarm): Outcome<Farm> {
        when (val checked = FarmPolicy.validateCreate(command)) {
            is Outcome.Rejected -> return checked
            is Outcome.Ok -> Unit
        }
        val farm = Farm(id = newId(), name = command.name.trim(), areaNote = command.areaNote.trim())
        dao.insert(farm.toRecord())
        return Outcome.Ok(farm)
    }

    override fun get(id: EntityId): Farm? = dao.findById(id.value)?.toModel()

    override fun list(): List<Farm> = dao.findAll().map { it.toModel() }

    private fun Farm.toRecord() = DemoFarmRecord(id = id.value, name = name, areaNote = areaNote)

    private fun DemoFarmRecord.toModel() = Farm(id = EntityId(id), name = name, areaNote = areaNote)
}
