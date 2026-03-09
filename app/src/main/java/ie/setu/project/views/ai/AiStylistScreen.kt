package ie.setu.project.views.ai

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse

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
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D001A), Color(0xFF3D0066), Color(0xFF6200EE))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {


        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(52.dp)
        )
        Text(
            "AI Stylist ",
            fontSize = 34.sp,
            fontFamily = FontFamily.Cursive,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )



        if (current != null && condition != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Today's Weather", color = Color.White.copy(0.6f), fontSize = 12.sp)
                        Text(
                            "${current.temperature}°C",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(condition.description, color = Color.White.copy(0.8f), fontSize = 15.sp)
                    }
                    Icon(
                        painter = painterResource(
                            if (current.is_day == 1) condition.dayIcon else condition.nightIcon
                        ),
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Weather data not available", color = Color.White.copy(0.6f), fontSize = 14.sp)
                }
            }
        }


        Surface(shape = RoundedCornerShape(50), color = Color.White.copy(0.1f)) {
            Text(
                "${clothingItems.size} items in your wardrobe",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White.copy(0.7f),
                fontSize = 13.sp
            )
        }

        Button(
            onClick = { viewModel.getSuggestion(weatherData, clothingItems) },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(29.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color(0xFF0D001A),
                disabledContainerColor = Color(0xFFFFD700).copy(0.5f),
                disabledContentColor = Color(0xFF0D001A).copy(0.5f)
            ),
            enabled = aiState !is AiState.Loading
        ) {
            if (aiState is AiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color(0xFF0D001A),
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
                is AiState.Success -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Your Outfit for Today",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1A0033)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFF6200EE).copy(alpha = 0.2f)
                        )
                        Text(
                            text = state.suggestion,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF1A0033)
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.reset() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6200EE))
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Get another suggestion")
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
                            Text("Try again", color = Color(0xFF6200EE), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is AiState.Loading -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFFFD700),
                            strokeWidth = 2.dp
                        )
                        Column {
                            Text(
                                "Analysing your wardrobe...",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Checking fashion rules & weather",
                                color = Color.White.copy(0.6f),
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
                color = Color.White.copy(0.6f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}