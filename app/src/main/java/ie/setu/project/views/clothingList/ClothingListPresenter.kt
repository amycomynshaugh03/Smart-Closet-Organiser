package ie.setu.project.views.clothingList


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.ClosetOrganiserModel
import ie.setu.project.views.main.MainView
import java.util.*

class ClothingListPresenter(private val view: ClothingListView) {
    lateinit var app: MainApp
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var imageList: List<Uri>
    private var carouselItems = mutableListOf<ClosetOrganiserModel>()

    init {
        app = view.application as MainApp
        registerActivityResultCallback()
        initializeCarouselData()
    }

    private fun initializeCarouselData() {
        carouselItems = arrayListOf(
            ClosetOrganiserModel(
                id = 0,
                title = "Sample Item 1",
                description = "Description",
                colourPattern = "Pattern",
                size = "9",
                season = "Spring",
                lastWorn = Date(),
                image = Uri.parse("https://images2.drct2u.com/plp_full_width_1/products/sr/sr379/c01sr379750w.jpg")
            ),
            ClosetOrganiserModel(
                id = 0,
                title = "Sample Item 2",
                description = "Description",
                colourPattern = "Pattern",
                size = "9",
                season = "Spring",
                lastWorn = Date(),
                image = Uri.parse("content://com.android.providers.media.documents/document/image%3A43")
            )
        )
    }

    fun getCarouselItems(): List<ClosetOrganiserModel> = carouselItems

    fun onClosetItemClick(item: ClosetOrganiserModel) {
        val launcherIntent = Intent(view, MainView::class.java)
        launcherIntent.putExtra("closet_item_edit", item)
        getResult.launch(launcherIntent)
        view.showSnackbar("Selected: ${item.title}", Snackbar.LENGTH_SHORT)
    }

    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        app.clothingItems.delete(item)
        view.updateClothingList(app.clothingItems.findAll().filter { it != item })
        view.showSnackbar("Clothing Item Deleted", Snackbar.LENGTH_LONG)
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.notifyClothingListChanged()
                Activity.RESULT_CANCELED -> view.showSnackbar(
                    "Placemark Add Cancelled",
                    Snackbar.LENGTH_LONG
                )
            }
        }
    }
}