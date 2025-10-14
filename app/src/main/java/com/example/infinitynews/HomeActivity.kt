package com.example.infinitynews

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinitynews.adapters.NewsAdapter
import com.example.infinitynews.api.NewsResponse
import com.example.infinitynews.api.RetrofitClient
import com.example.infinitynews.models.News
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var searchInput: EditText
    private lateinit var categoriesButton: Button
    private lateinit var bookmarksButton: Button
    private lateinit var profileIconTop: ImageView
    private lateinit var notificationIcon: ImageView
    private lateinit var notificationBadge: View
    private lateinit var homeIcon: ImageView
    private lateinit var categoriesIcon: ImageView
    private lateinit var bookmarksIcon: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var settingsIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Views
        newsRecyclerView = findViewById(R.id.newsRecyclerView)
        searchInput = findViewById(R.id.searchInput)
        categoriesButton = findViewById(R.id.categoriesButton)
        bookmarksButton = findViewById(R.id.bookmarksButton)
        profileIconTop = findViewById(R.id.profileIconTop)
        notificationIcon = findViewById(R.id.notificationIcon)
        notificationBadge = findViewById(R.id.notificationBadge)
        homeIcon = findViewById(R.id.homeIcon)
        categoriesIcon = findViewById(R.id.categoriesIcon)
        bookmarksIcon = findViewById(R.id.bookmarksIcon)
        profileIcon = findViewById(R.id.profileIcon)
        settingsIcon = findViewById(R.id.settingsIcon)

        setupRecyclerView()

        // Load news
        val category = intent.getStringExtra("category")
        if (category != null) {
            loadNewsByCategory(category)
        } else {
            loadTopHeadlines()
        }

        setupSearch()
        setupButtons()
        checkNotifications()
        setupNotificationClick()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter { news, isBookmarked ->
            handleBookmark(news, isBookmarked)
        }
        newsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        newsRecyclerView.adapter = newsAdapter
    }

    private fun loadTopHeadlines() {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val call = RetrofitClient.apiService.getTopHeadlines(
            country = "us",
            apiKey = RetrofitClient.API_KEY
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarks = sharedPref.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
                    val newsItems = response.body()!!.articles.mapNotNull { article ->
                        val url = article.url ?: return@mapNotNull null
                        News(
                            id = url,
                            title = article.title ?: "No Title",
                            imageUrl = article.urlToImage ?: "",
                            category = "General",
                            isBookmarked = bookmarks.contains(url),
                            url = url
                        )
                    }
                    newsAdapter.updateNews(newsItems)
                } else {
                    Toast.makeText(this@HomeActivity, "API Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadNewsByCategory(category: String) {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val call = RetrofitClient.apiService.getTopHeadlinesByCategory(
            country = "us",
            category = category,
            apiKey = RetrofitClient.API_KEY
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarks = sharedPref.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
                    val newsItems = response.body()!!.articles.mapNotNull { article ->
                        val url = article.url ?: return@mapNotNull null
                        News(
                            id = url,
                            title = article.title ?: "No Title",
                            imageUrl = article.urlToImage ?: "",
                            category = category,
                            isBookmarked = bookmarks.contains(url),
                            url = url
                        )
                    }
                    newsAdapter.updateNews(newsItems)
                } else {
                    Toast.makeText(this@HomeActivity, "API Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleBookmark(news: News, isBookmarked: Boolean) {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        val bookmarks = sharedPref.getStringSet("bookmarks", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (isBookmarked) {
            bookmarks.add(news.id)
            editor.putString("bookmark_${news.id}_title", news.title)
            editor.putString("bookmark_${news.id}_image", news.imageUrl)
            editor.putString("bookmark_${news.id}_category", news.category)
            editor.putString("bookmark_${news.id}_url", news.url)
        } else {
            bookmarks.remove(news.id)
            editor.remove("bookmark_${news.id}_title")
            editor.remove("bookmark_${news.id}_image")
            editor.remove("bookmark_${news.id}_category")
            editor.remove("bookmark_${news.id}_url")
        }

        editor.putStringSet("bookmarks", bookmarks)
        editor.apply()
    }

    private fun setupSearch() {
        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchNews(query)
            }
            true
        }
    }

    private fun searchNews(query: String) {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val call = RetrofitClient.apiService.searchNews(
            query = query,
            apiKey = RetrofitClient.API_KEY
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarks = sharedPref.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
                    val newsItems = response.body()!!.articles.mapNotNull { article ->
                        val url = article.url ?: return@mapNotNull null
                        News(
                            id = url,
                            title = article.title ?: "No Title",
                            imageUrl = article.urlToImage ?: "",
                            category = article.source?.name ?: "General",
                            isBookmarked = bookmarks.contains(url),
                            url = url
                        )
                    }
                    newsAdapter.updateNews(newsItems)
                } else {
                    Toast.makeText(this@HomeActivity, "Search failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupButtons() {
        categoriesButton.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        bookmarksButton.setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
        }
    }

    private fun checkNotifications() {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val hasNotifications = sharedPref.getBoolean("hasNotifications", true)
        notificationBadge.visibility = if (hasNotifications) View.VISIBLE else View.GONE
    }

    private fun setupNotificationClick() {
        notificationIcon.setOnClickListener {
            notificationBadge.visibility = View.GONE
            val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
            sharedPref.edit().putBoolean("hasNotifications", false).apply()
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        profileIconTop.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        homeIcon.setOnClickListener { loadTopHeadlines() }
        categoriesIcon.setOnClickListener { startActivity(Intent(this, CategoriesActivity::class.java)) }
        bookmarksIcon.setOnClickListener { startActivity(Intent(this, BookmarksActivity::class.java)) }
        profileIcon.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        settingsIcon.setOnClickListener { startActivity(Intent(this, SideMenuActivity::class.java)) }
    }

    // Convert language name to NewsAPI code
    private fun getLanguageCode(language: String): String {
        return when(language) {
            "English" -> "en"
            "Afrikaans" -> "af"
            "Arabic" -> "ar"
            "German" -> "de"
            "Spanish" -> "es"
            "French" -> "fr"
            "Italian" -> "it"
            "Dutch" -> "nl"
            "Portuguese" -> "pt"
            else -> "en"
        }
    }
}
