package ie.setu.project.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.helpers.showImagePicker
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber.i
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Main activity for managing a clothing item in the app.
 * This allows the user to either add a new clothing item or edit an existing one.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    var closetOrganiser = ClosetOrganiserModel()
    // private lateinit var CarouselAdapter: CarouselAdapter
    // private val imageUris = mutableListOf<Uri>()

    var app: MainApp? = null
    var edit = false

    //    private val weatherViewModel: WeatherViewModel by viewModels()
    //    private val city = "London"

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up the spinner for clothing season,
     * handles editing or adding new items, and sets listeners for buttons.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set up the binding for UI components
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the action bar with the title
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        // Initialize the app instance
        app = application as MainApp

        // Set up the season spinner with predefined values
        val seasonSpinner: Spinner = findViewById(R.id.clothingSeason)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.seasons_array,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        seasonSpinner.adapter = adapter

        // If we are editing an existing clothing item, populate fields with the data
        if (intent.hasExtra("closet_item_edit")) {
            edit = true
            closetOrganiser = intent.getParcelableExtra("closet_item_edit")!!
            binding.clothingItemTitle.setText(closetOrganiser.title)
            binding.clothingDescription.setText(closetOrganiser.description)
            binding.clothingColour.setText(closetOrganiser.colourPattern)
            binding.clothingSize.setText(closetOrganiser.size)

            val seasonPosition = adapter.getPosition(closetOrganiser.season)
            seasonSpinner.setSelection(seasonPosition)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(closetOrganiser.lastWorn)
            binding.lastWorn.setText(formattedDate)

            Picasso.get()
                .load(closetOrganiser.image)
                .resize(600, 600)
                .rotate(90f)
                .into(binding.clothingImage)
            if (closetOrganiser.image != Uri.EMPTY) {
                binding.chooseImage.setText(R.string.change_clothing_image)
            }

            binding.btnAdd.text = getString(R.string.save_clothing_item)
        }

        // Set up a listener for selecting a date for 'last worn'
        binding.lastWorn.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Last Worn Date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selection

                closetOrganiser.lastWorn = calendar.time

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = sdf.format(closetOrganiser.lastWorn)

                binding.lastWorn.setText(selectedDate)
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        // Set up the listener for the 'add' button to save the clothing item
        binding.btnAdd.setOnClickListener {
            closetOrganiser.title = binding.clothingItemTitle.text.toString()
            closetOrganiser.description = binding.clothingDescription.text.toString()
            closetOrganiser.colourPattern = binding.clothingColour.text.toString()
            closetOrganiser.size = binding.clothingSize.text.toString()
            closetOrganiser.season = seasonSpinner.selectedItem.toString()

            // Validate that the title is not empty before saving
            if (closetOrganiser.title.isNotEmpty()) {
                if (edit) {
                    app!!.clothingItems.update(closetOrganiser.copy())
                    i("Update Button Pressed: ${closetOrganiser.title}")
                } else if (closetOrganiser.id == 0L) {
                    app!!.clothingItems.create(closetOrganiser.copy())
                    i("Add Button Pressed: ${closetOrganiser.title}")

                    // Log all clothing items for debugging
                    for (i in app!!.clothingItems.findAll().indices) {
                        i("Clothing Item[i]: ${app!!.clothingItems.findAll()[i].title}, ${app!!.clothingItems.findAll()[i].description}")
                    }
                }

                // Return result and finish the activity
                setResult(RESULT_OK)
                finish()
            } else {
                // Show an error message if the title is missing
                Snackbar.make(
                    it,
                    getString(R.string.please_enter_missing_item),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        // Allow the user to pick an image for the clothing item
        binding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        // Register the image picker callback
        registerImagePickerCallback()
    }

    /**
     * Creates the options menu for this activity.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_clothing_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handles item selections from the options menu.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_cancel -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Registers the callback for handling the image picker result.
     * When the user selects an image, the image is displayed in the UI.
     */
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    RESULT_OK -> {
                        if (result.data != null) {
                            i("Got Result ${result.data!!.data}")

                            val image = result.data!!.data!!
                            contentResolver.takePersistableUriPermission(image,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            closetOrganiser.image = image


                            // Load the picked image into the ImageView
                            Picasso.get()
                                .load(closetOrganiser.image)
                                .rotate(90f)
                                .resize(600, 600)
                                .into(binding.clothingImage)

                            // Update the button text to reflect a change in the image
                            binding.chooseImage.setText(R.string.change_clothing_image)
                        }
                    }
                    RESULT_CANCELED -> {
                    }
                    else -> { }
                }
            }
    }
}
