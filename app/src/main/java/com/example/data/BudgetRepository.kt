package com.example.data

import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    val allTransactions: Flow<List<Transaction>> = budgetDao.getAllTransactions()
    val allMonthlyBudgets: Flow<List<MonthlyBudget>> = budgetDao.getAllMonthlyBudgetsFlow()
    val allCategories: Flow<List<Category>> = budgetDao.getAllCategoriesFlow()

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

    suspend fun insertCategory(category: Category) {
        budgetDao.insertCategory(category)
    }

    suspend fun insertCategories(categories: List<Category>) {
        budgetDao.insertCategories(categories)
    }

    suspend fun deleteCategory(category: Category) {
        budgetDao.deleteCategory(category)
        budgetDao.updateTransactionsOfDeletedCategory(category.id)
    }

    suspend fun deleteCategoryById(id: String) {
        budgetDao.deleteCategoryById(id)
        budgetDao.updateTransactionsOfDeletedCategory(id)
    }

    suspend fun updateCategory(oldId: String, category: Category) {
        if (oldId != category.id) {
            // If the ID (name) changed, we insert the new one, update matching transactions, and delete the old one
            budgetDao.insertCategory(category)
            budgetDao.updateTransactionsCategory(oldId, category.id)
            budgetDao.deleteCategoryById(oldId)
        } else {
            budgetDao.insertCategory(category)
        }
    }
}
