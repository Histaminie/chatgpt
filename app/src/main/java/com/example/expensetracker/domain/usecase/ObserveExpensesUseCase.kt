package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class ObserveExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke() = repository.observeExpenses()
}
