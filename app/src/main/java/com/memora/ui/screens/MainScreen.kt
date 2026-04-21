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
        topBar = {
            TopAppBar(
                title = { Text("Memora") },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
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
                placeholder = { Text("Search your Brain...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFD700)) },
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
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
                            selectedContainerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFFFD700),
                            labelColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Discovery",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFFD700),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "No memories yet!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try copying some text or waiting for a notification to arrive.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
