package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.CategoryModel
import com.example.expensetracker.domain.model.ExpenseModel
import com.example.expensetracker.domain.model.SummaryStats
import com.example.expensetracker.domain.model.TrendPoint
import com.example.expensetracker.domain.usecase.AddCategoryUseCase
import com.example.expensetracker.domain.usecase.AddExpenseUseCase
import com.example.expensetracker.domain.usecase.DeleteCategoryUseCase
import com.example.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.example.expensetracker.domain.usecase.ExportCsvUseCase
import com.example.expensetracker.domain.usecase.ImportCsvUseCase
import com.example.expensetracker.domain.usecase.ObserveCategoriesUseCase
import com.example.expensetracker.domain.usecase.ObserveExpensesUseCase
import com.example.expensetracker.domain.usecase.UpdateCategoryUseCase
import com.example.expensetracker.domain.usecase.UpdateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class DateFilter { TODAY, WEEK, MONTH, CUSTOM, ALL }
enum class SortOrder { HIGH_TO_LOW, LOW_TO_HIGH, LATEST }

data class UiState(
    val expenses: List<ExpenseModel> = emptyList(),
    val categories: List<CategoryModel> = emptyList(),
    val selectedDateFilter: DateFilter = DateFilter.ALL,
    val selectedCategoryId: Long? = null,
    val customStart: Long? = null,
    val customEnd: Long? = null,
    val sortOrder: SortOrder = SortOrder.LATEST,
    val searchQuery: String = "",
    val message: String? = null
) {
    val filteredExpenses: List<ExpenseModel>
        get() = expenses
            .filterByDate(selectedDateFilter, customStart, customEnd)
            .filter { selectedCategoryId == null || it.categoryId == selectedCategoryId }
            .filter { it.notes.contains(searchQuery, true) || it.categoryName.contains(searchQuery, true) }
            .sortedByOrder(sortOrder)

    val summary: SummaryStats
        get() {
            val list = filteredExpenses
            val total = list.sumOf { it.amount }
            val days = list.map { it.timestamp.toLocalDate() }.distinct().size.coerceAtLeast(1)
            val categoryTotals = list.groupBy { it.categoryName }.mapValues { (_, e) -> e.sumOf { it.amount } }
            return SummaryStats(total = total, averageDaily = total / days, categoryTotals = categoryTotals)
        }

    val trend: List<TrendPoint>
        get() = filteredExpenses.groupBy { it.timestamp.toLocalDate() }
            .map { (date, expenses) -> TrendPoint(date, expenses.sumOf { it.amount }) }
            .sortedBy { it.date }
}

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    observeExpensesUseCase: ObserveExpensesUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val importCsvUseCase: ImportCsvUseCase
) : ViewModel() {

    private val mutableState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = combine(
        mutableState,
        observeExpensesUseCase(),
        observeCategoriesUseCase()
    ) { base, expenses, categories ->
        base.copy(expenses = expenses, categories = categories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun addOrUpdateExpense(
        id: Long?,
        amount: Double,
        categoryId: Long,
        timestamp: Long,
        notes: String
    ) = viewModelScope.launch {
        runCatching {
            if (id == null) {
                addExpenseUseCase(amount, categoryId, timestamp, notes)
            } else {
                val category = uiState.value.categories.first { it.id == categoryId }
                updateExpenseUseCase(
                    ExpenseModel(
                        id = id,
                        amount = amount,
                        categoryId = categoryId,
                        categoryName = category.name,
                        categoryColorHex = category.colorHex,
                        timestamp = timestamp,
                        notes = notes
                    )
                )
            }
        }.onFailure { setMessage(it.message ?: "Unable to save expense") }
    }

    fun deleteExpense(expense: ExpenseModel) = viewModelScope.launch {
        deleteExpenseUseCase(expense)
    }

    fun addCategory(name: String, color: String) = viewModelScope.launch {
        runCatching { addCategoryUseCase(name, color) }
            .onFailure { setMessage(it.message ?: "Unable to add category") }
    }

    fun updateCategory(category: CategoryModel) = viewModelScope.launch {
        updateCategoryUseCase(category)
    }

    fun deleteCategory(category: CategoryModel) = viewModelScope.launch {
        deleteCategoryUseCase(category)
    }

    fun setDateFilter(filter: DateFilter) = mutableState.update { it.copy(selectedDateFilter = filter) }
    fun setCategoryFilter(categoryId: Long?) = mutableState.update { it.copy(selectedCategoryId = categoryId) }
    fun setSortOrder(sortOrder: SortOrder) = mutableState.update { it.copy(sortOrder = sortOrder) }
    fun setSearchQuery(value: String) = mutableState.update { it.copy(searchQuery = value) }
    fun clearMessage() = mutableState.update { it.copy(message = null) }

    suspend fun exportCsv(): String = exportCsvUseCase()

    fun importCsv(content: String) = viewModelScope.launch {
        runCatching { importCsvUseCase(content) }
            .onSuccess { setMessage("Import complete") }
            .onFailure { setMessage("Import failed: ${it.message}") }
    }

    private fun setMessage(message: String) = mutableState.update { it.copy(message = message) }
}

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun List<ExpenseModel>.filterByDate(filter: DateFilter, customStart: Long?, customEnd: Long?): List<ExpenseModel> {
    val now = LocalDate.now()
    return filter {
        val date = it.timestamp.toLocalDate()
        when (filter) {
            DateFilter.TODAY -> date == now
            DateFilter.WEEK -> !date.isBefore(now.minusDays(6))
            DateFilter.MONTH -> !date.isBefore(now.minusDays(29))
            DateFilter.CUSTOM -> {
                val start = customStart?.toLocalDate() ?: LocalDate.MIN
                val end = customEnd?.toLocalDate() ?: LocalDate.MAX
                (date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))
            }
            DateFilter.ALL -> true
        }
    }
}

private fun List<ExpenseModel>.sortedByOrder(order: SortOrder): List<ExpenseModel> {
    return when (order) {
        SortOrder.HIGH_TO_LOW -> sortedByDescending { it.amount }
        SortOrder.LOW_TO_HIGH -> sortedBy { it.amount }
        SortOrder.LATEST -> sortedByDescending { it.timestamp }
    }
}
