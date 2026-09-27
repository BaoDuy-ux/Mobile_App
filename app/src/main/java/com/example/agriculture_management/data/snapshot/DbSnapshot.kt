package com.example.agriculture_management.data.snapshot

/**
 * Template snapshot DB (điểm ảnh phục vụ recovery/backup).
 * Chỉ mô tả trạng thái persistence, không điều khiển UI.
 */
data class DbSnapshot(
    val schemaVersion: Int,
    val takenAtEpochMs: Long,
    val payloadHint: String,
)
