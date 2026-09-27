package com.example.agriculture_management.platform.clock

/**
 * Template đồng hồ. Domain/data nhận epoch từ đây — dễ fake khi test.
 */
fun interface AppClock {
    fun nowEpochMs(): Long
}

object SystemAppClock : AppClock {
    override fun nowEpochMs(): Long = System.currentTimeMillis()
}
