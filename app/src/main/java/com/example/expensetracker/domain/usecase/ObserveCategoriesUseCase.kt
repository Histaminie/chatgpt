package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class ObserveCategoriesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke() = repository.observeCategories()
}
