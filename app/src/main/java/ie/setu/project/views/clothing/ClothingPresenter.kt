package ie.setu.project.views.clothing

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.views.main.MainView

/**
 * Presenter for managing the clothing items in the closet.
 * This class is responsible for handling user interactions related to clothing items,
 * such as adding, deleting, or editing items. It also interacts with the `MainApp`
 * to retrieve and modify the data.
 */
class ClothingPresenter(private val view: ClothingView) {

    // Reference to the main app to access the clothing items store
    private val app: MainApp = view.application as MainApp

    // Launcher to handle result from the add or edit clothing item activity
    private lateinit var getResult: ActivityResultLauncher<Intent>

    init {
        // Register activity result callback for adding/editing clothing item
        registerActivityResultCallback()
    }

    /**
     * Retrieves all the closet items.
     *
     * @return A list of all closet items.
     */
    fun getClosetItems(): List<ClosetOrganiserModel> = app.clothingItems.findAll()

    /**
     * Handles the selection of a menu item.
     *
     * @param itemId The ID of the menu item that was selected.
     * @return A boolean indicating if the menu item was handled.
     */
    fun handleMenuSelection(itemId: Int): Boolean {
        return when (itemId) {
            R.id.nav_to_main -> {
                // Navigate to the main view
                view.navigateToMain()
                true
            }
            R.id.item_add -> {
                // Launch the add item activity
                launchAddItem()
                true
            }
            else -> false
        }
    }

    /**
     * Handles the click event on a closet item. Launches the activity to edit the selected item.
     *
     * @param item The closet item that was clicked.
     */
    fun onClosetItemClick(item: ClosetOrganiserModel) {
        // Create an intent to edit the selected closet item
        val intent = Intent(view, MainView::class.java).apply {
            putExtra("closet_item_edit", item)
        }
        // Launch the activity for editing the item
        getResult.launch(intent)
    }

    /**
     * Handles the click event for deleting a closet item.
     * The item is deleted from the store and a snackbar is shown to confirm the action.
     *
     * @param item The closet item to delete.
     */
    fun onDeleteItemClick(item: ClosetOrganiserModel) {
        // Delete the item from the store
        app.clothingItems.delete(item)
        // Show a snackbar confirmation
        view.showSnackbar("Item deleted", Snackbar.LENGTH_SHORT)
        // Update the adapter to reflect the changes
        view.updateAdapter()
    }

    /**
     * Launches the activity for adding a new clothing item.
     */
    private fun launchAddItem() {
        // Launch the add item activity
        getResult.launch(Intent(view, MainView::class.java))
    }

    /**
     * Registers the activity result callback for adding or editing a clothing item.
     */
    private fun registerActivityResultCallback() {
        // Register the result launcher to handle the results of add/edit activity
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> view.notifyAdapterChanged() // Notify that the adapter data has changed
                Activity.RESULT_CANCELED -> view.showSnackbar( // Show snackbar if the operation was cancelled
                    "Operation cancelled",
                    Snackbar.LENGTH_SHORT
                )
            }
        }
    }
}
