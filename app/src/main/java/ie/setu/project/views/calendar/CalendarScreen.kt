package ie.setu.project.views.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.setu.project.models.calendar.OutfitCalendarEntry
import ie.setu.project.models.outfit.OutfitModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val calendarEntries by viewModel.calendarEntries.collectAsStateWithLifecycle()
    val outfits         by viewModel.outfits.collectAsStateWithLifecycle()
    val isLoading       by viewModel.isLoading.collectAsStateWithLifecycle()

    var currentMonth     by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate     by remember { mutableStateOf<LocalDate?>(null) }
    var showOutfitPicker by remember { mutableStateOf(false) }

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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Outfit Planner", fontSize = 28.sp,
                            fontFamily = FontFamily.Cursive, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            MonthHeader(
                yearMonth  = currentMonth,
                onPrevious = { currentMonth = currentMonth.minusMonths(1) },
                onNext     = { currentMonth = currentMonth.plusMonths(1) }
            )

            DayOfWeekRow()

            CalendarGrid(
                yearMonth       = currentMonth,
                selectedDate    = selectedDate,
                calendarEntries = calendarEntries,
                onDateClick     = { date -> selectedDate = date; showOutfitPicker = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            UpcomingOutfitsList(
                calendarEntries = calendarEntries,
                onEntryClick    = { entry ->
                    selectedDate     = LocalDate.parse(entry.dateKey, DATE_FMT)
                    showOutfitPicker = true
                }
            )
        }
    }

    if (showOutfitPicker && selectedDate != null) {
        OutfitPickerSheet(
            date         = selectedDate!!,
            outfits      = outfits,
            currentEntry = calendarEntries[selectedDate!!.format(DATE_FMT)],
            onAssign     = { outfit, note ->
                viewModel.assignOutfit(selectedDate!!.format(DATE_FMT), outfit, note)
                showOutfitPicker = false
            },
            onRemove  = { viewModel.assignOutfit(selectedDate!!.format(DATE_FMT), null); showOutfitPicker = false },
            onDismiss = { showOutfitPicker = false }
        )
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, "Previous month") }
        Text(
            "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
            fontSize = 20.sp, fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, "Next month") }
    }
}

@Composable
private fun DayOfWeekRow() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach { day ->
            Text(
                text = day, modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center, fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    calendarEntries: Map<String, OutfitCalendarEntry>,
    onDateClick: (LocalDate) -> Unit
) {
    val today       = LocalDate.now()
    val firstDay    = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value - 1  // Monday = 0
    val daysInMonth = yearMonth.lengthOfMonth()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).padding(horizontal = 8.dp),
        userScrollEnabled = false
    ) {
        items(startOffset) { Box(Modifier.aspectRatio(1f)) }

        items(daysInMonth) { index ->
            val day     = index + 1
            val date    = yearMonth.atDay(day)
            val dateKey = date.format(DATE_FMT)
            DayCell(
                day        = day,
                isToday    = date == today,
                isSelected = date == selectedDate,
                hasOutfit  = calendarEntries.containsKey(dateKey),
                isPast     = date.isBefore(today),
                onClick    = { onDateClick(date) }
            )
        }

        val remainder = (startOffset + daysInMonth) % 7
        if (remainder != 0) items(7 - remainder) { Box(Modifier.aspectRatio(1f)) }
    }
}

@Composable
private fun DayCell(
    day: Int, isToday: Boolean, isSelected: Boolean,
    hasOutfit: Boolean, isPast: Boolean, onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .aspectRatio(1f).padding(2.dp).clip(CircleShape)
            .background(when { isSelected -> primary; isToday -> primary.copy(0.15f); else -> Color.Transparent })
            .border(if (isToday && !isSelected) 1.5.dp else 0.dp,
                if (isToday && !isSelected) primary else Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                color = when { isSelected -> Color.White; isPast -> MaterialTheme.colorScheme.onSurface.copy(0.35f); else -> MaterialTheme.colorScheme.onSurface },
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasOutfit) {
                Box(Modifier.size(5.dp).clip(CircleShape)
                    .background(if (isSelected) Color.White else primary))
            }
        }
    }
}

@Composable
private fun UpcomingOutfitsList(
    calendarEntries: Map<String, OutfitCalendarEntry>,
    onEntryClick: (OutfitCalendarEntry) -> Unit
) {
    val today    = LocalDate.now()
    val upcoming = calendarEntries.values
        .filter { !LocalDate.parse(it.dateKey, DATE_FMT).isBefore(today) }
        .sortedBy { it.dateKey }
        .take(10)

    if (upcoming.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No outfits planned yet.\nTap any date above to assign one!",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        }
        return
    }

    Text("Upcoming", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(upcoming) { entry ->
            val date = LocalDate.parse(entry.dateKey, DATE_FMT)
            ListItem(
                headlineContent   = { Text(entry.outfitTitle, fontWeight = FontWeight.Medium) },
                supportingContent = {
                    Column {
                        Text(date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")))
                        if (entry.note.isNotBlank())
                            Text(entry.note, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 12.sp)
                    }
                },
                leadingContent = {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(0.12f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Checkroom, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.clickable { onEntryClick(entry) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutfitPickerSheet(
    date: LocalDate,
    outfits: List<OutfitModel>,
    currentEntry: OutfitCalendarEntry?,
    onAssign: (OutfitModel, String) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    var note           by remember(date) { mutableStateOf(currentEntry?.note ?: "") }
    var selectedOutfit by remember(date) { mutableStateOf(outfits.find { it.id == currentEntry?.outfitId }) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Note (e.g. Work, Wedding, Holiday)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            Text("Choose an outfit:", fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            if (outfits.isEmpty()) {
                Text("No outfits yet: create some in the Outfits section first.",
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    items(outfits) { outfit ->
                        val isChosen = outfit.id == selectedOutfit?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) MaterialTheme.colorScheme.primary.copy(0.12f) else Color.Transparent)
                                .clickable { selectedOutfit = outfit }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isChosen, onClick = { selectedOutfit = outfit })
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(outfit.title, fontWeight = FontWeight.Medium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (outfit.season.isNotBlank())
                                    Text(outfit.season, fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.55f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentEntry != null) {
                    OutlinedButton(
                        onClick = onRemove, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
                Button(
                    onClick = { selectedOutfit?.let { onAssign(it, note) } },
                    enabled = selectedOutfit != null,
                    modifier = Modifier.weight(1f)
                ) { Text(if (currentEntry != null) "Update" else "Save") }
            }
        }
    }
}