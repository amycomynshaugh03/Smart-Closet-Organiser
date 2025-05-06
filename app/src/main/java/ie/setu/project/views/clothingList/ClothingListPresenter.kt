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

class ClothingListPresenter(private val view: ClothingListView) {
    private val app: MainApp = view.application as MainApp
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private var carouselItems = mutableListOf<ClosetOrganiserModel>()
    private val weatherService = WeatherService()

    init {
        registerActivityResultCallback()
        loadCarouselData()
    }

    fun getCarouselItems(): List<ClosetOrganiserModel> = carouselItems.toList()


    fun onClosetItemClick(item: ClosetOrganiserModel) {
        getResult.launch(Intent(view, MainView::class.java).apply {
            putExtra("closet_item_edit", item)
        })
    }

    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        app.clothingItems.delete(item)
        loadCarouselData()
        view.showSnackbar("Item deleted", Snackbar.LENGTH_SHORT)
    }

    private fun loadCarouselData() {
        carouselItems.clear()
        carouselItems.addAll(app.clothingItems.findAll().take(5))
        view.refreshCarousel()
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.refreshCarousel()
                Activity.RESULT_CANCELED -> view.showSnackbar(
                    "Operation cancelled",
                    Snackbar.LENGTH_SHORT
                )
            }
        }
    }
    fun fetchWeather() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weather = weatherService.getWeather(53.3498, -6.2603) // Dublin
                view.updateWeatherUI(weather)
            } catch (e: Exception) {
                view.showWeatherError(e.message ?: "Weather unavailable")
            }
        }
    }
}
