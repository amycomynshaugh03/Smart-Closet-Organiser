package ie.setu.project.views.clothing

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import ie.setu.project.views.clothingList.ClothingListView
import kotlinx.coroutines.launch

class ClothingView : AppCompatActivity() {

    private lateinit var presenter: ClothingPresenter

    private var refreshTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = ClothingPresenter(this)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            ClothingScreen(
                itemsProvider = {
                    refreshTick
                    presenter.getClosetItems()
                },
                onAddClick = { presenter.launchAddItem() },
                onBackToHome = { startActivity(Intent(this, ClothingListView::class.java)) },
                onItemClick = { presenter.onClosetItemClick(it) },
                onDeleteClick = { item ->
                    presenter.onDeleteItemClick(item)
                    scope.launch { snackbarHostState.showSnackbar("Deleted ${item.title}") }
                    refreshTick++
                },
                snackbarHostState = snackbarHostState
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTick++
    }

    fun notifyAdapterChanged() { refreshTick++ }
    fun updateAdapter() { refreshTick++ }
    fun showSnackbar(message: String, duration: Int) {}
    fun navigateToMain() { startActivity(Intent(this, ClothingListView::class.java)) }
}
