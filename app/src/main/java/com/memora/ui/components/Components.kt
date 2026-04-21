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
    
    // Launch logic
    val onCardClick = {
        try {
            val intent = if (item.contentText.contains("http") || item.contentText.contains("www.")) {
                // Extract URL if it's embedded in text
                val url = extractUrl(item.contentText)
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            } else {
                context.packageManager.getLaunchIntentForPackage(item.sourceApp)
            }
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Could not open this app", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

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
            .padding(vertical = 6.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onCardClick() }
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (appIcon is ImageVector) {
                        Icon(
                            imageVector = appIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(appIcon)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = formatTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.contentText,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (item.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.tags.forEach { tag ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = "#$tag",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
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
