package ie.setu.project.weather

import ie.setu.project.models.weather.WeatherResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json

class WeatherService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
    }

    suspend fun getFullWeather(lat: Double, lon: Double): WeatherResponse {
        return client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", lat)
            parameter("longitude", lon)
            parameter("current_weather", true)
            parameter("hourly", "temperature_2m,precipitation_probability,weathercode")
            parameter("daily", "weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset")
            parameter("timezone", "auto")
        }.body()
    }
}