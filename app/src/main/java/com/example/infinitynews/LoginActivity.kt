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

    //A method to handle the login part

    private fun handleLogin() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Simple validation in real app, validate with backend
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val storedEmail = sharedPref.getString("email", "")
        val storedPassword = sharedPref.getString("password", "")

        if (email == storedEmail && password == storedPassword) {
            sharedPref.edit().putBoolean("isLoggedIn", true).apply()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Invalid credentials. Please sign up first!", Toast.LENGTH_SHORT).show()
        }
    }
}