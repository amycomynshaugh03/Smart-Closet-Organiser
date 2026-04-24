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

/**
 * Presenter for [AddOutfitView] in the MVP layer.
 *
 * Handles creating and editing [OutfitModel] records. On save, the outfit is written
 * to the local [OutfitStore] and synced to Firestore. Also manages launching the
 * [SelectClothingActivity] for clothing selection and the date picker for last-worn date.
 *
 * @constructor Creates the presenter, loads any existing outfit from the intent,
 *   and registers the clothing selection activity result callback.
 * @param view The [AddOutfitView] this presenter is attached to.
 */
class AddOutfitPresenter(private val view: AddOutfitView) {

    /** The outfit being created or edited. Defaults to a new empty [OutfitModel]. */
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

    /**
     * Saves or updates the outfit with the provided field values.
     * Creates a new outfit if [outfit.id] is 0, otherwise updates the existing one.
     * Writes to the local [OutfitStore] and syncs to Firestore if the user is authenticated.
     * Finishes the view with [RESULT_OK] on completion.
     *
     * @param title The outfit title entered by the user.
     * @param description The outfit description entered by the user.
     * @param season The selected season for the outfit.
     */
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

    /**
     * Launches [SelectClothingActivity] so the user can pick clothing items for the outfit.
     * Passes the currently selected items so checkboxes are pre-populated.
     */
    fun launchClothingSelection() {
        val intent = Intent(view, SelectClothingActivity::class.java).apply {
            putExtra("selected_clothing", ArrayList(outfit.clothingItems))
        }
        clothingSelectionLauncher.launch(intent)
    }

    /**
     * Shows a [MaterialDatePicker] and updates [outfit.lastWorn] with the chosen date.
     * Also updates the view's displayed date string.
     */
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

    /**
     * Registers the activity result callback for the clothing selection screen.
     * On [RESULT_OK], updates [outfit.clothingItems] with the user's selections
     * and refreshes the view.
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
                            view.showOutfit(outfit)
                        }
                }
            }
        }
    }
}