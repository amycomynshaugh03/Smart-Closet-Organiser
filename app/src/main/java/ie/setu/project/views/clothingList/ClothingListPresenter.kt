package ie.setu.project.views.clothingList

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ie.setu.project.firebase.clothing.ClothingFirestoreRepository
import ie.setu.project.firebase.outfit.OutfitFirestoreRepository
import ie.setu.project.firebase.services.AuthService
import ie.setu.project.firebase.storage.ImageStorageRepository
import ie.setu.project.models.LocalBackupRepository
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.preferences.LocationPreferencesRepository
import ie.setu.project.weather.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Represents the current data synchronisation state of the clothing list.
 */
enum class SyncState {
    /** A Firestore sync is currently in progress. */
    SYNCING,
    /** Data was successfully synced from Firestore. */
    SYNCED,
    /** Data is being served from Firestore's offline cache. */
    OFFLINE_CACHE,
    /** Device is offline; data is being served from the local SQLite backup. */
    OFFLINE_BACKUP,
    /** A sync attempt failed unexpectedly. */
    SYNC_ERROR
}

/**
 * ViewModel/Presenter for the main clothing list screen (ClothingListView).
 *
 * Combines MVP-style presentation logic with Android ViewModel lifecycle management.
 * Responsible for loading clothing and outfit data from Firestore (or a local SQLite backup
 * when offline), fetching weather data, handling search, and managing data exports.
 *
 * Injected via Hilt.
 *
 * @param authService Provides the current user's authentication state and UID.
 * @param clothingRepo Firestore repository for clothing item operations.
 * @param outfitRepo Firestore repository for outfit operations.
 * @param imageStorageRepo Firebase Storage repository for image deletion.
 * @param localBackup Local SQLite backup repository used when offline.
 * @param locationPrefs DataStore repository for the user's saved weather location.
 * @param context Application context used for connectivity checks.
 */
