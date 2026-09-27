package com.example.agriculture_management.recovery.generation

/**
 * Template thế hệ backup (generation). Một generation = một bản có thể restore.
 */
data class BackupGeneration(
    val number: Long,
    val createdAtEpochMs: Long,
    val manifestPath: String,
)
