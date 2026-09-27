package com.example.agriculture_management.data.local.demo

import com.example.agriculture_management.data.local.demo.DemoFarmRecord

/**
 * Template DAO. Chỉ CRUD/SQL. Không gửi thông báo, không mở picker, không tính tiền.
 * Khi dùng Room: gắn @Dao và @Query/@Insert.
 */
interface DemoFarmDao {
    fun insert(record: DemoFarmRecord)
    fun findById(id: String): DemoFarmRecord?
    fun findAll(): List<DemoFarmRecord>
}

class InMemoryFarmDao : DemoFarmDao {
    private val rows = LinkedHashMap<String, DemoFarmRecord>()

    override fun insert(record: DemoFarmRecord) {
        rows[record.id] = record
    }

    override fun findById(id: String): DemoFarmRecord? = rows[id]

    override fun findAll(): List<DemoFarmRecord> = rows.values.toList()
}
