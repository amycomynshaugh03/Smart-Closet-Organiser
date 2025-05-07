package ie.setu.project.views.addOutfit

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.databinding.ActivityAddOutfitBinding
import ie.setu.project.models.outfit.OutfitModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Activity for adding or editing an outfit.
 * It provides UI elements for entering outfit details, selecting clothing items, and choosing a date for the "last worn" field.
 * This activity interacts with the `AddOutfitPresenter` to manage the data and actions.
 */
class AddOutfitView : AppCompatActivity() {

    // Binding object to interact with UI elements
    private lateinit var binding: ActivityAddOutfitBinding

    // Presenter for handling the logic of adding or editing an outfit
    private lateinit var presenter: AddOutfitPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set up the view binding
        binding = ActivityAddOutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the season spinner with available season options
        val seasonSpinner: Spinner = findViewById(R.id.outfitSeason)
        ArrayAdapter.createFromResource(
            this,
            R.array.seasons_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            seasonSpinner.adapter = adapter
        }

        // Initialize the presenter
        presenter = AddOutfitPresenter(this)

        // Set up the listeners for UI elements
        binding.lastWorn.setOnClickListener {
            presenter.showDatePicker() // Show date picker for selecting the "last worn" date
        }

        binding.chooseClothing.setOnClickListener {
            presenter.launchClothingSelection() // Launch clothing selection activity
        }

        // Save outfit when the "Add" button is clicked
        binding.btnAdd.setOnClickListener {
            if (binding.outfitTitle.text.isNullOrEmpty()) {
                // Show a Snackbar message if the title is empty
                Snackbar.make(it, R.string.please_enter_missing_item, Snackbar.LENGTH_LONG).show()
            } else {
                // Save or update the outfit details
                presenter.doAddOrSave(
                    binding.outfitTitle.text.toString(),
                    binding.outfitDescription.text.toString(),
                    seasonSpinner.selectedItem.toString()
                )
            }
        }
    }

    /**
     * Populates the fields with the details of the outfit to be edited.
     *
     * @param outfit The `OutfitModel` containing the details of the outfit.
     */
    fun showOutfit(outfit: OutfitModel) {
        binding.outfitTitle.setText(outfit.title)
        binding.outfitDescription.setText(outfit.description)

        val seasonSpinner: Spinner = findViewById(R.id.outfitSeason)
        val seasonsArray = resources.getStringArray(R.array.seasons_array)

        // Set the season spinner to the season of the outfit if available
        try {
            val seasonPosition = seasonsArray.indexOf(outfit.season)
            if (seasonPosition >= 0) {
                seasonSpinner.setSelection(seasonPosition)
            }
        } catch (e: Exception) {
            seasonSpinner.setSelection(0)
        }

        // Set the last worn date to the outfit's last worn date if available
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.lastWorn.setText(sdf.format(outfit.lastWorn))
        } catch (e: Exception) {
            binding.lastWorn.setText("")
        }
    }

    /**
     * Updates the displayed "last worn" date in the UI.
     *
     * @param date The date string to be displayed.
     */
    fun updateLastWornDate(date: String) {
        binding.lastWorn.setText(date)
    }
}
