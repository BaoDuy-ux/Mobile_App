package com.example.agriculture_management.recovery.backup

import com.example.agriculture_management.platform.file.AppFileStore
import com.example.agriculture_management.recovery.generation.BackupGeneration

/**
 * Template backup: dùng file/DB port, không đụng UI picker trong data transaction.
 */
class BackupJob(private val files: AppFileStore) {
    fun run(nowEpochMs: Long): BackupGeneration {
        val path = "backups/gen_$nowEpochMs"
        files.write(path, "manifest-placeholder")
        return BackupGeneration(number = nowEpochMs, createdAtEpochMs = nowEpochMs, manifestPath = path)
    }
}
