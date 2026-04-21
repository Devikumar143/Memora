package com.memora.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
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
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.memora.ui.theme.MemoraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FloatingSearchService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showFloatingButton()
    }

    private fun showFloatingButton() {
        val composeView = ComposeView(this).apply {
            setContent {
                MemoraTheme {
                    FloatingButton(onClick = { openFullSearch() })
                }
            }
        }

        // Lifecycle and SavedStateRegistry setup for ComposeView in Service
        val lifecycleOwner = object : LifecycleOwner {
            private val lifecycle = LifecycleRegistry(this)
            override val lifecycle: Lifecycle = lifecycle
            init { lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE); lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
        }
        ViewTreeLifecycleOwner.set(composeView, lifecycleOwner)
        ViewTreeSavedStateRegistryOwner.set(composeView, object : SavedStateRegistryOwner {
            override val lifecycle = lifecycleOwner.lifecycle
            override val savedStateRegistry = SavedStateRegistryController.create(this).apply { performRestore(null) }.savedStateRegistry
        })

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
