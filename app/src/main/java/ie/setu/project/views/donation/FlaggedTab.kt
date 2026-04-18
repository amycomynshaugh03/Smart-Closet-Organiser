package ie.setu.project.views.donation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ie.setu.project.models.clothing.ClosetOrganiserModel

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