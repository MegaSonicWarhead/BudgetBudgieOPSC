package com.example.budget_budgie_opsc
import androidx.room.*

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAccountsForUser(userId: Int): List<Account>

    @Delete
    suspend fun delete(account: Account)
}