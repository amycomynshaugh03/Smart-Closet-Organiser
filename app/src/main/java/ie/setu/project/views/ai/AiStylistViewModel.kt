package ie.setu.project.views.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.weather.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ie.setu.project.BuildConfig
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Represents the state of an AI outfit suggestion request.
 */
sealed class AiState {
    /** No request has been made. */
    object Idle : AiState()
    /** A request is currently in progress. */
    object Loading : AiState()
    /** The request completed successfully with a [suggestion] string. */
    data class Success(val suggestion: String) : AiState()
    /** The request failed with an error [message]. */
    data class Error(val message: String) : AiState()
}

/**
 * ViewModel for the AI Stylist feature.
 *
 * Uses the Gemini 2.5 Flash model to generate a personalised outfit suggestion
 * based on the user's current wardrobe, local weather, and an optional mood/vibe input.
 * Fashion rules from [FashionRules] are included in the prompt to guide the AI.
 *
 * Injected via Hilt.
 */
@HiltViewModel
class AiStylistViewModel @Inject constructor() : ViewModel() {

    private val _aiState = MutableStateFlow<AiState>(AiState.Idle)

    /** The current state of the AI suggestion (idle, loading, success, or error). */
    val aiState: StateFlow<AiState> = _aiState.asStateFlow()

    private val model = GenerativeModel(modelName = "gemini-2.5-flash", apiKey = BuildConfig.GEMINI_API_KEY)

    private val _userVibe = MutableStateFlow("")

    /** The user's optional mood/vibe input for the outfit suggestion. */
    val userVibe: StateFlow<String> = _userVibe.asStateFlow()

    /**
     * Updates the user's vibe/mood input for the next suggestion.
     * @param vibe A free-text string describing the user's desired style or mood.
     */
    fun setUserVibe(vibe: String) { _userVibe.value = vibe }

    /**
     * Requests an outfit suggestion from Gemini based on the current weather and wardrobe.
     * Updates [aiState] to [AiState.Loading] while the request is in progress.
     *
     * @param weather The current [WeatherResponse] from the weather API, or null if unavailable.
     * @param clothingItems The user's current wardrobe items.
     */
    fun getSuggestion(weather: WeatherResponse?, clothingItems: List<ClosetOrganiserModel>) {
        viewModelScope.launch {
            _aiState.value = AiState.Loading
            try {
                val current = weather?.current_weather
                val tempC = current?.temperature ?: 15f
                val weatherCode = current?.weathercode ?: 0
                val isDay = current?.is_day ?: 1

                val weatherDesc = describeWeather(weatherCode, isDay)

                val wardrobeSummary = if (clothingItems.isEmpty()) {
                    "No items in wardrobe yet."
                } else {
                    clothingItems
                        .groupBy { it.category.ifBlank { "Uncategorised" } }
                        .entries
                        .distinctBy { it.key }
                        .joinToString("\n") { (cat, items) ->
                            val list = items.take(5).joinToString(", ") { item ->
                                buildString {
                                    append(item.title.ifBlank { "Unnamed" })
                                    if (item.colourPattern.isNotBlank()) append(" (${item.colourPattern})")
                                    if (item.season.isNotBlank()) append(" [${item.season}]")
                                }
                            }
                            "  $cat: $list"
                        }
                }

                val fashionRules = FashionRules.buildRulesPrompt(weatherCode, tempC)

                val vibeSection = if (userVibe.value.isNotBlank()) {
                    "User's Mood Request\nThe user said: \"${userVibe.value.trim()}\"\nTry to honour this while still being practical for the weather."
                } else ""

                val prompt = """
                You are a friendly personal stylist AI inside a wardrobe app called Smart Closet Organiser.
    
                ## Current Conditions
                Temperature: ${tempC}°C
                Weather: $weatherDesc
                Time of day: ${if (isDay == 1) "Daytime" else "Evening/Night"}
    
                ## User's Wardrobe
                $wardrobeSummary
    
                ## Fashion Rules You MUST Follow
                $fashionRules
    
                ## User Input
                $vibeSection
    
                ## Your Task
                Suggest a complete outfit using items from the wardrobe above.
                - Name actual items from the wardrobe
                - Each clothing category should only appear ONCE in your suggestion
                - Do not repeat or list the same category twice
                - Explain briefly why the outfit works (weather + colour)
                - Give one practical tip for today's weather
                - Keep it to 4-5 sentences, warm and conversational
                - No bullet points — write in natural sentences
                - End with a short encouraging sign-off
                """.trimIndent()

                val response = model.generateContent(prompt)
                val text = response.text
                if (text.isNullOrBlank()) {
                    _aiState.value = AiState.Error("Gemini returned an empty response. Please try again.")
                } else {
                    _aiState.value = AiState.Success(text.trim())
                }
            } catch (e: Exception) {
                Timber.e(e, "Gemini API call failed")
                _aiState.value = AiState.Error("Something went wrong: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private fun describeWeather(code: Int, isDay: Int): String = when {
        code == 0 -> if (isDay == 1) "Clear sky" else "Clear night"
        code in 1..2 -> "Partly cloudy"
        code == 3 -> "Overcast"
        code in 51..67 -> "Rain"
        code in 71..77 -> "Snow"
        code in 80..82 -> "Rain showers"
        code >= 95 -> "Thunderstorm"
        else -> "Cloudy"
    }

    /** Resets [aiState] to idle and clears the user vibe input. */
    fun reset() {
        _aiState.value = AiState.Idle
        _userVibe.value = ""
    }
}