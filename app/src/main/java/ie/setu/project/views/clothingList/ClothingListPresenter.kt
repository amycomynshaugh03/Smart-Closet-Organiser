package ie.setu.project.views.clothingList

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.ClosetOrganiserModel
import ie.setu.project.views.main.MainView

class ClothingListPresenter(private val view: ClothingListView) {
    private var app: MainApp
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private var carouselItems = mutableListOf<ClosetOrganiserModel>()

    init {
        app = view.application as MainApp
        registerActivityResultCallback()
        loadCarouselData()
    }

    private fun loadCarouselData() {
        carouselItems.clear()
        carouselItems.addAll(app.clothingItems.findAll())
        view.refreshCarousel()
    }

    fun getCarouselItems() = carouselItems.toList()

    fun onClosetItemClick(item: ClosetOrganiserModel) {
        val launcherIntent = Intent(view, MainView::class.java)
        launcherIntent.putExtra("closet_item_edit", item)
        getResult.launch(launcherIntent)
    }

    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        app.clothingItems.delete(item)
        loadCarouselData()
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.refreshCarousel()
                Activity.RESULT_CANCELED -> view.showSnackbar("Operation cancelled", Snackbar.LENGTH_SHORT)
            }
        }
    }
}