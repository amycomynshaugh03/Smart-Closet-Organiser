package ie.setu.project.views.outfit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.adapters.outfit.OutfitAdapter
import ie.setu.project.databinding.ActivityOutfitBinding

/**
 * Activity that handles the display of outfits. It shows a list of outfits and allows users to interact
 * with them by clicking to view or delete them.
 */
class OutfitView : AppCompatActivity() {

    // View binding for the ActivityOutfit layout
    private lateinit var binding: ActivityOutfitBinding

    // Presenter that handles the business logic for OutfitView
    private lateinit var presenter: OutfitPresenter

    // Adapter for displaying the list of outfits in a RecyclerView
    private lateinit var adapter: OutfitAdapter

    /**
     * Initializes the activity, sets up the toolbar, presenter, and RecyclerView,
     * and loads the outfits from the presenter.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar with the current activity title
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        // Initialize the presenter and setup the RecyclerView
        presenter = OutfitPresenter(this)
        setupRecyclerView()

        // Load the list of outfits
        loadOutfits()
    }

    /**
     * Sets up the RecyclerView with an OutfitAdapter and defines the item click behavior.
     */
    private fun setupRecyclerView() {
        adapter = OutfitAdapter(mutableListOf()) { outfit ->
            // Handle outfit item click (navigate to outfit details)
            presenter.onOutfitClick(outfit)
        }

        // Apply layout manager and adapter to the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OutfitView)
            adapter = this@OutfitView.adapter
        }
    }

    /**
     * Loads the list of outfits from the presenter and updates the RecyclerView.
     */
    fun loadOutfits() {
        adapter.updateItems(presenter.getOutfits())
    }

    /**
     * Displays a Snackbar message.
     *
     * @param message The message to be displayed in the Snackbar.
     */
    fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Inflates the options menu for the activity.
     *
     * @param menu The menu to inflate.
     * @return True if the menu was successfully created.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Handles selection of menu items.
     *
     * @param item The menu item that was selected.
     * @return True if the menu item was handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.handleMenuSelection(item.itemId) || super.onOptionsItemSelected(item)
    }
}
