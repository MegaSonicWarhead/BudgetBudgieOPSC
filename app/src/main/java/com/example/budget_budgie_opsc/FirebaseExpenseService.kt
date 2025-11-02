package com.example.budget_budgie_opsc

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseExpenseService {
    private val database = Firebase.database
    private val expensesRef = database.getReference("expenses")

    suspend fun insertExpense(expense: Expense): String {
        val key = expensesRef.push().key ?: throw Exception("Failed to generate key")
        val newExpense = expense.copy(id = key)
        expensesRef.child(key).setValue(newExpense).await()
        return key
    }

    suspend fun updateExpense(expense: Expense) {
        if (expense.id.isNotEmpty()) {
            expensesRef.child(expense.id).setValue(expense).await()
        }
    }

    suspend fun deleteExpense(expense: Expense) {
        if (expense.id.isNotEmpty()) {
            expensesRef.child(expense.id).removeValue().await()
        }
    }

    suspend fun deleteAllExpensesForAccount(userId: String, accountId: String) {
        val snapshot = expensesRef
            .orderByChild("userId")
            .equalTo(userId)
            .get()
            .await()
        
        val updates = mutableMapOf<String, Any?>()
        for (child in snapshot.children) {
            val expense = child.getValue(Expense::class.java)
            if (expense != null && expense.accountId == accountId) {
                updates[child.key ?: continue] = null
            }
        }
        
        if (updates.isNotEmpty()) {
            expensesRef.updateChildren(updates).await()
        }
    }

    suspend fun getExpensesForUserAccount(userId: String, accountId: String): List<Expense> {
        val snapshot = expensesRef
            .orderByChild("userId")
            .equalTo(userId)
            .get()
            .await()
        
        val expenses = mutableListOf<Expense>()
        for (child in snapshot.children) {
            val expense = child.getValue(Expense::class.java)
            if (expense != null && expense.accountId == accountId) {
                expenses.add(expense)
            }
        }
        return expenses.sortedByDescending { it.date }
    }

    suspend fun getExpensesByCategory(userId: String, accountId: String, categoryId: String): List<Expense> {
        val snapshot = expensesRef
            .orderByChild("userId")
            .equalTo(userId)
            .get()
            .await()
        
        val expenses = mutableListOf<Expense>()
        for (child in snapshot.children) {
            val expense = child.getValue(Expense::class.java)
            if (expense != null && expense.accountId == accountId && expense.categoryId == categoryId) {
                expenses.add(expense)
            }
        }
        return expenses.sortedByDescending { it.date }
    }

    suspend fun getCategoryTotals(userId: String, accountId: String): List<CategoryTotal> {
        val expenses = getExpensesForUserAccount(userId, accountId)
        val totals = mutableMapOf<String, Double>()
        
        for (expense in expenses) {
            totals[expense.categoryId] = totals.getOrDefault(expense.categoryId, 0.0) + expense.amount
        }
        
        return totals.map { CategoryTotal(it.key, it.value) }
    }

    suspend fun getTotalExpenses(userId: String, accountId: String): Double {
        val expenses = getExpensesForUserAccount(userId, accountId)
        return expenses.sumOf { it.amount }
    }
}

// Helper class for totals
data class CategoryTotal(
    val categoryId: String,
    val total: Double
)
