package com.example.budget_budgie_opsc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int,   // 🔑 Link category to an account
    val name: String,
    val allocatedAmount: Double
)