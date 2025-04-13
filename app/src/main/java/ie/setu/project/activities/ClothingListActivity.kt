package ie.setu.project.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.adapters.CarouselAdapter
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber.i
import java.text.SimpleDateFormat
import java.util.Date

// This Activity displays the list of clothing items and allows the user to navigate to another screen for managing them.
class ClothingListActivity : AppCompatActivity(), ClosetItemListener {

    lateinit var app: MainApp // MainApp instance to interact with the app's data
    private lateinit var binding: ActivityClothingListBinding // Binding for the activity's UI components
    private lateinit var imageList: List<Uri>
    private lateinit var viewPager: ViewPager2

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

        val imageListStrings = intent.getStringArrayListExtra("imageList") ?: emptyList()
        imageList = imageListStrings.mapNotNull { Uri.parse(it) }

        setupCarousel()
    }

    // Register for activity result to handle adding or editing clothing items
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                // Notify the RecyclerView adapter that the dataset has changed
                (binding.recyclerView.adapter)?.notifyItemRangeChanged(
                    0,
                    app.clothingItems.findAll().size
                )
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

    private fun setupCarousel(){

        viewPager = binding.carouselViewPager

        val clothingList = arrayListOf(
            ClosetOrganiserModel(
                id = 0,
                title = "jkj",
                description = "k",
                colourPattern = "k",
                size = "9",
                season = "Spring",
                lastWorn = Date(),
                image = Uri.parse("https://images2.drct2u.com/plp_full_width_1/products/sr/sr379/c01sr379750w.jpg")
            ),
            ClosetOrganiserModel(
                id = 0,
                title = "jkj",
                description = "k",
                colourPattern = "k",
                size = "9",
                season = "Spring",
                lastWorn = Date(),
                image=Uri.parse("content://com.android.providers.media.documents/document/image%3A43"))
        )
        val adapter = CarouselAdapter(clothingList)

        //val adapter = CarouselAdapter(app.clothingItems.findAll())

        viewPager.adapter = adapter

    }
}