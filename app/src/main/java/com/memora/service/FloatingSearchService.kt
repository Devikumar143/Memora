package com.memora.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.memora.data.db.MemoryDao
import com.memora.data.model.MemoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.memora.ui.theme.MemoraTheme
import dagger.hilt.android.AndroidEntryPoint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.jsoup.Jsoup

@AndroidEntryPoint
class FloatingSearchService : Service() {

    @Inject lateinit var memoryDao: MemoryDao
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var clipboardManager: ClipboardManager

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var screenshotObserver: android.database.ContentObserver

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        
        startForegroundService()
        setupClipboardListener()
        setupScreenshotObserver()
        showFloatingButton()
    }

    private fun startForegroundService() {
        val channelId = "memora_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Memora Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Memora is active")
            .setContentText("Capturing memories in the background")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun setupScreenshotObserver() {
        screenshotObserver = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: android.net.Uri?) {
                super.onChange(selfChange, uri)
                // When MediaStore changes, we check for new screenshots
                serviceScope.launch {
                    detectNewScreenshot()
                }
            }
        }
        contentResolver.registerContentObserver(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            screenshotObserver
        )
    }

    private suspend fun detectNewScreenshot() {
        val projection = arrayOf(
            android.provider.MediaStore.Images.Media._ID,
            android.provider.MediaStore.Images.Media.DISPLAY_NAME,
            android.provider.MediaStore.Images.Media.DATA,
            android.provider.MediaStore.Images.Media.DATE_ADDED
        )
        
        // Improved selection: Look for 'Screenshots' in the file path or name
        val selection = "${android.provider.MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%Screenshots%")
        val sortOrder = "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DISPLAY_NAME))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA))
                val dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATE_ADDED))
                
                // Only process if it's new (window of 15 seconds for reliability)
                if (System.currentTimeMillis() / 1000 - dateAdded < 15) {
                    saveScreenshotMemory(name, path)
                }
            }
        }
    }

    private fun saveScreenshotMemory(name: String, path: String) {
        serviceScope.launch {
            try {
                val image = InputImage.fromFilePath(this@FloatingSearchService, android.net.Uri.fromFile(java.io.File(path)))
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        serviceScope.launch {
                            val item = MemoryItem(
                                contentText = "Screenshot: $name",
                                sourceApp = "System",
                                contentType = "screenshot",
                                tags = listOf("screenshot", "image"),
                                extractedText = extractedText
                            )
                            memoryDao.insertMemory(item)
                            Log.d("Memora", "Saved screenshot with OCR text: ${extractedText.take(50)}...")
                        }
                    }
            } catch (e: Exception) {
                Log.e("Memora", "OCR Failed: ${e.message}")
                val item = MemoryItem(
                    contentText = "Screenshot: $name",
                    sourceApp = "System",
                    contentType = "screenshot",
                    tags = listOf("screenshot", "image")
                )
                memoryDao.insertMemory(item)
            }
        }
    }

    private fun setupClipboardListener() {
        clipboardManager.addPrimaryClipChangedListener {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString()
                if (!text.isNullOrEmpty()) {
                    saveClipboardMemory(text)
                }
            }
        }
    }

    private fun saveClipboardMemory(text: String) {
        serviceScope.launch {
            var displayTitle = text
            val tags = mutableListOf("copied")
            
            // Check if it's a URL
            if (text.startsWith("http") || text.contains("www.")) {
                tags.add("link")
                try {
                    val doc = Jsoup.connect(text).get()
                    val title = doc.title()
                    if (title.isNotEmpty()) {
                        displayTitle = title
                    }
                } catch (e: Exception) {
                    Log.e("Memora", "Link enrichment failed: ${e.message}")
                }
            }

            val item = MemoryItem(
                contentText = displayTitle,
                sourceApp = "Clipboard",
                contentType = "clipboard",
                tags = tags,
                metadata = if (displayTitle != text) text else null // Store raw URL in metadata if enriched
            )
            memoryDao.insertMemory(item)
            Log.d("Memora", "Saved clipboard memory: $displayTitle")
        }
    }

    private fun showFloatingButton() {
        val composeView = ComposeView(this).apply {
            setContent {
                MemoraTheme {
                    FloatingButton(onClick = { openFullSearch() })
                }
            }
        }

        // Lifecycle, ViewModelStore, and SavedStateRegistry setup for ComposeView in Service
        val lifecycleOwner = object : LifecycleOwner {
            private val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
            init {
                registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
        }

        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }

        val savedStateRegistryOwner = object : SavedStateRegistryOwner {
            private val controller = SavedStateRegistryController.create(this)
            override val lifecycle: Lifecycle get() = lifecycleOwner.lifecycle
            override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry
            init {
                controller.performRestore(null)
            }
        }

        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
        composeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 100
        }

        windowManager.addView(composeView, params)
        floatingView = composeView
    }

    private fun openFullSearch() {
        // Smart Poll: Check clipboard when user interacts with floating button
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString()
            if (!text.isNullOrEmpty()) {
                saveClipboardMemory(text)
            }
        }

        // Logic to expand overlay or launch activity
        val intent = Intent(this, com.memora.ui.MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
    }
}

@Composable
fun FloatingButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .padding(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.White
        )
    }
}
