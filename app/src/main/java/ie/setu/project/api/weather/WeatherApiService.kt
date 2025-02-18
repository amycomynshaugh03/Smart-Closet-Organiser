package ie.setu.project.api.weather

import ie.setu.project.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    // Endpoint to get current weather data
    @GET("current.json")  // Endpoint to fetch current weather info
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,  // API Key as parameter
        @Query("q") city: String      // City for which to get weather
    ): WeatherResponse
}