package com.memora.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.memora.data.db.MemoryDatabase
import com.memora.data.model.MemoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoraNotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var database: MemoryDatabase

    override fun onCreate() {
        super.onCreate()
        database = MemoryDatabase.getDatabase(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        if (!text.isNullOrEmpty() && packageName != "com.memora") {
            val content = if (title != null) "$title: $text" else text
            
            serviceScope.launch {
                // Smart Tagger logic for Notifications
                var category = "Alerts"
                val lowerPackage = packageName.lowercase()
                val lowerContent = content.lowercase()

                when {
                    lowerPackage.contains("whatsapp") || lowerPackage.contains("instagram") || 
                    lowerPackage.contains("facebook") || lowerPackage.contains("messenger") || 
                    lowerPackage.contains("discord") || lowerPackage.contains("social") -> category = "Social"
                    
                    lowerPackage.contains("amazon") || lowerPackage.contains("shopping") || 
                    lowerPackage.contains("flipkart") || lowerPackage.contains("shop") -> category = "Shopping"
                    
                    lowerPackage.contains("chrome") || lowerPackage.contains("browser") || 
                    lowerPackage.contains("research") || lowerContent.contains("http") -> category = "Research"
                }

                val item = MemoryItem(
                    contentText = content,
                    sourceApp = packageName,
                    contentType = "notification",
                    category = category,
                    description = if (content.length > 100) content.take(97) + "..." else null,
                    tags = extractSimpleTags(content)
                )
                database.memoryDao().insertMemory(item)
                Log.d("Memora", "Smart Saved notification from $packageName as $category")
            }
        }
    }

    private fun extractSimpleTags(text: String): List<String> {
        // Basic keyword extraction logic
        val words = text.lowercase().split(Regex("\\s+"))
            .filter { it.length > 3 }
            .distinct()
            .take(5)
        return words
    }
}
