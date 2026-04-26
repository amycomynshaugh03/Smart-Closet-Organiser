package ie.setu.project.views.donation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.setu.project.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    onBack: () -> Unit,
    vm: DonationViewModel = hiltViewModel()
) {
    val flaggedItems    by vm.flaggedItems.collectAsStateWithLifecycle()
    val thresholdMonths by vm.thresholdMonths.collectAsStateWithLifecycle()
    val remindersOn     by vm.remindersEnabled.collectAsStateWithLifecycle()
    val donationStats   by vm.donationStats.collectAsStateWithLifecycle()
    val pendingPlans    by vm.pendingPlans.collectAsStateWithLifecycle()
    val selectedItem    by vm.selectedItem.collectAsStateWithLifecycle()

    var showSettings      by remember { mutableStateOf(false) }
    var activeTab         by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.refreshFlaggedItems() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_heart), null, tint = Color.White,
                            modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Donation Tracker", fontSize = 28.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                            color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Icon(painterResource(R.drawable.ic_heart), null, tint = Color.White,
                            modifier = Modifier.size(22.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (selectedItem != null) {
                DonatePlanSheet(
                    item = selectedItem!!,
                    vm = vm,
                    snackbarHostState = snackbarHostState,
                    onDismiss = { vm.clearSelectedItem() }
                )
            }

            if (showSettings) {
                DonationSettingsPanel(
                    thresholdMonths   = thresholdMonths,
                    remindersEnabled  = remindersOn,
                    onThresholdChange = { vm.setThresholdMonths(it) },
                    onRemindersChange = { vm.setRemindersEnabled(it) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryColumn("${flaggedItems.size}",          "Flagged",   MaterialTheme.colorScheme.primary)
                SummaryColumn("${donationStats.totalDonated}", "Donated",   Color(0xFF2E7D32))
                SummaryColumn("${pendingPlans.size}",          "Planned",   Color(0xFFF57C00))
                SummaryColumn("${thresholdMonths}mo",          "Threshold", MaterialTheme.colorScheme.tertiary)
            }

            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 },
                    text = { Text("To Donate") }, icon = { Icon(Icons.Default.Checkroom, null) })
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 },
                    text = { Text("Planned") },   icon = { Icon(Icons.Default.Event, null) })
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 },
                    text = { Text("Stats") },     icon = { Icon(Icons.Default.BarChart, null) })
            }

            when (activeTab) {
                0 -> FlaggedTab(flaggedItems, thresholdMonths, onDonate = { vm.startDonationFlow(it) }, onKeep = { vm.keepItem(it) })
                1 -> PlannedTab(pendingPlans, onConfirm = { vm.confirmDonation(it) }, onCancel = { vm.cancelPlan(it) })
                2 -> StatsTab(donationStats)
            }
        }
    }
}

@Composable
private fun SummaryColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = color)
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Inactivity Threshold", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 15.sp)
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
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Donation Reminders", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 15.sp)
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