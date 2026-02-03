package ie.setu.project.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.weather.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClothingListPresenter(application: Application) : AndroidViewModel(application) {
    private val app: MainApp = application as MainApp
    private val weatherService = WeatherService()

    private val _carouselItems = MutableStateFlow<List<ClosetOrganiserModel>>(emptyList())
    val carouselItems: StateFlow<List<ClosetOrganiserModel>> = _carouselItems.asStateFlow()

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Any>>(emptyList())
    val searchResults: StateFlow<List<Any>> = _searchResults.asStateFlow()

    private val _showSearchResults = MutableStateFlow(false)
    val showSearchResults: StateFlow<Boolean> = _showSearchResults.asStateFlow()

    init {
        loadCarouselData()
        fetchWeather()
    }

    private fun loadCarouselData() {
        viewModelScope.launch {
            _carouselItems.value = app.clothingItems.findAll()
                .takeLast(5)
                .reversed()
        }
    }

    fun fetchWeather() {
        viewModelScope.launch {
            try {
                val weather = weatherService.getWeather(53.3498, -6.2603)
                _weatherData.value = weather
            } catch (e: Exception) {
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty()) {
            performSearch(query)
            _showSearchResults.value = true
        } else {
            _showSearchResults.value = false
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            val results = mutableListOf<Any>()
            val clothingResults = app.clothingItems.findAll().filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.colourPattern.contains(query, ignoreCase = true) ||
                        it.season.contains(query, ignoreCase = true)
            }
            val outfitResults = app.outfitItems.findAll().filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.season.contains(query, ignoreCase = true) ||
                        it.clothingItems.any { clothing ->
                            clothing.title.contains(query, ignoreCase = true) ||
                                    clothing.description.contains(query, ignoreCase = true)
                        }
            }
            results.addAll(clothingResults)
            results.addAll(outfitResults)
            _searchResults.value = results
        }
    }

    fun hideSearchResults() {
        _showSearchResults.value = false
        _searchQuery.value = ""
    }

    fun refreshCarousel() {
        loadCarouselData()
    }
}