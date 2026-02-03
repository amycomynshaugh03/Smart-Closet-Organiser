package ie.setu.project.views.clothing

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.views.clothingList.ClothingListView
import ie.setu.project.views.main.MainView
import kotlinx.coroutines.launch

class ClothingView : AppCompatActivity() {

    private lateinit var presenter: ClothingPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = ClothingPresenter(this)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            ClothingScreen(
                itemsProvider = { presenter.getClosetItems() },

                onAddClick = {
                    startActivity(Intent(this, MainView::class.java))
                },

                onBackToHome = {
                    startActivity(Intent(this, ClothingListView::class.java))
                },

                onItemClick = { item ->
                    presenter.onClosetItemClick(item)
                },

                onDeleteClick = { item ->
                    presenter.onDeleteItemClick(item)
                    scope.launch { snackbarHostState.showSnackbar("Deleted ${item.title}") }
                },

                snackbarHostState = snackbarHostState
            )
        }
    }

    // These are still used by ClothingPresenter callbacks
    fun notifyAdapterChanged() {
        // no RecyclerView now — screen refreshes itself
    }

    fun updateAdapter() {
        // no RecyclerView now — screen refreshes itself
    }

    fun showSnackbar(message: String, duration: Int) {
        // presenter still calls this sometimes; safe no-op or keep old Snackbar if you want
    }

    fun navigateToMain() {
        startActivity(Intent(this, ClothingListView::class.java))
    }
}
