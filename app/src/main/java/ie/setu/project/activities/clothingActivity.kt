package ie.setu.project.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

// This Activity is responsible for displaying a list of clothing items in the closet.
class clothingActivity : AppCompatActivity(), ClosetItemListener {

    lateinit var app: MainApp // MainApp instance to interact with the app's data
    private lateinit var binding: ActivityClothingBinding // Binding for the activity's UI components

    // onCreate method: Called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the binding to inflate the layout and set the content view
        binding = ActivityClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the title for the top app bar and set it as the action bar
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        // Initialize the app instance
        app = application as MainApp

        // Set up the RecyclerView to display the clothing items using a LinearLayoutManager
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        // Set the adapter for the RecyclerView to display the list of clothing items
        binding.recyclerView.adapter = ClosetAdapter(app.clothingItems.findAll(), this)
    }

    // Method to create the options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu items into the menu
        menuInflater.inflate(R.menu.menu_main, menu)
        menuInflater.inflate(R.menu.menu_location, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Method to handle item selection from the options menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // When the "Go to Main" item is selected, launch the ClothingListActivity
            R.id.nav_to_main -> {
                val intent = Intent(this, ClothingListView::class.java)
                startActivity(intent)
                return true
            }
            // When the "Add Item" item is selected, launch the MainActivity for adding a new item
            R.id.item_add -> {
                val intent = Intent(this, MainView::class.java)
                // Register the activity for result to handle the result from MainActivity
                getResult.launch(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Register for activity result to handle adding a new clothing item
    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // If the result is OK, update the RecyclerView with the new list of clothing items
            if (result.resultCode == Activity.RESULT_OK) {
                (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, app.clothingItems.findAll().size)
            }
            // If the result is cancelled, show a Snackbar message indicating cancellation
            if (result.resultCode == Activity.RESULT_CANCELED) {
                Snackbar.make(binding.root, "Clothing Add Cancelled", Snackbar.LENGTH_LONG).show()
            }
        }

    // When a clothing item is clicked, launch the MainActivity to edit the item
    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        val launcherIntent = Intent(this, MainView::class.java)
        // Pass the clicked item as an extra in the intent for editing
        launcherIntent.putExtra("closet_item_edit", item)
        getResult.launch(launcherIntent)
    }

    // When the delete button is clicked for a clothing item, delete the item and update the RecyclerView
    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        // Remove the item from the app's clothing items
        app.clothingItems.delete(item)
        // Update the RecyclerView to reflect the removal of the item
        val updatedList = app.clothingItems.findAll().filter { it != item }
        (binding.recyclerView.adapter as ClosetAdapter).updateItems(updatedList)
        // Show a Snackbar message indicating the item has been deleted
        Snackbar.make(binding.root, "Clothing Item Deleted", Snackbar.LENGTH_LONG).show()
    }
}
