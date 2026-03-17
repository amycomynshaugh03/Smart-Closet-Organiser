package ie.setu.project.views.clothingList

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse
import kotlinx.coroutines.delay
import ie.setu.project.views.ai.AiStylistScreen

private val Teal = Color(0xFF007A90)
private val TealDark = Color(0xFF004F60)

@Composable
fun OfflineSyncBanner(syncState: SyncState) {
    var showSyncedConfirm by remember { mutableStateOf(false) }
    LaunchedEffect(syncState) {
        if (syncState == SyncState.SYNCED) {
            showSyncedConfirm = true
            delay(3_000)
            showSyncedConfirm = false
        }
    }

    val (bgColor, icon, message) = when (syncState) {
        SyncState.SYNCING       -> Triple(Color(0xFF1565C0), Icons.Default.Sync,      "Syncing with cloud…")
        SyncState.SYNCED        -> Triple(Color(0xFF2E7D32), Icons.Default.CloudDone, "Up to date")
        SyncState.OFFLINE_CACHE -> Triple(Color(0xFFE65100), Icons.Default.CloudOff,  "Offline — changes will sync when you're back online")
        SyncState.OFFLINE_BACKUP-> Triple(Color(0xFFC62828), Icons.Default.CloudOff,  "Offline — showing local backup (read-only)")
    }

    val visible = syncState == SyncState.SYNCING ||
            syncState == SyncState.OFFLINE_CACHE ||
            syncState == SyncState.OFFLINE_BACKUP ||
            showSyncedConfirm

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = message, color = Color.White, fontSize = 13.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingListScreen(
    presenter: ClothingListPresenter,
    context: Context,
    syncState: SyncState,
    onExportWardrobe: () -> Unit,
    onNavigateToClothing: () -> Unit,
    onNavigateToOutfit: () -> Unit,
    onClothingItemClick: (ClosetOrganiserModel) -> Unit,
    onOutfitItemClick: (OutfitModel) -> Unit,
    onDeleteItemClick: (ClosetOrganiserModel) -> Unit,
    showSnackbar: (String, Int) -> Unit,
    updateWeatherUI: (WeatherResponse) -> Unit,
    showWeatherError: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val carouselItems by presenter.carouselItems.collectAsStateWithLifecycle()
    val clothingItems by presenter.clothingItems.collectAsStateWithLifecycle()
    val weatherData by presenter.weatherData.collectAsStateWithLifecycle()
    val weatherError by presenter.weatherError.collectAsStateWithLifecycle()
    val searchResults by presenter.searchResults.collectAsStateWithLifecycle()
    val showSearchResults by presenter.showSearchResults.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var currentCarouselPage by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(carouselItems.size) {
        if (carouselItems.isEmpty()) currentCarouselPage = 0
        else if (currentCarouselPage > carouselItems.lastIndex) currentCarouselPage = carouselItems.lastIndex
    }

    var lastWeatherKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(weatherData) {
        weatherData?.let { w ->
            val c = w.current_weather
            val key = "${c.temperature}_${c.weathercode}_${c.is_day}"
            if (key != lastWeatherKey) { lastWeatherKey = key; updateWeatherUI(w) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
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
                        Text("Closet Organiser", fontSize = 30.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(painter = painterResource(id = R.drawable.ic_heart), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Teal,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Teal, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Checkroom, contentDescription = "Wardrobe") },
                    label = { Text("Wardrobe") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(0.5f),
                        unselectedTextColor = Color.White.copy(0.5f),
                        indicatorColor = Color.White.copy(0.2f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Stylist") },
                    label = { Text("AI Stylist") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(0.5f),
                        unselectedTextColor = Color.White.copy(0.5f),
                        indicatorColor = Color.White.copy(0.2f)
                    )
                )
            }
        }
    ) { paddingValues ->

        if (selectedTab == 1) {
            AiStylistScreen(
                weatherData = weatherData,
                clothingItems = clothingItems,
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {

            OfflineSyncBanner(syncState = syncState)

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onNavigateToOutfit, colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text("Outfits") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onNavigateToClothing, colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text("Clothes") }
                Row(modifier = Modifier.weight(1f).padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = "Search", modifier = Modifier.padding(end = 8.dp), tint = Color.Gray)
                    TextField(
                        value = searchQuery,
                        onValueChange = { query -> searchQuery = query; presenter.updateSearchQuery(query) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search") },
                        singleLine = true
                    )
                }
            }

            if (showSearchResults && searchResults.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().height(320.dp).padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                    LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchResults) { item ->
                            SearchResultItem(
                                item = item,
                                onClothingClick = { clothing -> presenter.hideSearchResults(); searchQuery = ""; onClothingItemClick(clothing) },
                                onOutfitClick = { outfit -> presenter.hideSearchResults(); searchQuery = ""; onOutfitItemClick(outfit) }
                            )
                        }
                    }
                }
            }

            Text("Recently Added", style = MaterialTheme.typography.headlineSmall, color = Teal, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 16.dp))

            Card(modifier = Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(12.dp)) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        if (carouselItems.isNotEmpty()) {
                            carouselItems.getOrNull(currentCarouselPage)?.let { item ->
                                Box(modifier = Modifier.fillMaxSize().clickable { onClothingItemClick(item) }) {
                                    val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
                                    val ok = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()
                                    if (ok) {
                                        AsyncImage(model = imageModel, contentDescription = "Carousel item", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) { Text("No image") }
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) { Text("No items") }
                        }

                        if (carouselItems.size > 1) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (currentCarouselPage > 0) currentCarouselPage-- }, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous", tint = Color.White)
                                }
                                IconButton(onClick = { if (currentCarouselPage < carouselItems.size - 1) currentCarouselPage++ }, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
                                }
                            }
                        }
                    }

                    if (carouselItems.size > 1) {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center) {
                            repeat(carouselItems.size) { index ->
                                Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(if (index == currentCarouselPage) Teal else Color.Gray))
                            }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().background(brush = Brush.verticalGradient(colors = listOf(Teal, TealDark))).padding(16.dp)) {
                    when {
                        weatherData != null -> {
                            val current = weatherData!!.current_weather
                            val condition = WeatherCondition.fromCode(current.weathercode, current.is_day)
                            Column {
                                Text("Current Weather", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(id = if (current.is_day == 1) condition.dayIcon else condition.nightIcon), contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("${current.temperature}°C", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(condition.description, color = Color.White)
                                    }
                                }
                            }
                        }
                        weatherError != null -> {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("Weather unavailable", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SearchResultItem(item: Any, onClothingClick: (ClosetOrganiserModel) -> Unit, onOutfitClick: (OutfitModel) -> Unit) {
    val thumbModel: Any? = when (item) {
        is ClosetOrganiserModel -> item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
        is OutfitModel -> item.clothingItems.firstOrNull()?.let { it.imageUrl.takeIf { u -> u.isNotBlank() } ?: it.image }
        else -> null
    }
    val isValidThumb = thumbModel != null && thumbModel != Uri.EMPTY && thumbModel.toString().isNotBlank()
    val title = when (item) { is ClosetOrganiserModel -> item.title.ifBlank { "No title" }; is OutfitModel -> item.title.ifBlank { "No title" }; else -> "Unknown" }
    val subtitle = when (item) { is ClosetOrganiserModel -> item.description.ifBlank { "No description" }; is OutfitModel -> item.description.ifBlank { "No description" }; else -> "" }
    val icon = when (item) { is OutfitModel -> Icons.Default.Style; else -> Icons.Default.Checkroom }

    Card(modifier = Modifier.fillMaxWidth().clickable { when (item) { is ClosetOrganiserModel -> onClothingClick(item); is OutfitModel -> onOutfitClick(item) } }, shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(Color.LightGray), contentAlignment = Alignment.Center) {
                if (isValidThumb) AsyncImage(model = thumbModel, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, maxLines = 2)
            }
            if (item is OutfitModel) {
                Row(modifier = Modifier.padding(start = 8.dp).height(42.dp).widthIn(max = 150.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    item.clothingItems.take(4).forEach { clothing ->
                        val miniModel: Any? = clothing.imageUrl.takeIf { it.isNotBlank() } ?: clothing.image
                        val ok = miniModel != null && miniModel != Uri.EMPTY && miniModel.toString().isNotBlank()
                        Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color.LightGray), contentAlignment = Alignment.Center) {
                            if (ok) AsyncImage(model = miniModel, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Icon(imageVector = Icons.Default.Checkroom, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}