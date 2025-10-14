package com.example.infinitynews

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SideMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_side_menu)

        setupBottomNavigation()
        setupMenuItems()
    }

    private fun setupBottomNavigation() {
        // Home Icon
        findViewById<ImageView>(R.id.homeIcon).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Categories Icon
        findViewById<ImageView>(R.id.categoriesIcon).setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
            finish()
        }

        // Bookmarks Icon
        findViewById<ImageView>(R.id.bookmarksIcon).setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
            finish()
        }

        // Profile Icon
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        // Settings Icon 
        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            Toast.makeText(this, "You're already on Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMenuItems() {
        // Tech Menu
        findViewById<LinearLayout>(R.id.menuTech).setOnClickListener {
            Toast.makeText(this, "Tech Category Selected", Toast.LENGTH_SHORT).show()
            // Navigate back to home with tech category
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("category", "technology")
            startActivity(intent)
            finish()
        }

        // General Menu
        findViewById<LinearLayout>(R.id.menuGeneral).setOnClickListener {
            Toast.makeText(this, "General Category Selected", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("category", "general")
            startActivity(intent)
            finish()
        }

        // Politics Menu
        findViewById<LinearLayout>(R.id.menuPolitics).setOnClickListener {
            Toast.makeText(this, "Politics Category Selected", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("category", "politics")
            startActivity(intent)
            finish()
        }

        // Sports Menu
        findViewById<LinearLayout>(R.id.menuSports).setOnClickListener {
            Toast.makeText(this, "Sports Category Selected", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("category", "sports")
            startActivity(intent)
            finish()
        }

        // Settings Menu
        findViewById<LinearLayout>(R.id.menuSettings).setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()

        }

        // About Us Menu
        findViewById<LinearLayout>(R.id.menuAboutUs).setOnClickListener {
            Toast.makeText(this, "About Us", Toast.LENGTH_SHORT).show()

        }

        // Privacy Policy Menu
        findViewById<LinearLayout>(R.id.menuPrivacyPolicy).setOnClickListener {
            Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show()

        }
    }
}
