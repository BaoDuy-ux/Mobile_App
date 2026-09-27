package com.example.agriculture_management.core

/**
 * ID kiểu mới (value object). Thay `FarmId` bằng id của thực thể bạn đang làm.
 */
@JvmInline
value class EntityId(val value: String) {
    init {
        require(value.isNotBlank()) { "EntityId không được rỗng" }
    }

    companion object {
        fun new(prefix: String = "id"): EntityId =
            EntityId("${prefix}_${System.nanoTime()}")
    }
}
