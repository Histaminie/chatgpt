package com.example.expensetracker.domain.repository

import com.example.expensetracker.domain.model.CategoryModel
import com.example.expensetracker.domain.model.ExpenseModel
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(): Flow<List<ExpenseModel>>
    fun observeCategories(): Flow<List<CategoryModel>>

    suspend fun addExpense(amount: Double, categoryId: Long, timestamp: Long, notes: String)
    suspend fun updateExpense(expense: ExpenseModel)
    suspend fun deleteExpense(expense: ExpenseModel)

    suspend fun addCategory(name: String, colorHex: String, isDefault: Boolean = false)
    suspend fun updateCategory(category: CategoryModel)
    suspend fun deleteCategory(category: CategoryModel)

    suspend fun exportCsv(): String
    suspend fun importFromCsv(csvContent: String)
}
