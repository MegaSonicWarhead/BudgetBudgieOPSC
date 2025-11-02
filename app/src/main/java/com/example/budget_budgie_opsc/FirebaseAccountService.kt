package com.example.budget_budgie_opsc

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseAccountService {
    private val database = Firebase.database
    private val accountsRef = database.getReference("accounts")

    suspend fun insert(account: Account): String {
        val key = accountsRef.push().key ?: throw Exception("Failed to generate key")
        val newAccount = account.copy(id = key)
        accountsRef.child(key).setValue(newAccount).await()
        return key
    }

    suspend fun getAllAccounts(): List<Account> {
        val snapshot = accountsRef.get().await()
        val accounts = mutableListOf<Account>()
        for (child in snapshot.children) {
            val account = child.getValue(Account::class.java)
            if (account != null) {
                accounts.add(account)
            }
        }
        return accounts
    }

    suspend fun getAccountsForUser(userId: String): List<Account> {
        val snapshot = accountsRef.orderByChild("userId").equalTo(userId).get().await()
        val accounts = mutableListOf<Account>()
        for (child in snapshot.children) {
            val account = child.getValue(Account::class.java)
            if (account != null) {
                accounts.add(account)
            }
        }
        return accounts
    }

    suspend fun getAccountById(id: String): Account? {
        val snapshot = accountsRef.child(id).get().await()
        return snapshot.getValue(Account::class.java)
    }

    suspend fun delete(account: Account) {
        if (account.id.isNotEmpty()) {
            accountsRef.child(account.id).removeValue().await()
        }
    }

    suspend fun updateBalance(id: String, balance: Double) {
        accountsRef.child(id).child("balance").setValue(balance).await()
    }

    suspend fun updateBudgets(id: String, minBudget: Double, maxBudget: Double) {
        accountsRef.child(id).child("minBudget").setValue(minBudget).await()
        accountsRef.child(id).child("maxBudget").setValue(maxBudget).await()
    }

    suspend fun updateName(id: String, name: String) {
        accountsRef.child(id).child("name").setValue(name).await()
    }
}
