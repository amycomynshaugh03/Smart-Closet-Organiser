package ie.setu.project.models.weather

import android.support.annotation.DrawableRes
import ie.setu.project.R
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current_weather: CurrentWeather,
    val hourly: HourlyUnits? = null,
    val daily: DailyUnits? = null
)

@Serializable
data class CurrentWeather(
    val temperature: Float,
    val windspeed: Float,
    val winddirection: Float,
    val weathercode: Int,
    val time: String,
    val is_day: Int
)

@Serializable
data class HourlyUnits(
    val time: List<String>,
    val temperature_2m: List<Float>,
    val precipitation_probability: List<Int>? = null,
    val weathercode: List<Int>
)

@Serializable
data class DailyUnits(
    val time: List<String>,
    val weathercode: List<Int>,
    val temperature_2m_max: List<Float>,
    val temperature_2m_min: List<Float>,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null
)

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

    companion object {
        fun fromCode(code: Int, isDay: Int): WeatherCondition {
            return values().firstOrNull { it.code == code } ?: CLEAR_SKY
        }
    }
}