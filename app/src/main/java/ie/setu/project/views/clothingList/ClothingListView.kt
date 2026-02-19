package ie.setu.project.views.clothingList

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.ui.auth.AuthStateViewModel
import ie.setu.project.ui.auth.AuthViewModel
import ie.setu.project.ui.auth.LoginScreen
import ie.setu.project.ui.auth.RegisterScreen
import ie.setu.project.viewmodels.ClothingListPresenter
import ie.setu.project.views.clothing.ClothingView
import ie.setu.project.views.outfit.OutfitView

@AndroidEntryPoint
class ClothingListView : AppCompatActivity(), ClosetItemListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val presenter: ClothingListPresenter = hiltViewModel()
            val authStateVm: AuthStateViewModel = hiltViewModel()
            val authVm: AuthViewModel = hiltViewModel()

            LaunchedEffect(Unit) { presenter.fetchWeather() }

            val user by authStateVm.user.collectAsState()
            var showRegister by remember { mutableStateOf(false) }

            LaunchedEffect(user) {
                if (user == null) showRegister = false
            }

            if (user == null) {
                if (showRegister) {
                    RegisterScreen(
                        onGoToLogin = { showRegister = false },
                        onRegistered = {  }
                    )
                } else {
                    LoginScreen(
                        onGoToRegister = { showRegister = true },
                        onSignedIn = {  }
                    )
                }
                return@setContent
            }


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
                onOutfitItemClick = { outfit ->
                    startActivity(Intent(this, OutfitView::class.java).apply {
                        putExtra("outfit_item_edit", outfit)
                    })
                },
                onDeleteItemClick = { item ->
                    showSnackbar("Deleted ${item.title}", Snackbar.LENGTH_SHORT)
                    presenter.refreshCarousel()
                },
                showSnackbar = { message, duration -> showSnackbar(message, duration) },
                updateWeatherUI = { weather -> updateWeatherUI(weather) },
                showWeatherError = { message -> showWeatherError(message) },
                onSignOut = { authVm.signOut() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        startActivity(Intent(this, ClothingView::class.java).apply {
            putExtra("closet_item_edit", item)
        })
    }

    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        showSnackbar("Deleted ${item.title}", Snackbar.LENGTH_SHORT)
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
}
