package com.example.budget_budgie_opsc

import androidx.room.*

@Dao
interface ExpenseDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    // Update
    @Update
    suspend fun updateExpense(expense: Expense)

    // Delete
    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Delete all expenses for a specific account (optional utility)
    @Query("DELETE FROM expenses WHERE userId = :userId AND accountId = :accountId")
    suspend fun deleteAllExpensesForAccount(userId: Int, accountId: Int)

    // Queries
    @Query("SELECT * FROM expenses WHERE userId = :userId AND accountId = :accountId ORDER BY date DESC")
    suspend fun getExpensesForUserAccount(userId: Int, accountId: Int): List<Expense>

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId AND accountId = :accountId AND categoryId = :categoryId
        ORDER BY date DESC
    """)
    suspend fun getExpensesByCategory(userId: Int, accountId: Int, categoryId: Int): List<Expense>

    // Optional: Get total spent per category
    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses
        WHERE userId = :userId AND accountId = :accountId
        GROUP BY categoryId
    """)
    suspend fun getCategoryTotals(userId: Int, accountId: Int): List<CategoryTotal>

    // Optional: Get total spent overall
    @Query("""
        SELECT SUM(amount) 
        FROM expenses
        WHERE userId = :userId AND accountId = :accountId
    """)
    suspend fun getTotalExpenses(userId: Int, accountId: Int): Double?
}

// Helper class for totals
data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)