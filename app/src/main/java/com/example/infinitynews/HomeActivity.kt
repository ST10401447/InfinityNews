package com.example.infinitynews

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinitynews.adapters.NewsAdapter
import com.example.infinitynews.api.NewsResponse
import com.example.infinitynews.api.RetrofitClient
import com.example.infinitynews.models.News
import com.example.infinitynews.utils.NotificationHelper
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
    private lateinit var notificationCount: TextView
    private lateinit var homeIcon: ImageView
    private lateinit var categoriesIcon: ImageView
    private lateinit var bookmarksIcon: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var settingsIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Request notification permission
        NotificationHelper.checkAndRequestNotificationPermission(this)

        // ---- UI ----
        newsRecyclerView = findViewById(R.id.newsRecyclerView)
        searchInput = findViewById(R.id.searchInput)
        categoriesButton = findViewById(R.id.categoriesButton)
        bookmarksButton = findViewById(R.id.bookmarksButton)
        profileIconTop = findViewById(R.id.profileIconTop)
        notificationIcon = findViewById(R.id.notificationIcon)
        notificationBadge = findViewById(R.id.notificationBadge)
        notificationCount = findViewById(R.id.notificationCount)
        homeIcon = findViewById(R.id.homeIcon)
        categoriesIcon = findViewById(R.id.categoriesIcon)
        bookmarksIcon = findViewById(R.id.bookmarksIcon)
        profileIcon = findViewById(R.id.profileIcon)
        settingsIcon = findViewById(R.id.settingsIcon)

        setupRecyclerView()

        val category = intent.getStringExtra("category")
        if (category != null) {
            Log.d("HomeActivity", "START CATEGORY: $category")
            loadNewsByCategory(category)
        } else {
            Log.d("HomeActivity", "START TOP HEADLINES")
            loadTopHeadlines()
        }

        setupSearch()
        setupButtons()
        checkNotifications()
        setupNotificationClick()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        checkNotifications()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NotificationHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter { news, isBookmarked -> handleBookmark(news, isBookmarked) }
        newsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        newsRecyclerView.adapter = newsAdapter
    }

    // ---------- TOP HEADLINES (default home) ----------
    private fun loadTopHeadlines() {
        val sp = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val call = RetrofitClient.apiService.getTopHeadlines(country = "us", apiKey = RetrofitClient.API_KEY)

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarks = sp.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
                    val articles = response.body()!!.articles

                    // Check for new articles
                    checkForNewArticles(articles)

                    val filtered = filterOutPolitics(articles)
                    val items = filtered.mapNotNull { toNews(it, "General", bookmarks) }
                    newsAdapter.updateNews(items)
                    Log.d("HomeActivity", "TOP HEADLINES → ${items.size} (no politics)")
                }
            }
            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Network error", Toast.LENGTH_LONG).show()
            }
        })
    }

    // ---------- CHECK FOR NEW ARTICLES ----------
    private fun checkForNewArticles(articles: List<com.example.infinitynews.api.Article>) {
        val sp = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val seenArticles = sp.getStringSet("seenArticles", mutableSetOf()) ?: mutableSetOf()
        val notificationIds = sp.getStringSet("notifications", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        var newCount = 0
        var latestTitle = ""
        val editor = sp.edit()
        val currentTime = System.currentTimeMillis()

        articles.forEach { article ->
            val articleId = article.url ?: return@forEach

            if (!seenArticles.contains(articleId)) {
                // New article found
                newCount++
                if (latestTitle.isEmpty()) {
                    latestTitle = article.title ?: "New Article"
                }

                // Add to notifications
                notificationIds.add(articleId)
                editor.putString("notification_${articleId}_title", article.title ?: "No Title")
                editor.putString("notification_${articleId}_image", article.urlToImage ?: "")
                editor.putString("notification_${articleId}_url", articleId)
                editor.putLong("notification_${articleId}_timestamp", currentTime)

                Log.d("HomeActivity", "NEW ARTICLE: ${article.title}")
            }
        }

        if (newCount > 0) {
            // Update seen articles
            val updatedSeen = seenArticles.toMutableSet()
            articles.mapNotNull { it.url }.forEach { updatedSeen.add(it) }

            editor.putStringSet("seenArticles", updatedSeen)
            editor.putStringSet("notifications", notificationIds)
            editor.putBoolean("hasNotifications", true)
            editor.apply()

            checkNotifications()

            // Show toast
            Toast.makeText(this, "$newCount new article${if (newCount > 1) "s" else ""} available!", Toast.LENGTH_SHORT).show()

            // Send push notification
            NotificationHelper.sendNewArticleNotification(this, newCount, latestTitle)
        }
    }

    // ---------- CATEGORY ROUTER ----------
    private fun loadNewsByCategory(category: String) {
        newsAdapter.updateNews(emptyList())
        val sp = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)

        when (category.lowercase()) {
            "general" -> loadGeneral(sp)
            "politics" -> loadPolitics(sp)
            else -> loadOtherCategory(category, sp)
        }
    }

    // ---------- GENERAL ----------
    private fun loadGeneral(sp: android.content.SharedPreferences) {
        val call = RetrofitClient.apiService.getTopHeadlines(
            country = "us",
            apiKey = RetrofitClient.API_KEY
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarks = sp.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
                    val filtered = filterOutPolitics(response.body()!!.articles)
                    val items = filtered.mapNotNull { toNews(it, "General", bookmarks) }
                    newsAdapter.updateNews(items)
                }
            }
            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {}
        })
    }

    // ---------- POLITICS ----------
    private fun loadPolitics(sp: android.content.SharedPreferences) {
        val call = RetrofitClient.apiService.getPoliticsNews(
            query = "politics OR government OR election OR congress OR senate OR biden OR trump",
            language = "en",
            sortBy = "publishedAt",
            apiKey = RetrofitClient.API_KEY
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarks = sp.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
                    val items = response.body()!!.articles.mapNotNull { toNews(it, "Politics", bookmarks) }
                    newsAdapter.updateNews(items)
                }
            }
            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {}
        })
    }

    // ---------- OTHER CATEGORIES ----------
    private fun loadOtherCategory(category: String, sp: android.content.SharedPreferences) {
        val call = RetrofitClient.apiService.getTopHeadlinesByCategory(
            country = "us", category = category, apiKey = RetrofitClient.API_KEY
        )
        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarks = sp.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
                    val display = category.replaceFirstChar { it.uppercase() }
                    val items = response.body()!!.articles.mapNotNull { toNews(it, display, bookmarks) }
                    newsAdapter.updateNews(items)
                }
            }
            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {}
        })
    }

    // ---------- FILTER OUT POLITICS ----------
    private fun filterOutPolitics(list: List<com.example.infinitynews.api.Article>): List<com.example.infinitynews.api.Article> {
        val bad = listOf(
            "trump","biden","harris","kamala","vance","election","vote","poll",
            "democrat","republican","congress","senate","house","president",
            "white house","capitol","campaign","politics","government"
        )
        return list.filter { a ->
            val t = a.title?.lowercase() ?: ""
            val d = a.description?.lowercase() ?: ""
            !bad.any { it in t || it in d }
        }
    }

    // ---------- MAP ARTICLE → NEWS ----------
    private fun toNews(a: com.example.infinitynews.api.Article, cat: String, bm: Set<String>): News? {
        val url = a.url ?: return null
        return News(
            id = url,
            title = a.title ?: "No Title",
            imageUrl = a.urlToImage ?: "",
            category = cat,
            isBookmarked = bm.contains(url),
            url = url
        )
    }

    // ---------- BOOKMARK ----------
    private fun handleBookmark(news: News, add: Boolean) {
        val sp = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val set = sp.getStringSet("bookmarks", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val ed = sp.edit()
        if (add) {
            set.add(news.id)
            ed.putString("bookmark_${news.id}_title", news.title)
            ed.putString("bookmark_${news.id}_image", news.imageUrl)
            ed.putString("bookmark_${news.id}_category", news.category)
            ed.putString("bookmark_${news.id}_url", news.url)
        } else {
            set.remove(news.id)
            ed.remove("bookmark_${news.id}_title")
            ed.remove("bookmark_${news.id}_image")
            ed.remove("bookmark_${news.id}_category")
            ed.remove("bookmark_${news.id}_url")
        }
        ed.putStringSet("bookmarks", set).apply()
    }

    // ---------- SEARCH ----------
    private fun setupSearch() {
        searchInput.setOnEditorActionListener { _, _, _ ->
            val q = searchInput.text.toString().trim()
            if (q.isNotEmpty()) searchNews(q)
            true
        }
    }

    private fun searchNews(q: String) {
        val sp = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val call = RetrofitClient.apiService.searchNews(query = q, apiKey = RetrofitClient.API_KEY)
        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, r: Response<NewsResponse>) {
                if (r.isSuccessful && r.body() != null) {
                    val bm = sp.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
                    val items = r.body()!!.articles.mapNotNull {
                        val u = it.url ?: return@mapNotNull null
                        News(u, it.title ?: "No Title", it.urlToImage ?: "", it.source?.name ?: "Search", bm.contains(u), u)
                    }
                    newsAdapter.updateNews(items)
                }
            }
            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {}
        })
    }

    // ---------- UI ----------
    private fun setupButtons() {
        categoriesButton.setOnClickListener { startActivity(Intent(this, CategoriesActivity::class.java)) }
        bookmarksButton.setOnClickListener { startActivity(Intent(this, BookmarksActivity::class.java)) }
    }

    private fun checkNotifications() {
        val sp = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val notificationIds = sp.getStringSet("notifications", mutableSetOf()) ?: mutableSetOf()
        val count = notificationIds.size

        if (count > 0) {
            notificationBadge.visibility = View.VISIBLE
            notificationCount.visibility = View.VISIBLE
            notificationCount.text = count.toString()
        } else {
            notificationBadge.visibility = View.GONE
            notificationCount.visibility = View.GONE
        }
    }

    private fun setupNotificationClick() {
        notificationIcon.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
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
}