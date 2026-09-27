package com.example.agriculture_management.recovery.journal

/**
 * Template journal: ghi bước recovery. Không rollback generation đã COMMITTED.
 */
data class JournalEntry(
    val sessionId: String,
    val step: String,
    val committed: Boolean,
)

class RecoveryJournal {
    private val entries = mutableListOf<JournalEntry>()

    fun append(entry: JournalEntry) {
        val last = entries.lastOrNull { it.sessionId == entry.sessionId }
        check(!(last?.committed == true && !entry.committed)) {
            "Không được quay về old sau COMMITTED"
        }
        entries += entry
    }

    fun of(sessionId: String): List<JournalEntry> = entries.filter { it.sessionId == sessionId }
}
