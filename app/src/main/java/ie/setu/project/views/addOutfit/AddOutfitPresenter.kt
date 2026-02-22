package ie.setu.project.views.addOutfit

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.EntryPointAccessors
import ie.setu.project.activities.SelectClothingActivity
import ie.setu.project.di.FirebaseEntryPoint
import ie.setu.project.di.StoreEntryPoint
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.outfit.OutfitStore
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddOutfitPresenter(private val view: AddOutfitView) {

    var outfit = OutfitModel()

    private val outfitStore: OutfitStore by lazy {
        val entryPoint = EntryPointAccessors.fromApplication(
            view.applicationContext,
            StoreEntryPoint::class.java
        )
        entryPoint.outfitStore()
    }

    private val firebase by lazy {
        EntryPointAccessors.fromApplication(
            view.applicationContext,
            FirebaseEntryPoint::class.java
        )
    }

    private lateinit var clothingSelectionLauncher: ActivityResultLauncher<Intent>

    init {
        if (view.intent.hasExtra("outfit_edit")) {
            outfit = view.intent.getParcelableExtra("outfit_edit")!!
            view.showOutfit(outfit)
        }
        registerClothingSelectionCallback()
    }

    fun doAddOrSave(title: String, description: String, season: String) {

        outfit.title = title.trim()
        outfit.description = description.trim()
        outfit.season = season.trim()

        val saved: OutfitModel = if (outfit.id == 0L) {
            val created = outfit.copy()
            outfitStore.create(created)
            outfit.id = created.id
            created
        } else {
            val updated = outfit.copy()
            outfitStore.update(updated)
            updated
        }

        val uid = firebase.authService().currentUserId
        if (uid.isNotBlank()) {
            view.lifecycleScope.launch {
                try {
                    firebase.outfitFirestoreRepository().upsert(uid, saved)
                } catch (e: Exception) {
                    Timber.e(e, "Firestore outfit upsert failed")
                }
            }
        } else {
            Timber.w("Skipping Firestore outfit save: userId blank (not signed in?)")
        }

        view.setResult(RESULT_OK)
        view.finish()
    }

    fun launchClothingSelection() {
        val intent = Intent(view, SelectClothingActivity::class.java).apply {
            putExtra("selected_clothing", ArrayList(outfit.clothingItems))
        }
        clothingSelectionLauncher.launch(intent)
    }

    fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(outfit.lastWorn.time)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply { timeInMillis = selection }
            outfit.lastWorn = calendar.time
            view.updateLastWornDate(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(outfit.lastWorn)
            )
        }

        datePicker.show(view.supportFragmentManager, "DATE_PICKER")
    }

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