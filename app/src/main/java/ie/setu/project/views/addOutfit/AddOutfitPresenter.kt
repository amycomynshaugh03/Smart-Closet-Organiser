package ie.setu.project.views.addOutfit

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.datepicker.MaterialDatePicker
import ie.setu.project.activities.SelectClothingActivity
import ie.setu.project.closet.main.MainApp
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Presenter for managing the logic behind adding or editing an outfit.
 * Handles data operations like adding, updating, and launching the clothing selection.
 *
 * @param view The view interface that interacts with the user interface of the add/edit outfit screen.
 */
class AddOutfitPresenter(private val view: AddOutfitView) {

    // The current outfit being added or edited
    var outfit = OutfitModel()

    // Reference to the main application instance
    var app: MainApp = view.application as MainApp

    // Launcher for handling clothing selection results
    private lateinit var clothingSelectionLauncher: ActivityResultLauncher<Intent>

    init {
        // If the view has an outfit for editing, set it to the presenter and show it in the view
        if (view.intent.hasExtra("outfit_edit")) {
            outfit = view.intent.getParcelableExtra("outfit_edit")!!
            view.showOutfit(outfit)
        }
        // Register the callback for clothing selection
        registerClothingSelectionCallback()
    }

    /**
     * Adds a new outfit or saves the changes to an existing outfit.
     *
     * @param title The title of the outfit.
     * @param description The description of the outfit.
     * @param season The season for the outfit.
     */
    fun doAddOrSave(title: String, description: String, season: String) {
        outfit.title = title
        outfit.description = description
        outfit.season = season

        if (outfit.id == 0L) {
            // If the outfit is new, create it
            app.outfitItems.create(outfit)
        } else {
            // If the outfit exists, update it
            app.outfitItems.update(outfit)
        }
        // Set the result and finish the view
        view.setResult(RESULT_OK)
        view.finish()
    }

    /**
     * Launches the clothing selection activity to allow the user to select items for the outfit.
     */
    fun launchClothingSelection() {
        val intent = Intent(view, SelectClothingActivity::class.java).apply {
            putExtra("selected_clothing", ArrayList(outfit.clothingItems))
        }
        clothingSelectionLauncher.launch(intent)
    }

    /**
     * Displays a date picker to select the "last worn" date for the outfit.
     */
    fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(outfit.lastWorn.time)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply { timeInMillis = selection }
            outfit.lastWorn = calendar.time
            // Update the last worn date on the view
            view.updateLastWornDate(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(outfit.lastWorn)
            )
        }
        // Show the date picker dialog
        datePicker.show(view.supportFragmentManager, "DATE_PICKER")
    }

    /**
     * Registers the result callback for the clothing selection activity.
     * This callback updates the outfit with the selected clothing items.
     */
    private fun registerClothingSelectionCallback() {
        clothingSelectionLauncher = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    result.data?.getParcelableArrayListExtra<ClosetOrganiserModel>("selected_clothing")
                        ?.let { selectedItems ->
                            outfit.clothingItems = selectedItems.toMutableList()
                        }
                }
            }
        }
    }
}
