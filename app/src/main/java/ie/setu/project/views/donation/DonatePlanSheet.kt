package ie.setu.project.views.donation

import android.app.DatePickerDialog
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonatePlanSheet(
    item: ClosetOrganiserModel,
    vm: DonationViewModel,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit
) {
    val context          = LocalContext.current
    val scope            = rememberCoroutineScope()
    val nearbyLocations  by vm.nearbyLocations.collectAsStateWithLifecycle()
    val isSearching      by vm.isSearchingMap.collectAsStateWithLifecycle()
    var selectedLocation by remember { mutableStateOf<DonationLocation?>(null) }
    var scheduledDate    by remember { mutableStateOf<Date?>(null) }
    var userLatLng       by remember { mutableStateOf<com.google.android.gms.maps.model.LatLng?>(null) }
    var locationError    by remember { mutableStateOf(false) }
    var mapExpanded      by remember { mutableStateOf(false) }
    var fetchLocation    by remember { mutableStateOf(false) }
    val sdf              = remember { SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault()) }

    val hasLocationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocation = true
        else locationError = true
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) fetchLocation = true
        else locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(fetchLocation) {
        if (!fetchLocation) return@LaunchedEffect
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLatLng = com.google.android.gms.maps.model.LatLng(loc.latitude, loc.longitude)
                        vm.searchNearbyDonationSpots(userLatLng!!)
                    } else {
                        locationError = true
                    }
                }
                .addOnFailureListener { locationError = true }
        } catch (_: SecurityException) {
            locationError = true
        }
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(userLatLng) {
        userLatLng?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 14f))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Plan Donation: ${item.title.ifBlank { "Item" }}",
                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                "Nearby Donation Spots",
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            if (locationError) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOff, null, tint = Color.Gray,
                            modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Could not get your location", color = Color.Gray, fontSize = 14.sp)
                        Text("Please enable location and try again",
                            color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                val mapHeight = if (mapExpanded) 450.dp else 220.dp

                Box(modifier = Modifier.fillMaxWidth().height(mapHeight)) {
                    AndroidView(
                        factory = { ctx ->
                            ComposeView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setOnTouchListener { v, event ->
                                    v.parent?.requestDisallowInterceptTouchEvent(true)
                                    if (event.action == android.view.MotionEvent.ACTION_UP) v.performClick()
                                    false
                                }
                            }
                        },
                        update = { composeView ->
                            composeView.setContent {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                                    uiSettings = MapUiSettings(zoomControlsEnabled = true)
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
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isSearching || userLatLng == null) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    IconButton(
                        onClick = { mapExpanded = !mapExpanded },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            if (mapExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(4.dp)
                                .size(20.dp)
                        )
                    }
                }
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
                                if (loc.visitCount > 0) {
                                    Icon(Icons.Default.Star, null,
                                        tint = Color(0xFFF9A825),
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(loc.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        supportingContent = {
                            Text(
                                buildString {
                                    append(loc.address)
                                    if (loc.visitCount > 0) append("  •  Visited ${loc.visitCount}×")
                                },
                                fontSize = 12.sp, color = Color.Gray
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Default.Store, null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                        },
                        modifier = Modifier.clickable { selectedLocation = loc },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                    )
                    HorizontalDivider()
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Schedule Date", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))

            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            scheduledDate = Calendar.getInstance()
                                .apply { set(y, m, d, 9, 0, 0) }.time
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).also { it.datePicker.minDate = System.currentTimeMillis() }.show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(scheduledDate?.let { sdf.format(it) } ?: "Pick a date")
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    vm.scheduleDonation(item, selectedLocation!!, scheduledDate!!)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Donation planned for ${SimpleDateFormat("EEE dd MMM", Locale.getDefault()).format(scheduledDate!!)}"
                        )
                    }
                    onDismiss()
                },
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