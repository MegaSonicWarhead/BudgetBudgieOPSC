package com.example.budget_budgie_opsc

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import app.rive.runtime.kotlin.core.Rive
import com.example.budget_budgie_opsc.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class login : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setContentView(R.layout.activity_login)
        //Rive Stuff
        //Ivan Strings are in Values
        Rive.init(this)
        //binding.loginCharacter.controller.setBooleanState("Main", "isIdle", false) (Example of Swapping Conditions

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvError = findViewById<TextView>(R.id.tvError)

        // Ensure fields start empty and avoid overlaying previous text
        etUsername.text?.clear()
        etPassword.text?.clear()

        // Use default IME backspace handling; no custom deletion to avoid rendering artifacts

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            lifecycleScope.launch {
                val user = FirebaseServiceManager.userService.login(username, password)
                if (user != null) {
                    // Save userId to SharedPreferences for persistence
                    val prefs = getSharedPreferences("BudgetBudgiePrefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putString("USER_ID", user.id).apply()
                    
                    val intent = Intent(this@login, activity_account::class.java)
                    intent.putExtra("USER_ID", user.id)
                    startActivity(intent)
                    finish()
                } else {
                    runOnUiThread {
                        tvError.text = "Password is Incorrect"
                        tvError.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this@login, RegisterActivity::class.java))
        }
    }
}