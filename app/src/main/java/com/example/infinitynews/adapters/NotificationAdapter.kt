package com.example.infinitynews.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.infinitynews.R
import com.example.infinitynews.models.NotificationItem

class NotificationAdapter(
    private val onNotificationClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private var notifications = listOf<NotificationItem>()

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val notificationImage: ImageView = itemView.findViewById(R.id.notificationImage)
        private val notificationTitle: TextView = itemView.findViewById(R.id.notificationTitle)
        private val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)

        fun bind(notification: NotificationItem) {
            notificationTitle.text = notification.title

            // Format timestamp
            val timeAgo = getTimeAgo(notification.timestamp)
            notificationTime.text = timeAgo

            // Load image
            if (notification.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(notification.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(notificationImage)
            }

            itemView.setOnClickListener {
                onNotificationClick(notification)
            }
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> "${diff / 86400000}d ago"
            }
        }
    }
}