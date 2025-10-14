package com.example.infinitynews

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.infinitynews.api.NewsResponse
import com.example.infinitynews.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesActivity : AppCompatActivity() {
    private lateinit var homeIcon: ImageView
    private lateinit var categoriesIcon: ImageView
    private lateinit var bookmarksIcon: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var generalImage: ImageView
    private lateinit var politicsImage: ImageView
    private lateinit var sportsImage: ImageView
    private lateinit var techImage: ImageView
    private lateinit var settingsIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

   //Bottom navigation buttons  and news category images
        homeIcon = findViewById(R.id.homeIcon)
        categoriesIcon = findViewById(R.id.categoriesIcon)
        bookmarksIcon = findViewById(R.id.bookmarksIcon)
        profileIcon = findViewById(R.id.profileIcon)
        generalImage = findViewById(R.id.generalImage)
        politicsImage = findViewById(R.id.politicsImage)
        sportsImage = findViewById(R.id.sportsImage)
        techImage = findViewById(R.id.techImage)
        settingsIcon = findViewById(R.id.settingsIcon)

        setupCategoryButtons()
        setupBottomNavigation()
        loadCategoryImages()
    }

    private fun loadCategoryImages() {

        // Loading the latest image for each category
        loadCategoryImage("general", generalImage)
        loadCategoryImage("politics", politicsImage)
        loadCategoryImage("sports", sportsImage)
        loadCategoryImage("technology", techImage)
    }

    private fun loadCategoryImage(category: String, imageView: ImageView) {
        android.util.Log.d("CategoriesActivity", "Loading image for category: $category")

        val call = RetrofitClient.apiService.getTopHeadlinesByCategory(
            country = "us",
            category = category,
            apiKey = RetrofitClient.API_KEY
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                android.util.Log.d("CategoriesActivity", "Response for $category: ${response.code()}")

                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        android.util.Log.d("CategoriesActivity", "$category articles count: ${newsResponse.articles.size}")

                        // Find first article with an image
                        val articleWithImage = newsResponse.articles.firstOrNull {
                            !it.urlToImage.isNullOrEmpty()
                        }

                        articleWithImage?.let { article ->
                            android.util.Log.d("CategoriesActivity", "$category image URL: ${article.urlToImage}")
                            android.util.Log.d("CategoriesActivity", "$category article title: ${article.title}")

                            runOnUiThread {
                                Glide.with(this@CategoriesActivity)
                                    .load(article.urlToImage)
                                    .placeholder(android.R.drawable.ic_dialog_info)
                                    .error(android.R.drawable.ic_dialog_alert)
                                    .into(imageView)
                            }
                        } ?: run {
                            android.util.Log.e("CategoriesActivity", "No image found for $category")
                        }
                    }
                } else {
                    android.util.Log.e("CategoriesActivity", "$category error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                android.util.Log.e("CategoriesActivity", "Failed to load $category image: ${t.message}")
            }
        })
    }

    //method used to call different categories using ID
    private fun setupCategoryButtons() {
        findViewById<Button>(R.id.generalButton).setOnClickListener {
            loadCategoryNews("general", "General")
        }

        findViewById<Button>(R.id.politicsButton).setOnClickListener {
            loadCategoryNews("politics", "Politics")
        }

        findViewById<Button>(R.id.sportsButton).setOnClickListener {
            loadCategoryNews("sports", "Sports")
        }

        findViewById<Button>(R.id.techButton).setOnClickListener {
            loadCategoryNews("technology", "Technology")
        }
    }

    private fun loadCategoryNews(category: String, categoryName: String) {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("selectedCategory", category)
            putString("selectedCategoryName", categoryName)
            apply()
        }

        Toast.makeText(this, "Loading $categoryName news...", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        homeIcon.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        categoriesIcon.setOnClickListener {

        }

        bookmarksIcon.setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
        }

        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        settingsIcon.setOnClickListener {
            startActivity(Intent(this, SideMenuActivity::class.java))
        }
    }
}