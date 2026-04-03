package com.example.expensetracker.domain.model

data class ExpenseModel(
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val timestamp: Long,
    val notes: String = ""
)
