package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String, // e.g., "Dining & Food", "Transport", "Shopping", "Entertainment", "Utilities", "Other"
    val dateMillis: Long,
    val notes: String = ""
)

@Entity(tableName = "monthly_budgets")
data class MonthlyBudget(
    @PrimaryKey val yearMonth: String, //Format: "YYYY-MM" (e.g. "2026-05")
    val budgetLimit: Double
)
