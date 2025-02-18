package ie.setu.project.api.weather

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.BuildConfig
import com.github.ajalt.timberkt.Timber
import ie.setu.project.models.WeatherResponse
import kotlinx.coroutines.launch


//class WeatherViewModel : ViewModel() {
//
//    private val _weatherData = MutableLiveData<WeatherResponse>()
//    val weatherData: LiveData<WeatherResponse> get() = _weatherData
//
//    // Fetch weather data for a given city
//    fun fetchWeather(city: String) {
//        viewModelScope.launch {
//            try {
//                val apiKey = BuildConfig.WEATHER_API_KEY  // API key from BuildConfig
//                val response = RetrofitInstance.api.getCurrentWeather(apiKey, city)
//                _weatherData.postValue(response)  // Post the response to LiveData
//            } catch (e: Exception) {
//                Timber.i("WeatherViewModel", "Error fetching weather: ${e.message}")
//            }
//        }
//    }
//}