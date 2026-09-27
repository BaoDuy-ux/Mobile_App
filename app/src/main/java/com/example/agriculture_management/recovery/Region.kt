package com.example.agriculture_management.recovery

/**
 * Vùng RECOVERY — phiên phục hồi, journal, backup/restore.
 *
 * Được phụ thuộc: session factory; manifest; file/DB ports.
 * Không được làm: đóng DB còn lease; tự quay về old sau COMMITTED.
 *
 * Cấu trúc mẫu: session / generation / journal / backup / restore
 */
object RecoveryRegion
