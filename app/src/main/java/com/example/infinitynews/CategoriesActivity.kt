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

        // Clear any cached category
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        sharedPref.edit().remove("selectedCategory").apply()

        // Bottom navigation buttons and news category images
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
        android.util.Log.d("CategoriesActivity", "========================================")
        android.util.Log.d("CategoriesActivity", "LOADING CATEGORY IMAGES")
        android.util.Log.d("CategoriesActivity", "========================================")

        // Loading the latest image for each category
        loadGeneralImage()  // Special handling for general
        loadPoliticsImage() // Special handling for politics
        loadCategoryImage("sports", sportsImage)
        loadCategoryImage("technology", techImage)
    }

    // Special handler for General (no category parameter)
    private fun loadGeneralImage() {
        android.util.Log.d("CategoriesActivity", "Loading image for GENERAL")

        val call = RetrofitClient.apiService.getTopHeadlines(
            country = "us",
            apiKey = RetrofitClient.API_KEY
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                android.util.Log.d("CategoriesActivity", "General Response: ${response.code()}")
                android.util.Log.d("CategoriesActivity", "General URL: ${call.request().url}")

                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        android.util.Log.d("CategoriesActivity", "General articles count: ${newsResponse.articles.size}")

                        // Log first article title
                        newsResponse.articles.firstOrNull()?.let { article ->
                            android.util.Log.d("CategoriesActivity", "General Article 1: ${article.title}")
                        }

                        // Find first article with an image
                        val articleWithImage = newsResponse.articles.firstOrNull {
                            !it.urlToImage.isNullOrEmpty()
                        }

                        articleWithImage?.let { article ->
                            android.util.Log.d("CategoriesActivity", "General image from: ${article.title}")

                            runOnUiThread {
                                Glide.with(this@CategoriesActivity)
                                    .load(article.urlToImage)
                                    .placeholder(android.R.drawable.ic_dialog_info)
                                    .error(android.R.drawable.ic_dialog_alert)
                                    .into(generalImage)
                            }
                        } ?: run {
                            android.util.Log.e("CategoriesActivity", "No image found for General")
                        }
                    }
                } else {
                    android.util.Log.e("CategoriesActivity", "General error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                android.util.Log.e("CategoriesActivity", "Failed to load general image: ${t.message}", t)
            }
        })
    }

    // Special handler for Politics (uses /everything)
    private fun loadPoliticsImage() {
        android.util.Log.d("CategoriesActivity", "Loading image for POLITICS")

        val call = RetrofitClient.apiService.getPoliticsNews(
            query = "politics OR government OR election OR congress OR senate",
            language = "en",
            sortBy = "publishedAt",
            apiKey = RetrofitClient.API_KEY
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                android.util.Log.d("CategoriesActivity", "Politics Response: ${response.code()}")
                android.util.Log.d("CategoriesActivity", "Politics URL: ${call.request().url}")

                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        android.util.Log.d("CategoriesActivity", "Politics articles count: ${newsResponse.articles.size}")

                        // Log first article title
                        newsResponse.articles.firstOrNull()?.let { article ->
                            android.util.Log.d("CategoriesActivity", "Politics Article 1: ${article.title}")
                        }

                        // Find first article with an image
                        val articleWithImage = newsResponse.articles.firstOrNull {
                            !it.urlToImage.isNullOrEmpty()
                        }

                        articleWithImage?.let { article ->
                            android.util.Log.d("CategoriesActivity", "Politics image from: ${article.title}")

                            runOnUiThread {
                                Glide.with(this@CategoriesActivity)
                                    .load(article.urlToImage)
                                    .placeholder(android.R.drawable.ic_dialog_info)
                                    .error(android.R.drawable.ic_dialog_alert)
                                    .into(politicsImage)
                            }
                        } ?: run {
                            android.util.Log.e("CategoriesActivity", "No image found for Politics")
                        }
                    }
                } else {
                    android.util.Log.e("CategoriesActivity", "Politics error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                android.util.Log.e("CategoriesActivity", "Failed to load politics image: ${t.message}", t)
            }
        })
    }

    // Regular categories (sports, tech)
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
                android.util.Log.d("CategoriesActivity", "URL: ${call.request().url}")

                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        android.util.Log.d("CategoriesActivity", "$category articles count: ${newsResponse.articles.size}")

                        // Log first article title
                        newsResponse.articles.firstOrNull()?.let { article ->
                            android.util.Log.d("CategoriesActivity", "$category Article 1: ${article.title}")
                        }

                        // Find first article with an image
                        val articleWithImage = newsResponse.articles.firstOrNull {
                            !it.urlToImage.isNullOrEmpty()
                        }

                        articleWithImage?.let { article ->
                            android.util.Log.d("CategoriesActivity", "$category image from: ${article.title}")

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
                    android.util.Log.e("CategoriesActivity", "$category error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                android.util.Log.e("CategoriesActivity", "Failed to load $category image: ${t.message}", t)
            }
        })
    }

    private fun setupCategoryButtons() {
        findViewById<Button>(R.id.generalButton).setOnClickListener {
            android.util.Log.d("CategoriesActivity", "User clicked GENERAL button")
            loadCategoryNews("general", "General")
        }

        findViewById<Button>(R.id.politicsButton).setOnClickListener {
            android.util.Log.d("CategoriesActivity", "User clicked POLITICS button")
            loadCategoryNews("politics", "Politics")
        }

        findViewById<Button>(R.id.sportsButton).setOnClickListener {
            android.util.Log.d("CategoriesActivity", "User clicked SPORTS button")
            loadCategoryNews("sports", "Sports")
        }

        findViewById<Button>(R.id.techButton).setOnClickListener {
            android.util.Log.d("CategoriesActivity", "User clicked TECHNOLOGY button")
            loadCategoryNews("technology", "Technology")
        }
    }

    private fun loadCategoryNews(category: String, categoryName: String) {
        android.util.Log.d("CategoriesActivity", "========================================")
        android.util.Log.d("CategoriesActivity", "NAVIGATING TO: $categoryName ($category)")
        android.util.Log.d("CategoriesActivity", "========================================")

        Toast.makeText(this, "Loading $categoryName news...", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("category", category)
        intent.putExtra("categoryName", categoryName)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        homeIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        categoriesIcon.setOnClickListener {
            Toast.makeText(this, "Already viewing categories", Toast.LENGTH_SHORT).show()
        }

        bookmarksIcon.setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
            finish()
        }

        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        settingsIcon.setOnClickListener {
            startActivity(Intent(this, SideMenuActivity::class.java))
        }
    }
}