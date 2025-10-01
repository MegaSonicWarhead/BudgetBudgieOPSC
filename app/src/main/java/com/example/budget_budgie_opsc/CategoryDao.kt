package com.example.budget_budgie_opsc
import androidx.room.*

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesForUser(userId: Int): List<Category>

    @Query("UPDATE categories SET name = :name, budget = :budget WHERE id = :id")
    suspend fun updateNameAndBudget(id: Int, name: String, budget: Double)

    @Delete
    suspend fun delete(category: Category)
}