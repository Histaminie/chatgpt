package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.entities.Category
import com.example.expensetracker.data.local.entities.Expense
import com.example.expensetracker.domain.model.CategoryModel
import com.example.expensetracker.domain.model.ExpenseModel
import com.example.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun observeExpenses(): Flow<List<ExpenseModel>> {
        return combine(dao.observeExpenses(), dao.observeCategories()) { expenses, categories ->
            val map = categories.associateBy { it.id }
            expenses.mapNotNull { expense ->
                val category = map[expense.categoryId] ?: return@mapNotNull null
                expense.toModel(category)
            }
        }
    }

    override fun observeCategories(): Flow<List<CategoryModel>> {
        return dao.observeCategories().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addExpense(amount: Double, categoryId: Long, timestamp: Long, notes: String) {
        dao.insertExpense(Expense(amount = amount, categoryId = categoryId, timestamp = timestamp, notes = notes.trim()))
    }

    override suspend fun updateExpense(expense: ExpenseModel) {
        dao.updateExpense(
            Expense(
                id = expense.id,
                amount = expense.amount,
                categoryId = expense.categoryId,
                timestamp = expense.timestamp,
                notes = expense.notes
            )
        )
    }

    override suspend fun deleteExpense(expense: ExpenseModel) {
        dao.deleteExpense(
            Expense(
                id = expense.id,
                amount = expense.amount,
                categoryId = expense.categoryId,
                timestamp = expense.timestamp,
                notes = expense.notes
            )
        )
    }

    override suspend fun addCategory(name: String, colorHex: String, isDefault: Boolean) {
        dao.insertCategory(Category(name = name, colorHex = colorHex, isDefault = isDefault))
    }

    override suspend fun updateCategory(category: CategoryModel) {
        dao.updateCategory(Category(category.id, category.name, category.colorHex, category.isDefault))
    }

    override suspend fun deleteCategory(category: CategoryModel) {
        if (category.isDefault) return
        dao.deleteCategory(Category(category.id, category.name, category.colorHex, category.isDefault))
    }

    override suspend fun exportCsv(): String {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val categories = dao.observeCategories().first().associateBy { it.id }
        val rows = dao.observeExpenses().first().map { expense ->
            val categoryName = categories[expense.categoryId]?.name ?: "Unknown"
            val date = Instant.ofEpochMilli(expense.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            "${expense.id},${expense.amount},\"$categoryName\",${expense.timestamp},\"${date.format(formatter)}\",\"${expense.notes.replace("\"", "''")}\""
        }
        return buildString {
            appendLine("id,amount,category,timestamp,datetime,notes")
            rows.forEach { appendLine(it) }
        }
    }

    override suspend fun importFromCsv(csvContent: String) {
        val existingCategories = dao.observeCategories().first().toMutableList()
        val categoryIdByName = existingCategories.associateBy { it.name.lowercase() }.mapValues { it.value.id }.toMutableMap()
        val expenses = mutableListOf<Expense>()

        csvContent.lineSequence().drop(1).filter { it.isNotBlank() }.forEach { line ->
            val columns = parseCsvLine(line)
            if (columns.size < 6) return@forEach
            val amount = columns[1].toDoubleOrNull() ?: return@forEach
            val categoryName = columns[2]
            val timestamp = columns[3].toLongOrNull() ?: System.currentTimeMillis()
            val notes = columns[5]

            val existingId = categoryIdByName[categoryName.lowercase()]
            val categoryId = existingId ?: dao.insertCategory(
                Category(name = categoryName, colorHex = randomColorHex(), isDefault = false)
            ).also { newId ->
                categoryIdByName[categoryName.lowercase()] = newId
                existingCategories.add(Category(newId, categoryName, randomColorHex(), false))
            }
            expenses.add(Expense(amount = amount, categoryId = categoryId, timestamp = timestamp, notes = notes))
        }
        expenses.forEach { dao.insertExpense(it) }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val token = StringBuilder()
        var inQuotes = false
        line.forEach { char ->
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result += token.toString()
                    token.clear()
                }
                else -> token.append(char)
            }
        }
        result += token.toString()
        return result
    }

    private fun Expense.toModel(category: Category): ExpenseModel = ExpenseModel(
        id = id,
        amount = amount,
        categoryId = categoryId,
        categoryName = category.name,
        categoryColorHex = category.colorHex,
        timestamp = timestamp,
        notes = notes
    )

    private fun Category.toModel(): CategoryModel = CategoryModel(id, name, colorHex, isDefault)

    private fun randomColorHex(): String {
        val colors = listOf("#EF5350", "#AB47BC", "#5C6BC0", "#29B6F6", "#66BB6A", "#FFCA28", "#FFA726")
        return colors.random()
    }
}
