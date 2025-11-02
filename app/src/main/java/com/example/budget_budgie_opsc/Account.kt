package com.example.budget_budgie_opsc

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Account(
    val id: String = "",
    val userId: String = "",          // The owner of this account
    val name: String = "",         // e.g. "Savings", "Credit Card"
    val balance: Double = 0.0,      // Current account balance
    val minBudget: Double = 0.0,  // Lower spending limit (optional)
    val maxBudget: Double = 0.0   // Upper spending limit (optional)
)