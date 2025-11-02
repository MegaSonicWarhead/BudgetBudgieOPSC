package com.example.budget_budgie_opsc

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseCategoryService {
    private val database = Firebase.database
    private val categoriesRef = database.getReference("categories")

    suspend fun insert(category: Category): String {
        val key = categoriesRef.push().key ?: throw Exception("Failed to generate key")
        val newCategory = category.copy(id = key)
        categoriesRef.child(key).setValue(newCategory).await()
        return key
    }

    suspend fun getCategoriesForAccount(accountId: String): List<Category> {
        val snapshot = categoriesRef.orderByChild("accountId").equalTo(accountId).get().await()
        val categories = mutableListOf<Category>()
        for (child in snapshot.children) {
            val category = child.getValue(Category::class.java)
            if (category != null) {
                categories.add(category)
            }
        }
        return categories
    }

    suspend fun getCategoriesForUser(userId: String): List<Category> {
        val snapshot = categoriesRef.orderByChild("userId").equalTo(userId).get().await()
        val categories = mutableListOf<Category>()
        for (child in snapshot.children) {
            val category = child.getValue(Category::class.java)
            if (category != null) {
                categories.add(category)
            }
        }
        return categories
    }

    suspend fun getCategoriesForAccountAndUser(accountId: String, userId: String): List<Category> {
        val snapshot = categoriesRef
            .orderByChild("accountId")
            .equalTo(accountId)
            .get()
            .await()
        
        val categories = mutableListOf<Category>()
        for (child in snapshot.children) {
            val category = child.getValue(Category::class.java)
            if (category != null && category.userId == userId) {
                categories.add(category)
            }
        }
        return categories
    }

    suspend fun updateNameAndBudget(id: String, name: String, allocatedAmount: Double) {
        categoriesRef.child(id).child("name").setValue(name).await()
        categoriesRef.child(id).child("allocatedAmount").setValue(allocatedAmount).await()
    }

    suspend fun update(category: Category) {
        if (category.id.isNotEmpty()) {
            categoriesRef.child(category.id).setValue(category).await()
        }
    }

    suspend fun delete(category: Category) {
        if (category.id.isNotEmpty()) {
            categoriesRef.child(category.id).removeValue().await()
        }
    }
}
