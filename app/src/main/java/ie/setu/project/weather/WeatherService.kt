package ie.setu.project.weather

import ie.setu.project.models.weather.WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Open-Meteo weather forecast API.
 *
 * Base URL: `https://api.open-meteo.com/`
 */
interface WeatherApi {

    /**
     * Fetches current weather and forecast data for a given location.
     *
     * @param lat Latitude of the location.
     * @param lon Longitude of the location.
     * @param currentWeather Whether to include current weather data (default: true).
     * @param hourly Comma-separated hourly variables to include (default: temperature + weather code).
     * @param daily Comma-separated daily variables to include (default: weather code + min/max temp).
     * @param timezone Timezone for the response, "auto" uses the location's local timezone.
     * @return A [WeatherResponse] containing current, hourly, and daily weather data.
     */
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "temperature_2m,weathercode",
        @Query("daily") daily: String = "weathercode,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}

     /**
     * Service wrapper around [WeatherApi] for fetching weather data from Open-Meteo.
     *
     * Builds a Retrofit instance internally with Gson conversion.
     * Used by [ClothingListPresenter] and [AiStylistViewModel] to fetch the current weather.
     */
    class WeatherService {
        private val api: WeatherApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    /**
     * Fetches weather data for the given coordinates.
     *
     * @param lat Latitude of the location.
     * @param lon Longitude of the location.
     * @return A [WeatherResponse] from the Open-Meteo API.
     */
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return api.getWeather(lat, lon)
    }
}