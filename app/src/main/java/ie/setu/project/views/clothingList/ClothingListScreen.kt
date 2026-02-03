package ie.setu.project.views.clothingList

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.viewmodels.ClothingListPresenter
import ie.setu.project.views.clothing.ClothingView
import ie.setu.project.views.outfit.OutfitView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingListScreen(
    presenter: ClothingListPresenter,
    context: Context,
    onNavigateToClothing: () -> Unit,
    onNavigateToOutfit: () -> Unit,
    onClothingItemClick: (ClosetOrganiserModel) -> Unit,
    onOutfitItemClick: (OutfitModel) -> Unit,
    onDeleteItemClick: (ClosetOrganiserModel) -> Unit,
    showSnackbar: (String, Int) -> Unit,
    updateWeatherUI: (WeatherResponse) -> Unit,
    showWeatherError: (String) -> Unit
) {
    val carouselItems by presenter.carouselItems.collectAsStateWithLifecycle()
    val weatherData by presenter.weatherData.collectAsStateWithLifecycle()
    val searchResults by presenter.searchResults.collectAsStateWithLifecycle()
    val showSearchResults by presenter.showSearchResults.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var currentCarouselPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(weatherData) {
        weatherData?.let { updateWeatherUI(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_heart),
                            contentDescription = "Heart icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Closet Organiser",
                            fontSize = 30.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_heart),
                            contentDescription = "Heart icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNavigateToOutfit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("Outfits")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onNavigateToClothing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("Clothes")
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        modifier = Modifier.padding(end = 8.dp),
                        tint = Color.Gray
                    )
                    TextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            presenter.updateSearchQuery(query)
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search") },
                        singleLine = true
                    )
                }
            }

            if (showSearchResults && searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    LazyColumn {
                        items(searchResults) { item ->
                            SearchResultItem(
                                item = item,
                                onClothingClick = onClothingItemClick,
                                onOutfitClick = onOutfitItemClick
                            )
                        }
                    }
                }
            }

            Text(
                text = "Recently Added",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF0000FF),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (carouselItems.isNotEmpty()) {
                            val currentItem = carouselItems.getOrNull(currentCarouselPage)
                            currentItem?.let { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onClothingItemClick(item) }
                                ) {
                                    val imageUrl = if (item::class.members.any { it.name == "imageUrl" }) {
                                        item::class.members.first { it.name == "imageUrl" }.call(item) as? String
                                    } else null

                                    imageUrl?.let { url ->
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(url)
                                                .build(),
                                            contentDescription = "Carousel item",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } ?: run {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No image")
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No items")
                            }
                        }

                        if (carouselItems.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentCarouselPage > 0) currentCarouselPage--
                                    },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Previous",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (currentCarouselPage < carouselItems.size - 1) currentCarouselPage++
                                    },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Next",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    if (carouselItems.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(carouselItems.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentCarouselPage) Color(0xFF6200EE) else Color.Gray
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                            )
                        )
                        .padding(16.dp)
                ) {
                    if (weatherData != null) {
                        val current = weatherData!!.current_weather
                        val condition = WeatherCondition.fromCode(current.weathercode, current.is_day)
                        Column {
                            Text(
                                text = "Current Weather",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(
                                        id = if (current.is_day == 1) condition.dayIcon else condition.nightIcon
                                    ),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "${current.temperature}°C",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = condition.description,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SearchResultItem(
    item: Any,
    onClothingClick: (ClosetOrganiserModel) -> Unit,
    onOutfitClick: (OutfitModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (item) {
                    is ClosetOrganiserModel -> onClothingClick(item)
                    is OutfitModel -> onOutfitClick(item)
                }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            when (item) {
                is ClosetOrganiserModel -> {
                    Text(item.title ?: "No title", fontWeight = FontWeight.Bold)
                    Text(item.description ?: "No description", fontSize = 12.sp)
                }
                is OutfitModel -> {
                    Text(item.title ?: "No title", fontWeight = FontWeight.Bold)
                    Text(item.description ?: "No description", fontSize = 12.sp)
                }
            }
        }
    }
}