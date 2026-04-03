package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensetracker.data.local.entities.Category
import com.example.expensetracker.data.local.entities.Expense

@Database(entities = [Expense::class, Category::class], version = 1, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
