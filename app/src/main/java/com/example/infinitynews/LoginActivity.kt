package com.example.infinitynews

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var createAccountText: TextView

    // Make this internal so tests can access it
    internal val loginValidator = LoginValidator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        createAccountText = findViewById(R.id.createAccountText)

        loginButton.setOnClickListener {
            handleLogin()
        }

        createAccountText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    // Method to handle the login part
    private fun handleLogin() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        // Validate using LoginValidator
        val result = loginValidator.validateLogin(email, password)

        when (result) {
            is LoginResult.EmptyFields -> {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return
            }
            is LoginResult.InvalidEmail -> {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return
            }
            is LoginResult.InvalidPassword -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return
            }
            is LoginResult.Success -> {
                // Validation passed, now check credentials
                checkCredentials(email, password)
            }
        }
    }

    private fun checkCredentials(email: String, password: String) {
        // Simple validation - in real app, validate with backend
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val storedEmail = sharedPref.getString("email", "")
        val storedPassword = sharedPref.getString("password", "")

        if (email == storedEmail && password == storedPassword) {
            sharedPref.edit().putBoolean("isLoggedIn", true).apply()
            sharedPref.edit().putString("userEmail", email).apply()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Invalid credentials. Please sign up first!", Toast.LENGTH_SHORT).show()
        }
    }
}