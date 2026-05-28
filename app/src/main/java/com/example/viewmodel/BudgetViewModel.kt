package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BudgetRepository
import com.example.data.MonthlyBudget
import com.example.data.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository

    // User preferences states
    private val _userName = MutableStateFlow("Marcus")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    fun updateUserName(name: String) {
        _userName.value = name.ifBlank { "User" }
    }

    fun updateCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol.ifBlank { "$" }
    }

    fun formatMoney(amount: Double): String {
        return "${_currencySymbol.value}${String.format(Locale.US, "%,.2f", amount)}"
    }

    fun formatMoneyCompact(amount: Double): String {
        return "${_currencySymbol.value}${String.format(Locale.US, "%,.0f", amount)}"
    }

    fun getMoneyDecimalString(amount: Double): String {
        val cents = Math.round((amount - amount.toInt()) * 100).toInt()
        val normalizedCents = if (cents >= 100) 0 else if (cents < 0) 0 else cents
        return String.format(Locale.US, ".%02d", normalizedCents)
    }

    fun clearAllData() {
        viewModelScope.launch {
            val database = AppDatabase.getDatabase(getApplication())
            // Deletes elements
            allTransactions.value.forEach { repository.deleteTransaction(it) }
            allBudgets.value.forEach { /* delete budget or override */ }
            val current = _selectedMonth.value
            seedDefaultData(current)
        }
    }

    // Current State for Month Navigation (Format: "YYYY-MM")
    private val _selectedMonth = MutableStateFlow("")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BudgetRepository(database.budgetDao())

        // Set current month as initial selection
        val initialMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        _selectedMonth.value = initialMonth

        // Seed initial data if database is empty
        viewModelScope.launch {
            repository.allTransactions.first().let { list ->
                if (list.isEmpty()) {
                    seedDefaultData(initialMonth)
                }
            }
        }
    }

    // Expose all transactions flow
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expose all budgets flow
    val allBudgets: StateFlow<List<MonthlyBudget>> = repository.allMonthlyBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computed budget info for selected month
    val currentMonthBudgetState: StateFlow<MonthBudgetSummary> = combine(
        allTransactions,
        allBudgets,
        _selectedMonth
    ) { transactions, budgets, month ->
        calculateSummary(transactions, budgets, month)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthBudgetSummary()
    )

    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }

    fun nextMonth() {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        try {
            val date = sdf.parse(_selectedMonth.value) ?: return
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, 1)
            _selectedMonth.value = sdf.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun prevMonth() {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        try {
            val date = sdf.parse(_selectedMonth.value) ?: return
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, -1)
            _selectedMonth.value = sdf.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addTransaction(title: String, amount: Double, type: String, category: String, dateMillis: Long, notes: String = "") {
        viewModelScope.launch {
            val transaction = Transaction(
                title = title,
                amount = amount,
                type = type,
                category = category,
                dateMillis = dateMillis,
                notes = notes
            )
            repository.insertTransaction(transaction)
            
            // Automatically make sure a budget exists for that month
            val transMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(dateMillis))
            val existingBudget = repository.getBudgetForMonth(transMonth)
            if (existingBudget == null) {
                repository.insertMonthlyBudget(MonthlyBudget(transMonth, 2200.0))
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateMonthlyBudgetLimit(limit: Double) {
        viewModelScope.launch {
            val budget = MonthlyBudget(yearMonth = _selectedMonth.value, budgetLimit = limit)
            repository.insertMonthlyBudget(budget)
        }
    }

    private fun calculateSummary(allTrans: List<Transaction>, allBudg: List<MonthlyBudget>, targetMonth: String): MonthBudgetSummary {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        // Filter transactions for selected month
        val monthlyTrans = allTrans.filter {
            try {
                val m = sdf.format(Date(it.dateMillis))
                m == targetMonth
            } catch (e: Exception) {
                false
            }
        }

        // Find or fallback budget limit for this month
        val budgetLimit = allBudg.find { it.yearMonth == targetMonth }?.budgetLimit ?: 2200.0

        val totalSpent = monthlyTrans.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val totalIncome = monthlyTrans.filter { it.type == "INCOME" }.sumOf { it.amount }

        // Category stats (Expenses only)
        val expensesOnly = monthlyTrans.filter { it.type == "EXPENSE" }
        val categoryBreakdown = expensesOnly.groupBy { it.category }
            .map { (category, list) ->
                val total = list.sumOf { it.amount }
                val pct = if (totalSpent > 0) (total / totalSpent) * 100 else 0.0
                CategoryReport(
                    category = category,
                    totalSpent = total,
                    transactionCount = list.size,
                    percentage = pct
                )
            }.sortedByDescending { it.totalSpent }

        // Group by day of month for daily visualization
        val dailyFormat = SimpleDateFormat("d", Locale.getDefault())
        val dailyGrouped = expensesOnly.groupBy {
            dailyFormat.format(Date(it.dateMillis)).toIntOrNull() ?: 1
        }.mapValues { (_, list) -> list.sumOf { it.amount } }

        // Calculate a safe daily limit. Remaining Budget / Remaining Days in month (capped down to today)
        val cal = Calendar.getInstance()
        val currentYearMonth = sdf.format(cal.time)
        val dailyBudgetLimit: Double
        if (targetMonth == currentYearMonth) {
            val today = cal.get(Calendar.DAY_OF_MONTH)
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val remainingDays = (maxDays - today + 1).coerceAtLeast(1)
            val remainingBudget = (budgetLimit - totalSpent).coerceAtLeast(0.0)
            dailyBudgetLimit = remainingBudget / remainingDays
        } else {
            // Predict normal daily limits
            val parsedDate = try {
                if (targetMonth.isNotBlank()) sdf.parse(targetMonth) else null
            } catch (e: Exception) {
                null
            } ?: Date()
            cal.time = parsedDate
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            dailyBudgetLimit = budgetLimit / maxDays
        }

        return MonthBudgetSummary(
            yearMonth = targetMonth,
            budgetLimit = budgetLimit,
            totalSpent = totalSpent,
            totalIncome = totalIncome,
            transactions = monthlyTrans,
            categoryReports = categoryBreakdown,
            dailySpendList = dailyGrouped,
            dailyBudgetLimit = dailyBudgetLimit
        )
    }

    private suspend fun seedDefaultData(currentMonth: String) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        // Ensure we seed current month's budget
        repository.insertMonthlyBudget(MonthlyBudget(currentMonth, 2200.0))

        // Create helper dates spread across the current month
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        
        fun getTimestampForDay(day: Int): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, day.coerceIn(1, 28))
            return cal.timeInMillis
        }

        // Income - Monthly Salary (at the start of the month)
        repository.insertTransaction(
            Transaction(
                title = "Monthly Salary",
                amount = 3200.0,
                type = "INCOME",
                category = "Other",
                dateMillis = getTimestampForDay(1),
                notes = "Core monthly income"
            )
        )

        // Income - Freelance Project
        repository.insertTransaction(
            Transaction(
                title = "Design Freelance payment",
                amount = 450.0,
                type = "INCOME",
                category = "Other",
                dateMillis = getTimestampForDay(12),
                notes = "Logo draft client"
            )
        )

        // 12 Dining & Food transactions (Total: $420.0)
        val foodTrxs = listOf(
            60.0 to "Steakhouse Dinner", 15.5 to "Starbucks Coffee", 45.0 to "Supermarket Grocery",
            22.0 to "Pizzeria", 12.0 to "Boba Tea Delight", 85.0 to "Weekly Grocery Haul",
            18.2 to "Ramen Lunch", 35.0 to "Sushi Takeout", 24.5 to "Burger Joint",
            14.8 to "Bakery Treats", 55.0 to "Family Restaurant Dinner", 33.0 to "Whole Foods Market"
        )
        foodTrxs.forEachIndexed { i, pair ->
            // Spread evenly throughout the active month
            val day = ((i * 2) + 2).coerceAtMost(today)
            repository.insertTransaction(
                Transaction(
                    title = pair.second,
                    amount = pair.first,
                    type = "EXPENSE",
                    category = "Dining & Food",
                    dateMillis = getTimestampForDay(day)
                )
            )
        }

        // 8 Transport transactions (Total: $185.20)
        val transportTrxs = listOf(
            25.0 to "Uber Ride", 35.0 to "Gas Station Fill-up", 18.2 to "Train Ticket",
            12.0 to "Metro Pass", 22.0 to "Gas Refuel", 28.0 to "Uber Comfort ride",
            15.0 to "City Parking", 30.0 to "Gas Refuel"
        )
        transportTrxs.forEachIndexed { i, pair ->
            val day = ((i * 3) + 3).coerceAtMost(today)
            repository.insertTransaction(
                Transaction(
                    title = pair.second,
                    amount = pair.first,
                    type = "EXPENSE",
                    category = "Transport",
                    dateMillis = getTimestampForDay(day)
                )
            )
        }

        // 5 Shopping transactions (Total: $174.80)
        val shoppingTrxs = listOf(
            50.0 to "Uniqlo Cotton Tee", 30.0 to "Amazon Kindle Book", 44.80 to "H&M Pants",
            35.0 to "Desk Organizer", 15.0 to "Stationery Supplies"
        )
        shoppingTrxs.forEachIndexed { i, pair ->
            val day = ((i * 5) + 4).coerceAtMost(today)
            repository.insertTransaction(
                Transaction(
                    title = pair.second,
                    amount = pair.first,
                    type = "EXPENSE",
                    category = "Shopping",
                    dateMillis = getTimestampForDay(day)
                )
            )
        }

        // Utilities
        repository.insertTransaction(
            Transaction(
                title = "Electricity & Water Bill",
                amount = 120.0,
                type = "EXPENSE",
                category = "Utilities",
                dateMillis = getTimestampForDay(5),
                notes = "Auto-paid utility"
            )
        )

        // Entertainment
        repository.insertTransaction(
            Transaction(
                title = "Movie Night - IMAX",
                amount = 45.0,
                type = "EXPENSE",
                category = "Entertainment",
                dateMillis = getTimestampForDay(14)
            )
        )
    }
}

// Data structures for UI state representation
data class CategoryReport(
    val category: String,
    val totalSpent: Double,
    val transactionCount: Int,
    val percentage: Double
)

data class MonthBudgetSummary(
    val yearMonth: String = "",
    val budgetLimit: Double = 2200.0,
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val categoryReports: List<CategoryReport> = emptyList(),
    val dailySpendList: Map<Int, Double> = emptyMap(), // Day -> Expense sum
    val dailyBudgetLimit: Double = 0.0
)
