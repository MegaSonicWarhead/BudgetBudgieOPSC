package com.example.budget_budgie_opsc

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Category(
    val id: String = "",
    val userId: String = "",
    val accountId: String = "",   // 🔑 Link category to an account
    val name: String = "",
    val allocatedAmount: Double = 0.0
)