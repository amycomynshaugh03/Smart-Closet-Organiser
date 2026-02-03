package ie.setu.project.views.clothingList

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.viewmodels.ClothingListPresenter
import ie.setu.project.views.clothing.ClothingView
import ie.setu.project.views.outfit.OutfitView

class ClothingListView : AppCompatActivity(), ClosetItemListener {
    private lateinit var presenter: ClothingListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        presenter = ClothingListPresenter(application)
        presenter.fetchWeather()

        setContent {
            val context = LocalContext.current
            ClothingListScreen(
                presenter = presenter,
                context = context,
                onNavigateToClothing = { startActivity(Intent(this, ClothingView::class.java)) },
                onNavigateToOutfit = { startActivity(Intent(this, OutfitView::class.java)) },
                onClothingItemClick = { item ->
                    startActivity(Intent(this, ClothingView::class.java).apply {
                        putExtra("closet_item_edit", item)
                    })
                },
                onOutfitItemClick = { item ->
                    startActivity(Intent(this, OutfitView::class.java).apply {
                        putExtra("outfit_item_edit", item)
                    })
                },
                onDeleteItemClick = { item ->

                    showSnackbar("Deleted ${item.title}", Snackbar.LENGTH_SHORT)
                    presenter.refreshCarousel()
                },
                showSnackbar = { message, duration -> showSnackbar(message, duration) },
                updateWeatherUI = { weather -> updateWeatherUI(weather) },
                showWeatherError = { message -> showWeatherError(message) }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.refreshCarousel()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem) = super.onOptionsItemSelected(item)

    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        // This gets called from Compose screen
        startActivity(Intent(this, ClothingView::class.java).apply {
            putExtra("closet_item_edit", item)
        })
    }

    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        // This gets called from Compose screen
        showSnackbar("Deleted ${item.title}", Snackbar.LENGTH_SHORT)
        presenter.refreshCarousel()
    }

    fun showSnackbar(message: String, duration: Int) {
        Snackbar.make(window.decorView.rootView, message, duration).show()
    }

    @SuppressLint("SetTextI18n")
    fun updateWeatherUI(weather: WeatherResponse) {
        val current = weather.current_weather
        val condition = WeatherCondition.fromCode(current.weathercode, current.is_day)
        showSnackbar("Weather: ${current.temperature}°C, ${condition.description}", Snackbar.LENGTH_SHORT)
    }

    @SuppressLint("SetTextI18n")
    fun showWeatherError(message: String) {
        runOnUiThread {
            showSnackbar("Weather error: $message", Snackbar.LENGTH_SHORT)
        }
    }

    fun refreshCarousel() {
        presenter.refreshCarousel()
    }

    private fun openClothingDetails(item: ClosetOrganiserModel) {
        startActivity(
            Intent(this, ClothingView::class.java).apply {
                putExtra("closet_item_edit", item)
            }
        )
    }

    private fun openOutfitDetails(item: OutfitModel) {
        startActivity(
            Intent(this, OutfitView::class.java).apply {
                putExtra("outfit_item_edit", item)
            }
        )
    }
}