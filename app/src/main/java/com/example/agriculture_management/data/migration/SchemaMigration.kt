package com.example.agriculture_management.data.migration

/**
 * Template migration schema có phiên bản. Fixtures/schema xuất nằm ở app/schemas khi gắn Room.
 */
data class SchemaMigration(
    val fromVersion: Int,
    val toVersion: Int,
    val sql: List<String>,
)
