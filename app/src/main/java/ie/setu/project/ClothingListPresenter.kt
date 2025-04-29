package ie.setu.project


import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.activities.ClothingListView
import ie.setu.project.activities.MainView
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.ClosetOrganiserModel

class ClothingListPresenter(private val view: ClothingListView) {
    lateinit var app: MainApp
    private lateinit var getResult: ActivityResultLauncher<Intent>

    init {
        app = view.application as MainApp
        registerActivityResultCallback()
    }

    fun onClosetItemClick(item: ClosetOrganiserModel) {
        val launcherIntent = Intent(view, MainView::class.java)
        launcherIntent.putExtra("closet_item_edit", item)
        getResult.launch(launcherIntent)
        view.showSnackbar("Selected: ${item.title}", Snackbar.LENGTH_SHORT)
    }

    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        app.clothingItems.delete(item)
        view.notifyAdapterDataChanged()
        view.showSnackbar("Clothing Item Deleted", Snackbar.LENGTH_LONG)
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                view.notifyAdapterDataChanged()
            }
            if (result.resultCode == Activity.RESULT_CANCELED) {
                view.showSnackbar("Placemark Add Cancelled", Snackbar.LENGTH_LONG)
            }
        }
    }
}