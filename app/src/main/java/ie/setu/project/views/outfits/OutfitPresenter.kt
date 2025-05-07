package ie.setu.project.views.outfit

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.views.addOutfit.AddOutfitView

/**
 * Presenter responsible for managing the logic for the OutfitView.
 * This includes handling outfit management actions such as adding, editing, and deleting outfits.
 */
class OutfitPresenter(private val view: OutfitView) {

    // Reference to the application instance
    private val app: MainApp = view.application as MainApp

    // Launcher to handle the result of starting AddOutfitView activity
    private lateinit var getResult: ActivityResultLauncher<Intent>

    init {
        // Register the activity result callback to handle results from AddOutfitView
        registerActivityResultCallback()
    }

    /**
     * Fetches the list of all outfits from the app's database.
     *
     * @return A list of OutfitModel objects.
     */
    fun getOutfits(): List<OutfitModel> = app.outfitItems.findAll()

    /**
     * Handles menu item selections. If the "add" menu item is selected, launches the AddOutfitView.
     *
     * @param itemId The ID of the selected menu item.
     * @return True if the item was handled, false otherwise.
     */
    fun handleMenuSelection(itemId: Int): Boolean {
        return when (itemId) {
            R.id.item_add -> {
                launchAddOutfit()
                true
            }
            else -> false
        }
    }

    /**
     * Opens the AddOutfitView for editing an existing outfit.
     *
     * @param outfit The outfit to be edited.
     */
    fun onOutfitClick(outfit: OutfitModel) {
        val intent = Intent(view, AddOutfitView::class.java).apply {
            putExtra("outfit_edit", outfit)
        }
        getResult.launch(intent)
    }

    /**
     * Deletes the specified outfit and reloads the outfits list.
     *
     * @param outfit The outfit to be deleted.
     */
    fun onDeleteOutfitClick(outfit: OutfitModel) {
        app.outfitItems.delete(outfit)
        view.showSnackbar("Outfit deleted")
        view.loadOutfits()
    }

    /**
     * Launches the AddOutfitView for creating a new outfit.
     */
    private fun launchAddOutfit() {
        getResult.launch(Intent(view, AddOutfitView::class.java))
    }

    /**
     * Registers the callback to handle the result of launching AddOutfitView.
     * Based on the result code, it either reloads the outfits list or shows a cancellation message.
     */
    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.loadOutfits()
                Activity.RESULT_CANCELED -> view.showSnackbar("Operation cancelled")
            }
        }
    }
}
