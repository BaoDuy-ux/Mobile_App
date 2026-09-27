package com.example.agriculture_management.platform.reminder

/**
 * Template reminder/notification. Data không được gọi từ trong transaction DB.
 */
data class ReminderRequest(
    val id: String,
    val title: String,
    val triggerAtEpochMs: Long,
)

fun interface ReminderScheduler {
    fun schedule(request: ReminderRequest)
}