@HiltViewModel
class ClothingListPresenter @Inject constructor(
    private val authService: AuthService,
    private val clothingRepo: ClothingFirestoreRepository,
    private val outfitRepo: OutfitFirestoreRepository,
    private val imageStorageRepo: ImageStorageRepository,
    private val localBackup: LocalBackupRepository,
    private val locationPrefs: LocationPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val weatherService = WeatherService()

    private var cachedClothing: List<ClosetOrganiserModel> = emptyList()
    private var cachedOutfits: List<OutfitModel> = emptyList()

    private val _carouselItems = MutableStateFlow<List<ClosetOrganiserModel>>(emptyList())
    /** The most recent 5 clothing items for the home screen carousel (most recently added first). */
    val carouselItems: StateFlow<List<ClosetOrganiserModel>> = _carouselItems.asStateFlow()

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    /** The latest weather data for the user's saved location. Null if unavailable. */
    val weatherData: StateFlow<WeatherResponse?> = _weatherData.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    /** The current search query string. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Any>>(emptyList())
    /** Combined clothing and outfit search results matching the current query. */
    val searchResults: StateFlow<List<Any>> = _searchResults.asStateFlow()

    private val _showSearchResults = MutableStateFlow(false)
    /** True when search results should be shown in the UI. */
    val showSearchResults: StateFlow<Boolean> = _showSearchResults.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.SYNCING)
    /** The current Firestore synchronisation state. */
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _exportJson = MutableStateFlow<String?>(null)
    /** The exported wardrobe JSON string, or null if no export is pending. */
    val exportJson: StateFlow<String?> = _exportJson.asStateFlow()

    private val _clothingItems = MutableStateFlow<List<ClosetOrganiserModel>>(emptyList())
    /** All clothing items for the current user. */
    val clothingItems: StateFlow<List<ClosetOrganiserModel>> = _clothingItems.asStateFlow()

    private val _weatherError = MutableStateFlow<String?>(null)
    /** An error message from the last weather fetch, or null if no error occurred. */
    val weatherError: StateFlow<String?> = _weatherError.asStateFlow()

    init {
        viewModelScope.launch {
            locationPrefs.locationFlow.collect {
                fetchWeather()
            }
        }
        refreshFromFirestore()
    }

    /**
     * Checks whether the device currently has an active internet connection.
     *
     * @return True if internet is available, false otherwise.
     */
    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Refreshes clothing and outfit data from Firestore, falling back to the local SQLite
     * backup if the device is offline. Clears all state if the user is not authenticated.
     */
    fun refreshFromFirestore() {
        val uid = authService.currentUserId
        if (uid.isBlank()) {
            cachedClothing = emptyList()
            cachedOutfits = emptyList()
            _carouselItems.value = emptyList()
            _clothingItems.value = emptyList()
            _searchResults.value = emptyList()
            _showSearchResults.value = false
            _syncState.value = SyncState.SYNCED
            return
        }

        if (!isOnline()) {
            viewModelScope.launch {
                val backupItems = localBackup.getAllLocal()
                cachedClothing = backupItems
                _carouselItems.value = backupItems.sortedBy { it.id }.takeLast(5).reversed()
                _clothingItems.value = backupItems
                _syncState.value = SyncState.OFFLINE_BACKUP
            }
            return
        }

        _syncState.value = SyncState.SYNCING

        viewModelScope.launch {
            try {
                cachedClothing = clothingRepo.getAll(uid)
                cachedOutfits = outfitRepo.getAll(uid)
                _carouselItems.value = cachedClothing.sortedBy { it.id }.takeLast(5).reversed()
                _clothingItems.value = cachedClothing

                val q = _searchQuery.value.trim()
                if (q.isNotBlank()) performSearch(q)

                localBackup.backupFromFirestore(cachedClothing)

                _syncState.value = SyncState.SYNCED

            } catch (e: Exception) {
                Timber.e(e, "Firestore read failed — loading from local SQLite backup")

                val backupItems = localBackup.getAllLocal()
                cachedClothing = backupItems
                _carouselItems.value = backupItems.sortedBy { it.id }.sortedBy { it.id }.takeLast(5).reversed()
                _clothingItems.value = backupItems
                _syncState.value = SyncState.OFFLINE_BACKUP
            }
        }
    }

    /**
     * Pushes all locally backed-up clothing items back to Firestore.
     * Used to re-sync data after recovering from an offline period.
     */
    fun syncLocalToFirestore() {
        val uid = authService.currentUserId
        if (uid.isBlank()) return

        _syncState.value = SyncState.SYNCING

        viewModelScope.launch {
            try {
                val localItems = localBackup.getAllLocal()
                localItems.forEach { item ->
                    clothingRepo.upsert(uid, item)
                }
                refreshFromFirestore()
            } catch (e: Exception) {
                Timber.e(e, "syncLocalToFirestore failed")
                _syncState.value = SyncState.SYNC_ERROR
            }
        }
    }

    /**
     * Fetches weather data from the Open-Meteo API for the user's saved location preference.
     * Updates [weatherData] on success or sets [weatherError] on failure.
     */
    fun fetchWeather() {
        viewModelScope.launch {
            try {
                val loc = locationPrefs.locationFlow.first()
                val weather = weatherService.getWeather(loc.lat, loc.lon)
                _weatherData.value = weather
            } catch (e: Exception) {
                Timber.e(e, "Weather fetch failed")
                _weatherData.value = null
                _weatherError.value = "Could not load weather"
            }
        }
    }

    /**
     * Updates the search query and triggers a filtered search across clothing items and outfits.
     * Hides search results if the query is empty.
     *
     * @param query The new search query string entered by the user.
     */
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

    /**
     * Filters [cachedClothing] and [cachedOutfits] against the given query and
     * updates [searchResults]. Searches across title, description, colour, season, and category.
     *
     * @param query The trimmed, non-empty search string.
     */
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

    /**
     * Deletes a clothing item and its associated image from Firestore and Firebase Storage,
     * then refreshes the clothing list.
     *
     * @param item The clothing item to delete.
     */
    fun deleteClothing(item: ClosetOrganiserModel) {
        val uid = authService.currentUserId
        if (uid.isBlank()) return

        viewModelScope.launch {
            try {
                val doc = clothingRepo.findDocByItemId(uid, item.id)
                val imagePath = doc.getString("imagePath").orEmpty()
                imageStorageRepo.deleteByPath(imagePath)
                clothingRepo.deleteByDocId(uid, doc.id)
                refreshFromFirestore()
            } catch (e: Exception) {
                Timber.e(e, "Delete failed for id=${item.id}")
            }
        }
    }

    /**
     * Serialises all locally backed-up clothing items to a JSON string
     * and emits it via [exportJson] for the UI to consume (e.g. share or save to file).
     */
    fun exportWardrobe() {
        viewModelScope.launch {
            try {
                _exportJson.value = localBackup.exportToJson()
            } catch (e: Exception) {
                Timber.e(e, "exportWardrobe failed")
            }
        }
    }

    /**
     * Resets [exportJson] to null after the UI has consumed the exported data.
     */
    fun clearExport() {
        _exportJson.value = null
    }

    /**
     * Hides the search results panel and clears the search query and results.
     */
    fun hideSearchResults() {
        _showSearchResults.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}