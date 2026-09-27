package com.example.agriculture_management.domain.query

import com.example.agriculture_management.core.EntityId

/**
 * Template query (đọc, không side-effect).
 */
data class GetFarm(val id: EntityId)

object ListFarms
