package com.example.infinitynews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class SideMenuActivity : AppCompatActivity() {

    private lateinit var homeIcon: ImageView
    private lateinit var categoriesIcon: ImageView
    private lateinit var bookmarksIcon: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var profileIconTop: ImageView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_side_menu)

        // Tech Menu - Load tech news
        findViewById<LinearLayout>(R.id.menuTech).setOnClickListener {
            loadCategoryNews("technology", "Tech")
        }

        // General Menu - Load general news
        findViewById<LinearLayout>(R.id.menuGeneral).setOnClickListener {
            loadCategoryNews("general", "General")
        }

        // Politics Menu - Load politics news
        findViewById<LinearLayout>(R.id.menuPolitics).setOnClickListener {
            loadCategoryNews("politics", "Politics")
        }

        // Sports Menu - Load sports news
        findViewById<LinearLayout>(R.id.menuSports).setOnClickListener {
            loadCategoryNews("sports", "Sports")
        }

        // Settings Menu - Go to Profile/Settings
        findViewById<LinearLayout>(R.id.menuSettings).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        // About Us Menu - Show About Us dialog
        findViewById<LinearLayout>(R.id.menuAboutUs).setOnClickListener {
            showAboutUsDialog()
        }

        // Privacy Policy Menu - Show Privacy Policy dialog
        findViewById<LinearLayout>(R.id.menuPrivacyPolicy).setOnClickListener {
            showPrivacyPolicyDialog()
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
                    Toast.makeText(this@SideMenuActivity, "API Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Toast.makeText(this@SideMenuActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadCategoryNews(category: String, categoryName: String) {
        // Save selected category
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("selectedCategory", category)
            putString("selectedCategoryName", categoryName)
            apply()
        }

        Toast.makeText(this, "Loading $categoryName news...", Toast.LENGTH_SHORT).show()

        // Go to HomeActivity which will load the category news
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
        finish()
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter { news, isBookmarked ->
            handleBookmark(news, isBookmarked)
        }
        newsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        newsRecyclerView.adapter = newsAdapter
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

    private fun showAboutUsDialog() {
        AlertDialog.Builder(this)
            .setTitle("About InfinityNews")
            .setMessage("""
                InfinityNews - Your Gateway to Global Information
                
                Version: 1.0
                
                InfinityNews brings you the latest news from around the world, curated and organized by category for your convenience.
                
                Features:
                • Latest news from reliable sources
                • Multiple categories (General, Politics, Sports, Tech)
                • Bookmark your favorite articles
                • Personalized news preferences
                • Clean and intuitive interface
                
                Stay informed, stay connected with InfinityNews!
            """.trimIndent())
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Contact Us") { _, _ ->
                // Open email client
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@infinitynews.com")
                    putExtra(Intent.EXTRA_SUBJECT, "InfinityNews Feedback")
                }
                if (emailIntent.resolveActivity(packageManager) != null) {
                    startActivity(emailIntent)
                }
            }
            .show()
    }

    private fun showPrivacyPolicyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("""
                Privacy Policy for InfinityNews
                
                Last updated: October 2025
                
                1. Information Collection
                We collect minimal information to provide you with personalized news experience:
                • Email address for account creation
                • News preferences and bookmarks
                • App usage analytics
                
                2. How We Use Your Information
                • To provide personalized news content
                • To improve our service
                • To send important updates (with your consent)
                
                3. Data Security
                We implement industry-standard security measures to protect your personal information.
                
                4. Third-Party Services
                We use NewsAPI.org to fetch news content. Their privacy policy applies to content they provide.
                
                5. Your Rights
                You have the right to:
                • Access your personal data
                • Request data deletion
                • Opt-out of analytics
                
                6. Cookies and Tracking
                We use minimal cookies for authentication and preferences only.
                
                7. Contact Us
                For privacy concerns, contact: privacy@infinitynews.com
                
                By using InfinityNews, you agree to this Privacy Policy.
            """.trimIndent())
            .setPositiveButton("I Understand") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Disagree") { _, _ ->
                Toast.makeText(this, "You must agree to continue using the app", Toast.LENGTH_LONG).show()
            }
            .show()
    }

}