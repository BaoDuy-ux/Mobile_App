package com.example.agriculture_management.domain.command

/**
 * Template command (ý định thay đổi trạng thái). Một command = một hành động.
 */
data class CreateFarm(
    val name: String,
    val areaNote: String = "",
)
