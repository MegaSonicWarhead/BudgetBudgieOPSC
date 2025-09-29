package com.example.budget_budgie_opsc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = AppDatabase.getDatabase(this)

        val etNewUsername = findViewById<EditText>(R.id.etNewUsername)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val btnCreate = findViewById<Button>(R.id.btnCreateAccount)
        val btnUpdate = findViewById<Button>(R.id.btnUpdateCredentials)

        btnCreate.setOnClickListener {
            val username = etNewUsername.text.toString().trim()
            val password = etNewPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val existing = db.userDao().findByUsername(username)
                if (existing != null) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    db.userDao().insert(User(username = username, password = password))
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Account created", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        btnUpdate.setOnClickListener {
            val username = etNewUsername.text.toString().trim()
            val password = etNewPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and new password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val existing = db.userDao().findByUsername(username)
                if (existing == null) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    db.userDao().updatePassword(existing.id, password)
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Password updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }
}


