package com.example.agriculture_management.data.local.demo

/**
 * Template bản ghi persistence. Đây là hình dạng cột/bảng, không phải domain model.
 * Khi dùng Room: gắn @Entity(tableName = "farms").
 */
data class DemoFarmRecord(
    val id: String,
    val name: String,
    val areaNote: String,
)
