package ie.setu.project.views.donation

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import ie.setu.project.models.clothing.ClosetOrganiserModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonatePlanSheet(
    item: ClosetOrganiserModel,
    vm: DonationViewModel,
    onDismiss: () -> Unit
) {
    val context          = LocalContext.current
    val nearbyLocations  by vm.nearbyLocations.collectAsStateWithLifecycle()
    val isSearching      by vm.isSearchingMap.collectAsStateWithLifecycle()
    var selectedLocation by remember { mutableStateOf<DonationLocation?>(null) }
    var scheduledDate    by remember { mutableStateOf<Date?>(null) }
    var userLatLng       by remember { mutableStateOf(LatLng(53.3498, -6.2603)) }
    val sdf              = remember { SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        try {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) userLatLng = LatLng(loc.latitude, loc.longitude)
                    vm.searchNearbyDonationSpots(userLatLng)
                }
        } catch (_: SecurityException) { vm.searchNearbyDonationSpots(userLatLng) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 14f)
    }
    LaunchedEffect(userLatLng) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding()
        ) {
            Text("Plan Donation — ${item.title.ifBlank { "Item" }}",
                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp))

            Text("Nearby Donation Spots", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp))

            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    nearbyLocations.forEach { loc ->
                        Marker(
                            state = MarkerState(position = loc.latLng),
                            title = loc.name,
                            snippet = loc.address,
                            icon = if (loc.visitCount > 0)
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            else
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                            onClick = { selectedLocation = loc; false }
                        )
                    }
                }
                if (isSearching) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            Spacer(Modifier.height(10.dp))
            Text("Select a location:", fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))

            LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                items(nearbyLocations) { loc ->
                    val isSelected = selectedLocation?.placeId == loc.placeId
                    ListItem(
                        headlineContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (loc.visitCount > 0) Icon(Icons.Default.Star, null,
                                    tint = Color(0xFFF9A825), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(loc.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        supportingContent = {
                            Text(buildString {
                                append(loc.address)
                                if (loc.visitCount > 0) append("  •  Visited ${loc.visitCount}×")
                            }, fontSize = 12.sp, color = Color.Gray)
                        },
                        leadingContent = {
                            Icon(Icons.Default.Store, null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                        },
                        modifier = Modifier.clickable { selectedLocation = loc },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    )
                    HorizontalDivider()
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Schedule Date", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))

            OutlinedButton(onClick = {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, y, m, d ->
                    scheduledDate = Calendar.getInstance().apply { set(y, m, d, 9, 0, 0) }.time
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .also { it.datePicker.minDate = System.currentTimeMillis() }.show()
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(scheduledDate?.let { sdf.format(it) } ?: "Pick a date")
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.scheduleDonation(item, selectedLocation!!, scheduledDate!!) },
                enabled = selectedLocation != null && scheduledDate != null,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.EventAvailable, null)
                Spacer(Modifier.width(8.dp))
                Text("Schedule Donation", fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}