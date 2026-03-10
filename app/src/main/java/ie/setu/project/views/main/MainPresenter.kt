package ie.setu.project.views.main

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.EntryPointAccessors
import ie.setu.project.di.FirebaseEntryPoint
import ie.setu.project.di.StoreEntryPoint
import ie.setu.project.helpers.correctImageRotation
import ie.setu.project.helpers.removeBackgroundAndSave
import ie.setu.project.helpers.showImagePicker
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.clothing.ClothingStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainPresenter(private val view: MainView) {

    var closetOrganiser = ClosetOrganiserModel()

    private val clothingStore: ClothingStore by lazy {
        val entryPoint = EntryPointAccessors.fromApplication(
            view.applicationContext,
            StoreEntryPoint::class.java
        )
        entryPoint.clothingStore()
    }

    private val firebase by lazy {
        EntryPointAccessors.fromApplication(
            view.applicationContext,
            FirebaseEntryPoint::class.java
        )
    }

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

        val uid = firebase.authService().currentUserId

        view.lifecycleScope.launch {

            val saved: ClosetOrganiserModel = withContext(Dispatchers.IO) {
                if (edit) {
                    val updated = closetOrganiser.copy()
                    clothingStore.update(updated)
                    Timber.i("Update Button Pressed: ${updated.title} (${updated.category})")
                    updated
                } else {
                    val created = closetOrganiser.copy()
                    clothingStore.create(created)
                    Timber.i("Add Button Pressed: ${created.title} (${created.category})")
                    created
                }
            }

            closetOrganiser.id = saved.id

            if (uid.isNotBlank()) {
                try {
                    val localUri = saved.image
                    if (localUri != null && localUri != Uri.EMPTY) {
                        val upload = firebase.imageStorageRepository()
                            .uploadClothingImage(uid, saved.id, localUri)

                        val updatedForCloud = saved.copy(
                            imageUrl = upload.downloadUrl
                        )

                        firebase.clothingFirestoreRepository()
                            .upsert(uid, updatedForCloud, imagePath = upload.storagePath)
                    } else {
                        firebase.clothingFirestoreRepository().upsert(uid, saved, imagePath = null)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Storage+Firestore upsert failed")
                }
            }

            view.setResult(RESULT_OK)
            view.finish()
        }
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
                                val processedUri = if (view.removeBgState) {
                                    withContext(Dispatchers.Default) {
                                        removeBackgroundAndSave(view, pickedUri)
                                    }
                                } else {
                                    withContext(Dispatchers.Default) {
                                        correctImageRotation(view, pickedUri)
                                    }
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
                    else -> { }
                }
            }
    }
}