package com.example.infinitynews

// This class represents the different possible results of login validation
sealed class LoginResult {
    object Success : LoginResult()
    object EmptyFields : LoginResult()
    object InvalidEmail : LoginResult()
    object InvalidPassword : LoginResult()
}

// This class handles validating the email and password input for login
class LoginValidator {

    fun validateLogin(email: String, password: String): LoginResult {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        // Check for empty fields
        if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
            return LoginResult.EmptyFields
        }

        // Check for valid email format
        if (!isValidEmail(trimmedEmail)) {
            return LoginResult.InvalidEmail
        }

        // Check for password length
        if (trimmedPassword.length < 6) {
            return LoginResult.InvalidPassword
        }

        // Everything is valid
        return LoginResult.Success
    }

    // Helper function to check email format
    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(Regex(emailPattern))
    }
}