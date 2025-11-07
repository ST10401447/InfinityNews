package com.example.infinitynews

import com.example.infinitynews.models.News
import org.junit.Assert.*
import org.junit.Test

class AppTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun newsModel_createsCorrectly() {
        val news = News(
            id = "test-123",
            title = "Test News",
            imageUrl = "https://example.com/image.jpg",
            category = "General",
            isBookmarked = false,
            url = "https://example.com/news"
        )

        assertEquals("Test News", news.title)
        assertEquals("General", news.category)
        assertFalse(news.isBookmarked)
        assertEquals("https://example.com/news", news.url)
    }

    @Test
    fun newsModel_withNullValues() {
        val news = News(
            id = "test-456",
            title = "News Without Image",
            imageUrl = null,
            category = "Politics",
            isBookmarked = true,
            url = null
        )

        assertNull(news.imageUrl)
        assertNull(news.url)
        assertTrue(news.isBookmarked)
        assertEquals("Politics", news.category)
    }

    @Test
    fun url_validation_works() {
        val validUrl = "https://newsapi.org/v2/top-headlines"
        val invalidUrl = "not-a-url"

        assertTrue(validUrl.startsWith("https://"))
        assertFalse(invalidUrl.startsWith("https://"))
    }

    @Test
    fun bookmarkToggle_works() {
        val news = News(
            id = "test-789",
            title = "Bookmarked News",
            imageUrl = "https://example.com/image.jpg",
            category = "Sports",
            isBookmarked = false,
            url = "https://example.com/sports"
        )

        assertFalse(news.isBookmarked)

        // Toggle bookmark on/off
        news.isBookmarked = true
        assertTrue(news.isBookmarked)

        news.isBookmarked = false
        assertFalse(news.isBookmarked)
    }

    @Test
    fun categoryList_validation() {
        val categories = listOf("General", "Politics", "Sports", "Technology")

        assertEquals(4, categories.size)
        assertTrue(categories.contains("Politics"))
        assertTrue(categories.contains("General"))
        assertFalse(categories.contains("Entertainment"))
    }
}
