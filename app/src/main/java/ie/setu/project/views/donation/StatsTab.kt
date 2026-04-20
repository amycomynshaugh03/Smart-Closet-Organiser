package ie.setu.project.views.donation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatsTab(stats: DonationStats) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("${stats.totalDonated}", fontSize = 36.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text("Items donated in total", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }

        if (stats.favouriteLocation != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFF9A825), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Favourite Drop-off", fontSize = 12.sp, color = Color.Gray)
                            Text(stats.favouriteLocation, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        if (stats.locationBreakdown.isNotEmpty()) {
            item {
                Text("Donations by Location", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            val sorted = stats.locationBreakdown.entries.sortedByDescending { it.value }
            val max    = sorted.first().value.toFloat()
            items(sorted) { (name, count) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        LinearProgressIndicator(
                            progress = { count / max },
                            modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("$count", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}