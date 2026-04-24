package ie.setu.project.views.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.project.preferences.LocationPreference
import ie.setu.project.preferences.LocationPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

private interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

/**
 * Retrofit response wrapper for the Open-Meteo geocoding API.
 * @property results A list of matching [GeoResult] objects, or null if no results.
 */
data class GeocodingResponse(val results: List<GeoResult>? = null)

/**
 * Retrofit response wrapper for the Open-Meteo geocoding API.
 * @property results A list of matching [GeoResult] objects, or null if no results.
 */
data class GeoResult(
    /** A formatted display string combining name and country (e.g. "Dublin, Ireland"). */
    val name: String,
    val country: String?,
    val latitude: Double,
    val longitude: Double
) {
    val displayName get() = if (country != null) "$name, $country" else name
}

/**
 * ViewModel for the Settings screen.
 *
 * Allows the user to search for a city and save it as their preferred weather location.
 * Uses the Open-Meteo geocoding API to resolve city names to coordinates, which are
 * then persisted via [LocationPreferencesRepository].
 *
 * Injected via Hilt.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val locationPrefs: LocationPreferencesRepository
) : ViewModel() {

    private val geocodingApi = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeocodingApi::class.java)

    private val _currentLocation = MutableStateFlow<LocationPreference?>(null)

    /** The currently saved location preference. Null until the DataStore emits. */
    val currentLocation: StateFlow<LocationPreference?> = _currentLocation.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeoResult>>(emptyList())

    /** Geocoding search results for the current query. */
    val searchResults: StateFlow<List<GeoResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)

    /** True while a geocoding API request is in progress. */
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        viewModelScope.launch {
            locationPrefs.locationFlow.collect { _currentLocation.value = it }
        }
    }

    /**
     * Searches for cities matching the given query using the Open-Meteo geocoding API.
     * Clears results if the query is blank.
     * @param query The city name to search for.
     */
    fun searchCity(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val response = geocodingApi.search(query)
                _searchResults.value = response.results ?: emptyList()
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * Persists the selected city as the user's location preference and clears search results.
     * @param result The [GeoResult] the user selected.
     */
    fun selectCity(result: GeoResult) {
        viewModelScope.launch {
            locationPrefs.saveLocation(result.displayName, result.latitude, result.longitude)
            _searchResults.value = emptyList()
        }
    }

    /** Clears the current search results without saving. */
    fun clearSearch() {
        _searchResults.value = emptyList()
    }
}