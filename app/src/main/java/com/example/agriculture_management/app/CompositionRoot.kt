package com.example.agriculture_management.app

import android.content.Context
import com.example.agriculture_management.data.local.demo.InMemoryFarmDao
import com.example.agriculture_management.data.repository.FarmRepository
import com.example.agriculture_management.domain.port.FarmPort
import com.example.agriculture_management.feature.farm.FarmViewModel
import com.example.agriculture_management.platform.clock.AppClock
import com.example.agriculture_management.platform.clock.SystemAppClock
import com.example.agriculture_management.platform.crypto.Checksum
import com.example.agriculture_management.platform.crypto.DummyChecksum
import com.example.agriculture_management.platform.file.AppFileStore
import com.example.agriculture_management.platform.file.InMemoryAppFileStore
import com.example.agriculture_management.recovery.backup.BackupJob
import com.example.agriculture_management.recovery.journal.RecoveryJournal
import com.example.agriculture_management.recovery.session.InMemoryRecoverySessionFactory
import com.example.agriculture_management.recovery.session.RecoverySessionFactory

/**
 * Template composition root: chỉ lắp ghép. Không viết policy tiền ở đây.
 */
class CompositionRoot(
    val clock: AppClock,
    val files: AppFileStore,
    val checksum: Checksum,
    val farms: FarmPort,
    val farmViewModel: FarmViewModel,
    val recoverySessions: RecoverySessionFactory,
    val journal: RecoveryJournal,
    val backupJob: BackupJob,
) {
    companion object {
        fun create(@Suppress("UNUSED_PARAMETER") context: Context): CompositionRoot {
            val clock: AppClock = SystemAppClock
            val files: AppFileStore = InMemoryAppFileStore()
            val farms: FarmPort = FarmRepository(InMemoryFarmDao())
            return CompositionRoot(
                clock = clock,
                files = files,
                checksum = DummyChecksum,
                farms = farms,
                farmViewModel = FarmViewModel(farms).also { it.refresh() },
                recoverySessions = InMemoryRecoverySessionFactory(),
                journal = RecoveryJournal(),
                backupJob = BackupJob(files),
            )
        }
    }
}
