package ie.setu.project.views.outfit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import ie.setu.project.views.addOutfit.AddOutfitView
import kotlinx.coroutines.launch

class OutfitView : AppCompatActivity() {

    private lateinit var presenter: OutfitPresenter
    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var refreshTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = OutfitPresenter(this)

        getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> loadOutfits()
                Activity.RESULT_CANCELED -> { /* no-op */ }
            }
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            OutfitScreen(
                outfitsProvider = {
                    refreshTick
                    presenter.getOutfits()
                },

                onBack = { finish() },

                onAddOutfit = {
                    getResult.launch(Intent(this, AddOutfitView::class.java))
                },

                onOutfitClick = { outfit ->

                    val intent = Intent(this, AddOutfitView::class.java).apply {
                        putExtra("outfit_edit", outfit)
                    }
                    getResult.launch(intent)
                },

                onDeleteOutfit = { outfit ->
                    presenter.onDeleteOutfitClick(outfit)
                    loadOutfits()
                    scope.launch { snackbarHostState.showSnackbar("Outfit deleted") }
                },

                snackbarHostState = snackbarHostState
            )
        }
    }

    override fun onResume() {
        super.onResume()
        loadOutfits()
    }

    fun loadOutfits() {
        refreshTick++
    }

    fun showSnackbar(message: String) { /* optional */ }
}
