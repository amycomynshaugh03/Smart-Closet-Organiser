package ie.setu.project.views.clothing

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.ClosetOrganiserModel
import ie.setu.project.views.main.MainView

class ClothingPresenter(private val view: ClothingView) {
    lateinit var app: MainApp
    private lateinit var getResult: ActivityResultLauncher<Intent>

    init {
        app = view.application as MainApp
        registerActivityResultCallback()
    }

    fun handleMenuSelection(itemId: Int): Boolean {
        return when (itemId) {
            R.id.nav_to_main -> {
                view.navigateToMain()
                true
            }
            R.id.item_add -> {
                val intent = Intent(view, MainView::class.java)
                getResult.launch(intent)
                true
            }
            else -> false
        }
    }

    fun onClosetItemClick(item: ClosetOrganiserModel) {
        val launcherIntent = Intent(view, MainView::class.java)
        launcherIntent.putExtra("closet_item_edit", item)
        getResult.launch(launcherIntent)
    }

    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        app.clothingItems.delete(item)
        view.updateAdapter()
        view.showSnackbar("Clothing Item Deleted", Snackbar.LENGTH_LONG)
    }

    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.notifyAdapterChanged()
                Activity.RESULT_CANCELED -> view.showSnackbar("Clothing Add Cancelled", Snackbar.LENGTH_LONG)
            }
        }
    }
}