package com.example.budget_budgie_opsc

import androidx.room.*

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category)

    @Query("SELECT * FROM categories WHERE accountId = :accountId")
    suspend fun getCategoriesForAccount(accountId: Int): List<Category>

    @Query("UPDATE categories SET name = :name, allocatedAmount = :allocatedAmount WHERE id = :id")
    suspend fun updateNameAndBudget(id: Int, name: String, allocatedAmount: Double)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}