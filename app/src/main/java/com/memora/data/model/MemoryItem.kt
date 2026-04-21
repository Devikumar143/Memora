package com.memora.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "memory_items")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contentText: String,
    val sourceApp: String,
    val contentType: String, // notification, clipboard, screenshot, file
    val timestamp: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val metadata: String? = null // JSON string
)

enum class ContentType {
    NOTIFICATION, CLIPBOARD, SCREENSHOT, FILE
}
