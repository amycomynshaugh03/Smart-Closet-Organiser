package ie.setu.project.views.donation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    onBack: () -> Unit,
    vm: DonationViewModel = hiltViewModel()
) {
    val flaggedItems    by vm.flaggedItems.collectAsStateWithLifecycle()
    val thresholdMonths by vm.thresholdMonths.collectAsStateWithLifecycle()
    val remindersOn     by vm.remindersEnabled.collectAsStateWithLifecycle()
    val donatedCount    by vm.donatedCount.collectAsStateWithLifecycle()

    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refreshFlaggedItems() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_heart), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Donation Tracker", fontSize = 30.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(painter = painterResource(id = R.drawable.ic_heart), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (showSettings) {
                DonationSettingsPanel(
                    thresholdMonths  = thresholdMonths,
                    remindersEnabled = remindersOn,
                    onThresholdChange = { vm.setThresholdMonths(it) },
                    onRemindersChange = { vm.setRemindersEnabled(it) }
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryColumn("${flaggedItems.size}", "Flagged",   MaterialTheme.colorScheme.primary)
                SummaryColumn("$donatedCount",        "Donated",   Color(0xFF2E7D32))
                SummaryColumn("${thresholdMonths}mo", "Threshold", MaterialTheme.colorScheme.tertiary)
            }

            if (flaggedItems.isEmpty()) {
                EmptyDonationState()
            } else {
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
                    items(flaggedItems, key = { it.id }) { item ->
                        DonationItemCard(
                            item    = item,
                            onDonate = { vm.markForDonation(item) },
                            onKeep   = { vm.keepItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun DonationSettingsPanel(
    thresholdMonths: Int,
    remindersEnabled: Boolean,
    onThresholdChange: (Int) -> Unit,
    onRemindersChange: (Boolean) -> Unit
) {
    val options = listOf(3, 6, 9, 12)

    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Inactivity Threshold", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { months ->
                    FilterChip(
                        selected = thresholdMonths == months,
                        onClick  = { onThresholdChange(months) },
                        label    = { Text("${months}mo") },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
            }
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Donation Reminders", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Show badge on nav bar", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onRemindersChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
                val hasImage = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()
                if (hasImage) {
                    AsyncImage(model = imageModel, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Checkroom, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(72.dp))
            Text("Your wardrobe is clutter-free!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("No items inactive past your threshold.\nGreat job!", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
