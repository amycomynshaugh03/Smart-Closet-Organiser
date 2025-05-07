package ie.setu.project.weather

import ie.setu.project.models.weather.WeatherResponse
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Service class responsible for fetching weather data from the Open-Meteo API.
 */
class WeatherService {

    // HTTP client for making API requests, using the CIO engine and Content Negotiation for JSON parsing
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            // Set up JSON serialization with lenient parsing and ignoring unknown keys
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Fetches the weather data for a given latitude and longitude from the Open-Meteo API.
     *
     * @param lat Latitude of the location to fetch weather for.
     * @param lon Longitude of the location to fetch weather for.
     * @return A WeatherResponse object containing the weather data.
     */
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return client.get("https://api.open-meteo.com/v1/forecast") {
            // Adding the query parameters for the API request
            parameter("latitude", lat)
            parameter("longitude", lon)
            parameter("current_weather", true)
            parameter("hourly", "temperature_2m,weathercode")
            parameter("daily", "weathercode,temperature_2m_max,temperature_2m_min")
            parameter("timezone", "auto")
        }.body()  // Parse the response body into a WeatherResponse object
    }
}
