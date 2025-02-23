package ie.setu.project.api.weather

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.BuildConfig
import ie.setu.project.models.WeatherResponse
import ie.setu.project.utils.Config
import kotlinx.coroutines.launch
import timber.log.Timber



class WeatherViewModel : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

    @SuppressLint("TimberArgCount")
    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                val apiKey = Config.WEATHER_API_KEY
                val response = RetrofitInstance.api.getCurrentWeather(apiKey, city)
                _weatherData.postValue(response)
            } catch (e: Exception) {
                Timber.e("WeatherViewModel", "Error fetching weather: ${e.message}")
            }
        }
    }
}