package com.example.infinitynews.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.infinitynews.services.NewsNotificationService

object NotificationHelper {

    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    fun checkAndRequestNotificationPermission(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                return false
            }
        }
        return true
    }

    fun sendNewArticleNotification(context: Context, count: Int, latestTitle: String) {
        val title = if (count == 1) {
            "New Article Available!"
        } else {
            "$count New Articles Available!"
        }

        val message = if (count == 1) {
            latestTitle
        } else {
            "$latestTitle and ${count - 1} more..."
        }

        val intent = Intent(context, NewsNotificationService::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("count", count)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}