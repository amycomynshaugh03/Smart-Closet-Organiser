package ie.setu.project.models.weather

import android.support.annotation.DrawableRes
import ie.setu.project.R
import kotlinx.serialization.Serializable

/**
 * Represents the response structure from a weather API.
 * Contains the geographical location (latitude and longitude), current weather information,
 * and optionally hourly and daily forecast data.
 *
 * @property latitude The latitude of the location.
 * @property longitude The longitude of the location.
 * @property current_weather The current weather details for the location.
 * @property hourly The hourly forecast details (optional).
 * @property daily The daily forecast details (optional).
 */
@Serializable
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current_weather: CurrentWeather,
    val hourly: HourlyUnits? = null,
    val daily: DailyUnits? = null
)

/**
 * Represents the current weather details.
 * Includes temperature, wind speed, wind direction, weather code, and the time of the report.
 *
 * @property temperature The current temperature in degrees Celsius.
 * @property windspeed The wind speed in meters per second.
 * @property winddirection The wind direction in degrees (meteorological).
 * @property weathercode The weather condition code (used to map to a specific weather type).
 * @property time The timestamp of the weather report.
 * @property is_day Indicates whether it is day (1) or night (0) at the location.
 */
@Serializable
data class CurrentWeather(
    val temperature: Float,
    val windspeed: Float,
    val winddirection: Float,
    val weathercode: Int,
    val time: String,
    val is_day: Int
)

/**
 * Represents hourly forecast data, including time, temperature, precipitation probability, and weather conditions.
 *
 * @property time A list of times for which hourly data is available.
 * @property temperature_2m A list of temperatures at 2 meters above the ground (for each time period).
 * @property precipitation_probability A list of precipitation probabilities (optional).
 * @property weathercode A list of weather condition codes (to map to specific weather types).
 */
@Serializable
data class HourlyUnits(
    val time: List<String>,
    val temperature_2m: List<Float>,
    val precipitation_probability: List<Int>? = null,
    val weathercode: List<Int>
)

/**
 * Represents daily forecast data, including time, temperature, weather conditions, sunrise and sunset times.
 *
 * @property time A list of dates for which daily data is available.
 * @property weathercode A list of weather condition codes (to map to specific weather types).
 * @property temperature_2m_max A list of maximum temperatures at 2 meters above the ground (for each day).
 * @property temperature_2m_min A list of minimum temperatures at 2 meters above the ground (for each day).
 * @property sunrise A list of sunrise times for each day (optional).
 * @property sunset A list of sunset times for each day (optional).
 */
@Serializable
data class DailyUnits(
    val time: List<String>,
    val weathercode: List<Int>,
    val temperature_2m_max: List<Float>,
    val temperature_2m_min: List<Float>,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null
)

/**
 * Enum class representing different weather conditions, each with a specific code, day and night icons, and a description.
 * The icons are tied to resources and can be used to display weather icons in the app.
 *
 * @property code The unique code representing the weather condition.
 * @property dayIcon The resource ID for the icon representing the condition during the day.
 * @property nightIcon The resource ID for the icon representing the condition during the night.
 * @property description A textual description of the weather condition.
 */
enum class WeatherCondition(
    val code: Int,
    @DrawableRes val dayIcon: Int,
    @DrawableRes val nightIcon: Int,
    val description: String
) {
    CLEAR_SKY(
        code = 0,
        dayIcon = R.drawable.ic_weather_sunny,
        nightIcon = R.drawable.ic_weather_clear_night,
        description = "Clear sky"
    ),
    PARTLY_CLOUDY(
        code = 1,
        dayIcon = R.drawable.ic_weather_partly_cloudy,
        nightIcon = R.drawable.ic_weather_cloudy_night,
        description = "Partly cloudy"
    ),
    CLOUDY(
        code = 2,
        dayIcon = R.drawable.ic_weather_cloudy,
        nightIcon = R.drawable.ic_weather_cloudy,
        description = "Cloudy"
    ),
    RAIN(
        code = 61,
        dayIcon = R.drawable.ic_weather_rainy,
        nightIcon = R.drawable.ic_weather_rainy,
        description = "Rain"
    ),
    SNOW(
        code = 71,
        dayIcon = R.drawable.ic_weather_snowy,
        nightIcon = R.drawable.ic_weather_snowy,
        description = "Snow"
    ),
    THUNDERSTORM(
        code = 95,
        dayIcon = R.drawable.ic_weather_thunder,
        nightIcon = R.drawable.ic_weather_thunder,
        description = "Thunderstorm"
    );

    /**
     * Provides a utility method to map raw API weather codes to [WeatherCondition] enum values.
     */
    companion object {
        /**
         * Converts a weather code and a day/night indicator to the corresponding [WeatherCondition].
         * If no match is found, it defaults to [CLEAR_SKY].
         *
         * @param code The weather condition code.
         * @param isDay The day/night indicator (1 for day, 0 for night).
         * @return The corresponding [WeatherCondition].
         */
        fun fromCode(code: Int, isDay: Int): WeatherCondition {
            return values().firstOrNull { it.code == code } ?: CLEAR_SKY
        }
    }
}
