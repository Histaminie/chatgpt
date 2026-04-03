package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expensetracker.data.local.entities.Category
import com.example.expensetracker.data.local.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun observeExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): Category?

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Transaction
    suspend fun replaceAll(categories: List<Category>, expenses: List<Expense>) {
        clearExpenses()
        clearCategories()
        categories.forEach { insertCategory(it) }
        expenses.forEach { insertExpense(it) }
    }
}
