package ie.setu.project.views.addOutfit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.ui.theme.ClosetOrganiserTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class AddOutfitView : AppCompatActivity() {

    private lateinit var presenter: AddOutfitPresenter
    private var lastWornState by mutableStateOf("")
    private var selectedCountState by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = AddOutfitPresenter(this)

        lastWornState = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(presenter.outfit.lastWorn)
        } catch (e: Exception) { "" }
        selectedCountState = presenter.outfit.clothingItems.size

        setContent { ClosetOrganiserTheme {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            AddOutfitScreen(
                initialTitle = presenter.outfit.title ?: "",
                initialDescription = presenter.outfit.description ?: "",
                initialSeason = presenter.outfit.season,
                lastWornText = lastWornState,
                selectedClothingCount = selectedCountState,
                snackbarHostState = snackbarHostState,
                onPickLastWorn = { presenter.showDatePicker() },
                onChooseClothing = { presenter.launchClothingSelection() },
                onSave = { title, desc, season -> presenter.doAddOrSave(title, desc, season) },
                onBack = { finish() },
                showError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
            )
        } }
    }

    fun showOutfit(outfit: OutfitModel) {
        lastWornState = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(outfit.lastWorn)
        } catch (e: Exception) { "" }
        selectedCountState = outfit.clothingItems.size
    }

    fun updateLastWornDate(date: String) { lastWornState = date }
}