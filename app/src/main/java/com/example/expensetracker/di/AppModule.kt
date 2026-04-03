package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.repository.ExpenseRepositoryImpl
import com.example.expensetracker.domain.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return Room.databaseBuilder(context, ExpenseDatabase::class.java, "expense_db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val now = System.currentTimeMillis()
                    db.execSQL("INSERT INTO categories (name, colorHex, isDefault) VALUES ('Food', '#EF5350', 1)")
                    db.execSQL("INSERT INTO categories (name, colorHex, isDefault) VALUES ('Transport', '#5C6BC0', 1)")
                    db.execSQL("INSERT INTO categories (name, colorHex, isDefault) VALUES ('Bills', '#FFA726', 1)")
                    db.execSQL("INSERT INTO expenses (amount, categoryId, timestamp, notes) VALUES (12.5, 1, ${now - 86_400_000L}, 'Lunch')")
                    db.execSQL("INSERT INTO expenses (amount, categoryId, timestamp, notes) VALUES (7.0, 2, ${now - 172_800_000L}, 'Bus')")
                    db.execSQL("INSERT INTO expenses (amount, categoryId, timestamp, notes) VALUES (45.9, 3, ${now - 259_200_000L}, 'Electricity')")
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao = database.expenseDao()

    @Provides
    @Singleton
    fun provideExpenseRepository(dao: ExpenseDao): ExpenseRepository = ExpenseRepositoryImpl(dao)
}
