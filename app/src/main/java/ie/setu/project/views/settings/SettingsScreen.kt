package ie.setu.project.views.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ie.setu.project.R
import ie.setu.project.views.clothingList.SyncState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    syncState: SyncState,
    onExportWardrobe: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentLocation by settingsViewModel.currentLocation.collectAsState()
    val searchResults by settingsViewModel.searchResults.collectAsState()
    val isSearching by settingsViewModel.isSearching.collectAsState()
    var cityQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_heart), null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Settings",
                            fontSize = 30.sp,
                            fontFamily = FontFamily.Cursive,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(painterResource(R.drawable.ic_heart), null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SyncStatusCard(syncState = syncState)

            Text(
                "Location",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentLocation?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Current: ${it.cityName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    OutlinedTextField(
                        value = cityQuery,
                        onValueChange = {
                            cityQuery = it
                            settingsViewModel.searchCity(it)
                        },
                        label = { Text("Search city…") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    searchResults.forEach { result ->
                        TextButton(
                            onClick = {
                                settingsViewModel.selectCity(result)
                                cityQuery = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                result.displayName,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Text(
                "Data & Backup",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsRow(
                    icon = Icons.Default.CloudUpload,
                    title = "Export Wardrobe",
                    subtitle = "Download a backup of all your clothing and outfits",
                    onClick = onExportWardrobe
                )
            }

            Text(
                "Account",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Sign Out", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun SyncStatusCard(syncState: SyncState) {
    var showSyncedConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(syncState) {
        if (syncState == SyncState.SYNCED) {
            showSyncedConfirm = true
            delay(4_000)
            showSyncedConfirm = false
        }
    }

    val bgColor: Color
    val icon: ImageVector
    val title: String
    val subtitle: String

    when (syncState) {
        SyncState.SYNCING -> {
            bgColor  = Color(0xFF1565C0)
            icon     = Icons.Default.Sync
            title    = "Syncing…"
            subtitle = "Uploading your latest changes to the cloud."
        }
        SyncState.SYNCED -> {
            bgColor  = Color(0xFF2E7D32)
            icon     = Icons.Default.CloudDone
            title    = "Up to date"
            subtitle = "All changes are saved to the cloud."
        }
        SyncState.OFFLINE_CACHE -> {
            bgColor  = Color(0xFFE65100)
            icon     = Icons.Default.CloudOff
            title    = "Offline"
            subtitle = "Changes will sync automatically when you reconnect."
        }
        SyncState.OFFLINE_BACKUP -> {
            bgColor  = Color(0xFFC62828)
            icon     = Icons.Default.CloudOff
            title    = "Offline — read-only"
            subtitle = "Showing a local backup. Changes can't be saved right now."
        }
    }

    val visible = syncState != SyncState.SYNCED || showSyncedConfirm

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                }
            }
        }
    }

    if (syncState == SyncState.SYNCED && !showSyncedConfirm) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2E7D32))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text("Up to date", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("All changes are saved to the cloud.", color = Color.White.copy(0.85f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}