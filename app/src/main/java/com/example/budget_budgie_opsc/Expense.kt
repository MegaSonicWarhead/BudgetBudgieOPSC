package com.example.budget_budgie_opsc

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Expense(
    val id: String = "",
    val userId: String = "",
    val accountId: String = "",
    val categoryId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: Long = 0L,            // store timestamp (System.currentTimeMillis)
    val receiptUri: String? = null    // optional photo path
)