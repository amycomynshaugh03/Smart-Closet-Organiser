package ie.setu.project.views.clothing

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ie.setu.project.R
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.views.clothingList.ClothingListView

/**
 * The main activity for managing clothing items in the closet.
 * This activity allows the user to view, delete, and navigate to other views related to clothing items.
 */
class ClothingView : AppCompatActivity(), ClosetItemListener {
    private lateinit var binding: ActivityClothingBinding
    private lateinit var presenter: ClothingPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the title and initialize the top app bar
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        // Initialize the presenter to handle business logic
        presenter = ClothingPresenter(this)

        // Set up the recycler view to display clothing items
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ClosetAdapter(
            (application as MainApp).clothingItems.findAll(),
            this
        )
    }

    /**
     * Inflates the menu options for the activity, allowing users to navigate and perform actions.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuInflater.inflate(R.menu.menu_location, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handles the selection of a menu item.
     *
     * @param item The selected menu item.
     * @return A boolean indicating whether the item was handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.handleMenuSelection(item.itemId) || super.onOptionsItemSelected(item)
    }

    /**
     * Handles the click event for a closet item.
     * This triggers the opening of the item in edit mode.
     *
     * @param item The closet item that was clicked.
     */
    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        presenter.onClosetItemClick(item)
    }

    /**
     * Handles the click event for deleting a closet item.
     * This removes the item from the store and updates the UI accordingly.
     *
     * @param item The closet item to be deleted.
     */
    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        presenter.onDeleteItemClick(item)
        onRefresh() // Refresh the list of items after deletion
    }

    /**
     * Refreshes the clothing item list displayed in the recycler view.
     */
    private fun onRefresh() {
        (binding.recyclerView.adapter as ClosetAdapter)
            .updateItems(presenter.getClosetItems())
    }

    /**
     * Navigates to the clothing list view activity.
     */
    fun navigateToMain() {
        startActivity(Intent(this, ClothingListView::class.java))
    }

    /**
     * Notifies the adapter that the data set has changed and it should refresh.
     */
    fun notifyAdapterChanged() {
        (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, binding.recyclerView.adapter?.itemCount ?: 0)
    }

    /**
     * Updates the adapter with the latest list of clothing items from the app.
     */
    fun updateAdapter() {
        val updatedList = (application as MainApp).clothingItems.findAll()
        (binding.recyclerView.adapter as ClosetAdapter).updateItems(updatedList)
    }

    /**
     * Displays a snackbar message with the specified content.
     *
     * @param message The message to display in the snackbar.
     * @param duration The duration for which the snackbar should be visible.
     */
    fun showSnackbar(message: String, duration: Int) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, duration).show()
    }
}
