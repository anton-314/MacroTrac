package dev.antonlammers.trainist.domain.model

import java.time.LocalDate

data class WeightEntry(
    val id: Long = 0,
    val weightKg: Double,
    val date: LocalDate,
    val timestampMs: Long,
)
