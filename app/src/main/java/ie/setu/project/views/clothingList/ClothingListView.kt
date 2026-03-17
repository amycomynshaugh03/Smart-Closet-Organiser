package ie.setu.project.views.clothingList

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.ui.auth.AuthStateViewModel
import ie.setu.project.ui.auth.AuthViewModel
import ie.setu.project.ui.auth.LoginScreen
import ie.setu.project.ui.auth.RegisterScreen
import ie.setu.project.ui.theme.ClosetOrganiserTheme
import ie.setu.project.ui.user.UserEditScreen
import ie.setu.project.ui.user.UserProfileScreen
import ie.setu.project.views.calendar.CalendarView
import ie.setu.project.views.clothing.ClothingView
import ie.setu.project.views.outfit.OutfitView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ClothingListView : AppCompatActivity(), ClosetItemListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { ClosetOrganiserTheme {
            val presenter: ClothingListPresenter = hiltViewModel()
            val authStateVm: AuthStateViewModel = hiltViewModel()
            val authVm: AuthViewModel = hiltViewModel()

            val user by authStateVm.user.collectAsState()
            val syncState by presenter.syncState.collectAsStateWithLifecycle()
            val exportJson by presenter.exportJson.collectAsStateWithLifecycle()

            var showRegister by remember { mutableStateOf(false) }
            var showProfile by remember { mutableStateOf(false) }
            var showEditProfile by remember { mutableStateOf(false) }

            LaunchedEffect(exportJson) {
                exportJson?.let { json ->
                    writeAndShareExport(json)
                    presenter.clearExport()
                }
            }

            val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val obs = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        presenter.refreshFromFirestore()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(obs)
                onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
            }

            LaunchedEffect(user) {
                if (user == null) {
                    showRegister = false
                    showProfile = false
                    showEditProfile = false
                }
            }

            if (user == null) {
                if (showRegister) {
                    RegisterScreen(onGoToLogin = { showRegister = false }, onRegistered = {})
                } else {
                    LoginScreen(onGoToRegister = { showRegister = true }, onSignedIn = { })
                }
                return@ClosetOrganiserTheme
            }

            LaunchedEffect(user) { presenter.refreshFromFirestore() }
            LaunchedEffect(Unit) { presenter.fetchWeather() }

            val context = LocalContext.current

            if (showEditProfile) {
                UserEditScreen(onBack = { showEditProfile = false })
                return@ClosetOrganiserTheme
            }

            if (showProfile) {
                UserProfileScreen(
                    onBack = { showProfile = false },
                    onExportWardrobe = { presenter.exportWardrobe(); showProfile = false },
                    onSignOut = { authVm.signOut() },
                    onEditProfile = { showEditProfile = true }
                )
                return@ClosetOrganiserTheme
            }

            ClothingListScreen(
                presenter = presenter,
                context = context,
                syncState = syncState,
                onExportWardrobe = { presenter.exportWardrobe() },
                onNavigateToClothing = { startActivity(Intent(this, ClothingView::class.java)) },
                onNavigateToOutfit = { startActivity(Intent(this, OutfitView::class.java)) },
                onNavigateToCalendar = { startActivity(Intent(this, CalendarView::class.java)) }, // ADD
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
                    presenter.deleteClothing(item)
                    showSnackbar("Deleted ${item.title}", Snackbar.LENGTH_SHORT)
                },
                showSnackbar = { message, duration -> showSnackbar(message, duration) },
                updateWeatherUI = { weather -> updateWeatherUI(weather) },
                showWeatherError = { message -> showWeatherError(message) },
                onNavigateToProfile = { showProfile = true }
            )
        } }
    }

    private fun writeAndShareExport(json: String) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "wardrobe_backup_$timestamp.json"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            file.writeText(json)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                this, "${packageName}.provider", file
            )
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Wardrobe Backup")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Export Wardrobe Backup"))
            showSnackbar("Backup saved to Downloads/$fileName", Snackbar.LENGTH_LONG)
        } catch (e: Exception) {
            showSnackbar("Export failed: ${e.message}", Snackbar.LENGTH_LONG)
        }
    }

    override fun onResume() { super.onResume() }

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
        runOnUiThread { showSnackbar("Weather error: $message", Snackbar.LENGTH_SHORT) }
    }
}