package com.example.infinitynews

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinitynews.adapters.NewsAdapter
import com.example.infinitynews.models.News

class BookmarksActivity : AppCompatActivity() {
    private lateinit var bookmarksRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var homeIcon: ImageView
    private lateinit var categoriesIcon: ImageView
    private lateinit var bookmarksIcon: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var settingsIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        bookmarksRecyclerView = findViewById(R.id.bookmarksRecyclerView)
        homeIcon = findViewById(R.id.homeIcon)
        categoriesIcon = findViewById(R.id.categoriesIcon)
        bookmarksIcon = findViewById(R.id.bookmarksIcon)
        profileIcon = findViewById(R.id.profileIcon)
        settingsIcon = findViewById(R.id.settingsIcon)

        setupBookmarkedNews()
        setupBottomNavigation()
    }

    private fun setupBookmarkedNews() {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val bookmarks = sharedPref.getStringSet("bookmarks", mutableSetOf()) ?: mutableSetOf()
        val bookmarkedNews = mutableListOf<News>()

        for (bookmarkId in bookmarks) {
            val title = sharedPref.getString("bookmark_${bookmarkId}_title", null)
            val image = sharedPref.getString("bookmark_${bookmarkId}_image", "")
            val category = sharedPref.getString("bookmark_${bookmarkId}_category", "General")
            val url = sharedPref.getString("bookmark_${bookmarkId}_url", null)

            if (title != null && url != null) {
                bookmarkedNews.add(
                    News(
                        id = bookmarkId,
                        title = title,
                        imageUrl = image ?: "",
                        category = category ?: "General",
                        isBookmarked = true,
                        url = url
                    )
                )
            }
        }

        if (bookmarkedNews.isEmpty()) {
            Toast.makeText(this, "No bookmarks yet", Toast.LENGTH_SHORT).show()
        }

        newsAdapter = NewsAdapter { news, isBookmarked ->
            handleBookmark(news, isBookmarked)
        }
        bookmarksRecyclerView.layoutManager = GridLayoutManager(this, 1)
        bookmarksRecyclerView.adapter = newsAdapter
        newsAdapter.updateNews(bookmarkedNews)
    }

    private fun handleBookmark(news: News, isBookmarked: Boolean) {
        val sharedPref = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        val bookmarks = sharedPref.getStringSet("bookmarks", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (!isBookmarked) {
            // Remove bookmark
            bookmarks.remove(news.id)
            editor.remove("bookmark_${news.id}_title")
            editor.remove("bookmark_${news.id}_image")
            editor.remove("bookmark_${news.id}_category")
            editor.remove("bookmark_${news.id}_url")
            Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
        } else {
            // This usually won't happen in BookmarksActivity
            bookmarks.add(news.id)
        }

        editor.putStringSet("bookmarks", bookmarks)
        editor.apply()

        // Refresh list
        setupBookmarkedNews()
    }

    private fun setupBottomNavigation() {
        homeIcon.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        categoriesIcon.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        bookmarksIcon.setOnClickListener {
            // Already in bookmarks, do nothing
        }

        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        settingsIcon.setOnClickListener {
            startActivity(Intent(this, SideMenuActivity::class.java))
        }
    }
}
