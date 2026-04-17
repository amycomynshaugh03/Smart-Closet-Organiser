package ie.setu.project.views.ai

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse

private val LightGrey = Color(0xFFF0F0F0)

fun findMentionedItems(
    suggestion: String,
    clothingItems: List<ClosetOrganiserModel>
): List<ClosetOrganiserModel> {
    val lowerSuggestion = suggestion.lowercase()
    return clothingItems.filter { item ->
        item.title.isNotBlank() && lowerSuggestion.contains(item.title.lowercase())
    }.take(4)
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
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(LightGrey),
            contentAlignment = Alignment.Center
        ) {
            if (imageModel != null) {
                AsyncImage(model = imageModel, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(imageVector = Icons.Default.Checkroom, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = item.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 14.sp)
    }
}

@Composable
fun BoldMarkdownText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
    lineHeight: androidx.compose.ui.unit.TextUnit = 24.sp,
    color: Color = Color.Unspecified
) {
    val annotated = buildAnnotatedString {
        val pattern = Regex("""\*\*(.+?)\*\*""")
        var last = 0
        pattern.findAll(text).forEach { match ->
            append(text.substring(last, match.range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
            last = match.range.last + 1
        }
        append(text.substring(last))
    }
    Text(text = annotated, fontSize = fontSize, lineHeight = lineHeight, color = color)
}

@Composable
fun AiStylistScreen(
    weatherData: WeatherResponse?,
    clothingItems: List<ClosetOrganiserModel>,
    modifier: Modifier = Modifier,
    viewModel: AiStylistViewModel = hiltViewModel()
) {
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val userVibe by viewModel.userVibe.collectAsStateWithLifecycle()
    val current = weatherData?.current_weather
    val condition = current?.let { WeatherCondition.fromCode(it.weathercode, it.is_day) }

    val speechLauncher = rememberLauncherForActivityResult(
        StartActivityForResult()
    ) { result ->
        val matches = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = matches?.firstOrNull()
        if (!text.isNullOrBlank()) viewModel.setUserVibe(text)
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
        Text("AI Stylist", fontSize = 34.sp, fontFamily = FontFamily.Cursive, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)

        if (current != null && condition != null) {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Today's Weather", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        Text("${current.temperature}°C", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        Text(condition.description, color = Color.White.copy(0.9f), fontSize = 15.sp)
                    }
                    Icon(
                        painter = painterResource(if (current.is_day == 1) condition.dayIcon else condition.nightIcon),
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp)
                    )
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = LightGrey)) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Weather data not available", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            }
        }

        Surface(shape = RoundedCornerShape(50), color = LightGrey) {
            Text("${clothingItems.size} items in your wardrobe", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightGrey)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("Tell me your vibe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text("Describe how you're feeling or what you want to wear today.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                Button(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault().toLanguageTag())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me your vibe...")
                        }
                        speechLauncher.launch(intent)
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tap to speak")
                }
                if (userVibe.isNotBlank()) {
                    Text(
                        "Captured from voice, edit if needed",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedTextField(
                    value = userVibe,
                    onValueChange = { viewModel.setUserVibe(it) },
                    placeholder = {
                        Text(
                            if (userVibe.isNotBlank()) "Captured from voice, you can edit it here"
                            else "e.g. warm out but feeling a hoodie and shorts today…",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2, maxLines = 4
                )
            }
        }

        Button(
            onClick = { viewModel.getSuggestion(weatherData, clothingItems) },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(29.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(0.5f)
            ),
            enabled = aiState !is AiState.Loading,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (aiState is AiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Styling your outfit...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("Get Today's Outfit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        AnimatedVisibility(visible = aiState !is AiState.Idle, enter = fadeIn() + slideInVertically { it / 2 }) {
            when (val state = aiState) {
                is AiState.Success -> {
                    val mentionedItems = findMentionedItems(state.suggestion, clothingItems)
                    Card(
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = LightGrey),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Your Outfit for Today", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            if (mentionedItems.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                                    mentionedItems.forEach { item -> OutfitItemImage(item) }
                                }
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                Spacer(Modifier.height(16.dp))
                            }
                            BoldMarkdownText(
                                text = state.suggestion,
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.reset() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Get another suggestion")
                            }
                        }
                    }
                }
                is AiState.Error -> Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Oops!", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        Spacer(Modifier.height(6.dp))
                        Text(state.message, color = Color(0xFFC62828), fontSize = 14.sp, lineHeight = 20.sp)
                        TextButton(onClick = { viewModel.reset() }) {
                            Text("Try again", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is AiState.Loading -> Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGrey)
                ) {
                    Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                        Column {
                            Text("Analysing your wardrobe...", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Checking fashion rules & weather", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                }
                else -> {}
            }
        }

        if (aiState is AiState.Idle) {
            Text(
                "I'll check your wardrobe and today's weather, then suggest a complete outfit",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                textAlign = TextAlign.Center, fontSize = 14.sp, lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}