package ie.setu.project.views.clothingList

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.views.main.MainView
import ie.setu.project.weather.WeatherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Presenter class for managing interactions and data operations related to the clothing list view.
 * Handles tasks like fetching weather data, loading carousel data, and managing clothing items.
 */
class ClothingListPresenter(private val view: ClothingListView) {
    private val app: MainApp = view.application as MainApp
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private var carouselItems = mutableListOf<ClosetOrganiserModel>()
    private val weatherService = WeatherService()

    /**
     * Initializes the presenter and registers activity result callback for handling results from other activities.
     * Also loads the initial data for the carousel view.
     */
    init {
        registerActivityResultCallback()
        loadCarouselData()
    }

    /**
     * Retrieves the list of carousel items to be displayed in the carousel view.
     * @return A list of ClosetOrganiserModel representing the items.
     */
    fun getCarouselItems(): List<ClosetOrganiserModel> = carouselItems.toList()

    /**
     * Handles the click event on a closet item in the carousel.
     * Launches the MainView activity to edit the selected closet item.
     * @param item The closet item that was clicked.
     */
    fun onClosetItemClick(item: ClosetOrganiserModel) {
        getResult.launch(Intent(view, MainView::class.java).apply {
            putExtra("closet_item_edit", item)
        })
    }

    /**
     * Handles the click event for deleting a closet item.
     * Deletes the selected item and updates the carousel data.
     * @param item The closet item to be deleted.
     */
    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        app.clothingItems.delete(item)
        loadCarouselData()  // Refresh carousel data after deletion
        view.showSnackbar("Item deleted", Snackbar.LENGTH_SHORT)
    }

    /**
     * Loads the latest data for the carousel view by fetching the first 5 closet items.
     */
    private fun loadCarouselData() {
        carouselItems.clear()
        carouselItems.addAll(app.clothingItems.findAll().take(5))
        view.refreshCarousel()
    }

    /**
     * Registers a callback to handle results from launched activities.
     * If the result is OK, refreshes the carousel view.
     */
    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.refreshCarousel()  // Refresh carousel on success
                Activity.RESULT_CANCELED -> view.showSnackbar(
                    "Operation cancelled", Snackbar.LENGTH_SHORT
                )
            }
        }
    }

    /**
     * Fetches weather data asynchronously and updates the UI.
     * If the weather data is successfully fetched, it updates the view's UI with the weather information.
     * If the fetch fails, it shows an error message in the UI.
     */
    fun fetchWeather() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("Attempting to fetch weather...")
                val weather = weatherService.getWeather(53.3498, -6.2603)  // Fetch weather for coordinates (Dublin)
                println("Weather data received: $weather")
                withContext(Dispatchers.Main) {
                    view.updateWeatherUI(weather)  // Update UI on main thread
                }
            } catch (e: Exception) {
                println("Weather fetch failed: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    view.showWeatherError("Failed to load weather data")  // Show error on UI if fetching fails
                }
            }
        }
    }

    /**
     * Performs a search operation on both clothing items and outfits.
     * Searches for items that match the query in any of their properties.
     * @param query The search query string.
     * @return A list of search results containing matching items and outfits.
     */
    fun performSearch(query: String): List<Any> {
        val results = mutableListOf<Any>()

        // Search in clothing items
        val clothingResults = app.clothingItems.findAll().filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.colourPattern.contains(query, ignoreCase = true) ||
                    it.season.contains(query, ignoreCase = true)
        }

        // Search in outfits
        val outfitResults = app.outfitItems.findAll().filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.season.contains(query, ignoreCase = true) ||
                    it.clothingItems.any { clothing ->
                        clothing.title.contains(query, ignoreCase = true) ||
                                clothing.description.contains(query, ignoreCase = true)
                    }
        }

        results.addAll(clothingResults)  // Add clothing results to the final list
        results.addAll(outfitResults)  // Add outfit results to the final list
        return results
    }
}
