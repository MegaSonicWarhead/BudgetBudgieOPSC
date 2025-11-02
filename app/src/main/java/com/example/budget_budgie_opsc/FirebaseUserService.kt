package com.example.budget_budgie_opsc

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseUserService {
    private val database = Firebase.database
    private val usersRef = database.getReference("users")

    suspend fun insert(user: User): String {
        val key = usersRef.push().key ?: throw Exception("Failed to generate key")
        val newUser = user.copy(id = key)
        usersRef.child(key).setValue(newUser).await()
        return key
    }

    suspend fun login(username: String, password: String): User? {
        val snapshot = usersRef.get().await()
        for (child in snapshot.children) {
            val user = child.getValue(User::class.java)
            if (user != null && user.username == username && user.password == password) {
                return user
            }
        }
        return null
    }

    suspend fun findByUsername(username: String): User? {
        val snapshot = usersRef.get().await()
        for (child in snapshot.children) {
            val user = child.getValue(User::class.java)
            if (user != null && user.username == username) {
                return user
            }
        }
        return null
    }

    suspend fun updatePassword(userId: String, newPassword: String) {
        usersRef.child(userId).child("password").setValue(newPassword).await()
    }

    suspend fun updateUsername(userId: String, newUsername: String) {
        usersRef.child(userId).child("username").setValue(newUsername).await()
    }
}
