package com.memora.capture

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.memora.data.db.MemoryDatabase
import com.memora.data.model.MemoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenshotObserver(private val context: Context) : ContentObserver(Handler(Looper.getMainLooper())) {

    private val database = MemoryDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun register() {
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            this
        )
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        if (uri != null && uri.toString().contains(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
            detectNewImage(uri)
        }
    }

    private fun detectNewImage(uri: Uri) {
        scope.launch {
            // In a real app, we'd use OCR here. For MVP, we'll just log the screenshot event.
            // We can try to get the display name to check if it contains "Screenshot"
            val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayName = cursor.getString(0)
                    if (displayName.contains("Screenshot", ignoreCase = true)) {
                        saveToMemory(displayName, uri.toString())
                    }
                }
            }
        }
    }

    private fun saveToMemory(name: String, path: String) {
        scope.launch {
            val item = MemoryItem(
                contentText = "New Screenshot captured: $name",
                sourceApp = "System",
                contentType = "screenshot",
                tags = listOf("screenshot", "image"),
                metadata = "{\"uri\": \"$path\"}"
            )
            database.memoryDao().insertMemory(item)
            Log.d("Memora", "Screenshot detected and saved")
        }
    }
}
