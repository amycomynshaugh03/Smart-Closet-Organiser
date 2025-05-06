package ie.setu.project.models.weather

import ie.setu.project.R
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current_weather: CurrentWeather,
    val hourly: HourlyData? = null,
    val daily: DailyData? = null
)

@Serializable
data class CurrentWeather(
    val temperature: Float,
    val windspeed: Float,
    val winddirection: Float,
    val weathercode: Int,
    val time: Instant
)

@Serializable
data class HourlyData(
    val time: List<Instant>,
    val temperature_2m: List<Float>
)

@Serializable
data class DailyData(
    val time: List<Instant>,
    val temperature_2m_max: List<Float>,
    val temperature_2m_min: List<Float>
)

enum class WeatherCondition(val code: Int, val iconRes: Int) {
    CLEAR(0, R.drawable.ic_sunny),
    PARTLY_CLOUDY(1, R.drawable.ic_partly_cloudy),
    CLOUDY(2, R.drawable.ic_cloudy),
    FOG(45, R.drawable.ic_fog),
    DRIZZLE(51, R.drawable.ic_drizzle),
    RAIN(61, R.drawable.ic_rain),
    SNOW(71, R.drawable.ic_snow),
    THUNDERSTORM(95, R.drawable.ic_thunderstorm);

    companion object {
        fun fromCode(code: Int): WeatherCondition {
            return values().firstOrNull { it.code == code } ?: CLEAR
        }
    }
}