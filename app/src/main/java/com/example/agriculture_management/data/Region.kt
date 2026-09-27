package com.example.agriculture_management.data

/**
 * Vùng DATA — adapter persistence (Room/SQLite đã chọn).
 *
 * Được phụ thuộc: domain ports; adapter Room/SQLite.
 * Không được làm: tự gọi notification hoặc mở file picker trong transaction.
 *
 * Room: local/database, local/entity, local/dao. Mã mẫu lưu RAM: local/demo.
 * Repository xử lý dữ liệu; snapshot và migration phục vụ sao lưu/nâng cấp.
 */
object DataRegion
