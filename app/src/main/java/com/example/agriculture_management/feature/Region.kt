package com.example.agriculture_management.feature

/**
 * Vùng FEATURE — UI (farm, cycle, activity, cash, stock, task, report, backup, settings).
 *
 * Được phụ thuộc: domain model/port; state/UI dùng chung.
 * Không được làm: import DAO/Room; tự cộng tiền bằng chuỗi hiển thị.
 *
 * Copy package `feature/farm` thành feature mới; giữ ViewModel chỉ nói chuyện với port.
 */
object FeatureRegion
