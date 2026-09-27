package com.example.agriculture_management.recovery.session

/**
 * Template phiên recovery. Factory tạo session; không đóng DB khi còn lease.
 */
enum class RecoveryPhase { PREPARE, LEASED, COMMITTED, FAILED }

data class RecoverySession(
    val id: String,
    val phase: RecoveryPhase,
)

fun interface RecoverySessionFactory {
    fun open(reason: String): RecoverySession
}

class InMemoryRecoverySessionFactory : RecoverySessionFactory {
    override fun open(reason: String): RecoverySession =
        RecoverySession(id = "session_${reason.hashCode()}", phase = RecoveryPhase.PREPARE)
}
