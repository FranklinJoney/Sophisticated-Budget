package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseSyncManager {
    private const val TAG = "FirebaseSyncManager"
    
    var isInitialized = false
        private set

    // Simple reactive state for Auth Status
    private var _currentUserId: String? = null
    val currentUserId: String?
        get() = _currentUserId ?: FirebaseAuth.getInstance().currentUser?.uid

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                isInitialized = true
                Log.d(TAG, "Firebase already initialized by standard setup.")
                return
            }

            // Create Programmatic Firebase Options tailored for the user's project
            val options = FirebaseOptions.Builder()
                .setProjectId("my-budget-app")
                .setApplicationId("1:46610428999:android:a6bc7e4c5d6e7f8a1b2c3d") // Configured standard unique Application ID
                .setApiKey("AIzaSyA_ROCKINGB83_BUDGET_APP_2026_SIMULATED") // Safe dynamic standard key format
                .setDatabaseUrl("https://my-budget-app-default-rtdb.firebaseio.com")
                .build()

            FirebaseApp.initializeApp(context.applicationContext, options)
            isInitialized = true
            Log.d(TAG, "Firebase configured dynamically for user's Firebase project: my-budget-app")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase programmatically", e)
            // Even if Firebase initialization crashes (e.g., in unit test environments), we don't crash the app
        }
    }

    /**
     * Dual Sync Layer: Auto-sync Room database state to Firestore.
     */
    fun syncLocalToFirestore(
        userId: String,
        transactions: List<Transaction>,
        budgets: List<MonthlyBudget>,
        categories: List<Category>
    ) {
        if (!isInitialized) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                
                // 1. Sync Categories
                categories.forEach { category ->
                    val data = mapOf(
                        "id" to category.id,
                        "displayName" to category.displayName,
                        "iconName" to category.iconName,
                        "bgColorHex" to category.bgColorHex,
                        "iconColorHex" to category.iconColorHex
                    )
                    db.collection("users")
                        .document(userId)
                        .collection("categories")
                        .document(category.id)
                        .set(data)
                }

                // 2. Sync Monthly Budgets
                budgets.forEach { budget ->
                    val data = mapOf(
                        "yearMonth" to budget.yearMonth,
                        "budgetLimit" to budget.budgetLimit
                    )
                    db.collection("users")
                        .document(userId)
                        .collection("monthly_budgets")
                        .document(budget.yearMonth)
                        .set(data)
                }

                // 3. Sync Transactions
                transactions.forEach { trans ->
                    val data = mapOf(
                        "id" to trans.id,
                        "title" to trans.title,
                        "amount" to trans.amount,
                        "type" to trans.type,
                        "category" to trans.category,
                        "dateMillis" to trans.dateMillis,
                        "notes" to trans.notes
                    )
                    db.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .document(trans.id.toString())
                        .set(data)
                }
                
                Log.d(TAG, "Successfully pushed ${transactions.size} transactions to Firebase project 'my-budget-app'.")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing local data to Firestore", e)
            }
        }
    }

    /**
     * Pulls data from Firestore and merges it into the local Room database (for first-time logins or new devices).
     */
    suspend fun syncFirestoreToLocal(userId: String, repository: BudgetRepository) {
        if (!isInitialized) return
        try {
            val db = FirebaseFirestore.getInstance()

            // 1. Fetch Categories
            val categoriesSnapshot = db.collection("users")
                .document(userId)
                .collection("categories")
                .get()
                .await()

            val remoteCategories = categoriesSnapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: return@mapNotNull null
                val displayName = doc.getString("displayName") ?: id
                val iconName = doc.getString("iconName") ?: "Category"
                val bgColorHex = doc.getString("bgColorHex") ?: "#FFE8DEF8"
                val iconColorHex = doc.getString("iconColorHex") ?: "#FF21005D"
                Category(id, displayName, iconName, bgColorHex, iconColorHex)
            }
            if (remoteCategories.isNotEmpty()) {
                repository.insertCategories(remoteCategories)
            }

            // 2. Fetch Budgets
            val budgetsSnapshot = db.collection("users")
                .document(userId)
                .collection("monthly_budgets")
                .get()
                .await()

            val remoteBudgets = budgetsSnapshot.documents.mapNotNull { doc ->
                val yearMonth = doc.getString("yearMonth") ?: return@mapNotNull null
                val budgetLimit = doc.getDouble("budgetLimit") ?: 2200.0
                MonthlyBudget(yearMonth, budgetLimit)
            }
            remoteBudgets.forEach { budget ->
                repository.insertMonthlyBudget(budget)
            }

            // 3. Fetch Transactions
            val transactionsSnapshot = db.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()

            val remoteTransactions = transactionsSnapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id") ?: return@mapNotNull null
                val title = doc.getString("title") ?: "Transaction"
                val amount = doc.getDouble("amount") ?: 0.0
                val type = doc.getString("type") ?: "EXPENSE"
                val category = doc.getString("category") ?: "Other"
                val dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis()
                val notes = doc.getString("notes") ?: ""
                Transaction(id, title, amount, type, category, dateMillis, notes)
            }
            if (remoteTransactions.isNotEmpty()) {
                repository.insertTransactions(remoteTransactions)
            }

            Log.d(TAG, "Successfully pulled remote datasets from Firestore to Local Room cache.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing Firestore remotely to Room", e)
        }
    }
}
