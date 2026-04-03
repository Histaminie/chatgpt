package com.example.expensetracker.domain.model

import java.time.LocalDate

data class SummaryStats(
    val total: Double,
    val averageDaily: Double,
    val categoryTotals: Map<String, Double>
)

data class TrendPoint(
    val date: LocalDate,
    val amount: Double
)
