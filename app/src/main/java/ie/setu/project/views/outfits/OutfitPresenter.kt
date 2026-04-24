package ie.setu.project.views.outfit

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.EntryPointAccessors
import ie.setu.project.di.FirebaseEntryPoint
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.views.addOutfit.AddOutfitView
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Presenter for [OutfitView] in the MVP layer.
 *
 * Loads the user's outfits from Firestore and handles navigation to the add/edit screen
 * and outfit deletion. Uses [FirebaseEntryPoint] to resolve Firebase dependencies
 * outside the Hilt graph.
 *
 * @constructor Registers the activity result callback and triggers an initial Firestore refresh.
 * @param view The [OutfitView] this presenter is attached to.
 */
class OutfitPresenter(private val view: OutfitView) {

    private val firebase by lazy {
        EntryPointAccessors.fromApplication(
            view.applicationContext,
            FirebaseEntryPoint::class.java
        )
    }

    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var cachedOutfits: List<OutfitModel> = emptyList()

    init {
        registerActivityResultCallback()
        refreshFromFirestore()
    }

    /**
     * Returns the most recently loaded list of outfits.
     */
    fun getOutfits(): List<OutfitModel> = cachedOutfits

    /**
     * Fetches all outfits from Firestore for the current user and notifies the view to reload.
     * Silently returns if the user is not authenticated.
     */
    fun refreshFromFirestore() {
        val uid = firebase.authService().currentUserId
        if (uid.isBlank()) return

        view.lifecycleScope.launch {
            try {
                cachedOutfits = firebase.outfitFirestoreRepository().getAll(uid)
                view.loadOutfits()
            } catch (e: Exception) {
                Timber.e(e, "Firestore outfit read failed")
            }
        }
    }

    /**
     * Launches [AddOutfitView] with the given outfit pre-loaded for editing.
     *
     * @param outfit The outfit to edit.
     */
    fun onOutfitClick(outfit: OutfitModel) {
        val intent = Intent(view, AddOutfitView::class.java).apply {
            putExtra("outfit_edit", outfit)
        }
        getResult.launch(intent)
    }

    /**
     * Deletes the given outfit from Firestore and refreshes the outfit list.
     *
     * @param outfit The outfit to delete.
     */
    fun onDeleteOutfitClick(outfit: OutfitModel) {
        val uid = firebase.authService().currentUserId
        if (uid.isBlank()) return

        view.lifecycleScope.launch {
            try {
                firebase.outfitFirestoreRepository().delete(uid, outfit.id)
                refreshFromFirestore()
            } catch (e: Exception) {
                Timber.e(e, "Firestore outfit delete failed")
            }
        }
    }

    /**
     * Registers the activity result callback for the add/edit outfit screen.
     * Refreshes the outfit list on [Activity.RESULT_OK] and shows a snackbar on cancellation.
     */
    private fun registerActivityResultCallback() {
        getResult = view.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> refreshFromFirestore()
                Activity.RESULT_CANCELED -> view.showSnackbar("Operation cancelled")
            }
        }
    }
}