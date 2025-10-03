package com.example.budget_budgie_opsc

import androidx.room.*

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category)

    // Get categories for a specific account
    @Query("SELECT * FROM categories WHERE accountId = :accountId")
    suspend fun getCategoriesForAccount(accountId: Int): List<Category>

    // Get categories for a specific user
    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesForUser(userId: Int): List<Category>

    // Get categories for a specific account AND user
    @Query("SELECT * FROM categories WHERE accountId = :accountId AND userId = :userId")
    suspend fun getCategoriesForAccountAndUser(accountId: Int, userId: Int): List<Category>

    // Update category name and allocated budget
    @Query("UPDATE categories SET name = :name, allocatedAmount = :allocatedAmount WHERE id = :id")
    suspend fun updateNameAndBudget(id: Int, name: String, allocatedAmount: Double)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}