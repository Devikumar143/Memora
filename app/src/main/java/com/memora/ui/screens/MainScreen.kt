package com.memora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.content.Intent
import com.memora.R
import com.memora.ui.components.IntelligenceHeader
import com.memora.ui.viewmodel.SearchViewModel
import java.util.Calendar
import com.memora.ui.components.MemoryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    
    // Permission state
    var isNotificationPermissionGranted by remember { 
        mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)) 
    }

    // Check permission on resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationPermissionGranted = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = Color(0xFF000000), // Pure Black background
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "memora",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = 4.sp
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = Color.White.copy(alpha = 0.05f)
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF000000)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp) // More whitespace
        ) {
            if (!isNotificationPermissionGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = Color.White.copy(alpha = 0.03f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
                    onClick = {
                        context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                           text = "notification access required for second brain",
                           style = MaterialTheme.typography.labelMedium,
                           color = Color.White.copy(alpha = 0.4f),
                           modifier = Modifier.weight(1f),
                           letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "grant",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // New Intelligence Header
            val todayMemories = searchResults.count { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                val today = Calendar.getInstance()
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
            IntelligenceHeader(recapCount = todayMemories)
            
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search...", color = Color.White.copy(alpha = 0.1f), style = MaterialTheme.typography.bodyLarge) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.1f)) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.05f),
                    cursorColor = Color.White.copy(alpha = 0.2f),
                    textColor = Color.White
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light, letterSpacing = 1.sp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Filter Chips
            val selectedFilter by viewModel.selectedFilter.collectAsState()
            val filters = listOf(
                "link" to "Links",
                "screenshot" to "Screenshots",
                "clipboard" to "Copied",
                "notification" to "Alerts"
            )
            
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { (type, label) ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { viewModel.onFilterChanged(type) },
                        label = { Text(label) },
                        leadingIcon = if (selectedFilter == type) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White.copy(alpha = 0.1f),
                            selectedLabelColor = Color.White,
                            labelColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Discovery",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (searchResults.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(searchResults) { item ->
                        MemoryCard(
                            item = item,
                            onDelete = { viewModel.deleteMemory(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFD700).copy(alpha = 0.05f),
            modifier = Modifier.size(120.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFFFD700).copy(alpha = 0.3f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your Second Brain is Ready",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFFFD700),
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Copy a link or take a screenshot\nto begin indexing your life.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.White.copy(alpha = 0.5f),
            lineHeight = 20.sp
        )
    }
}
