package ie.setu.project.views.main

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import ie.setu.project.closet.main.MainApp
import ie.setu.project.helpers.removeBackgroundAndSave
import ie.setu.project.helpers.showImagePicker
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun doAddOrSave(
        title: String,
        description: String,
        colourPattern: String,
        size: String,
        season: String,
        category: String

    ) {
        closetOrganiser.title = title.trim()
        closetOrganiser.description = description.trim()
        closetOrganiser.colourPattern = colourPattern.trim()
        closetOrganiser.size = size.trim()
        closetOrganiser.season = season.trim()
        closetOrganiser.category = category.trim()


        if (edit) {
            app.clothingItems.update(closetOrganiser.copy())
            i("Update Button Pressed: ${closetOrganiser.title} (${closetOrganiser.category})")
        } else {
            app.clothingItems.create(closetOrganiser.copy())
            i("Add Button Pressed: ${closetOrganiser.title} (${closetOrganiser.category})")
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
            view.updateLastWornDate(sdf.format(closetOrganiser.lastWorn))
        }
        datePicker.show(view.supportFragmentManager, "DATE_PICKER")
    }

    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        result.data?.data?.let { pickedUri ->


                            view.contentResolver.takePersistableUriPermission(
                                pickedUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )


                            view.lifecycleScope.launch {
                                val processedUri = withContext(Dispatchers.Default) {
                                    removeBackgroundAndSave(view, pickedUri)
                                }


                                view.grantUriPermission(
                                    view.packageName,
                                    processedUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )

                                closetOrganiser.image = processedUri
                                view.updateImage(processedUri)
                            }
                        }
                    }

                    else -> {}
                }
            }
    }
}
