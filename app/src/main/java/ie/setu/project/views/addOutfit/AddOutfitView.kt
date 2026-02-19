package ie.setu.project.views.addOutfit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.models.outfit.OutfitModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class AddOutfitView : AppCompatActivity() {

    private lateinit var presenter: AddOutfitPresenter


    private var lastWornState by mutableStateOf("")
    private var selectedCountState by mutableStateOf(0) // optional UI feedback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = AddOutfitPresenter(this)

        // initial values
        lastWornState = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(presenter.outfit.lastWorn)
        } catch (e: Exception) {
            ""
        }
        selectedCountState = presenter.outfit.clothingItems.size

        setContent {
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
                onSave = { title, desc, season ->
                    presenter.doAddOrSave(title, desc, season)
                },
                showError = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
        }
    }

    // Presenter calls this when editing. We don’t need to “fill fields” like XML anymore,
    // but we DO update any Activity state we display (date + count).
    fun showOutfit(outfit: OutfitModel) {
        lastWornState = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(outfit.lastWorn)
        } catch (e: Exception) {
            ""
        }
        selectedCountState = outfit.clothingItems.size
    }

    fun updateLastWornDate(date: String) {
        lastWornState = date
    }
}
