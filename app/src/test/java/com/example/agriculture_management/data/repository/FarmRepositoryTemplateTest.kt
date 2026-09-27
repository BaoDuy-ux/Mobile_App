package com.example.agriculture_management.data.repository

import com.example.agriculture_management.core.Outcome
import com.example.agriculture_management.data.local.demo.InMemoryFarmDao
import com.example.agriculture_management.domain.command.CreateFarm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FarmRepositoryTemplateTest {
    @Test
    fun create_rejectsBlankName() {
        val port = FarmRepository(InMemoryFarmDao())
        val result = port.create(CreateFarm(name = "  "))
        assertTrue(result is Outcome.Rejected)
    }

    @Test
    fun create_thenList() {
        val port = FarmRepository(InMemoryFarmDao())
        val created = port.create(CreateFarm(name = "Ruộng A", areaNote = "2 sào"))
        assertTrue(created is Outcome.Ok)
        assertEquals(1, port.list().size)
        assertEquals("Ruộng A", port.list().single().name)
    }
}
