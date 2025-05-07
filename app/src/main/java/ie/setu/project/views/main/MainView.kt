package ie.setu.project.views.main

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ie.setu.project.R
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel
import timber.log.Timber.i
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * MainActivity for displaying and editing clothing items.
 * This activity allows users to input or update clothing details,
 * including title, description, season, size, last worn date, and an image.
 */
class MainView : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: MainPresenter

    /**
     * Called when the activity is created. Sets up the UI elements, initializes the presenter,
     * and handles button clicks for adding or saving clothing items.
     * @param savedInstanceState A Bundle containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        presenter = MainPresenter(this)

        // Season spinner setup with an adapter for displaying available seasons
        val seasonSpinner: Spinner = findViewById(R.id.clothingSeason)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.seasons_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        seasonSpinner.adapter = adapter

        // Set up listener for selecting a date for "last worn"
        binding.lastWorn.setOnClickListener {
            presenter.showDatePicker()
        }

        // Set up listener for the "Add/Save" button to save the clothing item
        binding.btnAdd.setOnClickListener {
            if (binding.clothingItemTitle.text.toString().isEmpty()) {
                // Display a snackbar if the title is missing
                Snackbar.make(
                    it,
                    getString(R.string.please_enter_missing_item),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                // Pass the input data to the presenter for saving
                presenter.doAddOrSave(
                    binding.clothingItemTitle.text.toString(),
                    binding.clothingDescription.text.toString(),
                    binding.clothingColour.text.toString(),
                    binding.clothingSize.text.toString(),
                    seasonSpinner.selectedItem.toString()
                )
            }
        }

        // Set up listener for selecting an image
        binding.chooseImage.setOnClickListener {
            presenter.doSelectImage()
        }
    }

    /**
     * Inflates the options menu and adds the necessary items to the menu.
     * @param menu The options menu to populate.
     * @return true if the menu was created successfully.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_clothing_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handles item selections from the options menu, specifically the "Cancel" option.
     * @param item The menu item that was selected.
     * @return true if the item was handled, otherwise false.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_cancel -> presenter.doCancel() // Cancel the operation
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Updates the UI with the details of a clothing item.
     * This includes setting the title, description, size, season, and last worn date,
     * as well as loading the image and updating the button text for saving.
     * @param item The clothing item to display.
     */
    fun showClosetItem(item: ClosetOrganiserModel) {
        binding.clothingItemTitle.setText(item.title)
        binding.clothingDescription.setText(item.description)
        binding.clothingColour.setText(item.colourPattern)
        binding.clothingSize.setText(item.size)

        // Set the season spinner to the correct season value
        val seasonSpinner: Spinner = findViewById(R.id.clothingSeason)
        val adapter = seasonSpinner.adapter as ArrayAdapter<String>
        val seasonPosition = adapter.getPosition(item.season)
        seasonSpinner.setSelection(seasonPosition)

        // Format and display the last worn date
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(item.lastWorn)
        binding.lastWorn.setText(formattedDate)

        // Load the clothing item's image into the ImageView using Picasso
        Picasso.get()
            .load(item.image)
            .resize(600, 600)
            .rotate(90f)
            .into(binding.clothingImage)

        // If the item has an image, update the button text to allow changing the image
        if (item.image != Uri.EMPTY) {
            binding.chooseImage.setText(R.string.change_clothing_image)
        }

        // Change the "Add" button text to "Save"
        binding.btnAdd.text = getString(R.string.save_clothing_item)
    }

    /**
     * Updates the clothing item's image with the selected image URI.
     * @param image The URI of the selected image.
     */
    fun updateImage(image: Uri) {
        i("Got Result $image")
        Picasso.get()
            .load(image)
            .rotate(90f)
            .resize(600, 600)
            .into(binding.clothingImage)
        binding.chooseImage.setText(R.string.change_clothing_image) // Update the button text
    }

    /**
     * Updates the "Last Worn" date displayed in the UI.
     * @param date The new date to display.
     */
    fun updateLastWornDate(date: String) {
        binding.lastWorn.setText(date)
    }
}
