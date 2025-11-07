package com.example.infinitynews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.infinitynews.adapters.NotificationAdapter
import com.example.infinitynews.models.NotificationItem

class NotificationsActivity : AppCompatActivity() {
    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var backButton: ImageView
    private lateinit var notificationCount: TextView
    private lateinit var emptyMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        backButton = findViewById(R.id.backButton)
        notificationCount = findViewById(R.id.notificationCount)
        emptyMessage = findViewById(R.id.emptyMessage)
        notificationRecyclerView = findViewById(R.id.notificationRecyclerView)

        setupRecyclerView()
        loadNotifications()

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            // Open article in browser when clicked
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notification.url))
            startActivity(intent)
        }
        notificationRecyclerView.layoutManager = LinearLayoutManager(this)
        notificationRecyclerView.adapter = notificationAdapter
    }

    private fun loadNotifications() {
        val sp = getSharedPreferences("InfinityNewsPrefs", MODE_PRIVATE)
        val notificationIds = sp.getStringSet("notifications", mutableSetOf()) ?: mutableSetOf()

        val notifications = notificationIds.mapNotNull { id ->
            val title = sp.getString("notification_${id}_title", null) ?: return@mapNotNull null
            val image = sp.getString("notification_${id}_image", null) ?: ""
            val url = sp.getString("notification_${id}_url", null) ?: return@mapNotNull null
            val timestamp = sp.getLong("notification_${id}_timestamp", 0L)

            NotificationItem(id, title, image, url, timestamp)
        }.sortedByDescending { it.timestamp }

        if (notifications.isEmpty()) {
            emptyMessage.visibility = android.view.View.VISIBLE
            notificationRecyclerView.visibility = android.view.View.GONE
        } else {
            emptyMessage.visibility = android.view.View.GONE
            notificationRecyclerView.visibility = android.view.View.VISIBLE
            notificationCount.text = "${notifications.size} New Article${if (notifications.size > 1) "s" else ""}"
            notificationAdapter.updateNotifications(notifications)
        }
    }
}