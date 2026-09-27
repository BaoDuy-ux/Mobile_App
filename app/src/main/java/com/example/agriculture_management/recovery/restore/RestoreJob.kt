package com.example.agriculture_management.recovery.restore

import com.example.agriculture_management.recovery.generation.BackupGeneration
import com.example.agriculture_management.recovery.session.RecoveryPhase
import com.example.agriculture_management.recovery.session.RecoverySession

/**
 * Template restore. Không đóng DB khi phase còn LEASED. Không rollback sau COMMITTED.
 */
class RestoreJob {
    fun apply(generation: BackupGeneration, session: RecoverySession): RecoverySession {
        require(session.phase != RecoveryPhase.COMMITTED) {
            "Không tự quay về old sau COMMITTED (generation=${generation.number})"
        }
        return session.copy(phase = RecoveryPhase.COMMITTED)
    }

    fun releaseDb(session: RecoverySession) {
        require(session.phase != RecoveryPhase.LEASED) {
            "Không đóng DB còn lease"
        }
    }
}
