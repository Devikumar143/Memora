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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Scaffold(
        containerColor = Color(0xFF000000), // Pure Black background
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_zen),
                            contentDescription = "Memora Zen Logo",
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "memora",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                            letterSpacing = 2.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = Color.White.copy(alpha = 0.2f)
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
                .padding(16.dp)
        ) {
            // New Intelligence Header
            val todayMemories = searchResults.count { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                val today = Calendar.getInstance()
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
            IntelligenceHeader(recapCount = todayMemories)
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("search...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.3f)) },
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    textColor = Color.White
                )
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
