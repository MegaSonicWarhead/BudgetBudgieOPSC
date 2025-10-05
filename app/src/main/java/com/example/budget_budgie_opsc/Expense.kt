package com.example.budget_budgie_opsc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(index = true) val userId: Int,
    @ColumnInfo(index = true) val accountId: Int,
    @ColumnInfo(index = true) val categoryId: Int,

    val description: String,
    val amount: Double,
    val date: Long,            // store timestamp (System.currentTimeMillis)
    val receiptUri: String?    // optional photo path
)