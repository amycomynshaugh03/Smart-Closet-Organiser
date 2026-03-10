package ie.setu.project.views.ai

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse

private val Purple = Color(0xFF6200EE)
private val DarkText = Color(0xFF1A0033)
private val LightGrey = Color(0xFFF0F0F0)

// Finds clothing items mentioned in the AI suggestion text
fun findMentionedItems(
    suggestion: String,
    clothingItems: List<ClosetOrganiserModel>
): List<ClosetOrganiserModel> {
    val lowerSuggestion = suggestion.lowercase()
    return clothingItems.filter { item ->
        item.title.isNotBlank() && lowerSuggestion.contains(item.title.lowercase())
    }.take(4) // max 4 images
}

@Composable
fun OutfitItemImage(item: ClosetOrganiserModel) {
    val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() }
        ?: item.image?.takeIf { it != Uri.EMPTY }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightGrey),
            contentAlignment = Alignment.Center
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Checkroom,
                    contentDescription = null,
                    tint = Purple.copy(0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.title,
            fontSize = 11.sp,
            color = DarkText,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun AiStylistScreen(
    weatherData: WeatherResponse?,
    clothingItems: List<ClosetOrganiserModel>,
    modifier: Modifier = Modifier,
    viewModel: AiStylistViewModel = hiltViewModel()
) {
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val current = weatherData?.current_weather
    val condition = current?.let { WeatherCondition.fromCode(it.weathercode, it.is_day) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(52.dp)
        )
        Text(
            "AI Stylist",
            fontSize = 34.sp,
            fontFamily = FontFamily.Cursive,
            color = DarkText,
            fontWeight = FontWeight.Bold
        )

        if (current != null && condition != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Purple),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Today's Weather", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        Text(
                            "${current.temperature}°C",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(condition.description, color = Color.White.copy(0.9f), fontSize = 15.sp)
                    }
                    Icon(
                        painter = painterResource(
                            if (current.is_day == 1) condition.dayIcon else condition.nightIcon
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightGrey)
            ) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Weather data not available", color = DarkText.copy(0.6f), fontSize = 14.sp)
                }
            }
        }

        Surface(shape = RoundedCornerShape(50), color = LightGrey) {
            Text(
                "${clothingItems.size} items in your wardrobe",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = DarkText,
                fontSize = 13.sp
            )
        }

        Button(
            onClick = { viewModel.getSuggestion(weatherData, clothingItems) },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(29.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple,
                contentColor = Color.White,
                disabledContainerColor = Purple.copy(0.5f),
                disabledContentColor = Color.White.copy(0.5f)
            ),
            enabled = aiState !is AiState.Loading,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (aiState is AiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(Modifier.width(10.dp))
                Text("Styling your outfit...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("Get Today's Outfit ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        AnimatedVisibility(
            visible = aiState !is AiState.Idle,
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            when (val state = aiState) {
                is AiState.Success -> {
                    val mentionedItems = findMentionedItems(state.suggestion, clothingItems)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = LightGrey),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "Your Outfit for Today",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Purple
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Purple.copy(alpha = 0.2f)
                            )


                            if (mentionedItems.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        8.dp,
                                        Alignment.CenterHorizontally
                                    )
                                ) {
                                    mentionedItems.forEach { item ->
                                        OutfitItemImage(item)
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = Purple.copy(alpha = 0.1f))
                                Spacer(Modifier.height(16.dp))
                            }

                            Text(
                                text = state.suggestion,
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = DarkText
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Get another suggestion")
                            }
                        }
                    }
                }

                is AiState.Error -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Oops!", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        Spacer(Modifier.height(6.dp))
                        Text(state.message, color = Color(0xFFC62828), fontSize = 14.sp, lineHeight = 20.sp)
                        TextButton(onClick = { viewModel.reset() }) {
                            Text("Try again", color = Purple, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is AiState.Loading -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGrey)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Purple,
                            strokeWidth = 2.dp
                        )
                        Column {
                            Text(
                                "Analysing your wardrobe...",
                                color = DarkText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Checking fashion rules & weather",
                                color = DarkText.copy(0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                else -> {}
            }
        }

        if (aiState is AiState.Idle) {
            Text(
                "I'll check your wardrobe and today's weather, then suggest a complete outfit",
                color = DarkText.copy(0.6f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}