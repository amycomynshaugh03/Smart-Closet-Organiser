package ie.setu.project.views.clothing

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import ie.setu.project.ui.theme.ClosetOrganiserTheme
import ie.setu.project.views.clothingList.ClothingListView
import kotlinx.coroutines.launch

class ClothingView : AppCompatActivity() {

    private lateinit var presenter: ClothingPresenter
    private var refreshTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ClothingPresenter(this)

        setContent { ClosetOrganiserTheme {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val items = remember(refreshTick) { presenter.getClosetItems() }

            ClothingScreen(
                items = items,
                onAddClick = { presenter.launchAddItem() },
                onBackToHome = { startActivity(Intent(this, ClothingListView::class.java)) },
                onItemClick = { presenter.onClosetItemClick(it) },
                onDeleteClick = { item ->
                    presenter.onDeleteItemClick(item)
                    scope.launch { snackbarHostState.showSnackbar("Deleted ${item.title}") }
                },
                snackbarHostState = snackbarHostState
            )
        } }
    }

    override fun onResume() {
        super.onResume()
        presenter.refreshFromFirestore()
    }

    fun notifyAdapterChanged() { refreshTick++ }

    fun navigateToMain() {
        startActivity(Intent(this, ClothingListView::class.java))
    }
}