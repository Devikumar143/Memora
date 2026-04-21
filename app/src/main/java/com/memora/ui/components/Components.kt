package com.memora.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.memora.data.model.MemoryItem
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun MemoryCard(
    item: MemoryItem,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val (appName, appIcon) = remember(item.sourceApp) {
        if (item.sourceApp == "Clipboard") {
            "Clipboard" to Icons.Default.Assignment
        } else {
            try {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(item.sourceApp, 0)
                pm.getApplicationLabel(info).toString() to pm.getApplicationIcon(info)
            } catch (e: Exception) {
                item.sourceApp to android.R.drawable.sym_def_app_icon
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable { handleCardClick(context, item) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF000000) // Pure Black
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, 
            Color(0xFF4A4A4A) // Hairline Grey
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Header: App Label & Time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                   if (appIcon is android.graphics.drawable.Drawable) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(appIcon)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape),
                            alpha = 0.4f // Keep it Zen
                        )
                    } else if (appIcon is androidx.compose.ui.graphics.vector.ImageVector) {
                         Icon(
                            imageVector = appIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White.copy(alpha = 0.4f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = appName.lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 0.5.sp
                    )
                }
                
                Text(
                    text = formatTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f)
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Thumbnail
                if (!item.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.contentText,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 20.sp
                    )
                    
                    if (!item.description.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 3,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            
            // Footer: Category
            if (!item.category.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "categorized as ${item.category?.lowercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun IntelligenceHeader(recapCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "Your Second Brain",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Today: $recapCount memories indexed.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}

private fun handleCardClick(context: android.content.Context, item: MemoryItem) {
    try {
        val url = if (item.contentText.contains("http") || item.contentText.contains("www.")) {
             extractUrl(item.contentText)
        } else item.metadata ?: return
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun extractUrl(text: String): String {
    val regex = "(https?://[^\\s]+)".toRegex()
    val match = regex.find(text)
    return match?.value ?: text
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
