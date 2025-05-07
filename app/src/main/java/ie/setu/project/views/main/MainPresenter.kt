package ie.setu.project.views.main

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import ie.setu.project.closet.main.MainApp
import ie.setu.project.helpers.showImagePicker
import ie.setu.project.models.clothing.ClosetOrganiserModel
import timber.log.Timber.i
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Presenter responsible for handling the business logic for the MainView.
 * This includes handling clothing item creation or updates, image selection,
 * and date picker interactions.
 */
class MainPresenter(private val view: MainView) {

    // The current clothing item being edited or created
    var closetOrganiser = ClosetOrganiserModel()

    // Reference to the application instance
    var app: MainApp = view.application as MainApp

    // Launcher to handle image selection result
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>

    // Flag to determine if the presenter is handling an existing item for editing
    var edit = false

    init {
        // If the activity was launched with an existing clothing item for editing, load it
        if (view.intent.hasExtra("closet_item_edit")) {
            edit = true
            closetOrganiser = view.intent.getParcelableExtra("closet_item_edit")!!
            view.showClosetItem(closetOrganiser)
        }
        registerImagePickerCallback()  // Register the image picker callback
    }

    /**
     * Adds or saves a clothing item based on whether it's a new item or an update.
     * Updates the app's clothing items repository and finishes the activity with a result.
     *
     * @param title The title of the clothing item.
     * @param description The description of the clothing item.
     * @param colourPattern The color pattern of the clothing item.
     * @param size The size of the clothing item.
     * @param season The season for which the clothing item is suitable.
     */
    fun doAddOrSave(title: String, description: String, colourPattern: String, size: String, season: String) {
        closetOrganiser.title = title
        closetOrganiser.description = description
        closetOrganiser.colourPattern = colourPattern
        closetOrganiser.size = size
        closetOrganiser.season = season

        if (edit) {
            // If editing an existing item, update it
            app.clothingItems.update(closetOrganiser.copy())
            i("Update Button Pressed: ${closetOrganiser.title}")
        } else if (closetOrganiser.id == 0L) {
            // If it's a new item, create it
            app.clothingItems.create(closetOrganiser.copy())
            i("Add Button Pressed: ${closetOrganiser.title}")
            // Logging all clothing items for debugging
            for (i in app.clothingItems.findAll().indices) {
                i("Clothing Item[i]: ${app.clothingItems.findAll()[i].title}, ${app.clothingItems.findAll()[i].description}")
            }
        }
        // Return result to the activity and finish
        view.setResult(RESULT_OK)
        view.finish()
    }

    /**
     * Cancels the current operation and finishes the activity.
     */
    fun doCancel() {
        view.setResult(RESULT_CANCELED)
        view.finish()
    }

    /**
     * Launches an image picker intent for selecting an image for the clothing item.
     */
    fun doSelectImage() {
        showImagePicker(imageIntentLauncher)
    }

    /**
     * Displays a date picker dialog for selecting the "last worn" date for the clothing item.
     */
    fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Last Worn Date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            closetOrganiser.lastWorn = calendar.time
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = sdf.format(closetOrganiser.lastWorn)
            view.updateLastWornDate(selectedDate)
        }
        datePicker.show(view.supportFragmentManager, "DATE_PICKER")
    }

    /**
     * Registers a callback to handle the result of the image picker.
     * Once an image is selected, the clothing item's image is updated.
     */
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            i("Got Result ${result.data!!.data}")
                            val image = result.data!!.data!!
                            // Grant the app permission to read the image URI
                            view.contentResolver.takePersistableUriPermission(image,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            closetOrganiser.image = image
                            view.updateImage(image)
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> { }
                    else -> { }
                }
            }
    }
}
