package com.example.infinitynews.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.infinitynews.R
import com.example.infinitynews.models.News

class NewsAdapter(
    private val onBookmarkClick: (News, Boolean) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private val newsList = mutableListOf<News>()

    inner class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val newsImage: ImageView = view.findViewById(R.id.newsImage)
        val newsTitle: TextView = view.findViewById(R.id.newsTitle)
        val bookmarkIcon: ImageView = view.findViewById(R.id.bookmarkIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.newsTitle.text = news.title

        // Load image
        Glide.with(holder.itemView.context)
            .load(news.imageUrl)
            .placeholder(android.R.drawable.ic_dialog_info)
            .error(android.R.drawable.ic_dialog_alert)
            .into(holder.newsImage)

        // Set bookmark icon
        holder.bookmarkIcon.setImageResource(
            if (news.isBookmarked) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        // Bookmark toggle
        holder.bookmarkIcon.setOnClickListener {
            news.isBookmarked = !news.isBookmarked
            notifyItemChanged(position)  // Immediately updates the icon
            onBookmarkClick(news, news.isBookmarked)
        }

        // Open article on click
        val openUrl = {
            news.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                holder.itemView.context.startActivity(intent)
            }
        }
        holder.itemView.setOnClickListener { openUrl() }
        holder.newsImage.setOnClickListener { openUrl() }
    }

    override fun getItemCount(): Int = newsList.size

    fun updateNews(newNews: List<News>) {
        newsList.clear()
        newsList.addAll(newNews)
        notifyDataSetChanged()
    }
}
