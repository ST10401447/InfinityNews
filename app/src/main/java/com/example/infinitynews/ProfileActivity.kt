package com.example.infinitynews

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var languageDropdown: AutoCompleteTextView
    private lateinit var categoryDropdown: AutoCompleteTextView
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button
    private lateinit var homeIcon: ImageView
    private lateinit var categoriesIcon: ImageView
    private lateinit var bookmarksIcon: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var settingsIcon: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        languageDropdown = findViewById(R.id.languageDropdown)
        categoryDropdown = findViewById(R.id.categoryDropdown)
        saveButton = findViewById(R.id.saveButton)
        logoutButton = findViewById(R.id.logoutButton)
        homeIcon = findViewById(R.id.homeIcon)
        categoriesIcon = findViewById(R.id.categoriesIcon)
        bookmarksIcon = findViewById(R.id.bookmarksIcon)
        profileIcon = findViewById(R.id.profileIcon)
        settingsIcon = findViewById(R.id.settingsIcon)

        setupDropdowns()
        loadUserData()
        setupSaveButton()
        setupLogoutButton()
        setupBottomNavigation()
    }

    private fun setupDropdowns() {
        // Language dropdown with more options
        val languages = arrayOf(
            "English",
            "Afrikaans",
            "Sesotho",
            "Tsonga",
            "Venda",
        )
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        languageDropdown.setAdapter(languageAdapter)
        languageDropdown.threshold = 1 // Show dropdown after 1 character

        // Make it show dropdown when clicked
        languageDropdown.setOnClickListener {
            languageDropdown.showDropDown()
        }
        languageDropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                languageDropdown.showDropDown()
            }
        }

        // Category dropdown with news categories
        val categories = arrayOf(
            "General",
            "Technology",
            "Sports",
            "Politics"
        )
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(categoryAdapter)
        categoryDropdown.threshold = 1

        // Make it show dropdown when clicked
        categoryDropdown.setOnClickListener {
            categoryDropdown.showDropDown()
        }
        categoryDropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                categoryDropdown.showDropDown()
            }
        }
    }

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        emailInput.setText(sharedPref.getString("email", ""))
        nameInput.setText(sharedPref.getString("name", ""))
        languageDropdown.setText(sharedPref.getString("language", "English"), false)
        categoryDropdown.setText(sharedPref.getString("favoriteCategory", "General"), false)
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val language = languageDropdown.text.toString().trim()
            val category = categoryDropdown.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                nameInput.requestFocus()
                return@setOnClickListener
            }

            if (language.isEmpty()) {
                Toast.makeText(this, "Please select a language preference", Toast.LENGTH_SHORT).show()
                languageDropdown.requestFocus()
                return@setOnClickListener
            }
            if(language.equals("Sesotho")){
                setLocale(this, "nso")
            }
            else if(language.equals("Venda")) {
                setLocale(this, "ve")
            }
            else if(language.equals("Afrikaans")){
                setLocale(this, "af")
            }
            else if(language.equals("English")){
                setLocale(this, "nr")
            }
            else if(language.equals("Tsonga")){
                setLocale(this, "ts")
            }


            if (category.isEmpty()) {
                Toast.makeText(this, "Please select a favorite category", Toast.LENGTH_SHORT).show()
                categoryDropdown.requestFocus()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
            sharedPref.edit().apply {
                putString("name", name)
                putString("language", language)
                putString("favoriteCategory", category)
                apply()
            }
            Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
        }
    }
      //Method to  for the user to log out if they had signed in
    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
            sharedPref.edit().apply {
                putBoolean("isLoggedIn", false)
                apply()
            }

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        homeIcon.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        categoriesIcon.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        bookmarksIcon.setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
        }

        profileIcon.setOnClickListener {
            // Already on profile
        }

        settingsIcon.setOnClickListener {
            startActivity(Intent(this, SideMenuActivity::class.java))
        }
    }
    fun setLocale(activity: Activity, language: String) {

        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config,resources.displayMetrics)

        startActivity(Intent(activity,HomeActivity::class.java))
        finish()
    }
}