package com.example.budget_budgie_opsc

import androidx.room.*

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    // ✅ Get all accounts for debugging or admin views
    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Account>

    // ✅ Get accounts belonging to a specific user
    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAccountsForUser(userId: Int): List<Account>

    // ✅ Get a single account by ID
    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Int): Account?

    // ✅ Delete account
    @Delete
    suspend fun delete(account: Account)

    // ✅ Update account balance (so budgets can change when categories are assigned/spent)
    @Query("UPDATE accounts SET balance = :balance WHERE id = :id")
    suspend fun updateBalance(id: Int, balance: Double)

    // ✅ Update account min & max budget
    @Query("UPDATE accounts SET minBudget = :minBudget, maxBudget = :maxBudget WHERE id = :id")
    suspend fun updateBudgets(id: Int, minBudget: Double, maxBudget: Double)

    // ✅ Update account name
    @Query("UPDATE accounts SET name = :name WHERE id = :id")
    suspend fun updateName(id: Int, name: String)
}