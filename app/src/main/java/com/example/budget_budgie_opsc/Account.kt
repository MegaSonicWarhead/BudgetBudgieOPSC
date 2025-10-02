package com.example.budget_budgie_opsc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val userId: Int,          // The owner of this account
    val name: String,         // e.g. "Savings", "Credit Card"
    val balance: Double,      // Current account balance

    val minBudget: Double = 0.0,  // Lower spending limit (optional)
    val maxBudget: Double = 0.0   // Upper spending limit (optional)
)