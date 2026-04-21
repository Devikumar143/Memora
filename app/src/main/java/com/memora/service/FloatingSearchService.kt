package com.memora.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingCornerPathEffect
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

@AndroidEntryPoint
class FloatingSearchService : Service() {

    @Inject lateinit var memoryDao: MemoryDao
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var clipboardManager: ClipboardManager

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        
        startForegroundService()
        setupClipboardListener()
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

        startForeground(1, notification)
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
            val item = MemoryItem(
                contentText = text,
                sourceApp = "Clipboard",
                contentType = "clipboard",
                tags = listOf("copied")
            )
            memoryDao.insertMemory(item)
            Log.d("Memora", "Saved clipboard memory: $text")
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
