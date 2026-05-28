package com.example.data

import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    val allTransactions: Flow<List<Transaction>> = budgetDao.getAllTransactions()
    val allMonthlyBudgets: Flow<List<MonthlyBudget>> = budgetDao.getAllMonthlyBudgetsFlow()

    suspend fun insertTransaction(transaction: Transaction) {
        budgetDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        budgetDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        budgetDao.deleteTransactionById(id)
    }

    suspend fun getBudgetForMonth(yearMonth: String): MonthlyBudget? {
        return budgetDao.getBudgetForMonth(yearMonth)
    }

    suspend fun insertMonthlyBudget(budget: MonthlyBudget) {
        budgetDao.insertMonthlyBudget(budget)
    }
}
