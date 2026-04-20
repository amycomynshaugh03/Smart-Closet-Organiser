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
import ie.setu.project.models.donation.DonationPlan
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlannedTab(
    plans: List<DonationPlan>,
    onConfirm: (DonationPlan) -> Unit,
    onCancel: (DonationPlan) -> Unit
) {
    if (plans.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EventNote, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(8.dp))
                Text("No planned donations yet", color = Color.Gray)
                Text("Tap 'Donate' on a flagged item to schedule one",
                    fontSize = 12.sp, color = Color.Gray)
            }
        }
        return
    }

    val sdf   = SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault())
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.time

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(plans, key = { it.id }) { plan ->
            val isDueToday = !plan.scheduledDate.toDate().after(today)
            Card(
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDueToday) Color(0xFFE8F5E9)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Checkroom, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(plan.clothingTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (isDueToday) {
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF4CAF50)) {
                                Text("TODAY",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(plan.locationName, fontSize = 13.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(sdf.format(plan.scheduledDate.toDate()), fontSize = 13.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isDueToday) {
                            Button(
                                onClick = { onConfirm(plan) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Confirm Donated")
                            }
                        }
                        OutlinedButton(onClick = { onCancel(plan) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cancel Plan")
                        }
                    }
                }
            }
        }
    }
}