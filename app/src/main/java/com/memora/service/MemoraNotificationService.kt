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
                val item = MemoryItem(
                    contentText = content,
                    sourceApp = packageName,
                    contentType = "notification",
                    tags = extractSimpleTags(content)
                )
                database.memoryDao().insertMemory(item)
                Log.d("Memora", "Saved notification from $packageName")
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
