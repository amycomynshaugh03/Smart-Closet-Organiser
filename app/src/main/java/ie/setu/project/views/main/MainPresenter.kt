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

class MainPresenter(private val view: MainView) {
    var closetOrganiser = ClosetOrganiserModel()
    var app: MainApp = view.application as MainApp
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    var edit = false

    init {
        if (view.intent.hasExtra("closet_item_edit")) {
            edit = true
            closetOrganiser = view.intent.getParcelableExtra("closet_item_edit")!!
            view.showClosetItem(closetOrganiser)
        }
        registerImagePickerCallback()
    }

    fun doAddOrSave(title: String, description: String, colourPattern: String, size: String, season: String) {
        closetOrganiser.title = title
        closetOrganiser.description = description
        closetOrganiser.colourPattern = colourPattern
        closetOrganiser.size = size
        closetOrganiser.season = season

        if (edit) {
            app.clothingItems.update(closetOrganiser.copy())
            i("Update Button Pressed: ${closetOrganiser.title}")
        } else if (closetOrganiser.id == 0L) {
            app.clothingItems.create(closetOrganiser.copy())
            i("Add Button Pressed: ${closetOrganiser.title}")
            for (i in app.clothingItems.findAll().indices) {
                i("Clothing Item[i]: ${app.clothingItems.findAll()[i].title}, ${app.clothingItems.findAll()[i].description}")
            }
        }
        view.setResult(RESULT_OK)
        view.finish()
    }

    fun doCancel() {
        view.setResult(RESULT_CANCELED)
        view.finish()
    }

    fun doSelectImage() {
        showImagePicker(imageIntentLauncher)
    }

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

    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            i("Got Result ${result.data!!.data}")
                            val image = result.data!!.data!!
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