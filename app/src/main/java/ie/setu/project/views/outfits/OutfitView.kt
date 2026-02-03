package ie.setu.project.views.outfit

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ie.setu.project.views.addOutfit.AddOutfitView
import kotlinx.coroutines.launch

class OutfitView : AppCompatActivity() {

    private lateinit var presenter: OutfitPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = OutfitPresenter(this)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            OutfitScreen(
                outfitsProvider = { presenter.getOutfits() },

                onBack = { finish() },

                onAddOutfit = {
                    startActivity(Intent(this, AddOutfitView::class.java))
                },

                onOutfitClick = { outfit ->
                    presenter.onOutfitClick(outfit)
                },

                onDeleteOutfit = { outfit ->
                    presenter.onDeleteOutfitClick(outfit)
                    scope.launch { snackbarHostState.showSnackbar("Outfit deleted") }
                },

                snackbarHostState = snackbarHostState
            )
        }
    }

    fun loadOutfits() {}

    fun showSnackbar(message: String) {}
}
