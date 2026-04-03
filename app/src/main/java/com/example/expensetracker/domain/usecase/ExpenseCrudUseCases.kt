package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.CategoryModel
import com.example.expensetracker.domain.model.ExpenseModel
import com.example.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(amount: Double, categoryId: Long, timestamp: Long, notes: String) {
        require(amount > 0) { "Amount must be positive" }
        repository.addExpense(amount, categoryId, timestamp, notes)
    }
}

class UpdateExpenseUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: ExpenseModel) {
        require(expense.amount > 0) { "Amount must be positive" }
        repository.updateExpense(expense)
    }
}

class DeleteExpenseUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: ExpenseModel) = repository.deleteExpense(expense)
}

class AddCategoryUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(name: String, colorHex: String) {
        require(name.isNotBlank()) { "Category name required" }
        repository.addCategory(name.trim(), colorHex)
    }
}

class UpdateCategoryUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(category: CategoryModel) {
        require(category.name.isNotBlank()) { "Category name required" }
        repository.updateCategory(category)
    }
}

class DeleteCategoryUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(category: CategoryModel) = repository.deleteCategory(category)
}

class ExportCsvUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke() = repository.exportCsv()
}

class ImportCsvUseCase @Inject constructor(private val repository: ExpenseRepository) {
    suspend operator fun invoke(content: String) = repository.importFromCsv(content)
}
