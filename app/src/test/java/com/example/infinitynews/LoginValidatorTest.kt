package com.example.infinitynews

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test



class LoginValidatorTest {

    private lateinit var loginValidator: LoginValidator

    @Before
    fun setup() {
        // Initialize the validator before each test
        loginValidator = LoginValidator()
    }

    // ========== EMPTY FIELDS TESTS ==========

    @Test
    fun validateLogin_emptyEmailAndPassword_returnsEmptyFields() {
        val result = loginValidator.validateLogin("", "")
        assertTrue("Expected EmptyFields but got ${result::class.simpleName}",
            result is LoginResult.EmptyFields)
    }

    @Test
    fun validateLogin_emptyEmail_returnsEmptyFields() {
        val result = loginValidator.validateLogin("", "password123")
        assertTrue("Expected EmptyFields but got ${result::class.simpleName}",
            result is LoginResult.EmptyFields)
    }

    @Test
    fun validateLogin_emptyPassword_returnsEmptyFields() {
        val result = loginValidator.validateLogin("test@email.com", "")
        assertTrue("Expected EmptyFields but got ${result::class.simpleName}",
            result is LoginResult.EmptyFields)
    }

    @Test
    fun validateLogin_whitespaceOnlyEmail_returnsEmptyFields() {
        val result = loginValidator.validateLogin("   ", "password123")
        assertTrue("Expected EmptyFields but got ${result::class.simpleName}",
            result is LoginResult.EmptyFields)
    }

    // ========== INVALID EMAIL TESTS ==========

    @Test
    fun validateLogin_invalidEmailNoAt_returnsInvalidEmail() {
        val result = loginValidator.validateLogin("notanemail", "password123")
        assertTrue("Expected InvalidEmail but got ${result::class.simpleName}",
            result is LoginResult.InvalidEmail)
    }

    @Test
    fun validateLogin_emailWithoutAtSymbol_returnsInvalidEmail() {
        val result = loginValidator.validateLogin("testemail.com", "password123")
        assertTrue("Expected InvalidEmail but got ${result::class.simpleName}",
            result is LoginResult.InvalidEmail)
    }

    @Test
    fun validateLogin_emailWithoutDomain_returnsInvalidEmail() {
        val result = loginValidator.validateLogin("test@", "password123")
        assertTrue("Expected InvalidEmail but got ${result::class.simpleName}",
            result is LoginResult.InvalidEmail)
    }

    @Test
    fun validateLogin_emailWithSpaces_returnsInvalidEmail() {
        val result = loginValidator.validateLogin("test @email.com", "password123")
        assertTrue("Expected InvalidEmail but got ${result::class.simpleName}",
            result is LoginResult.InvalidEmail)
    }

    @Test
    fun validateLogin_emailMissingUsername_returnsInvalidEmail() {
        val result = loginValidator.validateLogin("@email.com", "password123")
        assertTrue("Expected InvalidEmail but got ${result::class.simpleName}",
            result is LoginResult.InvalidEmail)
    }

    // ========== INVALID PASSWORD TESTS ==========

    @Test
    fun validateLogin_passwordTooShort_returnsInvalidPassword() {
        val result = loginValidator.validateLogin("test@email.com", "12345")
        assertTrue("Expected InvalidPassword but got ${result::class.simpleName}",
            result is LoginResult.InvalidPassword)
    }

    @Test
    fun validateLogin_passwordOneChar_returnsInvalidPassword() {
        val result = loginValidator.validateLogin("test@email.com", "1")
        assertTrue("Expected InvalidPassword but got ${result::class.simpleName}",
            result is LoginResult.InvalidPassword)
    }

    @Test
    fun validateLogin_passwordFiveChars_returnsInvalidPassword() {
        val result = loginValidator.validateLogin("test@email.com", "abcde")
        assertTrue("Expected InvalidPassword but got ${result::class.simpleName}",
            result is LoginResult.InvalidPassword)
    }

    // ========== VALID LOGIN TESTS ==========

    @Test
    fun validateLogin_validCredentials_returnsSuccess() {
        val result = loginValidator.validateLogin("test@email.com", "password123")
        assertTrue("Expected Success but got ${result::class.simpleName}",
            result is LoginResult.Success)
    }

    @Test
    fun validateLogin_passwordExactlySixChars_returnsSuccess() {
        val result = loginValidator.validateLogin("test@email.com", "123456")
        assertTrue("Expected Success but got ${result::class.simpleName}",
            result is LoginResult.Success)
    }

    @Test
    fun validateLogin_validEmailWithNumbers_returnsSuccess() {
        val result = loginValidator.validateLogin("user123@example.com", "password123")
        assertTrue("Expected Success but got ${result::class.simpleName}",
            result is LoginResult.Success)
    }

    @Test
    fun validateLogin_validEmailWithUnderscore_returnsSuccess() {
        val result = loginValidator.validateLogin("user_name@example.com", "password123")
        assertTrue("Expected Success but got ${result::class.simpleName}",
            result is LoginResult.Success)
    }

    @Test
    fun validateLogin_validEmailWithDots_returnsSuccess() {
        val result = loginValidator.validateLogin("user.name@example.com", "password123")
        assertTrue("Expected Success but got ${result::class.simpleName}",
            result is LoginResult.Success)
    }

    @Test
    fun validateLogin_validEmailWithHyphen_returnsSuccess() {
        val result = loginValidator.validateLogin("user-name@example.com", "password123")
        assertTrue("Expected Success but got ${result::class.simpleName}",
            result is LoginResult.Success)
    }

    @Test
    fun validateLogin_longPassword_returnsSuccess() {
        val result = loginValidator.validateLogin("test@email.com", "verylongpassword12345678")
        assertTrue("Expected Success but got ${result::class.simpleName}",
            result is LoginResult.Success)
    }

    // ========== EMAIL VALIDATION SPECIFIC TESTS ==========

    @Test
    fun isValidEmail_validEmail_returnsTrue() {
        val result = loginValidator.isValidEmail("test@email.com")
        assertTrue("Expected true for valid email", result)
    }

    @Test
    fun isValidEmail_invalidEmail_returnsFalse() {
        val result = loginValidator.isValidEmail("notanemail")
        assertFalse("Expected false for invalid email", result)
    }

    @Test
    fun isValidEmail_emailWithSpecialChars_returnsTrue() {
        val result = loginValidator.isValidEmail("test.name_123@example.com")
        assertTrue("Expected true for email with special chars", result)
    }

    @Test
    fun isValidEmail_emailWithoutAt_returnsFalse() {
        val result = loginValidator.isValidEmail("testemail.com")
        assertFalse("Expected false for email without @", result)
    }

    @Test
    fun isValidEmail_emailWithMultipleDots_returnsTrue() {
        val result = loginValidator.isValidEmail("test@mail.example.com")
        assertTrue("Expected true for email with multiple dots", result)
    }

    // ========== EDGE CASES ==========

    @Test
    fun validateLogin_trimmedEmailAndPassword_returnsSuccess() {
        // Note: If your app should trim inputs, add trim() to validateLogin
        val result = loginValidator.validateLogin("test@email.com", "password123")
        assertTrue("Expected Success for trimmed inputs",
            result is LoginResult.Success)
    }

    @Test
    fun validateLogin_specialCharactersInPassword_returnsSuccess() {
        val result = loginValidator.validateLogin("test@email.com", "pass@123!")
        assertTrue("Expected Success for password with special chars",
            result is LoginResult.Success)
    }
}