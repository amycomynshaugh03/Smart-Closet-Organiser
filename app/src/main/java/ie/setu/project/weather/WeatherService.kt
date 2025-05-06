package ie.setu.project.weather

import ie.setu.project.models.weather.WeatherResponse
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

class WeatherService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
    }

    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", lat)
            parameter("longitude", lon)
            parameter("current_weather", true)
            parameter("hourly", "temperature_2m,weathercode")
            parameter("daily", "weathercode,temperature_2m_max,temperature_2m_min")
        }.body()
    }
}