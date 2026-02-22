package ie.setu.project.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.project.firebase.clothing.ClothingFirestoreRepository
import ie.setu.project.firebase.outfit.OutfitFirestoreRepository
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.weather.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ClothingListPresenter @Inject constructor(
    private val authService: AuthService,
    private val clothingRepo: ClothingFirestoreRepository,
    private val outfitRepo: OutfitFirestoreRepository
) : ViewModel() {

    private val weatherService = WeatherService()

    private var cachedClothing: List<ClosetOrganiserModel> = emptyList()
    private var cachedOutfits: List<OutfitModel> = emptyList()

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
        fetchWeather()
        refreshFromFirestore()
    }

    fun refreshFromFirestore() {
        val uid = authService.currentUserId
        if (uid.isBlank()) {
            cachedClothing = emptyList()
            cachedOutfits = emptyList()
            _carouselItems.value = emptyList()
            _searchResults.value = emptyList()
            _showSearchResults.value = false
            return
        }

        viewModelScope.launch {
            try {

                cachedClothing = clothingRepo.getAll(uid)
                cachedOutfits = outfitRepo.getAll(uid)

                _carouselItems.value = cachedClothing.takeLast(5).reversed()

                val q = _searchQuery.value.trim()
                if (q.isNotBlank()) performSearch(q)
            } catch (e: Exception) {
                Timber.e(e, "Firestore home read failed")
            }
        }
    }

    fun fetchWeather() {
        viewModelScope.launch {
            try {
                val weather = weatherService.getWeather(53.3498, -6.2603)
                _weatherData.value = weather
            } catch (_: Exception) {
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val q = query.trim()
        if (q.isNotEmpty()) {
            performSearch(q)
            _showSearchResults.value = true
        } else {
            _showSearchResults.value = false
            _searchResults.value = emptyList()
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            val results = mutableListOf<Any>()

            val clothingResults = cachedClothing.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.colourPattern.contains(query, ignoreCase = true) ||
                        it.season.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }

            val outfitResults = cachedOutfits.filter {
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

    fun deleteClothing(item: ClosetOrganiserModel) {
        val uid = authService.currentUserId
        if (uid.isBlank()) {
            Timber.w("deleteClothing: uid blank")
            return
        }

        viewModelScope.launch {
            try {
                Timber.i("deleteClothing: deleting id=${item.id} title=${item.title}")
                clothingRepo.delete(uid, item.id)
                refreshFromFirestore()
            } catch (e: Exception) {
                Timber.e(e, "Firestore delete failed for id=${item.id}")
            }
        }
    }
    fun hideSearchResults() {
        _showSearchResults.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}