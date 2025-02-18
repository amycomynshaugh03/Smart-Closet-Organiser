package ie.setu.project.models


data class WeatherResponse(
    val location: Location,
    val current: CurrentWeather
)

data class Location(
    val name: String,
    val country: String
)

data class CurrentWeather(
    val temp_c: Float,
    val condition: Condition,
    val humidity: Int
)

data class Condition(
    val text: String
)
