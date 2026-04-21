package com.memora.capture

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.memora.data.db.MemoryDatabase
import com.memora.data.model.MemoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClipboardCaptureManager(private val context: Context) {

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val database = MemoryDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startListening() {
        clipboard.addPrimaryClipChangedListener {
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString()
                if (!text.isNullOrEmpty()) {
                    saveToMemory(text)
                }
            }
        }
    }

    private fun saveToMemory(text: String) {
        scope.launch {
            val item = MemoryItem(
                contentText = text,
                sourceApp = "Clipboard",
                contentType = "clipboard",
                tags = listOf("copy", "clipboard")
            )
            database.memoryDao().insertMemory(item)
            Log.d("Memora", "Saved clipboard content")
        }
    }
}
