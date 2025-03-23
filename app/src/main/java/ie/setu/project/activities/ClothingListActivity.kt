package ie.setu.project.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.ClosetOrganiserModel

// This Activity displays the list of clothing items and allows the user to navigate to another screen for managing them.
class ClothingListActivity : AppCompatActivity(), ClosetItemListener {

    lateinit var app: MainApp  // MainApp instance to interact with the app's data
    private lateinit var binding: ActivityClothingListBinding  // Binding for the activity's UI components
//    private lateinit var imageList: List<Uri>  // Placeholder for image list if needed

    // onCreate method: Called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using the binding class
        binding = ActivityClothingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the toolbar as the action bar
        setSupportActionBar(binding.topAppBar)

        // Initialize the app instance
        app = application as MainApp

        // Button to navigate to clothingActivity to manage clothing items
        val clothesButton: Button = findViewById(R.id.btnClothes)
        clothesButton.setOnClickListener {
            val intent = Intent(this, clothingActivity::class.java)
            startActivity(intent)
        }

        // Uncommented sections for carousel and image picker setup (if required)
//
//        val imageListStrings = intent.getStringArrayListExtra("imageList") ?: emptyList()
//        imageList = imageListStrings.mapNotNull { Uri.parse(it) }
//
//        setupCarouselRecyclerView()
//
//        registerImagePickerCallback()
    }

//    // Set up RecyclerView for displaying a carousel (if needed)
//    private fun setupCarouselRecyclerView() {
//        binding.carouselRecyclerView.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//
//        val carouselAdapter = CarouselAdapter(imageList)
//        binding.carouselRecyclerView.adapter = carouselAdapter
//    }
//
//    // Register a callback for image picker (if needed)
//    private fun registerImagePickerCallback() {
//    }

    // Register for activity result to handle adding or editing clothing items
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                // Notify the RecyclerView adapter that the dataset has changed
                (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, app.clothingItems.findAll().size)
            }
            if (it.resultCode == Activity.RESULT_CANCELED) {
                // Show a Snackbar message when the action is cancelled
                Snackbar.make(binding.root, "Placemark Add Cancelled", Snackbar.LENGTH_LONG).show()
            }
        }

    // Handle click event for a clothing item
    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        val launcherIntent = Intent(this, MainActivity::class.java)
        // Pass the selected closet item for editing
        launcherIntent.putExtra("closet_item_edit", item)
        // Launch the activity to edit the item
        getResult.launch(launcherIntent)
        // Show a Snackbar message displaying the selected item title
        Snackbar.make(binding.root, "Selected: ${item.title}", Snackbar.LENGTH_SHORT).show()
    }

    // Handle item selection in options menu (currently not implemented)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    // Handle click event for deleting a clothing item
    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        // Delete the item from the clothing items list
        app.clothingItems.delete(item)
        // Update the RecyclerView to reflect the changes
        val updatedList = app.clothingItems.findAll().filter { it != item }
        (binding.recyclerView.adapter as ClosetAdapter).updateItems(updatedList)
        // Show a Snackbar message indicating the item has been deleted
        Snackbar.make(binding.root, "Clothing Item Deleted", Snackbar.LENGTH_LONG).show()
    }

}
