package ie.setu.project.views.donation

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ie.setu.project.models.clothing.ClosetOrganiserModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun FlaggedTab(
    items: List<ClosetOrganiserModel>,
    thresholdMonths: Int,
    onDonate: (ClosetOrganiserModel) -> Unit,
    onKeep: (ClosetOrganiserModel) -> Unit
) {
    if (items.isEmpty()) { EmptyDonationState(); return }
    Text(
        "Items unworn for ${thresholdMonths}+ months",
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.id }) { item ->
            DonationItemCard(item = item, onDonate = { onDonate(item) }, onKeep = { onKeep(item) })
        }
    }
}

@Composable
private fun DonationItemCard(
    item: ClosetOrganiserModel,
    onDonate: () -> Unit,
    onKeep: () -> Unit
) {
    val daysSince = TimeUnit.MILLISECONDS.toDays(Date().time - item.lastWorn.time)
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        border    = BorderStroke(1.5.dp, Color(0xFF007A90).copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F5F5))
                    .border(2.dp, Color(0xFF007A90).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
                val hasImage = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()
                if (hasImage) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(Icons.Default.Checkroom, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title.ifBlank { "Unnamed item" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                if (item.category.isNotBlank()) Text(item.category, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        daysSince >= 365 -> Color(0xFFFFCDD2)
                        daysSince >= 180 -> Color(0xFFFFE0B2)
                        else             -> Color(0xFFFFF9C4)
                    }
                ) {
                    Text(
                        "Unworn ${daysSince}d · last ${sdf.format(item.lastWorn)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = Color(0xFF4E342E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                FilledTonalButton(
                    onClick = onDonate,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFE8F5E9),
                        contentColor   = Color(0xFF2E7D32)
                    )
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Donate", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onKeep,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Keep", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyDonationState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(72.dp))
            Text("Your wardrobe is clutter-free!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("No items inactive past your threshold.\nGreat job!", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}