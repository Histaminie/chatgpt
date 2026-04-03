package com.example.expensetracker.domain.model

data class CategoryModel(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val isDefault: Boolean = false
)
